package com.example.mymusicapplication.service

import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.pm.ServiceInfo
import android.graphics.BitmapFactory
import android.media.MediaPlayer
import android.net.Uri
import android.os.Binder
import android.os.Build
import android.os.Handler
import android.os.IBinder
import android.util.Log
import android.widget.RemoteViews
import androidx.annotation.RequiresApi
import androidx.core.app.NotificationCompat
import com.example.mymusicapplication.Action_Cancel
import com.example.mymusicapplication.Action_Next
import com.example.mymusicapplication.Action_PlayPause
import com.example.mymusicapplication.Action_Previous
import com.example.mymusicapplication.Playback
import com.example.mymusicapplication.R
import com.example.mymusicapplication.SongActivity
import com.example.mymusicapplication.channel1
import com.example.mymusicapplication.constant.GetAudio
import com.example.mymusicapplication.constant.getImage
import com.example.mymusicapplication.model.MusicFile
import com.example.mymusicapplication.reciver.NotificationReceiver


class UpdatedMusicService : Service(), MediaPlayer.OnCompletionListener, Playback, Runnable {


    companion object{
        var instance : UpdatedMusicService? = null
    }
    private val mBinder: IBinder = MyMusicBinder()
    private var mediaPlayer: MediaPlayer? = null
    private var isrepeat: Boolean = false
    private var isSuffle: Boolean = false
    private var callBack: Playback? = null
    private var position: Int = -1
    private var isPlaying: Boolean = false
    private lateinit var uri: Uri
    private var musicFiles: ArrayList<MusicFile> = arrayListOf()
    private lateinit var notification :NotificationCompat.Builder
    private var handler: Handler? = null



    fun start() = mediaPlayer?.start()
    fun stop() = mediaPlayer?.stop()
    fun release() = mediaPlayer?.release()
    fun duration(): Int = mediaPlayer?.duration ?: 0
    fun currentPosition(): Int = mediaPlayer?.currentPosition ?: 0
    fun seekTo(seek: Int) = mediaPlayer?.seekTo(seek)
    fun createMediaPlayer(position: Int) {
        uri = Uri.parse(musicFiles[position].path)
        mediaPlayer = MediaPlayer.create(baseContext, uri)
    }

    fun pause() = mediaPlayer?.pause()
    fun reset() = mediaPlayer?.reset()
    private fun onComplete() = mediaPlayer?.setOnCompletionListener(this)
    fun setCallback(playBack: Playback) {
        callBack = playBack
    }


    override fun onBind(intent: Intent?): IBinder = mBinder

    inner class MyMusicBinder : Binder() {
        fun getServiceInstance(): UpdatedMusicService {
            return this@UpdatedMusicService
        }
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        intentData(intent)
        callBack?.preparePlayerAndView()
        onComplete()
        instance = this@UpdatedMusicService
        return START_STICKY
    }

    override fun onCompletion(mp: MediaPlayer?) {
        Log.d("Test", "onCompletion: ")

        if (!isrepeat) {
            if (isSuffle) {
                position = (0..<musicFiles.size - 1).random()
            } else {
                if (position + 1 < musicFiles.size) {
                    position += 1
                }
            }
            callBack?.setPosition(position)
           preparePlayerAndView()
        } else {
            callBack?.setPosition(position)
            preparePlayerAndView()
        }
        onComplete()
    }

    @RequiresApi(Build.VERSION_CODES.Q)
    private fun intentData(intent: Intent?) {
        if (isPlaying) return
        isPlaying = true
        if (intent != null) {
            position = intent.getIntExtra("servicePosition", -1)
            callBack?.setPosition(position)
            musicFiles = GetAudio.getAllAudio(this)
            showNotification(R.drawable.pause_24)
//            showCustomNotification()
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


    private fun createMusicPlayer(position: Int) {
        createMediaPlayer(position)
        start()
    }


    override fun nextClick() {
        if (isSuffle) {
            position = (0..<musicFiles.size - 1).random()
            preparePlayerAndView()
        } else {
            if (position + 1 < musicFiles.size) {
                position += 1
                preparePlayerAndView()
            }
        }
        callBack?.setPosition(position)
    }

    override fun previousClick() {
        if (isSuffle) {
            position = (0..<musicFiles.size - 1).random()
            preparePlayerAndView()
        } else {
            if (position - 1 >= 0) {
                position -= 1
                preparePlayerAndView()
            }
        }
    }

    override fun playPauseClick() {
        callBack?.playPauseClick()
    }

    override fun preparePlayerAndView() {
        if (isPlaying) {
            stopMediaPlayer()
            isPlaying = false
        }
        createMusicPlayer(position)
        isPlaying = true
//        showNotification(R.drawable.pause_24)
        showCustomNotification()
        callBack?.preparePlayerAndView()
        onComplete()

    }

    override fun setPosition(position: Int) {

    }

    override fun cancelSelf() {
        stopMediaPlayer()
        handler?.removeCallbacks(this)
        callBack?.cancelSelf()
        stopSelf()
    }

    fun getCurrentSongPosition() : Int {
        return position
    }


     private fun stopMediaPlayer() {
        stop()
        reset()
        release()
    }

    fun getFile(): MusicFile {
        return musicFiles[position]
    }

    fun getIsSuffle(): Boolean{
        return isSuffle
    }
    fun getIsPlaying(): Boolean{
        return isPlaying
    }
    fun getIsRepeat() : Boolean{
        return isrepeat
    }

    fun setSuffle(isSuffle : Boolean) {
        this.isSuffle = isSuffle
    }

    fun setIsPLaying(isPlaying : Boolean) {
        this.isPlaying = isPlaying
    }

    fun setRepeat(isRepeat : Boolean) {
        this.isrepeat = isRepeat
    }



    fun showNotification(id: Int) {
        val intent = Intent(this, SongActivity::class.java)
            .putExtra("position", position)
        val contentIntent =
            PendingIntent.getActivity(this, 1, intent, PendingIntent.FLAG_IMMUTABLE)
        val previousIntent =
            Intent(this, NotificationReceiver::class.java).setAction(Action_Previous)
        val previousPendingIntent =
            PendingIntent.getBroadcast(this, 1, previousIntent, (PendingIntent.FLAG_IMMUTABLE))
        val nextIntent = Intent(this, NotificationReceiver::class.java).setAction(Action_Next)
        val nextPendingIntent =
            PendingIntent.getBroadcast(this, 1, nextIntent, PendingIntent.FLAG_IMMUTABLE)
        val playIntent = Intent(this, NotificationReceiver::class.java).setAction(Action_PlayPause)
        val playPendingIntent =
            PendingIntent.getBroadcast(this, 1, playIntent, PendingIntent.FLAG_IMMUTABLE)
        val cancelIntent = Intent(this,NotificationReceiver::class.java).setAction(Action_Cancel)
        val cancelPendingIntent = PendingIntent.getBroadcast(this,1,cancelIntent,PendingIntent.FLAG_IMMUTABLE)

        val picBytes = getImage(musicFiles[position].path)
        val bitMap = if (picBytes != null) {
            BitmapFactory.decodeByteArray(picBytes, 0, picBytes.size)
        } else {
            null
        }
        val file = musicFiles[position]
         notification =
            NotificationCompat.Builder(this, channel1).setSmallIcon(id).setLargeIcon(bitMap)
                .setContentTitle(file.title)
                .setContentText(file.artist)
                .addAction(R.drawable.baseline_skip_previous_24, "pre", previousPendingIntent)
                .addAction(id, "", playPendingIntent)
                .addAction(R.drawable.skip_next_24, "next", nextPendingIntent)
                .addAction(R.drawable.ic_back,"cancel",cancelPendingIntent)
                .setStyle(androidx.media.app.NotificationCompat.MediaStyle()
                    .setShowActionsInCompactView(0, 1, 2,3))
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setOnlyAlertOnce(true)
                .setAutoCancel(false)
                .setOnlyAlertOnce(true)
                .setProgress(file.duration.toInt(),200,false)
                .setContentIntent(contentIntent)

        val notify = notification.build()
        showProgress()
        startForeground(1, notify, ServiceInfo.FOREGROUND_SERVICE_TYPE_MEDIA_PLAYBACK)
//        notificationManager.notify(0,notification)

    }

//    fun showCustomNotification() {
//
//        val file = musicFiles[position]
//        val remoteViews = RemoteViews(packageName, R.layout.custom_notification)
//
//        // Set texts and image
//        remoteViews.setTextViewText(R.id.song_title, file.title)
//        remoteViews.setTextViewText(R.id.song_artist, file.artist)
//        getImage(file.path)?.let {
//            val bitmap = BitmapFactory.decodeByteArray(it, 0, it.size)
//            remoteViews.setImageViewBitmap(R.id.album_art, bitmap)
//        }
//
//        // Set button actions
//        remoteViews.setOnClickPendingIntent(R.id.btn_play_pause, getPendingIntent(Action_PlayPause))
//        remoteViews.setOnClickPendingIntent(R.id.btn_next, getPendingIntent(Action_Next))
//        remoteViews.setOnClickPendingIntent(R.id.btn_prev, getPendingIntent(Action_Previous))
//        remoteViews.setOnClickPendingIntent(R.id.btn_cancel, getPendingIntent(Action_Cancel))
//
//        val notification = NotificationCompat.Builder(this, channel1)
//            .setSmallIcon(R.drawable.pause_24)
//            .setStyle(androidx.media.app.NotificationCompat.DecoratedMediaCustomViewStyle())
//            .setCustomContentView(remoteViews)
//            .setPriority(NotificationCompat.PRIORITY_HIGH)
//            .setOnlyAlertOnce(true)
//            .setAutoCancel(false)
//            .build()
//
//        startForeground(1, notification)
//        showProgress()
////        updateCustomProgress(remoteViews, file.duration.toInt())
//    }
//    private fun updateCustomProgress(remoteViews: RemoteViews, maxDuration: Int) {
////        handler = Handler()
////        handler?.post(object : Runnable {
////            override fun run() {
//                val current = mediaPlayer?.currentPosition ?: 0
//                val progress = (100 * current) / maxDuration
//                remoteViews.setProgressBar(R.id.seek_bar, 100, progress, false)
//
//                val manager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
//                val notification = NotificationCompat.Builder(this@UpdatedMusicService, channel1)
//                    .setSmallIcon(R.drawable.pause_24)
//                    .setStyle(androidx.media.app.NotificationCompat.DecoratedMediaCustomViewStyle())
//                    .setCustomContentView(remoteViews)
//                    .setOnlyAlertOnce(true)
//                    .build()
//
//                manager.notify(1, notification)
//
////                handler?.postDelayed(this, 1000)
////            }
////        })
//    }


    private fun getPendingIntent(actionNext: String): PendingIntent? {
        val intent = Intent(this,NotificationReceiver::class.java).setAction(actionNext)
        return PendingIntent.getBroadcast(this,1,intent,PendingIntent.FLAG_IMMUTABLE)
    }

    private fun showProgress() {
        handler = Handler()
        handler?.postDelayed(this, 0)
    }

    override fun run() {
        Log.d("test", "run: ser . ")
        handler?.post {
            val manager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            val noti = notification.setProgress(musicFiles[position].duration.toInt(),currentPosition(),false).build()
            manager.notify(1,noti)
//            updateCustomProgress( RemoteViews(packageName, R.layout.custom_notification),musicFiles[position].duration.toInt())
        }
        Log.d("test", "run: ser .. ")
        handler?.postDelayed(this, 1000)
        Log.d("test", "run: ser ... ")
    }

}