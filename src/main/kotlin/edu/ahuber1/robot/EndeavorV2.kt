package edu.ahuber1.robot

import edu.ahuber1.math.Point
import edu.ahuber1.math.equalsWithinDelta
import robocode.Rules
import robocode.ScannedRobotEvent
import robocode.TeamRobot
import java.util.*
import java.util.concurrent.locks.ReentrantLock
import kotlin.concurrent.withLock
import kotlin.math.asin
import kotlin.math.cos
import kotlin.math.sin

public class EndeavorV2 : TeamRobot() {
    private val enemyLock = ReentrantLock()
    private var enemyName: String? = null
    private var enemyLocation: Point? = null
    private var enemyAngles: Queue<Double>? = null
    private var encirclingClockwise: Boolean? = false

    private inline val Point.isOnBattlefield: Boolean
        get() = x in TANK_LENGTH..(battleFieldWidth - TANK_LENGTH) && y in TANK_LENGTH..(battleFieldHeight - TANK_LENGTH)

    override fun run() {
        super.run()

        isAdjustGunForRobotTurn = true
        isAdjustRadarForRobotTurn = true

        while (true) {
            enemyLock.withLock {
                val enemyAngles = this.enemyAngles
                val enemyLocation = this.enemyLocation

                println(enemyAngles)

                if (enemyAngles == null || enemyLocation == null) {
                    turnRadarRight(360.0)
                    return@withLock
                }

                if (enemyAngles.isEmpty()) {
                    val oldValue = this.encirclingClockwise
                    val newValue = oldValue?.let { old -> !old }
                    this.encirclingClockwise = newValue
                    return@withLock
                }

                val enemyAngle = enemyAngles.poll()
                val endLocation = getPointAlongCircle(enemyLocation, enemyAngle)
                moveTank(endLocation)
                attackEnemy(enemyLocation, Rules.MIN_BULLET_POWER)
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
            // Ignore any ScannedRobotEvents for other robots.
            val enemyName = this.enemyName
            log(
                "onScannedRobot",
                "enemyName" to enemyName,
                "event.name" to event.name
            )

            if (enemyName != null && event.name != enemyName) {
                return
            }

            val currentLocation = Point(x, y)
            val enemyLocation = calculateEnemyLocation(currentLocation, heading, event.bearing, event.distance)

            this.enemyName = event.name
            this.enemyLocation = enemyLocation

            val enemyAngles = this.enemyAngles
            if (enemyAngles != null && enemyAngles.isNotEmpty()) {
                return
            }

            val closestAngle = buildClockwiseAngleProgression()
                .map {
                    val point = getPointAlongCircle(enemyLocation, it)
                    val distance = currentLocation distanceTo point
                    Triple(it, point, distance)
                }
                .filter { !it.third.equalsWithinDelta(0.0, 1.0) }
                .minBy { it.third }
                .first

            val clockwiseAngles = buildClockwiseAngleProgression(closestAngle)
                .takeWhile { getPointAlongCircle(enemyLocation, it).isOnBattlefield }
                .toList()

            val counterclockwiseAngles = buildClockwiseAngleProgression(closestAngle)
                .toList()
                .reversed()
                .takeWhile { getPointAlongCircle(enemyLocation, it).isOnBattlefield }

            val encirclingClockwise = when (val encirclingClockwise = this.encirclingClockwise) {
                null -> when {
                    clockwiseAngles.size < counterclockwiseAngles.size -> false
                    clockwiseAngles.size > counterclockwiseAngles.size -> true
                    else -> true // TODO: Consider randomly selecting boolean value
                }
                else -> encirclingClockwise
            }

            val newEnemyAngles = when (encirclingClockwise) {
                true -> clockwiseAngles
                false -> counterclockwiseAngles
            }

            val repeatedEnemyAngles = LinkedList<Double>()
            repeat(ENCIRCLE_COUNT) { repeatedEnemyAngles.addAll(newEnemyAngles) }

            this.enemyAngles = repeatedEnemyAngles
            this.encirclingClockwise = encirclingClockwise
        }
    }

    private fun moveTank(endLocation: Point) {
        val startLocation = Point(x, y)
        val startHeading = heading

        val absoluteHeading = calculateAbsoluteHeading(startLocation, endLocation)
        val turnAmount = normalizeDegrees(absoluteHeading - startHeading, true)

        turnRight(turnAmount)
        ahead(startLocation distanceTo endLocation)
    }

    @Suppress("SameParameterValue") // TODO: Consider removing firePower param.
    private fun attackEnemy(enemyLocation: Point, firePower: Double) {
        val startLocation = Point(x, y)
        val startGunHeading = gunHeading

        val absoluteGunHeading = calculateAbsoluteHeading(startLocation, enemyLocation)
        val gunTurnAmount = normalizeDegrees(absoluteGunHeading - startGunHeading, true)

        turnGunRight(gunTurnAmount)
        fire(firePower)
    }

    public companion object {
        private const val SAFE_DISTANCE = 100.0
        private const val ENCIRCLING_STEP_ANGLE = 45.0
        private const val ENCIRCLE_COUNT = 1 // TODO: Consider making this random
        private const val TANK_LENGTH = 100.0

        private fun calculateEnemyLocation(
            yourLocation: Point,
            yourHeading: Double,
            theirBearing: Double,
            distance: Double
        ): Point {
            val angle = Math.toRadians(yourHeading + theirBearing)
            return yourLocation.translate(distance * sin(angle), distance * cos(angle))
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
            return normalizeDegrees(angleDegrees, true)
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

        private fun buildClockwiseAngleProgression(startAngle: Double = 0.0): Sequence<Double> {
            require(startAngle >= 0.0) {
                "\"startAngle\" must be greater than or equal to zero. $startAngle is not."
            }

            require(startAngle % ENCIRCLING_STEP_ANGLE == 0.0) {
                "\"startAngle\" must divide evenly into \"step\". $startAngle does not divide evenly into $ENCIRCLING_STEP_ANGLE"
            }

            return sequence {
                yield(startAngle)

                var theta = startAngle
                do {
                    theta = (theta + ENCIRCLING_STEP_ANGLE) % 360
                    yield(theta)
                } while (theta != startAngle)
            }
        }

        private fun getPointAlongCircle(center: Point, angle: Double): Point {
            val angleRadians = Math.toRadians(angle)
            return center.translate(SAFE_DISTANCE * sin(angleRadians), SAFE_DISTANCE * cos(angleRadians))
        }

        private fun log(message: String, vararg fields: Pair<String, Any?>) {
            println(message)
            for ((key, value) in fields) {
                println("    $key: $value")
            }
        }
    }
}
