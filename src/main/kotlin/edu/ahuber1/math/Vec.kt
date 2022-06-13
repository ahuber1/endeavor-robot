package edu.ahuber1.math

import kotlin.math.atan2
import kotlin.math.sqrt

/**
 * A point in 2D Cartesian space.
 */
public data class Vec(val x: Double, val y: Double) {

    /**
     * Creates a new [Vec] with its x and y component both set to 0.0.
     */
    public constructor() : this(0.0, 0.0)

    /**
     * Returns this [Vec] that is translated by the provided amount.
     * @param dx The x offset.
     * @param dy The y offset.
     */
    public fun translate(dx: Double, dy: Double): Vec {
        return Vec(this.x + dx, this.y + dy)
    }

    /**
     * Translates this [Vec] by a specified offset.
     * @return The translated [Vec].
     */
    public operator fun plus(other: Vec): Vec {
        return translate(other.x, other.y)
    }

    /**
     * Translates this [Vec] by the negative offset.
     * @return The translated [Vec].
     */
    public operator fun minus(other: Vec): Vec {
        return translate(-other.x, -other.y)
    }

    public companion object {
        /**
         * Calculates the straight-line distance between two vectors.
         */
        public fun distance(v1: Vec, v2: Vec): Double {
            return sqrt((v2.x - v1.x).squared + (v2.y - v1.y).squared)
        }

        /**
         * Calculates the angle between two vectors in radians.
         */
        public fun angleRadians(from: Vec, to: Vec): Double {
            val (dx, dy) = to - from
            return atan2(dy, dx)
        }
    }
}