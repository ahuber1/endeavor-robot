package edu.ahuber1.robot

import edu.ahuber1.math.Vec
import edu.ahuber1.math.getCoordinateAlongHeadingRadians
import edu.ahuber1.math.shortestRotationAngleRadians
import edu.ahuber1.math.toRadians
import mu.KotlinLogging
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
    private val logger = KotlinLogging.logger { }

    private var status: StatusEvent? = null
    private var enemies = HashMap<String, Enemy>()
    private var radarRotationDirection: RotationDirection? = null

    override fun onDeath(event: DeathEvent?) {
        super.onDeath(event)
        logger.error { "Endeavor died!" }
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
            val status = status ?: return@withLock
            val enemyLocation =
                getCoordinateAlongHeadingRadians(status.robotLocation, event.distance, event.headingRadians)

            enemies[event.name] = when (val matchingEnemy = enemies[event.name]) {
                null -> Enemy(event.name, enemyLocation)
                else -> matchingEnemy.copy(name = event.name, location = enemyLocation)
            }
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
        if (radarRotationDirection == null) {
            val shortestAngleRadians =
                shortestRotationAngleRadians(status.robotLocation, status.status.radarHeadingRadians, battlefieldCenter)

            radarRotationDirection = when {
                shortestAngleRadians < 0 -> RotationDirection.COUNTERCLOCKWISE
                else -> RotationDirection.CLOCKWISE
            }
            this.radarRotationDirection = radarRotationDirection
        }

        val rotationAngleRadians = when (radarRotationDirection) {
            RotationDirection.CLOCKWISE -> toRadians(45.0)
            RotationDirection.COUNTERCLOCKWISE -> -toRadians(45.0)
        }

        turnRadarRightRadians(rotationAngleRadians)
    }

    private fun selectEnemyToEngage(): String? {
        val status = status ?: return null
        val minOrder = enemies.values.asSequence().mapNotNull {
            val distance = Vec.distance(status.robotLocation, it.location)
            if (distance == 0.0) {
                return@mapNotNull null // Avoids divide-by-zero error
            }

            val bearingRadians = Vec.angleRadians(status.robotLocation, it.location)
            val headingRadians = status.status.gunHeadingRadians + bearingRadians
            val bulletPower = min(400.0 / distance, 3.0)
            val rotateTurnCount = (headingRadians - status.status.gunHeadingRadians) / Rules.GUN_TURN_RATE_RADIANS

            val shootTurnCountDenominator = Rules.getBulletSpeed(bulletPower) * cos(headingRadians)
            if (shootTurnCountDenominator == 0.0) {
                return@mapNotNull null // Avoids divide-by-zero error
            }

            val shootTurnCount = (status.robotLocation.x + it.location.x) / shootTurnCountDenominator
            EngagementOrder(it, bulletPower, time.toDouble() + rotateTurnCount + shootTurnCount)
        }.minByOrNull { it.timeOfImpact } ?: return null

        enemies[minOrder.enemy.name] = enemies[minOrder.enemy.name]!!.copy(engagementOrder = minOrder)
        return minOrder.enemy.name
    }

    private fun attack(enemyName: String) {
        val status = status ?: return
        val engagementOrder = enemies[enemyName]?.engagementOrder ?: return

        // Rotate gun
        val gunRotationAngleRadians = shortestRotationAngleRadians(
            status.robotLocation,
            status.status.gunHeadingRadians,
            engagementOrder.enemy.location
        )
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