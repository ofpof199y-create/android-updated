package com.youtubescript.app

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.lifecycle.viewmodel.compose.viewModel
import com.youtubescript.app.ui.screen.HomeScreen
import com.youtubescript.app.ui.theme.YouTubeScriptTheme
import com.youtubescript.app.viewmodel.MainViewModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            YouTubeScriptTheme {
                val viewModel: MainViewModel = viewModel()
                val state by viewModel.state.collectAsState()

                HomeScreen(
                    state = state,
                    onUrlChanged = viewModel::onUrlChanged,
                    onFetch = viewModel::fetchTranscript,
                    onClear = viewModel::clearResult,
                    onTabSelected = viewModel::onTabSelected,
                    onLocalVideoSelected = viewModel::fetchLocalVideoTranscript,
                    onShare = { text ->
                        val intent = Intent(Intent.ACTION_SEND).apply {
                            type = "text/plain"
                            putExtra(Intent.EXTRA_TEXT, text)
                        }
                        startActivity(Intent.createChooser(intent, "مشاركة النص"))
                    }
                )
            }
        }
    }
}
