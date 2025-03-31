package com.example.messenger.contacts.ui.adapters

import androidx.fragment.app.Fragment
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.example.messenger.contacts.ui.fragments.IncomingRequestsFragment
import com.example.messenger.contacts.ui.fragments.OutgoingRequestsFragment

class RequestsPagerAdapter(fragment: Fragment) : FragmentStateAdapter(fragment) {
    override fun getItemCount(): Int = 2
    override fun createFragment(position: Int): Fragment {
        return when (position) {
            0-> IncomingRequestsFragment()
            1-> OutgoingRequestsFragment()
            else-> throw IllegalArgumentException("Invalid position")
        }
    }
}