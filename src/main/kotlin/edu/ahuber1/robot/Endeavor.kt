package edu.ahuber1.robot

import edu.ahuber1.math.Vec
import edu.ahuber1.math.projectPoint
import edu.ahuber1.math.toDegrees
import edu.ahuber1.math.toRadians
import robocode.*
import java.util.concurrent.locks.ReentrantLock
import kotlin.concurrent.withLock

public class Endeavor : TeamRobot() {

    private enum class RotationDirection { CLOCKWISE, COUNTERCLOCKWISE }
    private data class Enemy(val name: String, val location: Vec, val engagementOrder: EngagementOrder? = null)
    private data class EngagementOrder(val enemy: Enemy, val bulletPower: Double)

    private val lock = ReentrantLock()
    private val enemies = HashMap<String, Enemy>()
    private var radarRotationDirection: RotationDirection? = null

    private inline val currentLocation: Vec
        get() = Vec(x, y)

    override fun onHitRobot(event: HitRobotEvent?) {
        super.onHitRobot(event)

        if (event == null) {
            return
        }

        lock.withLock {
            val hitEdges = getHitEdges(headingRadians, event.bearingRadians)
            println("Hit edges: $hitEdges")

            val farthest = getFarthestWall(currentLocation, battlefieldSize, hitEdges.opposite)
            println("Farthest: $farthest")

            val turnAmountRadians = farthest.radians - headingRadians
            println("Turn amount: ${toDegrees(turnAmountRadians)}")

            turnRightRadians(turnAmountRadians)
            ahead(advanceDistance)
        }
    }

    override fun onHitWall(event: HitWallEvent?) {
        super.onHitWall(event)

        if (event == null) {
            return
        }

        lock.withLock {
            val closestWall = getClosestWall(currentLocation, battlefieldSize)
            val turnAmountRadians = closestWall.opposite.radians - headingRadians

            turnRightRadians(turnAmountRadians)
            ahead(advanceDistance)
        }
    }

    override fun onScannedRobot(event: ScannedRobotEvent?) {
        super.onScannedRobot(event)

        if (event == null || shouldIgnoreRobot(event)) {
            return
        }

        lock.withLock {
            val enemyLocation = projectPoint(currentLocation, event.distance, event.bearingRadians)
            enemies[event.name] =
                enemies[event.name]?.copy(location = enemyLocation) ?: Enemy(event.name, enemyLocation)
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
        var radarRotationDirection = this.radarRotationDirection

        // If the radar isn't spinning, determine which direction to spin the radar so we reach the battlefield center
        // as quickly as possible.
        if (radarRotationDirection == null) {
            val shortestAngleRadians = Vec.angleRadians(currentLocation, battlefieldCenter)

            radarRotationDirection = when {
                shortestAngleRadians < 0 -> RotationDirection.COUNTERCLOCKWISE
                else -> RotationDirection.CLOCKWISE
            }

            this.radarRotationDirection = radarRotationDirection
        }

        // Start rotating the radar, 45 degrees per turn
        val rotationAmount = 360.0
        val rotationAngleRadians = when (radarRotationDirection) {
            RotationDirection.CLOCKWISE -> toRadians(rotationAmount)
            RotationDirection.COUNTERCLOCKWISE -> -toRadians(rotationAmount)
        }

        turnRadarRightRadians(rotationAngleRadians)
    }

    /**
     * Analyzes [enemies] and returns the name of the enemy to engage or `null` if no enemy should be engaged.
     */
    private fun selectEnemyToEngage(): String? {
        val engagementOrder = findShortestEnemyToEngage() ?: return null

        enemies[engagementOrder.enemy.name] =
            enemies[engagementOrder.enemy.name]!!.copy(engagementOrder = engagementOrder)

        return engagementOrder.enemy.name
    }

    /**
     * Finds the enemy in [enemies] that will take the shortest amount of time to shoot.
     */
    private fun findShortestEnemyToEngage(): EngagementOrder? {
        return enemies.values.mapNotNull(::createEngagementOrder)
            .minByOrNull { Vec.distance(it.enemy.location, currentLocation) }
    }

    /**
     * Creates an [EngagementOrder] to engage an enemy from Endeavor's current position.
     * @param enemy The enemy to engage.
     */
    private fun createEngagementOrder(enemy: Enemy): EngagementOrder? {
        // Calculate bullet power
//        val minBulletPower = 3.0
//        val bulletPower = when (val distance = Vec.distance(currentLocation, enemy.location)) {
//            0.0 -> minBulletPower // Avoids divide-by-zero error when calculating bullet power.
//            else -> min(400.0 / distance, minBulletPower)
//        }

        // Return EngagementOrder
        return EngagementOrder(enemy, 3.0)
    }

    private fun attack(enemyName: String) {
        val engagementOrder = enemies[enemyName]?.engagementOrder ?: return

        // Rotate gun
        val angleRadians = Vec.angleRadians(currentLocation, engagementOrder.enemy.location)
        turnGunRightRadians(angleRadians)

        // Fire gun
        fire(engagementOrder.bulletPower)

        // Advance on enemy
        val rotationAngleRadians = Vec.angleRadians(currentLocation, engagementOrder.enemy.location)
        turnRightRadians(rotationAngleRadians)

        val destination = projectPoint(currentLocation, advanceDistance, headingRadians)
        val canAdvance = enemies.values.none { Vec.distance(destination, it.location) <= 120 }
        if (canAdvance) {
            ahead(advanceDistance)
        }
    }

    public companion object {
        private const val advanceDistance: Double = 20.0
    }
}
