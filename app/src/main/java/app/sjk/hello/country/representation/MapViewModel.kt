package app.sjk.hello.country.representation

import android.content.Context
import android.content.res.Configuration
import android.util.Log
import androidx.lifecycle.ViewModel
import app.sjk.hello.country.domain.CountryRepository
import app.sjk.hello.country.domain.MyLatLng
import app.sjk.hello.country.preprocessing.indexing
import app.sjk.hello.country.preprocessing.preprocessing
import app.sjk.hello.country.preprocessing.search
import java.util.Locale

class MapViewModel (
    private val countryRepo : CountryRepository,
) : ViewModel(){

    val countryMap = countryRepo.getCountryMap().preprocessing()
    val searchTree = countryMap.indexing() //create indexing tree for search

    fun getLocalizedContext(context: Context, languageCode: String): Context {
        val locale = Locale(languageCode)
        val config = Configuration(context.resources.configuration)
        config.setLocale(locale)
        config.setLayoutDirection(locale)
        return context.createConfigurationContext(config)
    }

    fun selectCountry(geoPoint: MyLatLng) : String {
        Log.d("test", "selectCountry")
        val candidates = searchTree.search(geoPoint) // the given point is in which countries' bounding box
        for(candidate in candidates){
            val country = countryMap[candidate]
            if(country == null){
                Log.d("test", "country not found in country map")
                continue;
            }
            Log.d("test", "$candidate: test point in polygon")
            /*if (!country.circles.isNullOrEmpty()) {
                if (isPointInPolygon(geoPoint, country.touchCircle)) {
                    Log.d("test", "selected country: $candidate")
                    return candidate
                }
            }*/

                for (contour in country.contours) {
                    if (isPointInPolygon(geoPoint, contour)) {
                        Log.d("test", "selected country: $candidate")
                        return candidate
                    }
                }

            Log.d("test", "$candidate: point is not in the country")
        }
        Log.d("test", "no country selected")
        return ""
   }

    private fun isPointInPolygon(point: MyLatLng, polygon: List<MyLatLng>): Boolean {
        if (polygon.size < 3) return false

        val lat = point.latitude
        val lng = point.longitude
        var inside = false

        var j = polygon.lastIndex
        for (i in polygon.indices) {
            val pi = polygon[i]
            val pj = polygon[j]

            val xi = pi.longitude
            val yi = pi.latitude
            val xj = pj.longitude
            val yj = pj.latitude

            val intersect = ((yi > lat) != (yj > lat)) &&
                    (lng < (xj - xi) * (lat - yi) / (yj - yi + 0.00000001) + xi)

            if (intersect) inside = !inside
            j = i
        }

        return inside
    }
}