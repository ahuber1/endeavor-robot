package edu.ahuber1.robot

import edu.ahuber1.math.Point
import edu.ahuber1.math.equalsWithinDelta
import robocode.*
import java.util.concurrent.locks.ReentrantLock
import kotlin.collections.ArrayList
import kotlin.concurrent.withLock
import kotlin.math.absoluteValue
import kotlin.math.asin
import kotlin.math.cos
import kotlin.math.sin

public class EndeavorV2 : TeamRobot() {

    private val enemyLock = ReentrantLock()
    private var enemyInfo: EnemyInfo? = null

    override fun run() {
        super.run()

        isAdjustGunForRobotTurn = true
        isAdjustRadarForRobotTurn = true

        while (true) {
            enemyLock.withLock {
                val enemyInfo = this.enemyInfo
                if (enemyInfo == null) {
                    turnRadarRight(360.0)
                    return@withLock
                }

                val enemyLocation = enemyInfo.location
                val destination = determineDestination(enemyInfo)

                if (enemyLocation == null || destination == null || !enemyInfo.decrementPointsRemaining()) {
                    enemyInfo.setRotationDirection(enemyInfo.rotationDirection?.opposite, ENCIRCLE_POINT_COUNT)
                    return@withLock
                }

                attackEnemy(enemyLocation, Rules.MIN_BULLET_POWER) // TODO: Adjust power
                moveTank(destination.point)
                attackEnemy(enemyLocation, Rules.MIN_BULLET_POWER) // TODO: Adjust power
                enemyInfo.lastAngle = destination.angle
            }
        }
    }

    override fun onScannedRobot(event: ScannedRobotEvent?) {
        super.onScannedRobot(event)

        // Performs a null check.
        if (event == null) {
            return
        }

        enemyLock.withLock {
            val enemyName = enemyInfo?.name

            if (enemyName != null && event.name != enemyName) {
                return
            }

            val currentLocation = Point(x, y)
            val enemyInfo = this.enemyInfo ?: EnemyInfo(event.name)
            val enemyLocation = calculateEnemyLocation(currentLocation, heading, event.bearing, event.distance)
            val rotationDirection = enemyInfo.rotationDirection ?: determineRotationDirection(enemyLocation)

            enemyInfo.location = calculateEnemyLocation(currentLocation, heading, event.bearing, event.distance)
            enemyInfo.setRotationDirection(rotationDirection, ENCIRCLE_POINT_COUNT)
            this.enemyInfo = enemyInfo
        }
    }

    override fun onRobotDeath(event: RobotDeathEvent?) {
        super.onRobotDeath(event)

        // Performs a null check
        if (event == null) {
            return
        }

        enemyLock.withLock {
            if (this.enemyInfo?.name == event.name) {
                this.enemyInfo = null
            }
        }
    }

    override fun onHitWall(event: HitWallEvent?) {
        super.onHitWall(event)

        // Performs null check
        if (event == null) {
            return
        }

        enemyLock.withLock {
            // Reverse rotation direction
            this.enemyInfo?.setRotationDirection(this.enemyInfo?.rotationDirection?.opposite, ENCIRCLE_POINT_COUNT)
        }
    }

    private fun determineRotationDirection(enemyLocation: Point?): RotationDirection? {
        if (enemyLocation == null) {
            return null
        }

        val clockwiseAngles = buildRotationAngleProgression(RotationDirection.CLOCKWISE)
        val counterClockwiseAngles = clockwiseAngles.reversed()

        val averageClockwiseDistance =
            clockwiseAngles.map { getPointAlongCircle(enemyLocation, it) }
                .map { it.getShortestStraightLineDistanceToWall(battleFieldWidth, battleFieldHeight) }
                .average()
        val averageCounterClockwiseDistance =
            counterClockwiseAngles.map { getPointAlongCircle(enemyLocation, it) }
                .map { it.getShortestStraightLineDistanceToWall(battleFieldWidth, battleFieldHeight) }
                .average()

        return when {
            averageClockwiseDistance < averageCounterClockwiseDistance -> RotationDirection.COUNTERCLOCKWISE
            averageClockwiseDistance > averageCounterClockwiseDistance -> RotationDirection.CLOCKWISE
            else -> RotationDirection.CLOCKWISE // TODO: Consider returning random value
        }
    }

    private fun determineDestination(enemyInfo: EnemyInfo): EncirclePoint? {
        // Extract the data we need from the EnemyInfo object. Also perform null checks.
        val enemyLocation = enemyInfo.location ?: return null
        val rotationDirection = enemyInfo.rotationDirection ?: return null
        val progressionStartAngle = enemyInfo.lastAngle ?: 0.0

        // Perform the following steps:
        //
        // 1. Get list of angles around enemy.
        // 2. Remove duplicates.
        // 3. Calculate the point at that angle around the enemy (e.g., 0 degrees is directly above the enemy,
        //    90 degrees is directly to the right of the enemy); and store that point, the distance from the robot's
        //    current location to the enemy, and the angle in an EncirclePoint object.
        // 4. Remove any EncirclePoints where the distance to the enemy is 0.0 +/- DISTANCE_DELTA.
        val encirclePoints =
            buildRotationAngleProgression(rotationDirection, progressionStartAngle) // Get list of angles around enemy
                .distinct() // Remove duplicates
                .map { angle ->
                    val point = getPointAlongCircle(enemyLocation, angle)
                    val distance = point.distanceTo(x, y)
                    EncirclePoint(point, angle, distance)
                }
                .filter { !it.distanceFromRobot.equalsWithinDelta(0.0, DISTANCE_DELTA) }

        // Find the EncirclePoint that is closest to the robot.
        val closestDistance = encirclePoints.minByOrNull { it.distanceFromRobot }?.distanceFromRobot ?: return null

        // Find the first point where the distance from the robot to that point equals
        // closestDistance +/- DISTANCE_DELTA, and return it.
        return encirclePoints.firstOrNull { it.distanceFromRobot.equalsWithinDelta(closestDistance, DISTANCE_DELTA) }
    }


    private fun moveTank(endLocation: Point) {
        val startLocation = Point(x, y)
        val startHeading = heading

        val absoluteHeading = calculateAbsoluteHeading(startLocation, endLocation)
        val turnAmount = normalizeRotationAngle(absoluteHeading - startHeading)

        turnRight(turnAmount)
        ahead(startLocation distanceTo endLocation)
    }

    @Suppress("SameParameterValue") // TODO: Consider removing firePower param.
    private fun attackEnemy(enemyLocation: Point, firePower: Double) {
        val startLocation = Point(x, y)
        val startGunHeading = gunHeading

        val absoluteGunHeading = calculateAbsoluteHeading(startLocation, enemyLocation)
        val gunTurnAmount = normalizeRotationAngle(absoluteGunHeading - startGunHeading)

        turnGunRight(gunTurnAmount)
        fire(firePower)
    }

    public companion object {
        private const val SAFE_DISTANCE = 100.0
        private const val ENCIRCLING_STEP_ANGLE = 45.0
        private const val TANK_LENGTH = 100.0
        private const val MAX_ANGLE_PROGRESSION_COUNT = 1 + (360 / ENCIRCLING_STEP_ANGLE.toInt())
        private const val ENCIRCLE_COUNT = 1 // TODO: Consider making this random
        private const val ENCIRCLE_POINT_COUNT = MAX_ANGLE_PROGRESSION_COUNT * ENCIRCLE_COUNT
        private const val DISTANCE_DELTA = 5.0

        private fun calculateEnemyLocation(
            yourLocation: Point,
            yourHeading: Double,
            theirBearing: Double,
            distanceBetweenYouAndThem: Double
        ): Point {
            val angle = Math.toRadians(yourHeading + theirBearing)
            val dx = distanceBetweenYouAndThem * sin(angle)
            val dy = distanceBetweenYouAndThem * cos(angle)
            return yourLocation.translate(dx, dy)
        }

        private fun buildRotationAngleProgression(
            rotationDirection: RotationDirection,
            startAngle: Double = 0.0
        ): List<Double> {
            require(startAngle >= 0.0) {
                "\"startAngle\" must be greater than or equal to zero. $startAngle is not."
            }

            require(startAngle % ENCIRCLING_STEP_ANGLE == 0.0) {
                "\"startAngle\" must divide evenly into \"step\". $startAngle does not divide evenly into $ENCIRCLING_STEP_ANGLE"
            }

            val angleProgression = ArrayList<Double>(ENCIRCLE_POINT_COUNT)
            angleProgression.add(startAngle)

            var theta = startAngle
            while (angleProgression.size < ENCIRCLE_POINT_COUNT) {
                theta = (theta + ENCIRCLING_STEP_ANGLE) % 360
                angleProgression.add(theta)
            }

            return when (rotationDirection) {
                RotationDirection.CLOCKWISE -> angleProgression
                RotationDirection.COUNTERCLOCKWISE -> angleProgression.reversed()
            }
        }

        private fun getPointAlongCircle(center: Point, angle: Double): Point {
            val angleRadians = Math.toRadians(angle)
            return center.translate(SAFE_DISTANCE * sin(angleRadians), SAFE_DISTANCE * cos(angleRadians))
        }

        private fun Point.getShortestStraightLineDistanceToWall(
            battleFieldWidth: Double,
            battleFieldHeight: Double
        ): Double {
            val northDistance = this.y - TANK_LENGTH
            val southDistance = battleFieldHeight - TANK_LENGTH + this.y
            val eastDistance = battleFieldWidth - TANK_LENGTH - this.x
            val westDistance = this.x - TANK_LENGTH
            return listOf(northDistance, southDistance, eastDistance, westDistance).filter { it >= 0.0 }.minOrNull()!!
        }

        private fun calculateAbsoluteHeading(start: Point, end: Point): Double {
            val leftmostPoint: Point
            val rightmostPoint: Point
            val correctionAmount: Double // The amount by which we have to correct the calculated angle.

            if (end.x < start.x) {
                leftmostPoint = end
                rightmostPoint = start
                correctionAmount = -180.0
            } else {
                leftmostPoint = start
                rightmostPoint = end
                correctionAmount = 0.0
            }

            val radius = leftmostPoint distanceTo rightmostPoint
            val northPoint = leftmostPoint.translate(0.0, radius)
            val distance = northPoint distanceTo rightmostPoint
            val angleDegrees = Math.toDegrees(2 * asin((0.5 * distance) / radius)) + correctionAmount

            return when {
                angleDegrees.isFinite() -> normalizeDegrees(angleDegrees, true)
                else -> 0.0
            }
        }

        private fun normalizeRotationAngle(angle: Double): Double {
            val angle1 = normalizeDegrees(angle, true)
            val angle2 = when (angle1 <= 0.0) {
                true -> angle1 + 360.0
                else -> angle1 - 360.0
            }
            return when (angle1.absoluteValue < angle2.absoluteValue) {
                true -> angle1
                else -> angle2
            }
        }

        @Suppress("SameParameterValue") // TODO: Maybe we can get rid of "negativeAnglesAllowed"
        private fun normalizeDegrees(angle: Double, negativeAnglesAllowed: Boolean): Double {
            val minimum = when (negativeAnglesAllowed) {
                true -> -360.0
                false -> 0.0
            }

            var normalized = angle

            while (normalized < minimum || normalized.equalsWithinDelta(minimum, 1.0)) {
                normalized += 360
            }

            while (normalized > 360 || normalized.equalsWithinDelta(360.0, 1.0)) {
                normalized -= 360
            }

            return normalized
        }
    }
}
