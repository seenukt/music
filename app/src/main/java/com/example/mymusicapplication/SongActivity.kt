package com.example.mymusicapplication


import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.os.IBinder
import android.util.Log
import android.view.WindowInsets
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import android.widget.ImageView
import android.widget.SeekBar
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.palette.graphics.Palette
import com.bumptech.glide.Glide
import com.example.mymusicapplication.constant.GetAudio
import com.example.mymusicapplication.constant.getImage
import com.example.mymusicapplication.databinding.ActivitySongBinding
import com.example.mymusicapplication.model.MusicFile
import com.example.mymusicapplication.service.UpdatedMusicService

class SongActivity : AppCompatActivity(), Playback, ServiceConnection, Runnable {

    private lateinit var binding: ActivitySongBinding
    private lateinit var musicFiles: ArrayList<MusicFile>
    private var position: Int = -1
    private var handler: Handler? = null
    private var service: UpdatedMusicService? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySongBinding.inflate(layoutInflater)
        setContentView(binding.root)

        musicFiles = GetAudio.getAllAudio(this)
        clickListener()


    }

    override fun onResume() {
        val intent = Intent(this, UpdatedMusicService::class.java)
        bindService(intent, this, BIND_AUTO_CREATE)
        super.onResume()
    }

    override fun onPause() {
        unbindService(this)
        super.onPause()
    }

    private fun clickListener() {
        binding.btnPlay.setOnClickListener {
            playPauseClick()
        }

        binding.appCompatSeekBar.setOnSeekBarChangeListener(object :
            SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                if (fromUser) service?.seekTo(progress)
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {

            }

            override fun onStopTrackingTouch(seekBar: SeekBar?) {
            }

        })
        binding.ivPrevious.setOnClickListener {
            previousClick()
        }

        binding.ivNext.setOnClickListener {
            nextClick()
        }
        binding.ivSaffle.setOnClickListener {
            service?.setSuffle(!service?.getIsSuffle()!!)
            if (service?.getIsSuffle() == true) {
                binding.ivSaffle.setColorFilter(Color.BLACK)
            } else {
                binding.ivSaffle.setColorFilter(Color.WHITE)
            }
        }
        binding.ivRepeat.setOnClickListener {
            service?.setRepeat(!service?.getIsRepeat()!!)
            if (service?.getIsRepeat()!!) {
                binding.ivRepeat.setColorFilter(Color.BLACK)
            } else {
                binding.ivRepeat.setColorFilter(Color.WHITE)
            }
        }
    }


    override fun nextClick() {
        service?.nextClick()
    }

    override fun previousClick() {
        service?.previousClick()
    }

    override fun playPauseClick() { // todo need to change
        if (service?.getIsPlaying() == true) {
            service?.pause()
            binding.ivPlay.setImageResource(R.drawable.play_circle_outline_24)
            service?.showNotification(R.drawable.play_circle_outline_24)
//            service?.showCustomNotification()
        } else {
            service?.start()
            binding.ivPlay.setImageResource(R.drawable.pause_24)
            service?.showNotification(R.drawable.pause_24)
//            service?.showCustomNotification()
        }
        service?.getIsPlaying()?.let { service?.setIsPLaying(!it) }

    }

    override fun preparePlayerAndView() {
        service?.let {
            val file = it.getFile()
            binding.appCompatSeekBar.max = service?.duration() ?: 0
            binding.appCompatSeekBar.progress = 0
            binding.tvTotalTime.text = getTime(service?.duration()?.div(1000) ?: -1)
            binding.tvSongName.text = file.title
            binding.tvArtistName.text = file.artist
            val imageBytes = getImage(file.path)

            // change tha bg with album color
            changeBgColor(imageBytes)
        }

    }

    override fun setPosition(position: Int) {
        this.position = position
//        changeBgColor(getImage(musicFiles[this.position].path))

    }

    override fun cancelSelf() {
        handler?.removeCallbacks(this)
        finish()
    }


    private fun changeBgColor(imageBytes: ByteArray?) {
        if (imageBytes != null) {
            val bitmap = BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.size)
            animationListener(this@SongActivity, binding.ivCover, bitmap)

            Palette.from(bitmap).generate { palette ->
                val swatch: Palette.Swatch? = palette?.dominantSwatch

                if (swatch != null) {
                    binding.clColver.setBackgroundResource(R.drawable.black_white_gradiant_bg)
                    binding.clSongActivity.setBackgroundColor(
                        ContextCompat.getColor(
                            this@SongActivity,
                            R.color.black
                        )
                    )
                    val intArray = IntArray(2).apply {
                        this[0] = swatch.rgb
                        this[1] = 0x00000000
                    }
                    binding.clColver.background =
                        GradientDrawable(GradientDrawable.Orientation.BOTTOM_TOP, intArray)
                    intArray[1] = swatch.rgb
                    binding.clSongActivity.background =
                        GradientDrawable(GradientDrawable.Orientation.BOTTOM_TOP, intArray)
                    binding.tvSongName.setTextColor(swatch.titleTextColor)
                    binding.tvArtistName.setTextColor(swatch.bodyTextColor)
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.VANILLA_ICE_CREAM) { // Android 15+
                        window.decorView.setOnApplyWindowInsetsListener { view, insets ->
                            val statusBarInsets = insets.getInsets(WindowInsets.Type.statusBars())
                            view.setBackgroundColor(swatch.rgb)

                            // Adjust padding to avoid overlap
                            view.setPadding(0, statusBarInsets.top, 0, 0)
                            insets
                        }
                    } else {
                        // For Android 14 and below
                        window.statusBarColor = Color.BLACK
                    }
                } else {
                    binding.clColver.setBackgroundResource(R.drawable.black_white_gradiant_bg)
                    binding.clSongActivity.setBackgroundColor(
                        ContextCompat.getColor(
                            this@SongActivity,
                            R.color.black
                        )
                    )
                    val intArray = IntArray(2).apply {
                        this[0] = 0xff000000.toInt()
                        this[1] = 0x00000000
                    }
                    binding.clColver.background =
                        GradientDrawable(GradientDrawable.Orientation.BOTTOM_TOP, intArray)
                    intArray[1] = 0xff000000.toInt()
                    binding.clSongActivity.background =
                        GradientDrawable(GradientDrawable.Orientation.BOTTOM_TOP, intArray)
                    binding.tvSongName.setTextColor(Color.WHITE)
                    binding.tvArtistName.setTextColor(Color.WHITE)
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.VANILLA_ICE_CREAM) { // Android 15+
                        window.decorView.setOnApplyWindowInsetsListener { view, insets ->
                            val statusBarInsets = insets.getInsets(WindowInsets.Type.statusBars())
                            view.setBackgroundColor(Color.BLACK)

                            // Adjust padding to avoid overlap
                            view.setPadding(0, statusBarInsets.top, 0, 0)
                            insets
                        }
                    } else {
                        // For Android 14 and below
                        window.statusBarColor = Color.BLACK
                    }
                }
            }
        }

    }


    private fun getTime(duration: Int): String {
        val sec = duration.mod(60).toString()
        val min = duration.div(60).toString()
        return if (sec.length == 1) "$min:0$sec" else "$min:$sec"

    }

    private fun animationListener(context: Context, imageView: ImageView, bitMap: Bitmap) {
        val inAnim = AnimationUtils.loadAnimation(context, android.R.anim.fade_in)
        val outAnim = AnimationUtils.loadAnimation(context, android.R.anim.fade_out)
        outAnim.setAnimationListener(object : Animation.AnimationListener {
            override fun onAnimationStart(animation: Animation?) {

            }

            override fun onAnimationEnd(animation: Animation?) {
                Glide.with(context).asBitmap().load(bitMap).into(imageView)
                inAnim.setAnimationListener(object : Animation.AnimationListener {
                    override fun onAnimationStart(animation: Animation?) {

                    }

                    override fun onAnimationEnd(animation: Animation?) {
                    }

                    override fun onAnimationRepeat(animation: Animation?) {
                    }

                })
                imageView.startAnimation(inAnim)
            }

            override fun onAnimationRepeat(animation: Animation?) {

            }

        })
        imageView.startAnimation(outAnim)

    }


    override fun onStop() {
        super.onStop()
        finish()

    }

    override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
        val mBinder: UpdatedMusicService.MyMusicBinder =
            service as UpdatedMusicService.MyMusicBinder
        this.service = mBinder.getServiceInstance()
//        this.service?.onComplete()
        this.service?.setCallback(this)
        intent.apply {
            position = getIntExtra("position", -1)

            if (musicFiles.isNotEmpty()) {
                binding.ivPlay.setImageResource(R.drawable.pause_24)
//                Glide.with(this@SongActivity).asBitmap().load(getImage(file.path)) .into(binding.ivCover)


//                service?.createMediaPlayer(position)
//                service?.start()

                val intent = Intent(this@SongActivity, UpdatedMusicService::class.java)
                if (this@SongActivity.service?.getIsPlaying() == true) {
                    intent.putExtra(
                        "servicePosition",
                        this@SongActivity.service?.getCurrentSongPosition()
                    )
                } else {
                    intent.putExtra("servicePosition", position)
                }
                startService(intent)
//                this@SongActivity.service?.onComplete()

            }
        }

        binding.appCompatSeekBar.max = this.service?.duration() ?: 0
        handler = Handler()
        handler?.postDelayed(this, 0)
        binding.tvTotalTime.text = getTime((musicFiles[position].duration).toInt().div(1000))

        Toast.makeText(this, "sevice is connected", Toast.LENGTH_SHORT).show()
    }

    override fun onServiceDisconnected(name: ComponentName?) {
        this.service = null
    }

    private var updateRunnable: Runnable? = null

    override fun onDestroy() {
        super.onDestroy()
        handler?.removeCallbacks(this)
    }

    override fun run() {
        binding.appCompatSeekBar.progress =
            this@SongActivity.service?.currentPosition() ?: 0
        Log.d("test", "run: logging . ")
        handler?.post {
            binding.tvPlayedTime.text =
                getTime(this@SongActivity.service?.currentPosition()?.div(1000) ?: 0)
        }
        Log.d("test", "run: logging .. ")
        handler?.postDelayed(this, 1000)
        Log.d("test", "run: logging ... ")
    }


//    override fun onNewIntent(intent: Intent, caller: ComponentCaller) {
//        Log.d("", "onNewIntent: ")
//        position = service?.position!!
//        super.onNewIntent(intent, caller)
//
//    }
}

