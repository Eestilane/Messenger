package com.example.messenger.auth.ui.adapters

import androidx.fragment.app.Fragment
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.example.messenger.auth.ui.fragments.LoginFragment
import com.example.messenger.auth.ui.fragments.RegisterFragment

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