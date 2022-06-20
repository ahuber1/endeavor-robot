package edu.ahuber1.robot

import edu.ahuber1.math.Point
import edu.ahuber1.math.equalsWithinDelta
import robocode.Rules
import robocode.ScannedRobotEvent
import robocode.TeamRobot
import kotlin.math.*

public class EndeavorV2 : TeamRobot() {

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

        val currentLocation = Point(x, y)
        val enemyLocation = getEnemyLocation(currentLocation, headingRadians, event.bearingRadians, event.distance)
        val wantHeadingRadians = getEnemyAbsoluteHeadingRadians(currentLocation, enemyLocation)
        val rotationAngle = normalizeRadians(wantHeadingRadians - headingRadians)
        val gunRotationAngle = normalizeRadians(wantHeadingRadians - gunHeadingRadians)

        turnRightRadians(rotationAngle)
        turnGunRightRadians(gunRotationAngle)
        fire(Rules.MIN_BULLET_POWER)
        ahead((currentLocation distanceTo enemyLocation) - 100.0)
    }

    public companion object {
        private fun getEnemyLocation(
            yourLocation: Point,
            yourHeadingRadians: Double,
            theirBearingRadians: Double,
            distance: Double
        ): Point {
            val angle = yourHeadingRadians + theirBearingRadians
            return yourLocation.translate(distance * sin(angle), distance * cos(angle))
        }

        private fun getEnemyAbsoluteHeadingRadians(yourLocation: Point, theirLocation: Point): Double {
            val leftmostPoint: Point
            val rightmostPoint: Point
            val correctionAmount: Double // The amount by which we have to correct the calculated angle.

            if (theirLocation.x < yourLocation.x) {
                leftmostPoint = theirLocation
                rightmostPoint = yourLocation
                correctionAmount = -Math.PI // -180 degrees
            } else {
                leftmostPoint = yourLocation
                rightmostPoint = theirLocation
                correctionAmount = 0.0
            }

            val radius = leftmostPoint distanceTo rightmostPoint
            val northPoint = leftmostPoint.translate(0.0, radius)
            val distance = northPoint distanceTo rightmostPoint
            val angleRadians = (2 * asin((0.5 * distance) / radius)) + correctionAmount
            return normalizeRadians(angleRadians)
        }

        private fun normalizeRadians(angleRadians: Double): Double {
            val radians360 = 2 * Math.PI // 360 degrees in radians
            var normalized = angleRadians

            // Using equalsWithinDelta allows us to treat, for example, +/- 359.99999999 degrees as +/- 360 degrees.
            while (normalized < -radians360 || normalized.equalsWithinDelta(-radians360, 1.0)) {
                normalized += radians360
            }

            // Using equalsWithinDelta allows us to treat, for example, +/- 359.99999999 degrees as +/- 360 degrees.
            while (normalized > radians360 || normalized.equalsWithinDelta(radians360, 1.0)) {
                normalized -= radians360
            }

            return normalized
        }
    }
}
