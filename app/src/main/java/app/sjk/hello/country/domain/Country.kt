package app.sjk.hello.country.domain


data class MyLatLng(
    val latitude: Double,
    val longitude: Double,
)

data class MyBoundingBox(
    val minLatitude: Double,
    val minLongitude: Double,
    val maxLatitude: Double,
    val maxLongitude: Double,
)

data class MyCircle(
    val center: MyLatLng,
    val radiusDegree: Double,
)

data class Country(
    val iso2: String, //Two letter ISO code
    val name: String, //English name
    val color: String,
    val contours: List<List<MyLatLng>>,
    val labelLocation: MyLatLng,
    val area: Float,
    val circles: List<MyCircle>? = null, //enlarged circles for small countries
)

