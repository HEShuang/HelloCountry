package app.sjk.hello.country.preprocessing.model

import app.sjk.hello.country.domain.MyBoundingBox
import app.sjk.hello.country.domain.MyLatLng

data class ProcessedCountry(
    val iso2: String, //Two letter ISO code
    val name: String, //English name
    val color: String,
    val contours: List<List<MyLatLng>>, //normalized for dateline countries
    val labelLocation: MyLatLng,
    val area: Float,
    val boundingBox: MyBoundingBox,
    //very small countries have the properties below:
    val expandedCircles: List<List<MyLatLng>> ?= null,
    val convexHull: List<MyLatLng> ?= null,
)