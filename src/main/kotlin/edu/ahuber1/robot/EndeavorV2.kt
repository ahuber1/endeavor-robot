package edu.ahuber1.robot

import edu.ahuber1.math.normalizeRadians
import robocode.ScannedRobotEvent
import robocode.TeamRobot
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.concurrent.locks.ReentrantLock
import kotlin.concurrent.withLock

public class EndeavorV2 : TeamRobot() {

    override fun run() {
        super.run()

        isAdjustGunForRobotTurn = true
        isAdjustRadarForRobotTurn = true

        var previousHeading = Double.MAX_VALUE

        while (true) {
            turnRadarRightRadians(2 * Math.PI)
            log("endeavor_heading" to heading)
        }
    }

    override fun onScannedRobot(event: ScannedRobotEvent?) {
        super.onScannedRobot(event)

        if (event == null) {
            return
        }

        val wantHeading = headingRadians + event.bearingRadians

        log(
            "endeavor_heading" to heading,
            "endeavor_radar_heading" to radarHeading,
            "enemy_heading" to event.heading,
            "enemy_bearing" to event.bearing,
            "want_heading" to Math.toDegrees(wantHeading),
            "want_heading_normalized" to Math.toDegrees(normalizeRadians(wantHeading))
        )

        turnRightRadians(wantHeading - headingRadians)
        ahead(event.distance)
    }

    private val logLock = ReentrantLock()

    private fun log(vararg pairs: Pair<String, Any>) {
        if (pairs.isEmpty()) {
            return
        }

        logLock.withLock {
            val pairsWithTimestamp = Array(pairs.size + 1) { index ->
                when (index) {
                    // Add timestamp
                    0 -> "timestamp" to LocalDateTime.now().format(DateTimeFormatter.ISO_DATE_TIME)

                    // Add remaining fields
                    else -> pairs[index - 1]
                }
            }

            val longestName = pairsWithTimestamp.maxOf { it.first.length + 1 }
            pairsWithTimestamp.forEach { (name, value) ->
                println(
                    buildString {
                        append(name)
                        append(":")

                        var remaining = longestName - name.length + 1
                        while (remaining > 0) {
                            append(' ')
                            remaining--
                        }

                        append(value)
                    }
                )
            }

            println()
        }
    }
}