package com.vwap.high5_notouch

import android.content.Context
import android.util.Log
import java.io.*

object AssetManager {
    fun exportAssets(context: Context) {
        val assetManager = context.assets
        var files: Array<String>? = null
        try {
            files = assetManager.list("")
        } catch (e: IOException) {
            Log.e("tag", "Failed to get asset file list.", e)
        }
        for (filename in files!!) {
            var inputStream: InputStream? = null
            var outputStream: OutputStream? = null
            try {
                inputStream = assetManager.open(filename)

                val outDir = context.filesDir.absolutePath + "/shared"
                if (!File(outDir).exists()){
                    File(outDir).mkdirs()
                }
                val outFile = File(outDir, filename)
                outputStream = FileOutputStream(outFile)
                copyFile(inputStream, outputStream)
                inputStream.close()
                outputStream.flush()
                outputStream.close()
            } catch (e: IOException) {
                Log.e("tag", "Failed to copy asset file: $filename", e)
            }
        }
    }

    @Throws(IOException::class)
    private fun copyFile(`in`: InputStream, out: OutputStream) {
        val buffer = ByteArray(1024)
        var read: Int
        while (`in`.read(buffer).also { read = it } != -1) {
            out.write(buffer, 0, read)
        }
    }
}