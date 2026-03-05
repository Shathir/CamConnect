package com.outdu.camconnect.instrumented.data

import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.outdu.camconnect.data.CameraRepository
import com.outdu.camconnect.data.StoredCamera
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Instrumented tests for CameraRepository with real SharedPreferences and Context.
 * In package instrumented.data to avoid runner conflicts with app's data package.
 */
@RunWith(AndroidJUnit4::class)
class CameraRepositoryInstrumentedTest {

    private fun repository(): CameraRepository =
        CameraRepository(InstrumentationRegistry.getInstrumentation().targetContext)

    @After
    fun tearDown() {
        runBlocking { repository().clearAllCameras() }
    }

    @Test
    fun initial_cameras_flow_emitsEmptyList() {
        runBlocking {
            val repo = repository()
            repo.clearAllCameras()
            val list = repo.cameras.first()
            assertTrue(list.isEmpty())
        }
    }

    @Test
    fun addCamera_persistsAndEmitsInFlow() {
        runBlocking {
            val repo = repository()
            repo.clearAllCameras()
            val camera = StoredCamera(
                name = "Test Cam",
                ipAddress = "192.168.1.10",
                serialNumber = "SN001"
            )
            val result = repo.addCamera(camera)
            assertTrue(result.isSuccess)
            val list = repo.cameras.first()
            assertEquals(1, list.size)
            assertEquals("Test Cam", list[0].name)
            assertEquals("192.168.1.10", list[0].ipAddress)
        }
    }

    @Test
    fun addCamera_duplicateIp_returnsFailure() {
        runBlocking {
            val repo = repository()
            repo.clearAllCameras()
            val camera = StoredCamera(name = "Cam1", ipAddress = "192.168.1.20", serialNumber = "SN1")
            repo.addCamera(camera)
            val duplicate = StoredCamera(name = "Cam2", ipAddress = "192.168.1.20", serialNumber = "SN2")
            val result = repo.addCamera(duplicate)
            assertTrue(result.isFailure)
            assertEquals(1, repo.cameras.first().size)
        }
    }

    @Test
    fun removeCamera_removesFromFlow() {
        runBlocking {
            val repo = repository()
            repo.clearAllCameras()
            val camera = StoredCamera(name = "ToRemove", ipAddress = "192.168.1.30", serialNumber = "SN30")
            val addResult = repo.addCamera(camera)
            assertTrue(addResult.isSuccess)
            val id = addResult.getOrNull()!!.id.ifEmpty { repo.cameras.first().first().id }
            val removeResult = repo.removeCamera(id)
            assertTrue(removeResult.isSuccess)
            assertTrue(removeResult.getOrNull() == true)
            assertTrue(repo.cameras.first().isEmpty())
        }
    }

    @Test
    fun removeCamera_nonExistent_returnsFalse() {
        runBlocking {
            val repo = repository()
            repo.clearAllCameras()
            val result = repo.removeCamera("non-existent-id")
            assertTrue(result.isSuccess)
            assertFalse(result.getOrNull()!!)
        }
    }

    @Test
    fun getCameraById_returnsCamera() {
        runBlocking {
            val repo = repository()
            repo.clearAllCameras()
            val camera = StoredCamera(name = "ById", ipAddress = "192.168.1.40", serialNumber = "SN40")
            val addResult = repo.addCamera(camera)
            val added = addResult.getOrNull()!!
            val id = added.id.ifEmpty { repo.cameras.first().first().id }
            val found = repo.getCameraById(id)
            assertNotNull(found)
            assertEquals("ById", found!!.name)
        }
    }

    @Test
    fun getCameraByIP_returnsCamera() {
        runBlocking {
            val repo = repository()
            repo.clearAllCameras()
            val camera = StoredCamera(name = "ByIP", ipAddress = "192.168.1.50", serialNumber = "SN50")
            repo.addCamera(camera)
            val found = repo.getCameraByIP("192.168.1.50")
            assertNotNull(found)
            assertEquals("ByIP", found!!.name)
        }
    }

    @Test
    fun getCameraByIP_unknown_returnsNull() {
        assertNull(repository().getCameraByIP("192.168.99.99"))
    }

    @Test
    fun updateCameraOnlineStatus_updatesOnlineSet() {
        runBlocking {
            val repo = repository()
            repo.clearAllCameras()
            val camera = StoredCamera(name = "Online", ipAddress = "192.168.1.60", serialNumber = "SN60")
            repo.addCamera(camera)
            repo.updateCameraOnlineStatus(listOf("192.168.1.60"))
            val online = repo.onlineCameras.first()
            assertTrue(online.contains("192.168.1.60"))
        }
    }

    @Test
    fun getCamerasWithStatus_returnsListWithStatus() {
        runBlocking {
            val repo = repository()
            repo.clearAllCameras()
            val camera = StoredCamera(name = "Status", ipAddress = "192.168.1.70", serialNumber = "SN70")
            repo.addCamera(camera)
            repo.updateCameraOnlineStatus(listOf("192.168.1.70"))
            val withStatus = repo.getCamerasWithStatus()
            assertEquals(1, withStatus.size)
            assertTrue(withStatus[0].isOnline)
        }
    }

    @Test
    fun getStorageStats_returnsStats() {
        runBlocking {
            val repo = repository()
            repo.clearAllCameras()
            val camera = StoredCamera(name = "Stats", ipAddress = "192.168.1.80", serialNumber = "SN80")
            repo.addCamera(camera)
            val stats = repo.getStorageStats()
            assertEquals(1, stats.totalCameras)
            assertTrue(stats.storageSize >= 0)
        }
    }

    @Test
    fun clearAllCameras_clearsFlow() {
        runBlocking {
            val repo = repository()
            repo.clearAllCameras()
            repo.addCamera(StoredCamera(name = "A", ipAddress = "192.168.1.91", serialNumber = "S91"))
            repo.clearAllCameras()
            assertTrue(repo.cameras.first().isEmpty())
            assertTrue(repo.onlineCameras.first().isEmpty())
        }
    }

    @Test
    fun syncWithServer_returnsSuccess() {
        runBlocking {
            val repo = repository()
            val result = repo.syncWithServer()
            assertTrue(result.isSuccess)
            assertNotNull(result.getOrNull())
        }
    }
}
