package edu.ahuber1.robot

import edu.ahuber1.math.Point
import edu.ahuber1.math.equalsWithinDelta
import robocode.Rules
import robocode.ScannedRobotEvent
import robocode.TeamRobot
import kotlin.math.*

public class EndeavorV2 : TeamRobot() {

    private inline val Point.isOnBattlefield: Boolean
        get() = x in 0.0..battleFieldWidth && y in 0.0..battleFieldHeight

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
        val wantHeading = getAbsoluteHeading(currentLocation, enemyLocation)
        val rotationAngle = normalizeRadians(wantHeading - heading)
        val gunRotationAngle = normalizeRadians(wantHeading - gunHeading)

        // Fire gun first
        turnGunRight(gunRotationAngle)
        fire(Rules.MIN_BULLET_POWER)

        // Then move tank
        turnRight(rotationAngle)
        ahead((currentLocation distanceTo enemyLocation) - SAFE_DISTANCE)
    }

    public companion object {
        private const val SAFE_DISTANCE = 100.0
        private const val ENCIRCLING_STEP_ANGLE = 45.0

        private fun getEnemyLocation(
            yourLocation: Point,
            yourHeadingRadians: Double,
            theirBearingRadians: Double,
            distance: Double
        ): Point {
            val angle = yourHeadingRadians + theirBearingRadians
            return yourLocation.translate(distance * sin(angle), distance * cos(angle))
        }

        private fun getAbsoluteHeading(yourLocation: Point, theirLocation: Point): Double {
            val leftmostPoint: Point
            val rightmostPoint: Point
            val correctionAmount: Double // The amount by which we have to correct the calculated angle.

            if (theirLocation.x < yourLocation.x) {
                leftmostPoint = theirLocation
                rightmostPoint = yourLocation
                correctionAmount = -180.0
            } else {
                leftmostPoint = yourLocation
                rightmostPoint = theirLocation
                correctionAmount = 0.0
            }

            val radius = leftmostPoint distanceTo rightmostPoint
            val northPoint = leftmostPoint.translate(0.0, radius)
            val distance = northPoint distanceTo rightmostPoint
            val angleRadians = (2 * asin((0.5 * distance) / radius)) + correctionAmount
            return Math.toDegrees(normalizeRadians(angleRadians))
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

//        private fun getEncirclingPointsClockwise(center: Point, radius: Double): List<Pair<Double, Point>> {
//            val points = ArrayList<Pair<Double, Point>>(((360 / ENCIRCLING_STEP_ANGLE) + 1).toInt())
//            var theta = 0.0
//            while (theta <= 360) {
//                val thetaRadians = Math.toRadians(theta)
//                val dx = radius * sin(thetaRadians)
//                val dy = radius * cos(thetaRadians)
//                points.add(theta to center.translate(dx, dy))
//                theta += ENCIRCLING_STEP_ANGLE
//            }
//
//            return points
//        }

    }
}
