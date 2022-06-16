package edu.ahuber1.robot

import edu.ahuber1.math.*
import robocode.Robot
import robocode.StatusEvent
import kotlin.math.abs

/**
 * The center point of the battlefield.
 */
public inline val Robot.battlefieldCenter: Point
    get() = Point(battleFieldWidth / 2, battleFieldHeight / 2)

/**
 * The size of the battlefield.
 */
public inline val Robot.battlefieldSize: Size
    get() = Size(battleFieldWidth, battleFieldHeight)

/**
 * The location of the robot.
 */
public inline val StatusEvent.robotLocation: Point
    get() = Point(status.x, status.y)

/**
 * Determines the edges where another robot hit your robot.
 * @param headingRadians Your heading in radians.
 * @param otherRobotBearingRadians The bearing of the other robot in radians. This is relative to your gun heading.
 */
public fun getHitEdges(headingRadians: Double, otherRobotBearingRadians: Double): Set<Heading> {
    // Convert the bearing of the other robot, which is relative to my heading, to an angle relative to the
    // battlefield (0 radians is north).
    val otherRobotHeading = normalizeRadians(otherRobotBearingRadians + headingRadians)

    val otherIsAbove = otherRobotHeading !in toRadians(90.0)..toRadians(270.0)
    val otherIsRight = otherRobotHeading in 0.0..toRadians(180.0)

    val otherRobotVerticalEdge = if (otherIsAbove) Heading.NORTH else Heading.SOUTH
    val otherRobotHorizontalEdge = if (otherIsRight) Heading.EAST else Heading.WEST

    return setOf(otherRobotVerticalEdge, otherRobotHorizontalEdge)
}

/**
 * Returns a [Map] containing the straight-line distance to each of the provided walls from a point.
 * @param location The point from which to calculate the straight-line distance.
 * @param walls The walls from which to calculate the straight-line distance to [location].
 * @param battlefieldSize The size of the battlefield.
 */
public fun getStraightLineDistanceToWalls(
    location: Point,
    walls: Iterable<Heading>,
    battlefieldSize: Size
): Map<Heading, Double> {
    return walls.distinct().associateWith {
        when (it) {
            Heading.NORTH -> abs(battlefieldSize.height - location.y)
            Heading.SOUTH -> abs(location.y)
            Heading.WEST -> abs(location.x)
            Heading.EAST -> abs(battlefieldSize.width - location.x)
        }
    }
}

/**
 * Returns the wall with the shortest straight-line distance.
 * @param location The point from which to calculate the straight-line distances.
 * @param battlefieldSize The size of the battlefield.
 * @param walls The walls from which to calculate the straight-line distance to [location].
 *
 * @see getStraightLineDistanceToWalls
 * @see getFarthestWall
 */
public fun getClosestWall(
    location: Point,
    battlefieldSize: Size,
    walls: Iterable<Heading> = Heading.values().asIterable()
): Heading {
    return getStraightLineDistanceToWalls(location, walls, battlefieldSize).minByOrNull { it.value }?.key
        ?: error("There are no walls to choose from.")
}

/**
 * Returns the wall with the longest straight-line distance.
 * @param location The point from which to calculate the straight-line distances.
 * @param battlefieldSize The size of the battlefield.
 * @param walls The walls from which to calculate the straight-line distance to [location].
 *
 * @see getStraightLineDistanceToWalls
 * @see getClosestWall
 */
public fun getFarthestWall(
    location: Point,
    battlefieldSize: Size,
    walls: Iterable<Heading> = Heading.values().asIterable()
): Heading {
    return getStraightLineDistanceToWalls(location, walls, battlefieldSize).maxByOrNull { it.value }?.key
        ?: error("There are no walls to choose from.")
}