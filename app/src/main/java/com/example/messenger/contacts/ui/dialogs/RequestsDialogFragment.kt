package com.example.messenger.contacts.ui.dialogs

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isInvisible
import androidx.core.view.isVisible
import androidx.fragment.app.DialogFragment
import com.example.messenger.contacts.ui.adapters.RequestsPagerAdapter
import com.example.messenger.databinding.FragmentContactsRequestsBinding
import com.google.android.material.tabs.TabLayoutMediator

class RequestsDialogFragment : DialogFragment() {
    private var _binding: FragmentContactsRequestsBinding? = null
    private val binding get() = _binding!!
    lateinit var tabsMediator: TabLayoutMediator

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentContactsRequestsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        dialog?.window?.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT)
        binding.viewPager.adapter = RequestsPagerAdapter(this)
        tabsMediator = TabLayoutMediator(binding.tabLayout, binding.viewPager) { tab, position ->
            when (position) {
                0 -> tab.text = "Входящие"
                1 -> tab.text = "Исходящие"
            }
        }

        tabsMediator.attach()

        binding.toBack.setOnClickListener{
            dismiss()
        }

        childFragmentManager.setFragmentResultListener("request", viewLifecycleOwner) { key, bundle ->
            val progressBar = bundle.getBoolean("progressBar")
            binding.progressBar.isVisible = progressBar
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        tabsMediator.detach()
    }
}
