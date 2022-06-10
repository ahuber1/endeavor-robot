package edu.ahuber1.robot.endeavor

import kotlin.math.pow
import kotlin.math.sqrt

data class Point(val x: Double, val y: Double) {
    constructor() : this(0.0, 0.0)
    fun offset(dx: Double, dy: Double): Point = Point(this.x + dx, this.y + dy)

    infix fun distanceTo(other: Point): Double = sqrt((other.x - this.x).pow(2.0) + (other.y - this.y).pow(2.0))
    fun isLeftOf(other: Point): Boolean = this.x < other.x
    companion object {
        fun midpoint(p1: Point, p2: Point): Point =
            Point((p1.x + p2.x) / 2, (p1.y + p2.y) / 2)
    }
}