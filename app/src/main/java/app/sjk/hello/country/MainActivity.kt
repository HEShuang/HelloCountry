package app.sjk.hello.country

import android.os.Bundle
import android.speech.tts.TextToSpeech
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import app.sjk.hello.country.data.GeoJsonRepository
import app.sjk.hello.country.ui.theme.HelloCountryTheme


import app.sjk.hello.country.representation.MapScreen
import app.sjk.hello.country.representation.MapViewModel
import java.util.Locale

class MainActivity : ComponentActivity(), TextToSpeech.OnInitListener {
    private lateinit var viewModel: MapViewModel
    private lateinit var tts: TextToSpeech

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewModel = MapViewModel(GeoJsonRepository(this))
        tts = TextToSpeech(this, this)

        //enableEdgeToEdge()
        setContent {
            HelloCountryTheme {
                MapScreen(
                    viewModel = viewModel,
                    onLanguageSelected = ::changeTTSLanguage,
                    speakCountryName = { name ->
                        Log.d("test", "speakCoutryName: $name")
                        speak(name)
                    }
                )
            }
        }
    }

    override fun onInit(status: Int) {
        if (status == TextToSpeech.SUCCESS) {
            tts.language = Locale.getDefault()
        }
    }

    override fun onDestroy() {
        tts.stop()
        tts.shutdown()
        super.onDestroy()
    }

    fun changeTTSLanguage(locale: Locale, testString: String) {
        val result = tts.setLanguage(locale)
        Log.d("test", "current voice name: ${tts.voice.name}")
        if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED) {
            Log.e("test", "Selected language ${locale.displayName} is not supported!")
        } else {
            speak(testString)
        }
    }

    fun speak(text: String) {
        tts.speak(text, TextToSpeech.QUEUE_FLUSH, null, null)
    }
}