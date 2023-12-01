package com.example.myapplication

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import androidx.core.net.toUri
import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.ext.junit.runners.AndroidJUnit4
import junit.framework.TestCase.assertEquals
import org.junit.Test
import org.junit.runner.RunWith
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.io.InputStream
import java.io.OutputStream

@RunWith(AndroidJUnit4::class)
class ExampleInstrumentedTest {

    @Test
    fun useAppContext() {
        // Context of the app under test.
        val appContext = InstrumentationRegistry.getInstrumentation().targetContext
        assertEquals("com.example.myapplication", appContext.packageName)

        // Example test for APK installation
        installTestApk(appContext, R.raw.spotify)
    }

    private fun installTestApk(context: Context, rawResourceId: Int) {
        try {
            // Check if the APK is already installed
            val packageName = "com.example.testapp"
            if (isPackageInstalled(context, packageName)) {
                return
            }

            // Create a temporary file to copy the APK
            val tempFile = File(context.cacheDir, "temp.apk")

            // Copy the APK from resources to the temporary file
            context.resources.openRawResource(rawResourceId).use { input ->
                FileOutputStream(tempFile).use { output ->
                    val buffer = ByteArray(4 * 1024)
                    var bytesRead: Int
                    while (input.read(buffer).also { bytesRead = it } != -1) {
                        output.write(buffer, 0, bytesRead)
                    }
                }
            }

            // Install the APK in the background
            installApkInBackground(context, tempFile)

        } catch (e: IOException) {
            e.printStackTrace()
        }
    }

    private fun isPackageInstalled(context: Context, packageName: String): Boolean {
        return try {
            context.packageManager.getPackageInfo(packageName, PackageManager.GET_ACTIVITIES)
            true
        } catch (e: PackageManager.NameNotFoundException) {
            false
        }
    }

    private fun installApkInBackground(context: Context, apkFile: File) {
        // Run the installation process in the background
        // You might want to use a background thread or coroutine here
        // to avoid blocking the main thread
        val installIntent = Intent(Intent.ACTION_VIEW)
        installIntent.setDataAndType(apkFile.toUri(), "application/vnd.android.package-archive")
        installIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)

        // Check if there's an app to handle the install intent to avoid crashes in test
        if (installIntent.resolveActivity(context.packageManager) != null) {
            context.startActivity(installIntent)

            // Explicitly open or start the installed app after installation
            val startIntent = context.packageManager.getLaunchIntentForPackage("com.example.testapp")
            if (startIntent != null) {
                context.startActivity(startIntent)
            } else {
                throw RuntimeException("No launch intent found for the installed app.")
            }
        } else {
            throw RuntimeException("No activity found to handle the install intent.")
        }
    }
    }

