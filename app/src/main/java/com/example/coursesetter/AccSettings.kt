package com.example.coursesetter

import android.os.Bundle
import android.view.ViewGroup
import android.widget.Switch
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import android.speech.tts.TextToSpeech
import java.util.Locale
import android.util.Log

class AccSettings : AppCompatActivity() {

    private lateinit var switchTextToSpeech: Switch
    private lateinit var textToSpeech: TextToSpeech

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.accsettings)

        // Initialize the Text to Speech engine
        textToSpeech = TextToSpeech(this) { status ->
            if (status == TextToSpeech.SUCCESS) {
                val result = textToSpeech.setLanguage(Locale.US)
                if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED) {
                    Log.e("TTS", "The Language is not supported.")
                }
            } else {
                Log.e("TTS", "Initialization of Text to Speech Failed.")
            }
        }

        // Set up the Text to Speech toggle switch
        switchTextToSpeech = findViewById(R.id.switchTextToSpeech)
        switchTextToSpeech.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                val rootView = findViewById<ViewGroup>(android.R.id.content)
                val allText = getAllTextFromViews(rootView)
                speakOut(allText)
            }
        }
    }

    private fun speakOut(text: String) {
        textToSpeech.speak(text, TextToSpeech.QUEUE_FLUSH, null, "TTS1")
    }

    private fun getAllTextFromViews(view: ViewGroup): String {
        val stringBuilder = StringBuilder()
        for (i in 0 until view.childCount) {
            val child = view.getChildAt(i)
            if (child is ViewGroup) {
                stringBuilder.append(getAllTextFromViews(child)).append(" ")
            } else if (child is TextView) {
                stringBuilder.append(child.text).append(" ")
            }
        }
        return stringBuilder.toString()
    }

    override fun onDestroy() {
        // Stop and release the Text to Speech engine
        if (::textToSpeech.isInitialized) {
            textToSpeech.stop()
            textToSpeech.shutdown()
        }
        super.onDestroy()
    }
}