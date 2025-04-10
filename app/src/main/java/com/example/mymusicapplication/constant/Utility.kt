package com.example.mymusicapplication.constant

import android.content.Context
import android.media.MediaMetadataRetriever
import android.provider.MediaStore
import android.util.Log
import com.example.mymusicapplication.model.MusicFile

fun getImage(uri : String):ByteArray? {
    val retriever = MediaMetadataRetriever()
    retriever.setDataSource(uri)
    val art = retriever.embeddedPicture
    retriever.release()
    return art
}

object GetAudio{

   fun getAllAudio(context: Context): ArrayList<MusicFile> {

      val temList = ArrayList<MusicFile>()
      val uri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI
      val projection = arrayListOf(
         MediaStore.Audio.Media.DATA,
         MediaStore.Audio.Media.TITLE,
         MediaStore.Audio.Media.ARTIST,
         MediaStore.Audio.Media.ALBUM,
         MediaStore.Audio.Media.DURATION
      )

      val cursor = context.contentResolver.query(uri, projection.toTypedArray(), null, null, null)
      cursor?.use { cur ->

         while (cur.moveToNext()) {
            val path = cur.getString(0)
            val title = cur.getString(1)
            val artist = cur.getString(2)
            val album = cur.getString(3)
            val duration = cur.getString(4)
            Log.d(
               "Music Files",
               "getAllAudio: path: $path title : $title artist: $artist album: $album duration :$duration"
            )
            temList.add(MusicFile(path, title, artist, album, duration))
         }

      }

      return temList

   }
}
