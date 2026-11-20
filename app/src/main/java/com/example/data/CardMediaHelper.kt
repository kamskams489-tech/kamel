package com.example.data

import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import androidx.core.content.FileProvider
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream

object CardMediaHelper {

    suspend fun shareBitmap(context: Context, bitmap: Bitmap, title: String): Result<Unit> =
        withContext(Dispatchers.IO) {
            try {
                val cacheDir = File(context.cacheDir, "dhikr_cards").apply { mkdirs() }
                val outFile = File(cacheDir, "tasbih_card_${System.currentTimeMillis()}.png")
                FileOutputStream(outFile).use { stream ->
                    bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream)
                }
                val uri = FileProvider.getUriForFile(
                    context,
                    "${context.packageName}.fileprovider",
                    outFile
                )
                val shareIntent = Intent(Intent.ACTION_SEND).apply {
                    type = "image/png"
                    putExtra(Intent.EXTRA_STREAM, uri)
                    putExtra(Intent.EXTRA_TEXT, title)
                    addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                }
                val chooser = Intent.createChooser(shareIntent, "مشاركة بطاقة الذكر").apply {
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                }
                withContext(Dispatchers.Main) {
                    context.startActivity(chooser)
                }
                Result.success(Unit)
            } catch (e: Exception) {
                Result.failure(e)
            }
        }

    suspend fun saveBitmapToGallery(context: Context, bitmap: Bitmap): Result<Unit> =
        withContext(Dispatchers.IO) {
            try {
                val filename = "Tasbih_Card_${System.currentTimeMillis()}.png"
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    val values = ContentValues().apply {
                        put(MediaStore.Images.Media.DISPLAY_NAME, filename)
                        put(MediaStore.Images.Media.MIME_TYPE, "image/png")
                        put(
                            MediaStore.Images.Media.RELATIVE_PATH,
                            "${Environment.DIRECTORY_PICTURES}/Tasbih"
                        )
                        put(MediaStore.Images.Media.IS_PENDING, 1)
                    }
                    val resolver = context.contentResolver
                    val uri = resolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values)
                        ?: return@withContext Result.failure(
                            IllegalStateException("تعذر إنشاء ملف الصورة في المعرض.")
                        )
                    resolver.openOutputStream(uri)?.use { stream ->
                        bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream)
                    } ?: return@withContext Result.failure(
                        IllegalStateException("تعذر كتابة الصورة.")
                    )
                    values.clear()
                    values.put(MediaStore.Images.Media.IS_PENDING, 0)
                    resolver.update(uri, values, null, null)
                    Result.success(Unit)
                } else {
                    // On API < 29 avoid WRITE_EXTERNAL_STORAGE per Play Policy: share cleanly instead
                    shareBitmap(context, bitmap, "بطاقة ذكر من تطبيق تسبيح")
                }
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
}
