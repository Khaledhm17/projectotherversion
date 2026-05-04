package com.example.projectotherversion.data.remote.supabase

import android.content.Context
import android.net.Uri
import android.util.Log
import dagger.hilt.android.qualifiers.ApplicationContext
import io.github.jan.supabase.storage.Storage
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SupabaseStorageService @Inject constructor(
    private val storage: Storage,
    @ApplicationContext private val context: Context
) {
    suspend fun uploadImage(uri: Uri, bucketName: String, path: String): Result<String> {
        return try {
            val bucket = storage.from(bucketName)
            val inputStream = context.contentResolver.openInputStream(uri) 
                ?: throw Exception("تعذر فتح مسار الصورة")
            val bytes = inputStream.use { it.readBytes() }
            
            Log.d("SupabaseStorage", "Uploading to $bucketName/$path, size: ${bytes.size} bytes")
            
            bucket.upload(path, bytes) {
                upsert = true
            }
            
            // الحصول على الرابط العام
            val url = bucket.publicUrl(path)
            Log.d("SupabaseStorage", "Upload successful, URL: $url")
            Result.success(url)
        } catch (e: Exception) {
            Log.e("SupabaseStorage", "Upload failed: ${e.message}", e)
            Result.failure(e)
        }
    }
}
