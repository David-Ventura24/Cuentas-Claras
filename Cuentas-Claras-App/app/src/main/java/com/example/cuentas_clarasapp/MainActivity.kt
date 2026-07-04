package com.example.cuentas_clarasapp

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.fragment.app.FragmentActivity
import com.example.cuentas_clarasapp.data.api.ApiClient
import com.example.cuentas_clarasapp.navigation.MainNavigation
import com.example.cuentas_clarasapp.ui.theme.CuentasClarasAppTheme

class MainActivity : FragmentActivity() { // ➔ SE CAMBIA DE ComponentActivity A FragmentActivity
    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen() // SIEMPRE antes de super.onCreate()
        super.onCreate(savedInstanceState)
        ApiClient.inicializar(applicationContext)
        setContent {
            CuentasClarasAppTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    MainNavigation()
                }
            }
        }
    }
}