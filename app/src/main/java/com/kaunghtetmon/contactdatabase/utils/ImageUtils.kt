package com.kaunghtetmon.contactdatabase.utils

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.ImageDecoder
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import java.io.ByteArrayOutputStream

object ImageUtils {
    
    fun compressImage(bitmap: Bitmap, quality: Int = 50): ByteArray {
        val outputStream = ByteArrayOutputStream()
        
        // Resize the bitmap if it's too large
        val maxDimension = 512
        val resizedBitmap = if (bitmap.width > maxDimension || bitmap.height > maxDimension) {
            val ratio = Math.min(
                maxDimension.toFloat() / bitmap.width,
                maxDimension.toFloat() / bitmap.height
            )
            val width = (ratio * bitmap.width).toInt()
            val height = (ratio * bitmap.height).toInt()
            Bitmap.createScaledBitmap(bitmap, width, height, true)
        } else {
            bitmap
        }
        
        // Compress the bitmap
        resizedBitmap.compress(Bitmap.CompressFormat.JPEG, quality, outputStream)
        
        // Clean up if we created a new bitmap
        if (resizedBitmap != bitmap) {
            resizedBitmap.recycle()
        }
        
        return outputStream.toByteArray()
    }
    
    fun uriToBitmap(context: Context, uri: Uri): Bitmap? {
        return try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                val source = ImageDecoder.createSource(context.contentResolver, uri)
                ImageDecoder.decodeBitmap(source)
            } else {
                @Suppress("DEPRECATION")
                MediaStore.Images.Media.getBitmap(context.contentResolver, uri)
            }
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }
    
    fun byteArrayToBitmap(byteArray: ByteArray?): Bitmap? {
        return if (byteArray != null && byteArray.isNotEmpty()) {
            BitmapFactory.decodeByteArray(byteArray, 0, byteArray.size)
        } else {
            null
        }
    }
}

