package com.example.mymusicapplication

interface Playback {
    fun nextClick()
    fun previousClick()
    fun playPauseClick()
    fun preparePlayerAndView()
    fun setPosition(position : Int)
    fun cancelSelf()
}