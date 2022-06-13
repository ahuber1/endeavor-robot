package edu.ahuber1.math

import kotlin.math.abs
import kotlin.math.pow

/**
 * Returns this value raised to the power of 2.
 */
public inline val Double.squared: Double
    get() = this.pow(2)

/**
 * Determines whether the difference between this value and [other] is within the specified [delta] value.
 */
public fun Double.equalsWithinDelta(other: Double, delta: Double): Boolean {
    return abs(this - other) <= delta
}