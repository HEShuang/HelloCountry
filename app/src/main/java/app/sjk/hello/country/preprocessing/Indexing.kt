package app.sjk.hello.country.preprocessing

import app.sjk.hello.country.domain.MyLatLng
import app.sjk.hello.country.preprocessing.model.ProcessedCountry
import com.github.davidmoten.rtree.RTree
import com.github.davidmoten.rtree.geometry.Geometries
import com.github.davidmoten.rtree.geometry.Rectangle


fun Map<String, ProcessedCountry>.indexing(): RTree<String, Rectangle>{
    // create an R-tree using Quadratic split with max
    // children per node 4, min children 2 (the threshold
    // at which members are redistributed)
    var tree : RTree<String, Rectangle> = RTree.create();
    forEach { entry ->
        val bbox = entry.value.boundingBox
        tree = tree.add(
            entry.key,
            Geometries.rectangleGeographic(
                bbox.minLongitude,
                bbox.minLatitude,
                bbox.maxLongitude,
                bbox.maxLatitude,
            )
        )
    }
    return tree;
}

fun RTree<String, Rectangle>.search(geoPoint: MyLatLng): List<String> {
    var countryNames = mutableListOf<String>()
    search(Geometries.pointGeographic(geoPoint.longitude, geoPoint.latitude))
        .forEach { entry ->
           countryNames.add(entry.value())
        }
    return countryNames.toList()
}