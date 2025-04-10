package com.example.mymusicapplication

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.os.Build


const val channel1 = "channel1"
const val channel2 = "channel2"
const val Action_Previous = "actionPrevious"
const val Action_Next = "actionNext"
const val Action_PlayPause = "actionPlayPause"
const val Action_Cancel = "actionCancel"
class MyApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        createNotification()
    }

    private fun createNotification() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O){
            val notificationChannel1 = NotificationChannel(channel1,"Channel1(1)",NotificationManager.IMPORTANCE_HIGH)
            notificationChannel1.description = "This MusicPlayer 1 Dec.."
            val notificationChannel2 = NotificationChannel(channel2,"Channel2(2)",NotificationManager.IMPORTANCE_HIGH)
            notificationChannel2.description = "This MusicPlayer 2 Dec.."

            val notificationManager = getSystemService(NotificationManager::class.java)
            notificationManager.apply {
                createNotificationChannel(notificationChannel1)
                createNotificationChannel(notificationChannel2)
            }
        }
    }
}