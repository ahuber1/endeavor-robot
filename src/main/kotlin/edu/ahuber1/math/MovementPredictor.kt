package edu.ahuber1.math

import robocode.Rules
import robocode.util.Utils
import kotlin.math.*

@Suppress("NAME_SHADOWING")
/**
 * An implementation of the "[MovementPredictor](https://robowiki.net/wiki/User:Nat/Free_code#Movement_Predictor)"
 * from Robocode in Kotlin.
 */
public object MovementPredictor {
    public data class PredictionStatus(
        val x: Double,
        val y: Double,
        val heading: Double,
        val velocity: Double,
        val time: Long
    ) {
        val location: Point = Point(x, y)
    }

    /**
     * Calculate next tick prediction status. This always simulate accelerate to
     * max velocity.
     *
     * @param status
     *            beginning status
     * @param goAngle
     *            angle to move, in radians, absolute
     * @param maxVelocity
     *            max allowed velocity of robot
     * @return predicted state next tick
     */
    public fun predict(status: PredictionStatus, goAngle: Double, maxVelocity: Double = 8.0): PredictionStatus {
        val moveDir = if (cos(goAngle - status.heading) < 0) -1 else 1
        return predictInternal(status, goAngle, maxVelocity, Double.POSITIVE_INFINITY * moveDir)
    }

    /**
     * Calculate next tick prediction status. This always simulate accelerate to
     * max velocity.
     *
     * @param status
     *            beginning status
     * @param goAngle
     *            angle to move, in radians, absolute
     * @param maxVelocity
     *            max allowed velocity of robot
     * @return predicted state next tick
     */
    public fun predict(
        status: PredictionStatus,
        goAngle: Double,
        maxVelocity: Double,
        distanceRemaining: Double
    ): List<PredictionStatus> {
        val predicted = ArrayList<PredictionStatus>(20)
        predicted.add(status)

        var status = status
        var distanceRemaining = distanceRemaining

        while (distanceRemaining > 0) {
            status = predictInternal(status, goAngle, maxVelocity, distanceRemaining)
            predicted.add(status)

            // Deduct the distance remaining by the velocity
            distanceRemaining -= status.velocity
        }

        return predicted
    }

    /**
     * Calculate predicted status for every ticks before it reach its
     * destination.
     *
     * @param status
     *            beginning status
     * @param goAngle
     *            angle to move, in radians, absolute
     * @param maxVelocity
     *            max allowed velocity of robot
     * @param distanceRemaining
     *            remain distance before stop
     * @return list of predicted status
     */
    public fun predict(
        status: PredictionStatus,
        tick: Int,
        goAngle: Double,
        maxVelocity: Double
    ): List<PredictionStatus> {
        val predicted = ArrayList<PredictionStatus>(tick + 2)
        predicted.add(status)

        var status = status
        var tick = tick

        while (tick-- > 0) {
            status = predict(status, goAngle, maxVelocity)
            predicted.add(status)
        }

        return predicted
    }

    /**
     * Calculate predicted status for every ticks until timer run out.
     *
     * @param status
     *            beginning status
     * @param tick
     *            time available to move
     * @param goAngle
     *            angle to move, in radians, absolute
     * @param maxVelocity
     *            max allowed velocity of robot
     * @return list of predicted status
     */
    public fun predict(
        status: PredictionStatus,
        tick: Int,
        goAngle: Double,
        maxVelocity: Double,
        distanceRemaining: Double
    ): List<PredictionStatus> {
        val predicted = ArrayList<PredictionStatus>(tick + 2)
        predicted.add(status)

        var status = status
        var tick = tick
        var distanceRemaining = distanceRemaining

        while (distanceRemaining > 0 && tick-- > 0) {
            status = predictInternal(status, goAngle, maxVelocity, distanceRemaining)
            predicted.add(status)

            // Deduct the distance remaining by the velocity
            distanceRemaining -= status.velocity
        }

        return predicted
    }

    private fun predictInternal(
        status: PredictionStatus,
        goAngle: Double,
        maxVelocity: Double,
        distanceRemaining: Double
    ): PredictionStatus {
        var x = status.x
        var y = status.y
        var heading = status.heading
        var velocity = status.velocity
        var goAngle = goAngle

        // goAngle here is absolute, change to relative bearing
        goAngle -= heading

        // If angle is at back, consider change in direction
        if (cos(goAngle) < 0) {
            goAngle += PI
        }

        // Normalize angle
        goAngle = Utils.normalRelativeAngle(goAngle)

        // Max turning rate, taken from Rules class
        val maxTurning = Math.toRadians(10.0 - 0.75 * velocity)
        heading += limit(-maxTurning, goAngle, maxTurning)

        // Get next velocity
        velocity = getVelocity(velocity, maxVelocity, distanceRemaining)

        // Calculate new location
        x += sin(heading) * velocity
        y += cos(heading) * velocity

        // return the prediction status
        return PredictionStatus(x, y, heading, velocity, status.time + 1)
    }

    /**
     * This function return the new velocity base on the maximum velocity and
     * distance remaining. This is copied from internal bug-fixed Robocode
     * engine.
     *
     * @param currentVelocity
     * current velocity of the robot
     * @param maxVelocity
     * maximum allowed velocity of the robot
     * @param distanceRemaining
     * the remaining distance to move
     * @return velocity for current tick
     */
    private fun getVelocity(currentVelocity: Double, maxVelocity: Double, distanceRemaining: Double): Double {
        var currentVelocity = currentVelocity
        if (distanceRemaining < 0) {
            return -getVelocity(-currentVelocity, maxVelocity, -distanceRemaining)
        }

        var newVelocity: Double
        val maxSpeed = abs(maxVelocity)
        val currentSpeed = abs(currentVelocity)

        // Check if we are decelerating, i.e. if the velocity is negative.
        // Note that if the speed is too high due to a new max. velocity, we
        // must also decelerate.
        if (currentVelocity < 0 || currentSpeed > maxSpeed) {
            // If the velocity is negative, we are decelerating
            newVelocity = currentSpeed - Rules.DECELERATION

            // Check if we are going from deceleration into acceleration
            if (newVelocity < 0) {
                // If we have decelerated to velocity = 0, then the remaining
                // time must be used for acceleration
                val decelTime = currentSpeed / Rules.DECELERATION
                val accelTime = 1 - decelTime

                // New velocity (v) = d / t, where time = 1 (i.e. 1 turn).
                // Hence, v = d / 1 => v = d
                // However, the new velocity must be limited by the max.
                // velocity
                newVelocity = doubleArrayOf(
                    maxSpeed,
                    distanceRemaining,
                    (Rules.DECELERATION * decelTime.squared) + (Rules.ACCELERATION * accelTime.squared)
                ).min()

                // Note: We change the sign here due to the sign check later
                // when returning the result
                currentVelocity *= -1.0
            }
        } else {
            // Else, we are not decelerating, but might need to start doing so
            // due to the remaining distance

            // Deceleration time (t) is calculated by: v = a * t => t = v / a
            val decelTime = currentSpeed / Rules.DECELERATION

            // Deceleration time (d) is calculated by: d = 1/2 a * t^2 + v0 * t
            // + t
            // Adding the extra 't' (in the end) is special for Robocode, and v0
            // is the starting velocity = 0
            val decelDist = (0.5 * Rules.DECELERATION * decelTime.squared) + decelTime

            // Check if we should start decelerating
            if (distanceRemaining <= decelDist) {
                // If the distance < max. deceleration distance, we must
                // decelerate so we hit a distance = 0

                // Calculate time left for deceleration to distance = 0
                val time = distanceRemaining / (decelTime + 1) // 1 is added
                // here due
                // to the extra 't'
                // for Robocode

                // New velocity (v) = a * t, i.e. deceleration * time, but not
                // greater than the current speed
                if (time <= 1) {
                    // When there is only one turn left (t <= 1), we set the
                    // speed to match the remaining distance
                    newVelocity = max(currentSpeed - Rules.DECELERATION, distanceRemaining)
                } else {
                    // New velocity (v) = a * t, i.e. deceleration * time
                    newVelocity = time * Rules.DECELERATION
                    if (currentSpeed < newVelocity) {
                        // If the speed is less that the new velocity we just
                        // calculated, then use the old speed instead
                        newVelocity = currentSpeed
                    } else if (currentSpeed - newVelocity > Rules.DECELERATION) {
                        // The deceleration must not exceed the max.
                        // deceleration.
                        // Hence, we limit the velocity to the speed minus the
                        // max. deceleration.
                        newVelocity = currentSpeed - Rules.DECELERATION
                    }
                }
            } else {
                // Else, we need to accelerate, but only to max. velocity
                newVelocity = min(currentSpeed + Rules.ACCELERATION, maxSpeed)
            }
        }

        // Return the new velocity with the correct sign. We have been working
        // with the speed, which is always positive
        return if (currentVelocity < 0) -newVelocity else newVelocity
    }

    private fun limit(a: Double, b: Double, c: Double): Double {
        return max(a, min(b, c))
    }
}