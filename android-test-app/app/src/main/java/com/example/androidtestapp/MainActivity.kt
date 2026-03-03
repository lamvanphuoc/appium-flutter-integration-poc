package com.example.androidtestapp

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import io.flutter.embedding.android.FlutterActivity

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme {
                Surface(modifier = Modifier.fillMaxSize()) {
                    SingleButtonScreen(
                        onTap = {
                            startActivity(FlutterActivity.createDefaultIntent(this))
                        }
                    )
                }
            }
        }
    }
}

@Composable
private fun SingleButtonScreen(onTap: () -> Unit) {
    androidx.compose.foundation.layout.Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Button(onClick = onTap) {
            Text(text = "Tap me")
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun SingleButtonScreenPreview() {
    MaterialTheme {
        SingleButtonScreen(onTap = {})
    }
}
