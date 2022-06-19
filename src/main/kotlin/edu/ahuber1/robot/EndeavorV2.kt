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

        val currentLocation = Point(x, y)
        val angle = headingRadians + event.bearingRadians

        val enemyLocation = currentLocation.translate(event.distance * sin(angle), event.distance * cos(angle))
        val (dx, dy) = enemyLocation - currentLocation
        val calculatedAngle = atan2(dy, dx) // TODO: We need to fix the algorithm used to calculate the angle of rotation.

        println("enemyLocation: $enemyLocation")
        println("calculatedAngle: ${Math.toDegrees(calculatedAngle)}")
        println("actualAngle: ${event.bearing}")
    }
}
