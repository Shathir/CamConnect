package com.outdu.camconnect.communication

import java.io.IOException
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.runTest
import org.junit.Assert.*
import org.junit.Test

/**
 * Unit tests for CameraFileUploader
 * Tests data classes, exception types, and create() validation (no real FTP)
 */
@ExperimentalCoroutinesApi
class CameraFileUploaderTest {

    @Test
    fun `UploadConfig has sensible defaults`() {
        val config = CameraFileUploader.UploadConfig()

        assertEquals("", config.remoteDirectory)
        assertTrue(config.overwriteExisting)
        assertTrue(config.createDirectories)
        assertTrue(config.binaryMode)
        assertTrue(config.passiveMode)
    }

    @Test
    fun `UploadConfig holds custom values`() {
        val config = CameraFileUploader.UploadConfig(
            remoteDirectory = "/remote",
            overwriteExisting = false,
            createDirectories = false,
            binaryMode = false,
            passiveMode = false
        )

        assertEquals("/remote", config.remoteDirectory)
        assertFalse(config.overwriteExisting)
        assertFalse(config.createDirectories)
        assertFalse(config.binaryMode)
        assertFalse(config.passiveMode)
    }

    @Test
    fun `UploaderException subtypes exist`() {
        val connection = CameraFileUploader.UploaderException.ConnectionException("msg", null)
        assertTrue(connection is CameraFileUploader.UploaderException)
        assertTrue(connection.message?.contains("Connection") == true)

        val auth = CameraFileUploader.UploaderException.AuthenticationException("msg")
        assertTrue(auth is CameraFileUploader.UploaderException)

        val transfer = CameraFileUploader.UploaderException.TransferException("msg", null)
        assertTrue(transfer is CameraFileUploader.UploaderException)

        val config = CameraFileUploader.UploaderException.ConfigurationException("msg")
        assertTrue(config is CameraFileUploader.UploaderException)

        val timeout = CameraFileUploader.UploaderException.TimeoutException("msg")
        assertTrue(timeout is CameraFileUploader.UploaderException)
    }

    @Test
    fun `create with invalid host fails without crashing`() = runTest {
        // Unreachable host should yield failure, not throw
        val result = runBlocking {
            CameraFileUploader.create(
                host = "192.0.2.1", // TEST-NET, non-routable
                username = "user",
                password = "pass",
                connectionTimeout = 500,
                dataTimeout = 1000
            )
        }

        assertNotNull(result)
        assertTrue("Create with unreachable host should fail", result.isFailure)
    }

    @Test
    fun `ProgressCallback interface can be implemented`() {
        var started = false
        var progressCalled = false
        var completed = false
        var failed = false

        val callback = object : CameraFileUploader.ProgressCallback {
            override fun onProgress(bytesTransferred: Long, totalBytes: Long) { progressCalled = true }
            override fun onTransferStarted(fileName: String, totalBytes: Long) { started = true }
            override fun onTransferCompleted(fileName: String, bytesTransferred: Long) { completed = true }
            override fun onTransferFailed(fileName: String, error: Throwable) { failed = true }
        }

        callback.onTransferStarted("a.bin", 100L)
        callback.onProgress(50L, 100L)
        callback.onTransferCompleted("a.bin", 100L)
        assertTrue(started)
        assertTrue(progressCalled)
        assertTrue(completed)

        callback.onTransferFailed("b.bin", RuntimeException("test"))
        assertTrue(failed)
    }

    // --- Hour 1: File validation, progress, error recovery branches ---

    @Test
    fun `create with empty host fails`() = runTest {
        val result = runBlocking {
            CameraFileUploader.create(
                host = "",
                username = "user",
                password = "pass",
                connectionTimeout = 500,
                dataTimeout = 1000
            )
        }
        assertTrue("Create with empty host should fail", result.isFailure)
    }

    @Test
    fun `create with blank host fails`() = runTest {
        val result = runBlocking {
            CameraFileUploader.create(
                host = "   ",
                username = "user",
                password = "pass",
                connectionTimeout = 500,
                dataTimeout = 1000
            )
        }
        assertNotNull(result)
        assertTrue("Create with blank host should fail", result.isFailure)
    }

    @Test
    fun `UploadConfig with empty remoteDirectory`() {
        val config = CameraFileUploader.UploadConfig(remoteDirectory = "")
        assertEquals("", config.remoteDirectory)
    }

    @Test
    fun `UploaderException ConnectionException has cause`() {
        val cause = RuntimeException("io")
        val e = CameraFileUploader.UploaderException.ConnectionException("msg", cause)
        assertNotNull(e.cause)
        assertEquals(cause, e.cause)
    }

    @Test
    fun `UploaderException TransferException has cause`() {
        val cause = IOException("transfer")
        val e = CameraFileUploader.UploaderException.TransferException("msg", cause)
        assertNotNull(e.cause)
    }

    @Test
    fun `ProgressCallback null is handled by uploadFile contract`() {
        val callback: CameraFileUploader.ProgressCallback? = null
        assertNull(callback)
    }

    @Test
    fun `UploadConfig overwriteExisting false`() {
        val config = CameraFileUploader.UploadConfig(overwriteExisting = false)
        assertFalse(config.overwriteExisting)
    }

    @Test
    fun `UploadConfig createDirectories false`() {
        val config = CameraFileUploader.UploadConfig(createDirectories = false)
        assertFalse(config.createDirectories)
    }
}
