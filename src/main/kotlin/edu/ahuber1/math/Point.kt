package edu.ahuber1.math

import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt

/**
 * A point in 2D Cartesian space.
 */
public data class Point(val x: Double, val y: Double) {
    /**
     * Returns this [Point] that is translated by the provided amount.
     * @param dx The x offset.
     * @param dy The y offset.
     */
    public fun translate(dx: Double, dy: Double): Point {
        return Point(this.x + dx, this.y + dy)
    }

    /**
     * Translates this [Point] by a specified offset.
     * @return The translated [Point].
     */
    public operator fun plus(other: Point): Point {
        return translate(other.x, other.y)
    }

    /**
     * Translates this [Point] by the negative offset.
     * @return The translated [Point].
     */
    public operator fun minus(other: Point): Point {
        return translate(-other.x, -other.y)
    }

    /**
     * Rotates this [Point] around a pivot point.
     * @param pivot The pivot point.
     * @param angleRadians The angle by which to rotate in radians.
     */
    public fun rotateAround(pivot: Point, angleRadians: Double): Point {
        val translated = this - pivot
        val newX = translated.x * cos(angleRadians) - translated.y * sin(angleRadians)
        val newY = translated.x * sin(angleRadians) - translated.y * cos(angleRadians)
        return Point(newX + pivot.x, newY + pivot.y)
    }

    public companion object {
        /**
         * A [Point] at (0.0, 0.0)
         */
        public val zero: Point = Point(0.0, 0.0)

        /**
         * Calculates the straight-line distance between two vectors.
         */
        public fun distance(v1: Point, v2: Point): Double {
            return sqrt((v2.x - v1.x).squared + (v2.y - v1.y).squared)
        }

        /**
         * Calculates the angle between two vectors in radians.
         */
        public fun angleRadians(from: Point, to: Point): Double {
            val (dx, dy) = to - from
            return atan2(dy, dx)
        }
    }
}