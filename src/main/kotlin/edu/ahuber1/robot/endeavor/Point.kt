package edu.ahuber1.robot.endeavor

import kotlin.math.pow
import kotlin.math.sqrt

data class Point(val x: Double, val y: Double)

fun Point.offset(dx: Double, dy: Double): Point = Point(this.x + dx, this.y + dy)

infix fun Point.distanceTo(other: Point): Double = sqrt((other.x - this.x).pow(2.0) + (other.y - this.y).pow(2.0))
