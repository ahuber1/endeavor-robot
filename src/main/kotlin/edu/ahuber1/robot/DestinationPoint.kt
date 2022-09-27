package edu.ahuber1.robot

import edu.ahuber1.math.Point

internal interface DestinationPoint {
    val point: Point
    val distanceFromRobot: Double
}