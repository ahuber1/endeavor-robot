package edu.ahuber1.robot

import edu.ahuber1.math.toRadians

/**
 * An enum representing a compass heading.
 */
public enum class Heading {
    NORTH, SOUTH, EAST, WEST
}

/**
 * Returns true if this [Heading] is either [Heading.EAST] or [Heading.WEST]
 */
public inline val Heading.isHorizontal: Boolean
    get() = this == Heading.EAST || this == Heading.WEST

/**
 * Returns true if this [Heading] is either [Heading.NORTH] or [Heading.SOUTH]
 */
public inline val Heading.isVertical: Boolean
    get() = this == Heading.NORTH || this == Heading.SOUTH

/**
 * Returns the opposite [Heading].
 * @sample oppositeExample
 */
public val Heading.opposite: Heading
    get() = when (this) {
        Heading.NORTH -> Heading.SOUTH
        Heading.SOUTH -> Heading.NORTH
        Heading.EAST -> Heading.WEST
        Heading.WEST -> Heading.EAST
    }

private fun oppositeExample() {
    println(Heading.NORTH.opposite) // prints "SOUTH"
    println(Heading.EAST.opposite)  // prints "WEST"
}

public val Heading.radians: Double
    get() = when (this) {
        Heading.NORTH -> 0.0
        Heading.SOUTH -> toRadians(180.0)
        Heading.EAST -> toRadians(90.0)
        Heading.WEST -> toRadians(270.0)
    }
