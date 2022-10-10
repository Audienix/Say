package com.twain.say.helper

import android.app.Activity
import android.content.ContentResolver
import android.content.ContentValues
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import android.webkit.MimeTypeMap
import androidx.annotation.RequiresApi
import androidx.core.content.FileProvider
import com.twain.say.ui.home.model.Note
import java.io.File
import java.io.OutputStream
import java.util.*

class ShareNote(val context: Activity) {
    private fun shareNote(note: Note) {
//        showToast("You'll be able to share ${note.title} soon")
//        val sharePath = (note.filePath)
//        val uri: Uri = Uri.parse(sharePath)
//        val share = Intent(Intent.ACTION_SEND)
//        share.type = "audio/3gp"
//        share.putExtra(Intent.EXTRA_STREAM, uri)
//        startActivity(Intent.createChooser(share, "Share Sound File"))

        val uri = FileProvider.getUriForFile(
            context.applicationContext,
            "com.twain.say.fileprovider",
            File(note.filePath)
        )

        val shareIntent = Intent(Intent.ACTION_OPEN_DOCUMENT)
        shareIntent.type = "audio/*"
        shareIntent.putExtra(Intent.EXTRA_STREAM, uri)

        context.startActivity(Intent.createChooser(shareIntent, "Share Sound File"))
    }

    private fun shareNotes(note: Note) {
        val shareIntent = Intent()
        shareIntent.action = Intent.ACTION_SEND
        val shareMgText = note.title
        shareIntent.putExtra(Intent.EXTRA_TEXT, shareMgText)
        val audioUri: Uri? = getShareableAudioUri(note)
        shareIntent.putExtra(Intent.EXTRA_STREAM, audioUri)
        shareIntent.type = "audio/*"
        shareIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        context.startActivity(Intent.createChooser(shareIntent, "Select the app"))
    }

    private fun getShareableAudioUri(note: Note): Uri? {
        val fileName = note.filePath
        var imageUri: Uri? = null
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            imageUri = getAudioFileUri(fileName)
        } else {
//            val url: String = MediaStore.Audio.Media.insertImage(
//                context!!.contentResolver,
//                ImageUtil.getBitmapFromView(postImageView),
//                fileName,
//                ""
//            )
//            val fos: OutputStream?
//            try {
//                fos = context!!.contentResolver.openOutputStream(imageUri!!)
//                ImageUtil.getBitmapFromView(postImageView)
//                    .compress(Bitmap.CompressFormat.PNG, 100, fos)
//                fos!!.flush()
//                fos.close()
//            } catch (e: java.lang.Exception) {
//                e.printStackTrace()
//            }
//            if (url != null && !url.isEmpty()) {
//                imageUri = Uri.parse(url)
//            }
        }
        return imageUri
    }

    @RequiresApi(api = Build.VERSION_CODES.Q)
    private fun getAudioFileUri(fileName: String): Uri? {
        val contentValues = ContentValues()

        contentValues.put(MediaStore.Audio.Media.RELATIVE_PATH, "Music/Recordings/")
//        contentValues.put(MediaStore.Audio.Media.RELATIVE_PATH, "Audio/")
        contentValues.put(MediaStore.Audio.Media.TITLE, fileName)
        contentValues.put(MediaStore.Audio.Media.DISPLAY_NAME, fileName)
        contentValues.put(MediaStore.Audio.Media.MIME_TYPE, "audio/*")
        contentValues.put(MediaStore.Audio.Media.DATE_ADDED, System.currentTimeMillis() / 1000)
        contentValues.put(MediaStore.Audio.Media.DATE_TAKEN, System.currentTimeMillis())
        contentValues.put(MediaStore.Audio.Media.IS_PENDING, 1)
        val resolver = context.contentResolver
        val audioUri = resolver.insert(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, contentValues)
        val fos: OutputStream?
        try {
            fos = resolver.openOutputStream(audioUri!!)
            fos!!.flush()
            fos.close()
            contentValues.clear()
            contentValues.put(MediaStore.Audio.Media.IS_PENDING, 0)
            resolver.update(audioUri, contentValues, null, null)
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return audioUri
    }

    fun getMimeType(uri: Uri): String? {
        val mimeType: String? = if (ContentResolver.SCHEME_CONTENT == uri.scheme) {
            val cr: ContentResolver = context.applicationContext.contentResolver
            cr.getType(uri)
        } else {
            val fileExtension = MimeTypeMap.getFileExtensionFromUrl(
                uri
                    .toString()
            )
            MimeTypeMap.getSingleton().getMimeTypeFromExtension(
                fileExtension.lowercase(Locale.getDefault())
            )
        }
        return mimeType
    }
}