package com.example.messenger.root.ui

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.view.isVisible
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.setupWithNavController
import com.example.messenger.R
import com.example.messenger.databinding.ActivityRootBinding
import com.example.messenger.libs.ThemeManager
import com.example.messenger.libs.TokenManager

class RootActivity : AppCompatActivity() {
    private lateinit var binding: ActivityRootBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRootBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val navHostFragment = supportFragmentManager.findFragmentById(R.id.fragment_container_view) as NavHostFragment
        val navController = navHostFragment.navController
        binding.bottomNavigationView.setupWithNavController(navController)

        if (savedInstanceState == null && TokenManager.getToken(this) != null){
            navController.navigate(R.id.action_authFragment_to_chatsFragment)
        }

        if (savedInstanceState == null){
            if (ThemeManager.getTheme(this)){
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
            }
            else {
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
            }
        }

        navController.addOnDestinationChangedListener { _, destination, _ ->
            if (destination.id == R.id.chatFragment) {
                binding.bottomNavigationView.isVisible = false
            } else if (destination.id == R.id.authFragment) {
                binding.bottomNavigationView.isVisible = false
            } else {
                binding.bottomNavigationView.isVisible = true
            }
        }
    }
}
