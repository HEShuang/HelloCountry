package app.sjk.hello.country.representation

import android.graphics.Paint
import android.util.Log
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.withTransform
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import app.sjk.hello.country.representation.model.ProjectedCountry
import app.sjk.hello.country.preprocessing.project
import app.sjk.hello.country.preprocessing.reverseProject
import androidx.compose.animation.animateColor
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.ui.graphics.asAndroidPath
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.sp
import androidx.compose.ui.util.fastForEach
import java.util.Locale
import android.graphics.Paint as AndroidPaint

@Composable
fun getScreenSize(): Size {
    // Get screen dimensions in dp.
    val configuration = LocalConfiguration.current
    val screenWidthDp = configuration.screenWidthDp
    val screenHeightDp = configuration.screenHeightDp

    // Convert dp to pixels.
    val density = LocalDensity.current
    val screenWidthPx = with(density) { screenWidthDp.dp.toPx() }
    val screenHeightPx = with(density) { screenHeightDp.dp.toPx() }

    return Size(screenWidthPx, screenHeightPx)
}

@Composable
fun MapScreen(
    viewModel: MapViewModel,
    onLanguageSelected: (Locale, String) -> Unit,
    speakCountryName: (String) -> Unit,
) {
    val defaultContext = LocalContext.current
    var currentLanguage by remember { mutableStateOf(Locale.getDefault().language) }

    val currentContext = remember (currentLanguage){
        Log.d("test", "change currentLanguage to: $currentLanguage")
        viewModel.getLocalizedContext(defaultContext, currentLanguage)
    }

    val size = getScreenSize()
    var projectedCountryMap by remember(size) { mutableStateOf(viewModel.countryMap.project(size)) }
    var selectedCountry by remember { mutableStateOf<ProjectedCountry?>(null) }

    var scale by remember { mutableFloatStateOf(1f) }
    var offset by remember { mutableStateOf(Offset.Zero)}

    // Create an infinite transition for the glow color animation.
    val infiniteTransition = rememberInfiniteTransition()
    val animatedGlowColor by infiniteTransition.animateColor(
        initialValue = Color.Yellow,
        targetValue = Color.Red,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        )
    )

    Canvas(
        modifier = Modifier
        .fillMaxSize()
        .background(color = Color(0x220077BE))
        .pointerInput("zoomPan") {
            detectTransformGestures { centroid, pan, gestureZoom, _ ->
                scale *= gestureZoom
                offset = (offset - centroid) * gestureZoom + centroid + pan
            }
        }
        .pointerInput(currentContext){
            detectTapGestures { tapPoint ->
                Log.d("test", "taped")
                // Convert tap from screen to canvas coordinates and then project back to latlng position
                val geoPoint = reverseProject( (tapPoint - offset) / scale, size)
                val countryCode = viewModel.selectCountry(geoPoint)
                if(countryCode.isNotEmpty()){
                    selectedCountry = projectedCountryMap[countryCode]

                    //val codeLang = currentLocale.language
                    //Log.d("test", "currentLocale.language: $codeLang")
                    // val nameToSpeak = projectedCountryLabelMap[name]?.namesByLang[codeLang]
                    if(selectedCountry != null) {
                        Log.d("test", "country area: ${selectedCountry!!.area?:0f}" )
                        val contextLanguage = currentContext.resources.configuration.locales.get(0).language
                        Log.d("test", "context Language: $contextLanguage")
                        Log.d("test", "country code: ${selectedCountry!!.iso2}")
                        val id = currentContext.resources.getIdentifier(
                            "country_${selectedCountry!!.iso2.lowercase()}",
                            "string",
                            currentContext.packageName
                        )
                        if(id != 0 ){
                            val nameToSpeak = currentContext.getString(id)
                            Log.d("test", "nameToSpeak: $nameToSpeak")
                            speakCountryName(nameToSpeak)
                        }
                        else {
                            Log.d("test", "invalid resource id")
                        }

                    }
                }
            }
        }
    ){
        withTransform({
            translate(offset.x, offset.y)
            scale(scale, scale, Offset.Zero)
        }) {


            projectedCountryMap.forEach { entry ->
                val fillColor = entry.value.color
                val originalContour = entry.value.expandedCircles == null
                entry.value.paths.forEach { path ->
                    drawPath(
                        path = path,
                        color = fillColor,
                    )
                    //if(originalContour) {
                        drawPath(
                            path = path,
                            color = Color.LightGray,
                            style = Stroke(width = 0.25f),
                        )
                    //}

                }
                /*entry.value.extendedCircles?.forEach { path ->
                    drawPath(
                        path = path,
                        color = fillColor.copy(alpha = 0.2f),
                        //style = Stroke(width = 5f ),
                    )
                }*/
                /*entry.value.convexHull?.let { path ->
                    drawPath(
                        path = path,
                        color = fillColor,
                        style = Stroke(
                            width = 4f,
                            cap = StrokeCap.Round,
                            // Dash pattern: segments of 10 pixels drawn, 10 pixels gap
                            pathEffect = PathEffect.dashPathEffect(floatArrayOf(10f, 10f), 0f)
                        )
                    )
                }*/
                /*if (entry.value.area < 15000) {
                    drawIntoCanvas { canvas ->
                        val glowPaint = AndroidPaint().apply {
                            color = fillColor.copy(alpha = 0.5f).toArgb()
                            style = AndroidPaint.Style.FILL
                            // Adjust the radius to control the blur extent:
                            maskFilter = BlurMaskFilter(15f, BlurMaskFilter.Blur.NORMAL)
                            isAntiAlias = true
                        }
                        entry.value.paths.forEach { path ->
                            canvas.nativeCanvas.drawPath(path.asAndroidPath(), glowPaint)
                        }
                    }
                }*/
            }

            selectedCountry?.let { country ->
                // Create a blurred (glow) paint.
                val glowPaint = AndroidPaint().apply {
                    color = animatedGlowColor.copy(alpha = 0.5f).toArgb()
                    style = AndroidPaint.Style.FILL
                    //maskFilter = BlurMaskFilter(15f, BlurMaskFilter.Blur.NORMAL)
                    isAntiAlias = true
                }
                // Draw each path with the glow effect.
                country.paths.fastForEach { path ->
                    drawIntoCanvas { canvas ->
                        // Convert Compose Path to Android Path.
                        val androidPath = path.asAndroidPath()
                        canvas.nativeCanvas.drawPath(androidPath, glowPaint)
                    }
                }

                val id = currentContext.resources.getIdentifier(
                    "country_${country.iso2.lowercase()}",
                    "string",
                    currentContext.packageName,
                )
                if(id != 0 ) {
                    val name = currentContext.getString(id)
                    drawContext.canvas.nativeCanvas.drawText(
                        name,
                        country.labelPosition.x,
                        country.labelPosition.y,
                        Paint().apply {
                            color = android.graphics.Color.BLACK
                            textSize = (10.sp.toPx() * scale).coerceAtMost(15.sp.toPx())
                            isAntiAlias = true
                            textAlign = Paint.Align.CENTER
                        }
                    )
                }

            }
        }
    }
    LanguageMenu(
        onLanguageSelected = { locale, testString ->
            currentLanguage = locale.language
            onLanguageSelected(locale, testString)
        }
    )
}
