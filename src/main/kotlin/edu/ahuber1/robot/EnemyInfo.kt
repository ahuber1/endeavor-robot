package edu.ahuber1.robot

import edu.ahuber1.math.Point

internal class EnemyInfo(val name: String) {
    var location: Point? = null

    var lastAngle: Double? = null

    var rotationDirection: RotationDirection? = null
        private set

    var pointsRemaining = 0
        private set

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
}