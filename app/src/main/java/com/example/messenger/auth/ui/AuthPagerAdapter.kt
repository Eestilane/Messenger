package com.example.messenger.auth.ui

import androidx.fragment.app.Fragment
import androidx.viewpager2.adapter.FragmentStateAdapter

class AuthPagerAdapter(fragment: Fragment) : FragmentStateAdapter(fragment) {
    override fun getItemCount(): Int = 2
    override fun createFragment(position: Int): Fragment {
        return when (position) {
            0-> LoginFragment()
            1-> RegisterFragment()
            else-> throw IllegalArgumentException("Invalid position")
        }
    }
}