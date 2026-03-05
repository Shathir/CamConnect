package com.outdu.camconnect.communication

import androidx.test.core.app.ApplicationProvider
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.runner.RunWith
import java.io.File

/**
 * Instrumented integration tests for CameraFileUploader with real Android context and file I/O.
 * Covers create() failure paths, and upload with real temp files (upload fails without FTP server but exercises code paths).
 */
@RunWith(AndroidJUnit4::class)
class CameraFileUploaderIntegrationTest {

    @Test
    fun createWithInvalidHostReturnsFailure() = runBlocking {
        val result = CameraFileUploader.create(
            host = "192.0.2.1",
            username = "user",
            password = "pass",
            connectionTimeout = 2000,
            dataTimeout = 5000
        )

        assertTrue("Create with unreachable host should fail", result.isFailure)
        val ex = result.exceptionOrNull()
        assertNotNull(ex)
        assertTrue(
            ex is CameraFileUploader.UploaderException.ConnectionException ||
                ex is CameraFileUploader.UploaderException.TimeoutException
        )
    }

    @Test
    fun createWithEmptyHostReturnsFailure() = runBlocking {
        val result = CameraFileUploader.create(
            host = "",
            username = "user",
            password = "pass",
            connectionTimeout = 1000,
            dataTimeout = 2000
        )

        assertTrue("Create with empty host should fail", result.isFailure)
        assertNotNull(result.exceptionOrNull())
    }

    @Test
    fun createWithVeryShortTimeoutReturnsFailure() = runBlocking {
        val result = CameraFileUploader.create(
            host = "192.0.2.1",
            username = "user",
            password = "pass",
            connectionTimeout = 100,
            dataTimeout = 500
        )
        assertTrue("Create with short timeout should fail", result.isFailure)
        assertNotNull(result.exceptionOrNull())
    }

    @Test
    fun uploadConfigDefaultsAreApplied() {
        val config = CameraFileUploader.UploadConfig()
        assertTrue(config.remoteDirectory == "")
        assertTrue(config.overwriteExisting)
        assertTrue(config.createDirectories)
        assertTrue(config.binaryMode)
        assertTrue(config.passiveMode)
    }

    @Test
    fun uploadFileWithRealTempFileWhenNotConnectedReturnsFailure() = runBlocking {
        val dir = ApplicationProvider.getApplicationContext<android.content.Context>().cacheDir
        val file = File(dir, "upload_integration_test_${System.currentTimeMillis()}.tmp")
        try {
            file.writeBytes(ByteArray(100))
            val createResult = CameraFileUploader.create(
                host = "192.0.2.1",
                username = "u",
                password = "p",
                connectionTimeout = 500,
                dataTimeout = 1000
            )
            if (createResult.isSuccess) {
                createResult.getOrNull()!!.use { uploader ->
                    val result = uploader.uploadFile(
                        localFilePath = file.absolutePath,
                        remoteFileName = "test.bin",
                        progressCallback = null
                    )
                    assertFalse("Upload without real FTP server should fail", result.isSuccess)
                }
            }
        } finally {
            file.delete()
        }
    }
}
