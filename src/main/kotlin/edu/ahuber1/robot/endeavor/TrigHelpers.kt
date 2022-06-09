package edu.ahuber1.robot.endeavor

import kotlin.math.cos
import kotlin.math.sin

/**
 * Calculates the point relative to `origin` that is `distance` units away at an angle of `heading` radians.
 */
fun calculateCoordinateAlongHeadingRadians(origin: Point, distance: Double, heading: Double): Point {
    val dx = distance * cos(heading)
    val dy = distance * sin(heading)
    return origin.offset(dx, dy)
}
