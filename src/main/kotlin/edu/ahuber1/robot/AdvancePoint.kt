package edu.ahuber1.robot

import edu.ahuber1.math.Point

internal data class AdvancePoint(override val point: Point, override val distanceFromRobot: Double): DestinationPoint
