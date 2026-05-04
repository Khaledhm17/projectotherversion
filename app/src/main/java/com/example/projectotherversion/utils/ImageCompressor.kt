package com.example.projectotherversion.utils

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileOutputStream

object ImageCompressor {

    /**
     * ضغط الصورة من Uri وتخزينها في ملف مؤقت
     * @param context سياق التطبيق
     * @param imageUri رابط الصورة الأصلي
     * @param maxWidth أقصى عرض للصورة بعد الضغط (اختياري)
     * @param maxHeight أقصى ارتفاع للصورة بعد الضغط (اختياري)
     * @param quality جودة الضغط من 0 إلى 100
     * @return ملف مؤقت يحتوي على الصورة المضغوطة
     */
    fun compressImage(
        context: Context,
        imageUri: Uri,
        maxWidth: Int = 1024,
        maxHeight: Int = 1024,
        quality: Int = 80
    ): File? {
        return try {
            val inputStream = context.contentResolver.openInputStream(imageUri)
            val originalBitmap = BitmapFactory.decodeStream(inputStream)
            inputStream?.close()

            // حساب الأبعاد الجديدة مع الحفاظ على نسبة العرض إلى الارتفاع
            val width = originalBitmap.width
            val height = originalBitmap.height
            val ratio = width.toFloat() / height.toFloat()

            val newWidth: Int
            val newHeight: Int
            if (ratio > 1) {
                newWidth = maxWidth
                newHeight = (maxWidth / ratio).toInt()
            } else {
                newHeight = maxHeight
                newWidth = (maxHeight * ratio).toInt()
            }

            val scaledBitmap = Bitmap.createScaledBitmap(originalBitmap, newWidth, newHeight, true)
            originalBitmap.recycle()

            // تحويل bitmap إلى ملف
            val outputFile = File(context.cacheDir, "compressed_${System.currentTimeMillis()}.jpg")
            val outputStream = FileOutputStream(outputFile)
            val byteArrayOutputStream = ByteArrayOutputStream()

            scaledBitmap.compress(Bitmap.CompressFormat.JPEG, quality, byteArrayOutputStream)
            outputStream.write(byteArrayOutputStream.toByteArray())

            outputStream.flush()
            outputStream.close()
            byteArrayOutputStream.close()
            scaledBitmap.recycle()

            outputFile
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    /**
     * الحصول على Uri لملف مضغوط من صورة أصلية
     */
    fun getCompressedImageUri(context: Context, imageUri: Uri): Uri? {
        val compressedFile = compressImage(context, imageUri) ?: return null
        return Uri.fromFile(compressedFile)
    }
}