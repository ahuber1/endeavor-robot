package edu.ahuber1.robot

import edu.ahuber1.math.Point
import edu.ahuber1.robot.Constants.ENCIRCLE_COUNT_RANGE
import edu.ahuber1.robot.Constants.SAFE_DISTANCE_RANGE
import kotlin.math.ceil

internal class EnemyInfo(val name: String) {
    private val encircleAmount: Double = encircleAmounts.random()

    var location: Point? = null

    var lastAngle: Double? = null

    var rotationDirection: RotationDirection? = null
        private set

    var pointsRemaining = 0
        private set

    val encirclePointCount: Int = ceil(Constants.FULL_ROTATION_ANGLE_COUNT * encircleAmount).toInt()

    fun setRotationDirection(rotationDirection: RotationDirection?, encirclePointCount: Int = 0) {
        if (rotationDirection == null) {
            this.rotationDirection = null
            this.pointsRemaining = 0
            return
        }

        require(encirclePointCount > 0) {
            "encirclePointCount must be greater than zero when rotationDirection is not null, " +
                    "but it was $encirclePointCount"
        }

        this.rotationDirection = rotationDirection
        this.pointsRemaining = encirclePointCount
    }

    fun decrementPointsRemaining(): Boolean {
        if (pointsRemaining == 0) {
            return false
        }

        pointsRemaining--
        return true
    }

    companion object {
        private val encircleAmounts: List<Double> by lazy {
            generateSequence(ENCIRCLE_COUNT_RANGE.start) { it + Constants.ENCIRCLE_COUNT_DELTA }
                .takeWhile { it < SAFE_DISTANCE_RANGE.last }
                .toList()
        }
    }

}