package app.sjk.hello.country.preprocessing

import android.util.Log
import app.sjk.hello.country.domain.Country
import app.sjk.hello.country.domain.MyBoundingBox
import app.sjk.hello.country.domain.MyLatLng
import app.sjk.hello.country.preprocessing.model.ProcessedCountry
import kotlin.collections.map
import kotlin.math.PI
import kotlin.math.asin
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.sin

//normalize coordinates for dateline countries
//compute bounding box
//create expanded circles and convex hull for small countries
fun Map<String, Country>.preprocessing() : Map<String, ProcessedCountry> {

    val smallCountryThreshold = 10000f
    val expandCoefficient = 50

    val results = mutableMapOf<String, ProcessedCountry>()
    forEach { entry ->
        val country = entry.value
        val normalizedContours = if( country.isDatelineCountry() )
            country.normalizeDatelinePoints()
        else
            country.contours

        var expandedCircles : List<List<MyLatLng>> ?= null
        var convexHull : List<MyLatLng> ?= null
        if( country.circles != null && country.area < smallCountryThreshold) {
            expandedCircles = country.circles.map { circle ->
                createCircle(circle.center, circle.radiusDegree * expandCoefficient)
            }
            convexHull = computeConvexHull(normalizedContours.flatten())
        }

        results.put(
            entry.key,
            ProcessedCountry(
                iso2 = country.iso2,
                name = country.name,
                color = country.color,
                contours = normalizedContours,
                labelLocation = country.labelLocation,
                area = country.area,
                boundingBox = normalizedContours.flatten().computeBoundingBox(),
                expandedCircles = expandedCircles,
                convexHull = convexHull,
            )
        )
    }
    return results
}

private fun Country.isDatelineCountry() : Boolean {
    //val datelineCountries = listOf("RU", "KI", "FM")
    //TODO detect dateline countries or define a complete list of dateline countries
    return false
}

private fun Country.normalizeDatelinePoints(): List<List<MyLatLng>> {
    return contours.map { contour ->
        contour.map { point ->
            point.copy(longitude = if (point.longitude < 0) (point.longitude + 360) else point.longitude)
        }
    }
}

private fun List<MyLatLng>.computeBoundingBox(print: Boolean = false): MyBoundingBox {

    var minLat = Double.MAX_VALUE
    var maxLat = -Double.MAX_VALUE
    var minLng = Double.MAX_VALUE
    var maxLng = -Double.MAX_VALUE

    for (point in this) {
        if (point.latitude < minLat) minLat = point.latitude
        if (point.latitude > maxLat) maxLat = point.latitude
        if (point.longitude < minLng) minLng = point.longitude
        if (point.longitude > maxLng) maxLng = point.longitude
    }
    if(print) {
        Log.d("test", "$minLng, $minLat, $maxLng, $maxLat")
    }
    return MyBoundingBox(
        minLatitude = minLat,
        minLongitude = minLng,
        maxLatitude = maxLat,
        maxLongitude = maxLng,
    )
}

/**
 * Creates a circular polyline (list of LatLng points) centered at [center] with radius [radiusDeg] (in degrees),
 * using [steps] number of points to approximate the circle.
 *
 * @param center The center of the circle as a LatLng.
 * @param radiusDeg The radius of the circle in degrees.
 * @param steps The number of points along the circle's perimeter.
 * @return A list of LatLng points representing the circle.
 */
private fun createCircle(center: MyLatLng, radiusDeg: Double, steps: Int = 64): List<MyLatLng> {
    // Convert the center to radians.
    val centerLatRad = Math.toRadians(center.latitude)
    val centerLngRad = Math.toRadians(center.longitude)
    // Convert the input radius (in degrees) to an angular distance in radians.
    val angularDistance = Math.toRadians(radiusDeg)

    val circlePoints = mutableListOf<MyLatLng>()

    // Generate equally spaced points around the circle.
    for (i in 0 until steps) {
        val bearing = 2 * PI * i / steps  // Bearing in radians
        val latRad = asin(
            sin(centerLatRad) * cos(angularDistance) +
                    cos(centerLatRad) * sin(angularDistance) * cos(bearing)
        )
        val lngRad = centerLngRad + atan2(
            sin(bearing) * sin(angularDistance) * cos(centerLatRad),
            cos(angularDistance) - sin(centerLatRad) * sin(latRad)
        )
        circlePoints.add(MyLatLng(Math.toDegrees(latRad), Math.toDegrees(lngRad)))
    }

    return circlePoints
}