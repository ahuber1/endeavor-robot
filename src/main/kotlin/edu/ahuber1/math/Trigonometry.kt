package edu.ahuber1.math

import kotlin.math.abs
import kotlin.math.acos
import kotlin.math.cos
import kotlin.math.sin

/**
 * Calculates a position [distance] units away from [origin] at an angle of [headingRadians].
 * @param origin The starting point.
 * @param distance The distance between [origin] and the [Vec] this method returns.
 * @param headingRadians The angle between [origin] and the [Vec] this method returns in radians.
 */
public fun getCoordinateAlongHeadingRadians(origin: Vec, distance: Double, headingRadians: Double): Vec {
    val dx = distance * cos(headingRadians)
    val dy = distance * sin(headingRadians)
    return origin.translate(dx, dy)
}

/**
 * Calculates the angle between two points along a circle relative to that circle's center.
 * @param center The circle's center point.
 * @param point1 The first point along the circle.
 * @param point2 The second point along the circle.
 */
public fun calculateArcAngleRadians(center: Vec, point1: Vec, point2: Vec): Double {
    // https://math.stackexchange.com/questions/830413/calculating-the-arc-length-of-a-circle-segment
    val distance = Vec.distance(point1, point2)
    val radius = Vec.distance(center, point1)
    return acos(1.0 - (distance.squared / (2 * radius.squared)))
}

/**
 * Calculates the arc length of a circle segment.
 * @param angleRadians The arc angle in radians.
 * @param radius The radius of the circle.
 */
public fun calculateArcLengthRadians(angleRadians: Double, radius: Double): Double {
    return abs(angleRadians * radius)
}

/**
 * Normalizes an angle in radians to be in the range `[0, 2π]`.
 */
public fun normalizeRadians(radians: Double): Double {
    val radians360Degrees = 2 * Math.PI
    val normalized = radians % radians360Degrees
    return if (normalized < 0) normalized + radians360Degrees else normalized
}

/**
 * Converts an angle in degrees to radians.
 * @see toDegrees
 */
public fun toRadians(degrees: Double): Double {
    return degrees * (Math.PI / 180.0)
}

/**
 * Converts an angle in radians to degrees.
 * @see toRadians
 */
public fun toDegrees(radians: Double): Double {
    return (180.0 * radians) / Math.PI
}

/**
 * Returns the angle of rotation that has the shortest arc length from an origin point at a particular heading to
 * another point.
 * @param origin The origin point.
 * @param headingRadians The heading in radians.
 * @param destination The end point.
 */
public fun shortestRotationAngleRadians(origin: Vec, headingRadians: Double, destination: Vec): Double {
    data class TestArc(val angleRadians: Double, val arcLength: Double, val endPoint: Vec)

    val testPoint = getCoordinateAlongHeadingRadians(origin, Vec.distance(origin, destination), headingRadians)

    val angle1 = calculateArcAngleRadians(origin, testPoint, destination)
    val angle2 = (2 * Math.PI) - angle1

    val arcRadius = Vec.distance(origin, destination)
    val testArcAngles = doubleArrayOf(-angle1, -angle2, angle1, angle2)
    val testArcs = testArcAngles.map { angleRadians ->
        val arcLength = calculateArcLengthRadians(angleRadians, arcRadius)
        val endPoint = getCoordinateAlongHeadingRadians(
            origin,
            Vec.distance(origin, destination),
            headingRadians + angleRadians
        )
        TestArc(angleRadians, arcLength, endPoint)
    }

    val sortedTestArcs = testArcs.sortedWith(
        compareBy(
            { it.arcLength },
            { Vec.distance(it.endPoint, destination) }
        )
    )

    return sortedTestArcs.first().angleRadians

}
