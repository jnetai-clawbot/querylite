package com.jnetaol.querylite

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import com.jnetaol.querylite.logger.DebugLogger
import com.jnetaol.querylite.ui.MainApp
import com.jnetaol.querylite.ui.theme.QueryLiteTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        DebugLogger.i("QL-002", "MainActivity created")

        val externalDbPath = intent.data?.path

        setContent {
            QueryLiteTheme {
                Surface(modifier = Modifier.fillMaxSize()) {
                    MainApp(initialDbPath = externalDbPath)
                }
            }
        }
    }
}
