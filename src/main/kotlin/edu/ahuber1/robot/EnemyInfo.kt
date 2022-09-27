package edu.ahuber1.robot

import edu.ahuber1.math.Point
import edu.ahuber1.robot.Constants.ENCIRCLE_COUNT_RANGE
import edu.ahuber1.robot.Constants.SAFE_DISTANCE_RANGE
import kotlin.math.ceil

internal class EnemyInfo(val name: String) {
    var location: Point? = null
    var lastAngle: Double? = null
    var rotationDirection: RotationDirection? = null
    var turnNumber = 0L
    var state: State? = null

    enum class State {
        ADVANCING,
        ENCIRCLING
    }
}