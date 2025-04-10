package com.example.mymusicapplication.reciver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.widget.Toast
import com.example.mymusicapplication.Action_Cancel
import com.example.mymusicapplication.Action_Next
import com.example.mymusicapplication.Action_PlayPause
import com.example.mymusicapplication.Action_Previous
import com.example.mymusicapplication.Playback
import com.example.mymusicapplication.service.UpdatedMusicService
import com.google.android.material.animation.AnimatableView.Listener

open class NotificationReceiver : BroadcastReceiver() {
    private var listner : Playback? = null


    override fun onReceive(context: Context?, intent: Intent?) {
        listner = UpdatedMusicService.instance as Playback

        when(intent?.action){
            Action_Previous ->{
                Toast.makeText(context, "pre", Toast.LENGTH_SHORT).show()
            listner?.previousClick()
            }
            Action_Next ->{
                Toast.makeText(context, "nex", Toast.LENGTH_SHORT).show()
                listner?.nextClick()
            }
            Action_PlayPause ->{
                Toast.makeText(context, "play", Toast.LENGTH_SHORT).show()
                listner?.playPauseClick()
            }
            Action_Cancel -> {
                Toast.makeText(context, "cancel", Toast.LENGTH_SHORT).show()
                listner?.cancelSelf()
            }

            else ->{}
        }

    }
}