package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.data.KouhakuDatabase
import com.example.data.KouhakuRepository
import com.example.ui.HandheldShellScreen
import com.example.ui.theme.MyApplicationTheme
import com.example.viewmodel.GameEngineViewModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val database = KouhakuDatabase.getInstance(applicationContext)
        val repository = KouhakuRepository(database.kouhakuDao())

        setContent {
            MyApplicationTheme {
                val gameViewModel: GameEngineViewModel = viewModel(
                    factory = GameEngineViewModel.provideFactory(repository)
                )
                HandheldShellScreen(viewModel = gameViewModel)
            }
        }
    }
}
