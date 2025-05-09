package app.sjk.hello.country.data

import android.content.Context
import app.sjk.hello.country.domain.Country
import app.sjk.hello.country.domain.CountryRepository
import app.sjk.hello.country.domain.MyCircle
import app.sjk.hello.country.domain.MyLatLng
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.double
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonPrimitive


class GeoJsonRepository (
    private val context: Context,
) : CountryRepository {

    override fun getCountryMap(): Map<String, Country> {

        val jsonStr = context.assets.open("countries.geojson")
            .bufferedReader().use { it.readText() }
        val jsonFeatures = Json{ ignoreUnknownKeys = true }
            .decodeFromString<JsonFeatures>(jsonStr)

        val results = mutableMapOf<String, Country>()
        jsonFeatures.features.forEach { feature ->
            val contours = getContours(feature.geometry)
            val iso2 = feature.properties.ISO_A2
            val lng = feature.polylabel[0]
            val lat = feature.polylabel[1]
            val circles = feature.circles?.let { circles ->
                circles.map { circle ->
                    MyCircle(
                        center = MyLatLng(
                            longitude = circle[0],
                            latitude = circle[1],
                        ),
                        radiusDegree = circle[2],
                    )
                }
            }

            if(contours.isNotEmpty()){
                results[iso2] = Country(
                    iso2 = iso2,
                    name = feature.properties.ADMIN,
                    color = feature.properties.fillColor,
                    contours = contours,
                    labelLocation = MyLatLng(lat, lng),
                    area = feature.properties.area,
                    circles = circles,
                )
            }
        }
        return results.toMap()
    }

    private fun getContours(geometry: JsonGeometry?): List<List<MyLatLng>> {
        if(geometry == null) return emptyList()
        val contours = mutableListOf<List<MyLatLng>>()
        when (geometry.type) {
            "Polygon" -> {
                val rings = geometry.coordinates.jsonArray
                if (rings.isNotEmpty()) {
                    val outerRing = rings[0].jsonArray.map { pos ->
                        val coords = pos.jsonArray
                        val lng = coords[0].jsonPrimitive.double
                        val lat = coords[1].jsonPrimitive.double
                        MyLatLng(lat, lng)
                    }
                    contours.add(outerRing)
                }
            }
            "MultiPolygon" -> {
                val multiPolygons = geometry.coordinates.jsonArray
                for (poly in multiPolygons) {
                    val rings = poly.jsonArray
                    if (rings.isNotEmpty()) {
                        val outerRing = rings[0].jsonArray.map { pos ->
                            val coords = pos.jsonArray
                            val lon = coords[0].jsonPrimitive.double
                            val lat = coords[1].jsonPrimitive.double
                            MyLatLng(lat, lon)
                        }
                        contours.add(outerRing)
                    }
                }
            }
        }
        return contours
    }

}
