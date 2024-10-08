package com.example.parkingapp.Activities

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.example.parkingapp.databinding.ActivityMainBinding
import com.example.parkingapp.databinding.ActivitySplashBinding


class SplashActivity : ComponentActivity() {

    private lateinit var binding: ActivitySplashBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent { binding = ActivitySplashBinding.inflate(layoutInflater)
                    setContentView(binding.root)
                }
            }
        }



