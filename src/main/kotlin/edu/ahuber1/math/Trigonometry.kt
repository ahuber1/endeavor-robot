package edu.ahuber1.math

import kotlin.math.abs
import kotlin.math.acos

/**
 * Projects a point from one position outward at a particular angle.
 * @param from The point from which to project.
 * @param distance The distance between [from] and the projected point.
 * @param angleRadians The angle which to project the point in radians.
 */
public fun projectPoint(from: Point, distance: Double, angleRadians: Double): Point {
    val northPoint = from.translate(distance, 0.0)
    return northPoint.rotateAround(from, angleRadians)
}

/**
 * Calculates the angle between two points along a circle relative to that circle's center.
 * @param center The circle's center point.
 * @param point1 The first point along the circle.
 * @param point2 The second point along the circle.
 */
public fun calculateArcAngleRadians(center: Point, point1: Point, point2: Point): Double {
    // https://math.stackexchange.com/questions/830413/calculating-the-arc-length-of-a-circle-segment
    val distance = Point.distance(point1, point2)
    val radius = Point.distance(center, point1)
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
