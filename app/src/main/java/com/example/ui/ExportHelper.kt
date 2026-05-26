package com.example.ui

import android.content.Context
import android.content.Intent
import android.widget.Toast
import androidx.core.content.FileProvider
import java.io.File
import java.io.FileOutputStream

object ExportHelper {

    fun exportToExcel(context: Context, fileName: String, csvContent: String) {
        try {
            // Write to file in app cache
            val cachePath = File(context.cacheDir, "reports")
            cachePath.mkdirs() // Ensure directory exists
            
            val file = File(cachePath, fileName)
            val stream = FileOutputStream(file)
            stream.write(csvContent.toByteArray(Charsets.UTF_8))
            stream.close()

            // Get URI using FileProvider
            val authority = "${context.packageName}.fileprovider"
            val contentUri = FileProvider.getUriForFile(context, authority, file)

            if (contentUri != null) {
                val shareIntent = Intent().apply {
                    action = Intent.ACTION_SEND
                    addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                    setDataAndType(contentUri, "text/csv")
                    putExtra(Intent.EXTRA_STREAM, contentUri)
                    putExtra(Intent.EXTRA_SUBJECT, fileName.replace(".csv", ""))
                }
                
                val chooser = Intent.createChooser(shareIntent, "Share Report via")
                chooser.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                context.startActivity(chooser)
            } else {
                Toast.makeText(context, "Failed to generate report URI", Toast.LENGTH_LONG).show()
            }
        } catch (e: Exception) {
            e.printStackTrace()
            Toast.makeText(context, "Error saving report: ${e.localizedMessage}", Toast.LENGTH_LONG).show()
        }
    }
}
