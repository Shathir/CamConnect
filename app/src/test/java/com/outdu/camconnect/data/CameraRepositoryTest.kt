package com.outdu.camconnect.data

import android.content.Context
import android.content.SharedPreferences
import app.cash.turbine.test
import com.outdu.camconnect.testutils.MainDispatcherRule
import io.mockk.*
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Rule
import org.junit.Test

/**
 * Unit tests for CameraRepository
 * 
 * Tests cover:
 * - CRUD operations (add, remove, get)
 * - StateFlow emissions
 * - SharedPreferences integration
 * - Online status management
 * - Duplicate detection
 * - Error handling
 */
@OptIn(ExperimentalCoroutinesApi::class)
class CameraRepositoryTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private lateinit var mockContext: Context
    private lateinit var mockSharedPreferences: SharedPreferences
    private lateinit var mockEditor: SharedPreferences.Editor
    private lateinit var repository: CameraRepository

    private val testCamera = StoredCamera(
        id = "test-id-1",
        name = "Test Camera",
        ipAddress = "192.168.1.100",
        port = 80,
        serialNumber = "SN123456",
        manufacturer = "Test Manufacturer",
        model = "Test Model"
    )

    @Before
    fun setup() {
        // Mock Context
        mockContext = mockk(relaxed = true)
        
        // Mock SharedPreferences and Editor
        mockSharedPreferences = mockk(relaxed = true)
        mockEditor = mockk(relaxed = true)
        
        // Setup SharedPreferences mocking chain
        every { mockContext.getSharedPreferences(any(), any()) } returns mockSharedPreferences
        every { mockSharedPreferences.edit() } returns mockEditor
        every { mockEditor.putString(any(), any()) } returns mockEditor
        every { mockEditor.putLong(any(), any()) } returns mockEditor
        every { mockEditor.remove(any()) } returns mockEditor
        every { mockEditor.apply() } just Runs
        every { mockSharedPreferences.getString(any(), any()) } returns null
        every { mockSharedPreferences.getLong(any(), any()) } returns 0L
        
        // Create repository instance
        repository = CameraRepository(mockContext)
    }

    @After
    fun teardown() {
        clearAllMocks()
    }

    // ========== Initial State Tests ==========

    @Test
    fun `initial state should have empty cameras list`() = runTest {
        // Assert
        assertTrue(repository.cameras.value.isEmpty())
        assertTrue(repository.onlineCameras.value.isEmpty())
    }

    // ========== Add Camera Tests ==========

    @Test
    fun `addCamera should add camera successfully`() = runTest {
        // Act
        val result = repository.addCamera(testCamera)

        // Assert
        assertTrue(result.isSuccess)
        val addedCamera = result.getOrNull()
        assertNotNull(addedCamera)
        assertEquals(testCamera.name, addedCamera?.name)
        assertEquals(testCamera.ipAddress, addedCamera?.ipAddress)
        assertEquals(1, repository.cameras.value.size)
        
        // Verify SharedPreferences was called
        verify { mockEditor.putString(any(), any()) }
        verify { mockEditor.apply() }
    }

    @Test
    fun `addCamera should generate ID if empty`() = runTest {
        // Arrange
        val cameraWithoutId = testCamera.copy(id = "")

        // Act
        val result = repository.addCamera(cameraWithoutId)

        // Assert
        assertTrue(result.isSuccess)
        val addedCamera = result.getOrNull()
        assertNotNull(addedCamera)
        assertNotEquals("", addedCamera?.id)
        assertTrue(addedCamera!!.id.isNotEmpty())
    }

    @Test
    fun `addCamera should set addedAt and lastSeen timestamps`() = runTest {
        // Arrange
        val cameraWithoutTimestamps = testCamera.copy(addedAt = 0, lastSeen = 0)

        // Act
        val result = repository.addCamera(cameraWithoutTimestamps)

        // Assert
        assertTrue(result.isSuccess)
        val addedCamera = result.getOrNull()
        assertNotNull(addedCamera)
        assertTrue(addedCamera!!.addedAt > 0)
        assertTrue(addedCamera.lastSeen > 0)
    }

    @Test
    fun `addCamera should reject duplicate IP address`() = runTest {
        // Arrange
        repository.addCamera(testCamera)
        val duplicateCamera = testCamera.copy(id = "different-id", serialNumber = "DIFFERENT_SN")

        // Act
        val result = repository.addCamera(duplicateCamera)

        // Assert
        assertTrue(result.isFailure)
        val exception = result.exceptionOrNull()
        assertTrue(exception is CameraException)
        assertEquals("Camera with this IP or serial number already exists", exception?.message)
        assertEquals(1, repository.cameras.value.size) // Should still be 1
    }

    @Test
    fun `addCamera should reject duplicate serial number`() = runTest {
        // Arrange
        repository.addCamera(testCamera)
        val duplicateCamera = testCamera.copy(id = "different-id", ipAddress = "192.168.1.200")

        // Act
        val result = repository.addCamera(duplicateCamera)

        // Assert
        assertTrue(result.isFailure)
        val exception = result.exceptionOrNull()
        assertTrue(exception is CameraException)
        assertEquals(1, repository.cameras.value.size)
    }

    @Test
    fun `addCamera should emit StateFlow update`() = runTest {
        // Arrange & Act
        repository.cameras.test {
            assertEquals(emptyList<StoredCamera>(), awaitItem()) // Initial value
            
            repository.addCamera(testCamera)
            
            val updatedList = awaitItem()
            assertEquals(1, updatedList.size)
            assertEquals(testCamera.name, updatedList[0].name)
        }
    }

    // ========== Remove Camera Tests ==========

    @Test
    fun `removeCamera should remove existing camera`() = runTest {
        // Arrange
        repository.addCamera(testCamera)
        assertEquals(1, repository.cameras.value.size)

        // Act
        val result = repository.removeCamera(testCamera.id)

        // Assert
        assertTrue(result.isSuccess)
        assertTrue(result.getOrNull() == true) // Removed successfully
        assertEquals(0, repository.cameras.value.size)
        
        // Verify SharedPreferences was updated
        verify(atLeast = 2) { mockEditor.putString(any(), any()) } // Add + Remove
    }

    @Test
    fun `removeCamera should return false for non-existent camera`() = runTest {
        // Act
        val result = repository.removeCamera("non-existent-id")

        // Assert
        assertTrue(result.isSuccess)
        assertFalse(result.getOrNull() == true) // Not removed (didn't exist)
    }

    @Test
    fun `removeCamera should emit StateFlow update`() = runTest {
        // Arrange
        repository.addCamera(testCamera)
        
        // Act
        repository.cameras.test {
            skipItems(1) // Skip current state (1 camera)
            
            repository.removeCamera(testCamera.id)
            
            val updatedList = awaitItem()
            assertEquals(0, updatedList.size)
        }
    }

    // ========== Get Camera Tests ==========

    @Test
    fun `getCameraById should return camera when found`() = runTest {
        // Arrange
        repository.addCamera(testCamera)

        // Act
        val result = repository.getCameraById(testCamera.id)

        // Assert
        assertNotNull(result)
        assertEquals(testCamera.name, result?.name)
        assertEquals(testCamera.ipAddress, result?.ipAddress)
    }

    @Test
    fun `getCameraById should return null when not found`() = runTest {
        // Act
        val result = repository.getCameraById("non-existent-id")

        // Assert
        assertNull(result)
    }

    @Test
    fun `getCameraByIP should return camera when found`() = runTest {
        // Arrange
        repository.addCamera(testCamera)

        // Act
        val result = repository.getCameraByIP(testCamera.ipAddress)

        // Assert
        assertNotNull(result)
        assertEquals(testCamera.name, result?.name)
    }

    @Test
    fun `getCameraByIP should return null when not found`() = runTest {
        // Act
        val result = repository.getCameraByIP("192.168.1.999")

        // Assert
        assertNull(result)
    }

    // ========== Online Status Tests ==========

    @Test
    fun `updateCameraOnlineStatus should update online cameras set`() = runTest {
        // Arrange
        repository.addCamera(testCamera)
        val onlineIPs = listOf("192.168.1.100")

        // Act
        repository.updateCameraOnlineStatus(onlineIPs)

        // Assert
        assertEquals(1, repository.onlineCameras.value.size)
        assertTrue(repository.onlineCameras.value.contains("192.168.1.100"))
    }

    @Test
    fun `updateCameraOnlineStatus should update lastSeen for online cameras`() = runTest {
        // Arrange
        val addResult = repository.addCamera(testCamera)
        val initialLastSeen = addResult.getOrNull()?.lastSeen ?: 0
        
        // Wait a bit to ensure timestamp difference
        Thread.sleep(10)
        
        // Act
        repository.updateCameraOnlineStatus(listOf(testCamera.ipAddress))

        // Assert
        val camera = repository.getCameraById(testCamera.id)
        assertNotNull(camera)
        assertTrue(camera!!.lastSeen > initialLastSeen)
    }

    @Test
    fun `isCameraOnline should return true for online camera`() = runTest {
        // Arrange
        repository.addCamera(testCamera)
        repository.updateCameraOnlineStatus(listOf(testCamera.ipAddress))

        // Act
        val isOnline = repository.isCameraOnline(testCamera.id)

        // Assert
        assertTrue(isOnline)
    }

    @Test
    fun `isCameraOnline should return false for offline camera`() = runTest {
        // Arrange
        repository.addCamera(testCamera)
        repository.updateCameraOnlineStatus(emptyList())

        // Act
        val isOnline = repository.isCameraOnline(testCamera.id)

        // Assert
        assertFalse(isOnline)
    }

    @Test
    fun `isCameraOnline should return false for non-existent camera`() = runTest {
        // Act
        val isOnline = repository.isCameraOnline("non-existent-id")

        // Assert
        assertFalse(isOnline)
    }

    @Test
    fun `getCamerasWithStatus should return correct online status`() = runTest {
        // Arrange
        val camera1 = testCamera.copy(id = "cam1", ipAddress = "192.168.1.100", serialNumber = "SN1")
        val camera2 = testCamera.copy(id = "cam2", ipAddress = "192.168.1.101", serialNumber = "SN2")
        repository.addCamera(camera1)
        repository.addCamera(camera2)
        repository.updateCameraOnlineStatus(listOf("192.168.1.100")) // Only camera1 online

        // Act
        val camerasWithStatus = repository.getCamerasWithStatus()

        // Assert
        assertEquals(2, camerasWithStatus.size)
        
        val status1 = camerasWithStatus.find { it.camera.id == "cam1" }
        val status2 = camerasWithStatus.find { it.camera.id == "cam2" }
        
        assertNotNull(status1)
        assertNotNull(status2)
        assertTrue(status1!!.isOnline)
        assertFalse(status2!!.isOnline)
    }

    // ========== Clear Cameras Tests ==========

    @Test
    fun `clearAllCameras should remove all cameras`() = runTest {
        // Arrange
        repository.addCamera(testCamera)
        repository.addCamera(testCamera.copy(id = "cam2", ipAddress = "192.168.1.101", serialNumber = "SN2"))
        assertEquals(2, repository.cameras.value.size)

        // Act
        val result = repository.clearAllCameras()

        // Assert
        assertTrue(result.isSuccess)
        assertTrue(result.getOrNull() == true)
        assertEquals(0, repository.cameras.value.size)
        assertEquals(0, repository.onlineCameras.value.size)
        
        // Verify SharedPreferences cleared (2 keys: CAMERAS_KEY and LAST_SYNC_KEY)
        verify(atLeast = 2) { mockEditor.remove(any()) }
    }

    // ========== Storage Stats Tests ==========

    @Test
    fun `getStorageStats should return correct counts`() = runTest {
        // Arrange
        val camera1 = testCamera.copy(id = "cam1", ipAddress = "192.168.1.100", serialNumber = "SN1")
        val camera2 = testCamera.copy(id = "cam2", ipAddress = "192.168.1.101", serialNumber = "SN2")
        repository.addCamera(camera1)
        repository.addCamera(camera2)
        repository.updateCameraOnlineStatus(listOf("192.168.1.100"))

        // Act
        val stats = repository.getStorageStats()

        // Assert
        assertEquals(2, stats.totalCameras)
        assertEquals(1, stats.onlineCameras)
        assertEquals(1, stats.offlineCameras)
        assertTrue(stats.storageSize > 0)
    }

    @Test
    fun `getStorageStats should return zero for empty repository`() = runTest {
        // Act
        val stats = repository.getStorageStats()

        // Assert
        assertEquals(0, stats.totalCameras)
        assertEquals(0, stats.onlineCameras)
        assertEquals(0, stats.offlineCameras)
    }

    // ========== Sync Tests ==========

    @Test
    fun `syncWithServer should return current cameras`() = runTest {
        // Arrange
        repository.addCamera(testCamera)

        // Act
        val result = repository.syncWithServer()

        // Assert
        assertTrue(result.isSuccess)
        val cameras = result.getOrNull()
        assertNotNull(cameras)
        assertEquals(1, cameras?.size)
    }

    // ========== Integration Tests ==========

    @Test
    fun `multiple operations should maintain consistency`() = runTest {
        // Add multiple cameras
        val cam1 = testCamera.copy(id = "cam1", ipAddress = "192.168.1.100")
        val cam2 = testCamera.copy(id = "cam2", ipAddress = "192.168.1.101", serialNumber = "SN2")
        val cam3 = testCamera.copy(id = "cam3", ipAddress = "192.168.1.102", serialNumber = "SN3")
        
        repository.addCamera(cam1)
        repository.addCamera(cam2)
        repository.addCamera(cam3)
        assertEquals(3, repository.cameras.value.size)
        
        // Update online status
        repository.updateCameraOnlineStatus(listOf("192.168.1.100", "192.168.1.102"))
        assertEquals(2, repository.onlineCameras.value.size)
        
        // Remove one camera
        repository.removeCamera("cam2")
        assertEquals(2, repository.cameras.value.size)
        
        // Get stats
        val stats = repository.getStorageStats()
        assertEquals(2, stats.totalCameras)
        assertEquals(2, stats.onlineCameras)
        assertEquals(0, stats.offlineCameras)
    }
}
