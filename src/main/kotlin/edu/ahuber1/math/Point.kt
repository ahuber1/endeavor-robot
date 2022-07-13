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
     * Rotates this [Point] around a pivot point.
     * @param pivot The pivot point.
     * @param angleRadians The angle by which to rotate in radians.
     */
    @Deprecated("Do not use")
    public fun rotateAround(pivot: Point, angleRadians: Double): Point {
        val translated = this - pivot
        val newX = translated.x * cos(angleRadians) - translated.y * sin(angleRadians)
        val newY = translated.x * sin(angleRadians) - translated.y * cos(angleRadians)
        return Point(newX + pivot.x, newY + pivot.y)
    }

    /**
     * Returns this [Point] that is translated by the provided amount.
     * @param dx The x offset.
     * @param dy The y offset.
     */
    public fun translate(dx: Double, dy: Double): Point {
        return Point(this.x + dx, this.y + dy)
    }

    /**
     * Translates this [Point] by the negative offset.
     * @return The translated [Point].
     */
    public operator fun minus(other: Point): Point {
        return translate(-other.x, -other.y)
    }

    /**
     * Translates this [Point] by a specified offset.
     * @return The translated [Point].
     */
    public operator fun plus(other: Point): Point {
        return translate(other.x, other.y)
    }

    public infix fun distanceTo(other: Point): Double {
        return distanceTo(other.x, other.y)
    }

    public fun distanceTo(otherX: Double, otherY: Double): Double {
        return sqrt((otherX - this.x).squared + (otherY - this.y).squared)
    }

    public companion object {
        /**
         * A [Point] at (0.0, 0.0)
         */
        public val zero: Point = Point(0.0, 0.0)

        /**
         * Calculates the straight-line distance between two vectors.
         */
        @Deprecated("Infix syntax is preferred.", ReplaceWith("v1 distanceTo v2"))
        public fun distance(v1: Point, v2: Point): Double {
            return v1 distanceTo v2
        }

        /**
         * Calculates the angle between two vectors in radians.
         */
        @Deprecated("Do not use.")
        public fun angleRadians(from: Point, to: Point): Double {
            val (dx, dy) = to - from
            return atan2(dy, dx)
        }
        
        public fun midpoint(point1: Point, point2: Point): Point {
            return Point((point1.x + point2.x) / 2, (point1.y + point2.y) / 2)
        }
    }
}