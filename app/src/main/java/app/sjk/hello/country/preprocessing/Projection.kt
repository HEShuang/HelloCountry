package app.sjk.hello.country.preprocessing

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import app.sjk.hello.country.domain.MyLatLng
import app.sjk.hello.country.preprocessing.model.ProcessedCountry
import app.sjk.hello.country.representation.model.ProjectedCountry


fun Map<String, ProcessedCountry>.project(size: Size) : Map<String, ProjectedCountry> {

    var projectedCountries = mutableMapOf<String, ProjectedCountry>()
    val tEnlarge = 3

    forEach { entry ->
        val country = entry.value
        val paths = country.contours.map { contour ->
            contour.projectToPath(size)
        }
        val expandedCircles = country.expandedCircles?.map { circle ->
            circle.projectToPath(size)
        }
        val convexHull = country.convexHull?.projectToPath(size)

        if(paths.isNotEmpty()){
            projectedCountries.put(
                entry.key,
                ProjectedCountry(
                    iso2 = country.iso2,
                    name = country.name,
                    color = Color(country.color.toLong(16)),
                    paths = paths,
                    labelPosition = project(country.labelLocation, size),
                    area = country.area,
                    expandedCircles  = expandedCircles,
                    convexHull = convexHull,
                )
            )
        }
    }
    return projectedCountries.toMap()
}

private fun List<MyLatLng>.projectToPath(size: Size) : Path {
    return Path().apply {
        // Project the first point
        val start = project(first(), size)
        moveTo(start.x, start.y)
        // Project remaining points
        drop(1).forEach { point ->
            val pt = project(point, size)
            lineTo(pt.x, pt.y)
        }
        close()
    }
}

// --- Simple projection function --- //
// Assumes an equirectangular projection mapping:
//   - Longitude from -180 to 180 → x from 0 to canvas width
//   - Latitude from -90 to 90 → y from canvas height to 0
private fun project(latlng: MyLatLng, size: Size): Offset {
    val x = ((latlng.longitude + 180) / 360) * size.width
    val y = ((90 - latlng.latitude) / 180) * size.height
    return Offset(x.toFloat(), y.toFloat())
}

fun reverseProject(screenPos: Offset, size: Size): MyLatLng {
    val longitude = (screenPos.x / size.width) * 360 - 180
    val latitude = 90 - (screenPos.y / size.height) * 180
    return MyLatLng(latitude.toDouble(), longitude.toDouble())
}