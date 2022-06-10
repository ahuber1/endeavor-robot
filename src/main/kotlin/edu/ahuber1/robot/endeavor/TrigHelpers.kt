package edu.ahuber1.robot.endeavor

import kotlin.math.*

/**
 * Calculates the point relative to `origin` that is `distance` units away at an angle of `heading` radians.
 */
fun calculateCoordinateAlongHeadingRadians(origin: Point, distance: Double, heading: Double): Point {
    val dx = distance * cos(heading)
    val dy = distance * sin(heading)
    return origin.offset(dx, dy)
}

/**
 * Calculates the angle between two points along a circle (`p1` and `p2`) relative to that circle's center (`c`).
 */
fun calculateArcAngleRadians(c: Point, p1: Point, p2: Point): Double {
    // https://math.stackexchange.com/questions/830413/calculating-the-arc-length-of-a-circle-segment
    val distance = p1 distanceTo p2
    val radius = c distanceTo p1
    return acos(1 - (distance.pow(2) / (2 * radius.pow(2))))
}

fun calculateArcLengthRadians(angle: Double, radius: Double): Double = abs(angle * radius)

fun inverseRadians(radians: Double): Double = (2 * Math.PI) - radians

//fun clampRadians(radians: Double): Double {
//    val radians360 = 2 * Math.PI
//    var clamped = radians
//
//    while (clamped < 0) {
//        clamped += radians360
//    }
//
//    while (clamped > radians360) {
//        clamped -= radians360
//    }
//
//    return clamped
//}