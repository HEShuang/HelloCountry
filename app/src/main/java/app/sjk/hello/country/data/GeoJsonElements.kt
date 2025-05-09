package app.sjk.hello.country.data

import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonElement

@Serializable
data class JsonGeometry(
    val type: String,
    val coordinates: JsonElement,
)

@Serializable
data class JsonProperties(
    val ADMIN: String,
    val ISO_A3: String,
    val ISO_A2: String,
    val fillColor: String,
    val area: Float,
)

@Serializable
data class JsonFeature(
    val type: String,
    val geometry: JsonGeometry,
    val properties: JsonProperties,
    val polylabel: List<Double>,
    val circles: List<List<Double>> ?= null, // precomputed enlarged circles for small countries
)

@Serializable
data class JsonFeatures(
    val type: String,
    val features: List<JsonFeature>,
)