package com.example.mymusicapplication

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.example.mymusicapplication.adapter.viewpager.ViewPagerAdapter
import com.example.mymusicapplication.databinding.ActivityMainBinding
import com.example.mymusicapplication.fragment.AlbumFragment
import com.example.mymusicapplication.fragment.SongsFragment
import com.example.mymusicapplication.model.MusicFile

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        initView()

    }


    private fun initView() {
        val adapter = ViewPagerAdapter(supportFragmentManager)
        adapter.addFragment(SongsFragment(), "Songs")
        adapter.addFragment(AlbumFragment(), "Album")
        binding.viewPager.adapter = adapter
        binding.tabLayout.setupWithViewPager(binding.viewPager)
    }
}