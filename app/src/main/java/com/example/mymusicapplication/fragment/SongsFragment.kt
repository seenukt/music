package com.example.mymusicapplication.fragment

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.mymusicapplication.SongActivity
import com.example.mymusicapplication.adapter.recyclerview.MusicAdapter
import com.example.mymusicapplication.constant.GetAudio.getAllAudio
import com.example.mymusicapplication.databinding.FragmentSongsBinding


class SongsFragment : Fragment() {

    private lateinit var binding: FragmentSongsBinding
    private lateinit var adapter : MusicAdapter
    private val permission =
        registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { isgranted ->
//            if (isgranted) {
                Toast.makeText(requireContext(), "permission Granted", Toast.LENGTH_SHORT).show()
                adapter.submitList(getAllAudio(requireContext()))

//            } else {
                Toast.makeText(requireContext(), "permission Not Granted", Toast.LENGTH_SHORT).show()
//            }
        }



    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentSongsBinding.inflate(layoutInflater)
        adapter = MusicAdapter { file, position ->
            val intent = Intent(requireContext(),SongActivity::class.java)
            intent.putExtra("position", position)
            intent.putExtra("file" ,file)
            startActivity(intent)
        }
        binding.rvSongs.adapter = adapter
        binding.rvSongs.layoutManager = LinearLayoutManager(requireContext(), RecyclerView.VERTICAL,false)
        permissionCheck()

        return binding.root
    }

    private fun permissionCheck() {
        if (ContextCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.READ_MEDIA_AUDIO
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                permission.launch(arrayOf(Manifest.permission.READ_MEDIA_AUDIO, Manifest.permission.POST_NOTIFICATIONS))
            } else {
                permission.launch(arrayOf( Manifest.permission.READ_EXTERNAL_STORAGE))
            }
        } else {
           adapter.submitList(getAllAudio(requireContext()))
        }
    }
}
