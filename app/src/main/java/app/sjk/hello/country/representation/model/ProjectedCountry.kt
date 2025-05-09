package app.sjk.hello.country.representation.model

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path

data class ProjectedCountry(
    val iso2: String,
    val name: String,
    val color: Color,
    val paths: List<Path>,
    val labelPosition: Offset,
    val area: Float,
    val expandedCircles: List<Path> ?= null,
    val convexHull: Path ?= null,
)
