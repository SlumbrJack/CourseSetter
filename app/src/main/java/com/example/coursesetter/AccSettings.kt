package com.example.coursesetter

import android.os.Bundle
import android.widget.SeekBar
import android.widget.Switch
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import android.speech.tts.TextToSpeech
import java.util.Locale
import android.util.Log


class AccSettings : AppCompatActivity() {

    private lateinit var switchTextToSpeech: Switch
    private lateinit var switchColorInversion: Switch
    private lateinit var seekBarTextSize: SeekBar
    private lateinit var textToSpeech: TextToSpeech

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.accsettings)



        textToSpeech = TextToSpeech(this) { status ->
            if (status == TextToSpeech.SUCCESS) {
                val result = textToSpeech.setLanguage(Locale.US)
                if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED) {
                    Log.e("TTS", "This Language is not supported")
                }
            } else {
                Log.e("TTS", "Initialization Failed!")
            }
        }





    switchTextToSpeech = findViewById(R.id.switchTextToSpeech)
    switchTextToSpeech.setOnCheckedChangeListener { _, isChecked ->
        if (isChecked) {
            speakOut("Text to Speech is On.")
        }
    }
}

    private fun speakOut(text: String) {
        textToSpeech.speak(text, TextToSpeech.QUEUE_FLUSH, null, "")
    }

    override fun onDestroy() {
        if (::textToSpeech.isInitialized) {
            textToSpeech.stop()
            textToSpeech.shutdown()
        }
        super.onDestroy()
    }
}






