package edu.ahuber1.robot

import edu.ahuber1.math.Point

internal data class EncirclePoint(
    override val point: Point,
    val angle: Double,
    override val distanceFromRobot: Double
) : DestinationPoint
