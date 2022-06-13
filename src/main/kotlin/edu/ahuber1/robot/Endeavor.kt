package edu.ahuber1.robot

import edu.ahuber1.math.*
import robocode.*
import java.util.concurrent.locks.ReentrantLock
import kotlin.concurrent.withLock
import kotlin.math.cos
import kotlin.math.min

public class Endeavor : TeamRobot() {

    private enum class RotationDirection { CLOCKWISE, COUNTERCLOCKWISE }
    private data class Enemy(val name: String, val location: Vec, val engagementOrder: EngagementOrder? = null)
    private data class EngagementOrder(val enemy: Enemy, val bulletPower: Double, val timeOfImpact: Double)

    private val lock = ReentrantLock()
    private val enemies = HashMap<String, Enemy>()

    private var status: StatusEvent? = null
    private var radarRotationDirection: RotationDirection? = null

    override fun onStatus(e: StatusEvent?) {
        super.onStatus(e)

        if (e == null) {
            return
        }

        lock.withLock {
            this.status = e
             println("Current location: ${e.robotLocation}")
        }
    }

    override fun onDeath(event: DeathEvent?) {
        super.onDeath(event)
    }

    override fun onHitRobot(event: HitRobotEvent?) {
        super.onHitRobot(event)

        if (event == null) {
            return
        }

        lock.withLock {
            val status = this.status ?: return
            val hitEdges = getHitEdges(headingRadians, event.bearingRadians)
            val farthest = getFarthestWall(status.robotLocation, battlefieldSize, hitEdges)

            val turnAmountRadians = farthest.opposite.radians - headingRadians
            turnRightRadians(turnAmountRadians)
            ahead(20.0)
        }
    }

    override fun onHitWall(event: HitWallEvent?) {
        super.onHitWall(event)

        if (event == null) {
            return
        }

        lock.withLock {
            val status = this.status ?: return
            val closestWall = getClosestWall(status.robotLocation, battlefieldSize)

            val turnAmountRadians = closestWall.opposite.radians - headingRadians
            turnRightRadians(turnAmountRadians)
            ahead(20.0)
        }
    }

    override fun onScannedRobot(event: ScannedRobotEvent?) {
        super.onScannedRobot(event)

        if (event == null || shouldIgnoreRobot(event)) {
            return
        }

        lock.withLock {
            val status = this.status ?: return@withLock
            println("enemy_distance=${event.distance}")
            println("enemy_heading=${event.heading}")
            println("enemy_bearing=${event.bearing}")
            println("status_heading=${status.status.heading}")
            println("status_gun_heading=${status.status.gunHeading}")
            println("status_radar_heading=${status.status.radarHeading}")

            val enemyLocation =
                getCoordinateAlongHeadingRadians(status.robotLocation, event.distance, event.headingRadians)
            println("Location of ${event.name} is $enemyLocation")
            enemies[event.name] = Enemy(event.name, enemyLocation)
        }
    }

    private fun shouldIgnoreRobot(event: ScannedRobotEvent): Boolean {
        // TODO: Ignore sentry robots?
        return isTeammate(event.name)
    }

    override fun onRobotDeath(event: RobotDeathEvent?) {
        super.onRobotDeath(event)

        if (event == null) {
            return
        }

        lock.withLock {
            enemies.remove(event.name)
        }
    }

    override fun run() {
        super.run()

        isAdjustGunForRobotTurn = true
        isAdjustRadarForRobotTurn = true

        while (true) {
            lock.withLock(::updateRobot)
        }
    }

    private fun updateRobot() {
        updateRadar()

        val enemyName = selectEnemyToEngage() ?: return
        attack(enemyName)
    }

    private fun updateRadar() {
        val status = this.status ?: return
        var radarRotationDirection = this.radarRotationDirection

        // If the radar isn't spinning, determine which direction to spin the radar so we reach the battlefield center
        // as quickly as possible.
        if (radarRotationDirection == null) {
            val shortestAngleRadians =
                shortestRotationAngleRadians(status.robotLocation, status.status.radarHeadingRadians, battlefieldCenter)

            radarRotationDirection = when {
                shortestAngleRadians < 0 -> RotationDirection.COUNTERCLOCKWISE
                else -> RotationDirection.CLOCKWISE
            }
            this.radarRotationDirection = radarRotationDirection
        }

        // Start rotating the radar, 45 degrees per turn
        val rotationAngleRadians = when (radarRotationDirection) {
            RotationDirection.CLOCKWISE -> toRadians(45.0)
            RotationDirection.COUNTERCLOCKWISE -> -toRadians(45.0)
        }

        turnRadarRightRadians(rotationAngleRadians)
    }

    /**
     * Analyzes [enemies] and returns the name of the enemy to engage or `null` if no enemy should be engaged.
     */
    private fun selectEnemyToEngage(): String? {
        clearExpiredEngagementOrders()
        val engagementOrder = findShortestEnemyToEngage() ?: return null

        enemies[engagementOrder.enemy.name] =
            enemies[engagementOrder.enemy.name]!!.copy(engagementOrder = engagementOrder)

        return engagementOrder.enemy.name
    }

    /**
     * Clears any engagement orders that have expired.
     */
    private fun clearExpiredEngagementOrders() {
        val iterator = enemies.iterator()

        while (iterator.hasNext()) {
            val entry = iterator.next()
            val value = entry.value
            if (value.engagementOrder != null && value.engagementOrder.timeOfImpact >= time) {
                entry.setValue(value.copy(engagementOrder = null))
            }
        }
    }

    /**
     * Finds the enemy in [enemies] that will take the shortest amount of time to shoot.
     */
    private fun findShortestEnemyToEngage(): EngagementOrder? {
        val status = this.status ?: return null
        val possibleEngagementOrders = enemies.values.asSequence().mapNotNull { createEngagementOrder(status, it) }
        return possibleEngagementOrders.minByOrNull { it.timeOfImpact }
    }

    /**
     * Creates an [EngagementOrder] to engage an enemy from Endeavor's current position.
     * @param status A [StatusEvent] describing Endeavor's current location, heading, etc.
     * @param enemy The enemy to engage.
     */
    private fun createEngagementOrder(status: StatusEvent, enemy: Enemy): EngagementOrder? {
        // Calculate bullet power
        val minBulletPower = 3.0
        val bulletPower = when (val distance = Vec.distance(status.robotLocation, enemy.location)) {
            0.0 -> minBulletPower // Avoids divide-by-zero error when calculating bullet power.
            else -> min(400.0 / distance, minBulletPower)
        }

        // Calculate the number of turns needed to rotate the gun barrel so we're aiming at the target and the
        // number of turns needed to reach the target.
        //
        // TODO: As future enhancement, see if we can predict where the enemy will be in the future.
        val rotationAmountRadians = shortestRotationAngleRadians(status.robotLocation, status.status.gunHeadingRadians, enemy.location)
        println("Shortest rotation amount: ${toDegrees(rotationAmountRadians)}")

        val rotateTurnCount = (rotationAmountRadians) / Rules.GUN_TURN_RATE_RADIANS
        val shootTurnCount =
            when (val shootTurnCountDenominator = Rules.getBulletSpeed(bulletPower) * cos(headingRadians)) {
                0.0 -> return null // Avoids divide-by-zero error
                else -> (status.robotLocation.x + enemy.location.x) / shootTurnCountDenominator
            }

        // Return EngagementOrder
        val timeOfImpact = time.toDouble() + rotateTurnCount + shootTurnCount
        return EngagementOrder(enemy, bulletPower, timeOfImpact)
    }

    private fun attack(enemyName: String) {
        val status = this.status ?: return
        val engagementOrder = enemies[enemyName]?.engagementOrder ?: return

        // Rotate gun
        val gunRotationAngleRadians = shortestRotationAngleRadians(
            status.robotLocation,
            status.status.gunHeadingRadians,
            engagementOrder.enemy.location
        )
        println("gunHeading: ${toDegrees(status.status.gunHeadingRadians)}")
        println("shortestRotationAmount: ${toDegrees(gunRotationAngleRadians)}")
        turnGunRightRadians(gunRotationAngleRadians)

        // Fire gun
        fire(engagementOrder.bulletPower)

        // Advance on enemy
        val rotationAngleRadians = shortestRotationAngleRadians(
            status.robotLocation,
            headingRadians,
            engagementOrder.enemy.location
        )
        turnRightRadians(rotationAngleRadians)
    }
}
