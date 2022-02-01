package com.pocholomia.timer

import android.app.Application
import android.content.Context
import android.content.SharedPreferences
import androidx.core.content.edit
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.asFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import java.util.concurrent.TimeUnit
import kotlin.time.ExperimentalTime
import kotlin.time.toDuration

/**
 * uses AndroidViewModel to hold application instance
 */
@OptIn(ExperimentalTime::class)
class TimerViewModel(application: Application) : AndroidViewModel(application) {

    companion object {
        private const val ONE_SEC = 1000L
        private const val LAST_VALUE = "last_value"
        private const val SHARED_PREF = "timer"
    }

    /**
     * uses shared prefs to store last timer value so it can resume if user exits the app
     */
    private val prefs = application.getSharedPreferences(SHARED_PREF, Context.MODE_PRIVATE)

    private var timerJob: Job? = null

    private var currentTime: Long? = null

    /**
     * String display
     */
    private val _timeDisplay = MutableLiveData(prefs.getLastValue().getTimeDisplay())
    val timeDisplay: LiveData<String> = _timeDisplay

    /**
     * flag if timer is running
     */
    private val _isRunning = MutableLiveData(false)
    val isRunning: LiveData<Boolean> = _isRunning

    /**
     * @param fromViewResume true if trigger is from view onResume
     */
    fun start(fromViewResume: Boolean = false) {
        val initialValue = currentTime ?: prefs.getLastValue()

        if (initialValue == 0L && fromViewResume) return
        timerJob?.cancel()
        timerJob = viewModelScope.launch {
            _isRunning.value = true
            timer(initialValue)
                .collect {
                    currentTime = it
                    _timeDisplay.value = it.getTimeDisplay()
                }
        }
    }

    /**
     * @param fromViewOnly true if trigger is from view onPause
     */
    fun stop(fromViewOnly: Boolean = false) {
        timerJob?.cancel()
        if (!fromViewOnly) {
            currentTime = null
            _isRunning.value = false
            _timeDisplay.value = prefs.getLastValue().getTimeDisplay()
            saveLastValue(0L)
        }
    }

    /**
     * simple count timer per second
     */
    private fun timer(initialValue: Long = 0L) = (initialValue..Long.MAX_VALUE)
        .asSequence()
        .asFlow()
        .onEach { delay(ONE_SEC) }

    /**
     * convert Long to String display
     */
    private fun Long.getTimeDisplay() = toDuration(TimeUnit.SECONDS)
        .toComponents { hours, minutes, seconds, _ ->
            String.format("%02d:%02d:%02d", hours, minutes, seconds)
        }

    /**
     * get last value from disk
     */
    private fun SharedPreferences.getLastValue() = getLong(LAST_VALUE, 0L)

    /**
     * save last value to disk
     */
    private fun saveLastValue(value: Long) {
        prefs.edit(true) { putLong(LAST_VALUE, value) }
    }

    /**
     * save last value if user exits app
     */
    override fun onCleared() {
        saveLastValue(currentTime ?: 0L)
        super.onCleared()
    }

}