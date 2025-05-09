package app.sjk.hello.country.preprocessing

import app.sjk.hello.country.domain.MyLatLng

/**
 * Returns true if the sequence of points p, q, r makes a counter-clockwise turn.
 * The orientation is computed using the 2D cross product.
 */
fun ccw(p: MyLatLng, q: MyLatLng, r: MyLatLng): Boolean {
    val cross = (q.longitude - p.longitude) * (r.latitude - p.latitude) -
            (q.latitude - p.latitude) * (r.longitude - p.longitude)
    return cross > 0
}

/**
 * Computes the convex hull for a given list of LatLng points using Andrew's monotone chain algorithm.
 *
 * Preconditions: There must be at least 3 unique points.
 * The returned list of points is in counter-clockwise order. The last point is not repeated.
 *
 * @param points List of LatLng points.
 * @return List of LatLng points representing the convex hull.
 */
fun computeConvexHull(points: List<MyLatLng>): List<MyLatLng> {
    if (points.size <= 1) return points

    // Sort by longitude (x-axis), and then by latitude (y-axis).
    val sorted = points.sortedWith(compareBy({ it.longitude }, { it.latitude }))

    // Remove duplicates (using a pair of coordinates to check uniqueness).
    val uniquePoints = sorted.distinctBy { it.longitude to it.latitude }
    if (uniquePoints.size < 3) return uniquePoints

    // Build the lower hull.
    val lower = mutableListOf<MyLatLng>()
    for (p in uniquePoints) {
        while (lower.size >= 2 && !ccw(lower[lower.size - 2], lower[lower.size - 1], p)) {
            lower.removeAt(lower.size - 1)
        }
        lower.add(p)
    }

    // Build the upper hull.
    val upper = mutableListOf<MyLatLng>()
    for (p in uniquePoints.reversed()) {
        while (upper.size >= 2 && !ccw(upper[upper.size - 2], upper[upper.size - 1], p)) {
            upper.removeAt(upper.size - 1)
        }
        upper.add(p)
    }

    // Remove the last point of each list (it's the same as the first point of the other list).
    lower.removeAt(lower.size - 1)
    upper.removeAt(upper.size - 1)

    // Concatenate the lower and upper hulls to get the full convex hull.
    val convexHull = mutableListOf<MyLatLng>()
    convexHull.addAll(lower)
    convexHull.addAll(upper)
    return convexHull
}
