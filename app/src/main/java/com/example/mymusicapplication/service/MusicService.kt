package com.example.mymusicapplication.service

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Intent
import android.content.pm.ServiceInfo
import android.graphics.BitmapFactory
import android.media.MediaPlayer
import android.net.Uri
import android.os.Binder
import android.os.Build
import android.os.IBinder
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.core.app.NotificationCompat
import com.example.mymusicapplication.Action_Next
import com.example.mymusicapplication.Action_PlayPause
import com.example.mymusicapplication.Action_Previous
import com.example.mymusicapplication.MainActivity
import com.example.mymusicapplication.Playback
import com.example.mymusicapplication.R
import com.example.mymusicapplication.SongActivity
import com.example.mymusicapplication.channel1
import com.example.mymusicapplication.constant.GetAudio
import com.example.mymusicapplication.constant.getImage
import com.example.mymusicapplication.model.MusicFile
import com.example.mymusicapplication.reciver.NotificationReceiver


class MusicService : Service(), MediaPlayer.OnCompletionListener {

    private val mBinder: IBinder = MyBinder()
    private  var mediaPlayer : MediaPlayer? = null
    private lateinit var uri : Uri
    private var musicFiles : ArrayList<MusicFile> = arrayListOf()
    var position: Int = -1
    private var callBack : Playback? = null
     var isSuffle: Boolean = false
     var isrepeat: Boolean = false
     var isPlaying : Boolean = false

    override fun onBind(intent: Intent?): IBinder {
        Log.d("onbind", "onBind: ")
      return  mBinder
    }
    fun getCurrentSongPosition() : Int {
        return position
    }
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        intentData(intent)
        callBack?.preparePlayerAndView()
        return START_STICKY
    }

    @RequiresApi(Build.VERSION_CODES.Q)
    private fun intentData(intent: Intent?) {
        if (isPlaying) return
        isPlaying = true
        if (intent != null) {
            position = intent.getIntExtra("servicePosition", -1)
            callBack?.setPosition(position)
            musicFiles = GetAudio.getAllAudio(this)
            showNotification(R.drawable.play_circle_outline_24)
            if (mediaPlayer != null){
                stop()
                reset()
                release()
                if (position!= -1){
//                    if (musicFiles.isEmpty())  musicFiles = GetAudio.getAllAudio(this)
                    createMediaPlayer(position)
                    start()
                }
            }else{
                createMediaPlayer(position)
                start()
            }
        }

    }

    inner class MyBinder() : Binder() {
        fun getService(): MusicService {
            return this@MusicService
        }
    }

    fun start() = mediaPlayer?.start()
    fun stop() = mediaPlayer?.stop()
    fun release() = mediaPlayer?.release()
    fun duration() : Int = mediaPlayer?.duration ?:0
    fun currentPosition(): Int = mediaPlayer?.currentPosition ?:0
    fun seekTo(seek : Int) = mediaPlayer?.seekTo(seek)
    fun createMediaPlayer(position:Int )  {
        uri = Uri.parse(musicFiles[position].path)
        mediaPlayer = MediaPlayer.create(baseContext, uri)
    }
    fun pause() = mediaPlayer?.pause()
    fun reset() = mediaPlayer?.reset()
    fun onComplete() = mediaPlayer?.setOnCompletionListener(this)
    fun setCallback(playBack : Playback){ callBack = playBack }

    override fun onCompletion(mp: MediaPlayer?) {
        Log.d("Test", "onCompletion: ")

        if (!isrepeat) {
         if (isSuffle) {
                position = (0..<musicFiles.size-1).random()
            } else {
                if (position + 1 < musicFiles.size) {
                    position += 1
                }
            }
            callBack?.setPosition(position)
            callBack?.preparePlayerAndView()
        }else{
            callBack?.setPosition(position)
            callBack?.preparePlayerAndView()
        }
    }

     fun showNotification(id: Int) {
        val intent = Intent(this, SongActivity::class.java)
            .putExtra("position",position)
        val contentIntent =
            PendingIntent.getActivity(this, 1, intent, PendingIntent.FLAG_MUTABLE)
        val previousIntent =
            Intent(this, NotificationReceiver::class.java).setAction(Action_Previous)
        val previousPendingIntent =
            PendingIntent.getBroadcast(this, 1, previousIntent,(PendingIntent.FLAG_IMMUTABLE))
        val nextIntent = Intent(this, NotificationReceiver::class.java).setAction(Action_Next)
        val nextPendingIntent =
            PendingIntent.getBroadcast(this, 1, nextIntent, PendingIntent.FLAG_MUTABLE)
        val playIntent = Intent(this, NotificationReceiver::class.java).setAction(Action_PlayPause)
        val playPendingIntent =
            PendingIntent.getBroadcast(this, 1, playIntent, PendingIntent.FLAG_MUTABLE)

        val picBytes = getImage(musicFiles[position].path)
        val bitMap = if (picBytes != null) {
            BitmapFactory.decodeByteArray(picBytes, 0, picBytes.size)
        } else {
            null
        }
        val file = musicFiles[position]
        val notification =
            NotificationCompat.Builder(this, channel1).setSmallIcon(id).setLargeIcon(bitMap)
                .setContentTitle(file.title)
                .setContentText(file.artist)
                .addAction(R.drawable.baseline_skip_previous_24,"pre",previousPendingIntent)
                .addAction(id,"",playPendingIntent)
                .addAction(R.drawable.skip_next_24,"next",nextPendingIntent)
//                .setStyle(androidx.core.app.NotificationCompat.MediaStyle()) todo cant find
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setOnlyAlertOnce(true)
                .setAutoCancel(false)
                .setContentIntent(contentIntent)
                .build()
        val notificationManager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
        startForeground(id,notification,ServiceInfo.FOREGROUND_SERVICE_TYPE_MEDIA_PLAYBACK)
//        notificationManager.notify(0,notification)

    }



}