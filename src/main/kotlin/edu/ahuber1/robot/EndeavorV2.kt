package edu.ahuber1.robot

import edu.ahuber1.math.Point
import edu.ahuber1.math.equalsWithinDelta
import edu.ahuber1.math.normalizeRadians
import robocode.ScannedRobotEvent
import robocode.TeamRobot
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.concurrent.locks.ReentrantLock
import kotlin.concurrent.withLock
import kotlin.math.*

public class EndeavorV2 : TeamRobot() {

    private var shouldLog = true

    override fun run() {
        super.run()

        isAdjustGunForRobotTurn = true
        isAdjustRadarForRobotTurn = true

        while (true) {
            turnRadarRightRadians(2 * Math.PI)
        }
    }

    override fun onScannedRobot(event: ScannedRobotEvent?) {
        super.onScannedRobot(event)

        if (event == null) {
            return
        }

        val byPointInstruction = turnWithPoint(event) ?:
        MoveInstruction(event.bearingRadians, event.distance / 2.0).also { println("Moving closer") }

        turnRight(byPointInstruction.rotationAngleDegrees)
        ahead(byPointInstruction.distance)
    }

    private data class MoveInstruction(val rotationAngleRadians: Double, val distance: Double) {
        val rotationAngleDegrees = Math.toDegrees(rotationAngleRadians)

        override fun toString(): String {
            return "MoveInstruction(rotationAngle=$rotationAngleDegrees degrees, distance=$distance)"
        }
    }

    private fun turnWithAngle(event: ScannedRobotEvent): MoveInstruction {
        val wantHeading = headingRadians + event.bearingRadians
        return MoveInstruction(wantHeading - headingRadians, event.distance)
    }

    // TODO: Check for when the original bearing does not equal the angle between the two points

    private fun turnWithPoint(event: ScannedRobotEvent): MoveInstruction? {
        val currentLocation = Point(x, y)

        val angles: List<Double> = buildList {
            add(event.bearingRadians.absoluteValue)
            add((2.0 * Math.PI) - this[0])
            add(-this[0])
            add(-this[1])
        }

        if (shouldLog) {
            println("angles: ${angles.map { Math.toDegrees(it) }}")
        }

        val validXValues = 0.0..battleFieldWidth
        val validYValues = 0.0..battleFieldHeight

        val possibilities = angles.mapNotNull { angle ->
            val dx = event.distance * sin(angle)
            val dy = event.distance * cos(angle)
            val calculatedAngle = atan2(dx, dy) // flip x and y
            val enemyLocation = currentLocation.translate(dx, dy)

            val validX = enemyLocation.x in validXValues
            val validY = enemyLocation.y in validYValues
            val validAngle = angle.equalsWithinDelta(calculatedAngle, 1.0)

            val reasons = buildList {
                if (!validX) add("INVALID_X")
                if (!validY) add("INVALID_Y")
                if (!validAngle) add("INVALID_ANGLE")
            }

            if (reasons.isEmpty()) {
                enemyLocation to calculatedAngle
            } else {
                if (shouldLog) {
                    println(
                        "Enemy location $enemyLocation with angle of ${Math.toDegrees(calculatedAngle)} is invalid " +
                                "due to the following reasons: $reasons"
                    )
                }
                null
            }
        }

        possibilities.forEach { (location, angle) ->
            if (shouldLog) {
                println("Possibility: location=$location, angle=${Math.toDegrees(angle)}")
            }
        }

        if (shouldLog) {
            println()
        }

        val distinctPossibilities = buildList {
            for (possibility in possibilities) {
                val match = this.firstOrNull { distinct ->
                    possibility.allEqualsWithinDelta(distinct, 1e-4, { it.first.x }, { it.first.y }, { it.second })
                }

                if (match == null) {
                    add(possibility)
                }
            }
        }

        distinctPossibilities.forEach { (location, angle) ->
            if (shouldLog) {
                println("Distinct Possibility: location=$location, angle=${Math.toDegrees(angle)}")
            }
        }

        if (shouldLog) {
            println()
        }

        if (distinctPossibilities.isEmpty()) {
            return null
        }

        val avgX = distinctPossibilities.map { it.first.x }.average()
        val avgY = distinctPossibilities.map { it.first.y }.average()

        shouldLog = false
        return MoveInstruction(distinctPossibilities.first().second, Point.distance(Point(avgX, avgY), currentLocation))
    }

    private fun <T> T.allEqualsWithinDelta(other: T, delta: Double, vararg keySelectors: (T) -> Double): Boolean {
        val thisKeys = keySelectors.map { it(this) }
        val otherKeys = keySelectors.map { it(other) }
        val zippedKeys = thisKeys.zip(otherKeys)
        return zippedKeys.all { it.first.equalsWithinDelta(it.second, delta) }
    }
}
