package com.example.mymusicapplication.adapter.viewpager

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentPagerAdapter
import androidx.fragment.app.FragmentPagerAdapter.BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT
import androidx.viewpager.widget.PagerAdapter
import androidx.viewpager2.adapter.FragmentStateAdapter

 class ViewPagerAdapter(manager: FragmentManager) : FragmentPagerAdapter(manager) {
     private val fragment = arrayListOf<Fragment>()
     private val  title = arrayListOf<String>()

     fun addFragment(fragment: Fragment , title:String){
         this.fragment.add(fragment)
         this.title.add(title)
     }

     override fun getCount(): Int {
       return  fragment.size
     }

     override fun getItem(position: Int): Fragment {
         return fragment[position]
     }

     override fun getPageTitle(position: Int): CharSequence {
         return title[position]
     }
 }