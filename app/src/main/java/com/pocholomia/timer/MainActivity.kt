package com.pocholomia.timer

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Button
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.viewmodel.compose.viewModel
import com.pocholomia.timer.ui.theme.TimerTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            TimerTheme {
                // A surface container using the 'background' color from the theme
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colors.background
                ) {
                    Home()
                }
            }
        }
    }
}

@Composable
fun Home(viewModel: TimerViewModel = viewModel()) {
    // observe view lifecycle to trigger start() and stop() of timer
    val lifecycle = LocalLifecycleOwner.current.lifecycle
    DisposableEffect(lifecycle) {
        val lifecycleObserver = createLifecycleObserver(viewModel)
        lifecycle.addObserver(lifecycleObserver)
        onDispose {
            lifecycle.removeObserver(lifecycleObserver)
        }
    }

    val time = viewModel.timeDisplay.observeAsState()
    val isRunning = viewModel.isRunning.observeAsState()
    Timer(time.value, isRunning.value ?: false, viewModel::start, viewModel::stop)
}

@Composable
private fun Timer(
    time: String?,
    isRunning: Boolean,
    onStart: () -> Unit,
    onStop: () -> Unit
) {
    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(text = "$time", style = MaterialTheme.typography.h4)
        Button(
            modifier = Modifier.padding(vertical = 24.dp),
            onClick = { if (isRunning) onStop() else onStart() }
        ) {
            Text(text = if (isRunning) "Stop" else "Start")
        }
    }
}

/**
 * Handles lifecycle of provided mapView
 */
private fun createLifecycleObserver(
    viewModel: TimerViewModel
): LifecycleEventObserver = LifecycleEventObserver { _, event ->
    when (event) {
        Lifecycle.Event.ON_RESUME -> viewModel.start(fromViewResume = true)
        Lifecycle.Event.ON_PAUSE -> viewModel.stop(fromViewOnly = true)
        else -> {

        }
    }
}

@Preview(showBackground = true, widthDp = 320)
@Composable
fun DefaultPreview() {
    TimerTheme {
        Timer(time = "00:00:00", isRunning = false, onStart = {}, onStop = {})
    }
}