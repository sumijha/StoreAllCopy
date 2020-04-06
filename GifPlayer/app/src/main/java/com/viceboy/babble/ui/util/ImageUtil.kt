package com.viceboy.babble.ui.util

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.net.Uri
import android.provider.MediaStore
import androidx.core.net.toUri
import androidx.exifinterface.media.ExifInterface
import com.viceboy.babble.R
import timber.log.Timber
import java.io.*
import java.nio.ByteBuffer
import java.text.SimpleDateFormat
import java.util.*

object ImageUtil {

    fun compressImage(file: File, outputDir: File): Uri {
        val inputBitmap: Bitmap?

        Timber.e("Original Image size is ${file.length()}")
        //Decode with inSample size
        val bitmapOptions = BitmapFactory.Options()
        bitmapOptions.inSampleSize = 1
        val tempStream = FileInputStream(file)
        try {
            inputBitmap = BitmapFactory.decodeStream(tempStream, null, bitmapOptions)
        } finally {
            tempStream.close()
        }

        val tempByteOut =
            getByteArrayAfterImageCompression(inputBitmap, Bitmap.CompressFormat.JPEG, 100)
        val decodedBitmap = decodeBitmap(tempByteOut.toByteArray(), file.toUri(), 1000, 1000)
        val destFile = createFile(outputDir, FILENAME, FILE_FORMAT)

        var newByteOut: ByteArrayOutputStream? = null
        var newFileOutputStream: FileOutputStream? = null

        try {
            newByteOut =
                getByteArrayAfterImageCompression(decodedBitmap, Bitmap.CompressFormat.JPEG, 100)
            newFileOutputStream = FileOutputStream(destFile)
            newFileOutputStream.apply {
                write(newByteOut.toByteArray())
                flush()
            }
        } finally {
            if (newByteOut != null) {
                try {
                    newFileOutputStream?.close()
                    tempByteOut.close()
                    newByteOut.close()
                } catch (ignore: IOException) {
                }
            }
        }

        if (destFile.length() >= file.length())
            return Uri.fromFile(file)

        Timber.e("Compressed Image size is ${destFile.length()}")
        return Uri.fromFile(destFile)
    }

    @Suppress("SameParameterValue")
    private fun decodeBitmap(
        source: ByteArray,
        uri: Uri,
        maxWidth: Int,
        maxHeight: Int
    ): Bitmap {
        var bitmap: Bitmap
        var orientation = 0
        try {
            val exif = ExifInterface(uri.path!!)
            val exifOrientation: Int = exif.getAttributeInt(
                ExifInterface.TAG_ORIENTATION,
                ExifInterface.ORIENTATION_NORMAL
            )
            when (exifOrientation) {
                ExifInterface.ORIENTATION_NORMAL -> Unit
                ExifInterface.ORIENTATION_FLIP_HORIZONTAL -> orientation = 0
                ExifInterface.ORIENTATION_ROTATE_180 -> Unit
                ExifInterface.ORIENTATION_FLIP_VERTICAL -> orientation = 180
                ExifInterface.ORIENTATION_ROTATE_90 -> orientation = 90
                ExifInterface.ORIENTATION_TRANSPOSE -> orientation = 90
                ExifInterface.ORIENTATION_ROTATE_270 -> Unit
                ExifInterface.ORIENTATION_TRANSVERSE -> orientation = 270
            }

        } catch (e: IOException) {
            e.printStackTrace()
            orientation = 0
        }

        if (maxWidth < Int.MAX_VALUE || maxHeight < Int.MAX_VALUE) {
            val options = BitmapFactory.Options()
            options.inJustDecodeBounds = true
            BitmapFactory.decodeByteArray(source, 0, source.size, options)

            var outHeight = options.outHeight
            var outWidth = options.outWidth
            if (orientation % 180 != 0) {
                outHeight = options.outWidth
                outWidth = options.outHeight
            }
            options.inSampleSize = computeSampleSize(outWidth, outHeight, maxWidth, maxHeight)
            options.inJustDecodeBounds = false
            bitmap = BitmapFactory.decodeByteArray(source, 0, source.size, options)
        } else {
            bitmap = BitmapFactory.decodeByteArray(source, 0, source.size)
        }

        Matrix().apply {
            setRotate(orientation.toFloat())
            val temp = bitmap
            bitmap = Bitmap.createBitmap(bitmap, 0, 0, bitmap.width, bitmap.height, this, false)
            temp.recycle()
        }

        return bitmap
    }

    private fun computeSampleSize(
        outWidth: Int,
        outHeight: Int,
        maxWidth: Int,
        maxHeight: Int
    ): Int {
        var sampleSize = 1
        if (outHeight > maxHeight || outWidth > maxWidth) {
            while ((outHeight / sampleSize >= maxHeight)
                || (outWidth / sampleSize >= maxWidth)
            ) {
                sampleSize *= 2
            }
        }
        return sampleSize
    }

    @Suppress("SameParameterValue")
    private fun getByteArrayAfterImageCompression(
        bitmap: Bitmap?,
        format: Bitmap.CompressFormat,
        quality: Int
    ): ByteArrayOutputStream {
        val outByteArrayOutputStream = ByteArrayOutputStream()
        try {
            bitmap?.compress(format, quality, outByteArrayOutputStream)
        } catch (exception: Exception) {
            Timber.e(exception.message)
        }
        return outByteArrayOutputStream
    }

    private fun getRealPathFromUri(context: Context, uri: Uri): String {
        val result: String
        val cursor = context.contentResolver.query(uri, null, null, null, null)
        result = if (cursor == null) {
            uri.path.toString()
        } else {
            cursor.moveToFirst()
            val idx = cursor.getColumnIndex(MediaStore.Images.Media._ID)
            cursor.getColumnName(idx)
        }
        cursor?.close()
        return result
    }

    private fun getFileFromUri(context: Context, uri: Uri) = File(getRealPathFromUri(context, uri))

    /**
     * Convert bitmap to byte array using ByteBuffer.
     */
    fun Bitmap.convertToByteArray(): ByteArray {
        //minimum number of bytes that can be used to store this bitmap's pixels
        val size = this.byteCount

        //allocate new instances which will hold bitmap
        val buffer = ByteBuffer.allocate(size)
        val bytes = ByteArray(size)

        //copy the bitmap's pixels into the specified buffer
        this.copyPixelsToBuffer(buffer)

        //rewinds buffer (buffer position is set to zero and the mark is discarded)
        buffer.rewind()

        //transfer bytes from buffer into the given destination array
        buffer.get(bytes)

        //return bitmap's pixels
        return bytes
    }

    fun writeBitmapToFile(bitmap: Bitmap?, context: Context): File {
        val outputDir = getMediaDirectory(context)
        val outputFile = createFile(outputDir, FILENAME, FILE_FORMAT)

        var outputStream: FileOutputStream? = null
        try {
            outputStream = FileOutputStream(outputFile)
            bitmap?.compress(Bitmap.CompressFormat.JPEG, 20, outputStream)
        } finally {
            if (outputStream != null) {
                try {
                    outputStream.close()
                } catch (ignored: Exception) {
                }
            }
        }
        return outputFile
    }

    fun createFile(
        baseFolder: File,
        fileName: String = FILENAME,
        fileExt: String = FILE_FORMAT
    ): File {
        return File(
            baseFolder,
            FILENAME_STARTS_WITH + SimpleDateFormat(
                fileName,
                Locale.US
            ).format(System.currentTimeMillis()) + fileExt
        )
    }

    fun getMediaDirectory(context: Context): File {
        val appContext = context.applicationContext
        val mediaDir = context.externalMediaDirs.firstOrNull()?.let {
            File(it, appContext.resources.getString(R.string.app_name)).apply { mkdirs() }
        }
        return if (mediaDir != null && mediaDir.exists()) mediaDir else appContext.filesDir
    }

    fun deleteFileFromUri(context: Context,uri: Uri?) {
        uri?.let {
            val imageFile = getFileFromUri(context, it)
            imageFile.delete()
        }
    }


    // Constants used to create file for Image Receipts
    private const val FILE_FORMAT = ".jpg"
    private const val FILENAME = "yyyy-MM-dd-HH-mm-ss-SSS"
    private const val FILENAME_STARTS_WITH = "Receipt_"
}