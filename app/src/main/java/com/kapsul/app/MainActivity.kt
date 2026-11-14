package com.kapsul.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.kapsul.app.ui.KapsulRoot
import com.kapsul.app.ui.theme.KapsulTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            KapsulTheme {
                KapsulRoot()
            }
        }
    }
}
