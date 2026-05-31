package com.project.vaultly

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.project.vaultly.core.ComposeApp
import com.project.vaultly.feature.auth.data.UserRepository

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // INISIALISASI - Data akan tersimpan permanen
        UserRepository.init(this)

        enableEdgeToEdge()

        setContent {
            ComposeApp()
        }
    }
}