package edu.ahuber1.robot.endeavor

import robocode.ScannedRobotEvent
import robocode.TeamRobot

class Endeavor : TeamRobot() {

    private var initialTurnDirection: Direction
        get() {
            // Shortest rotational difference to the angle between robot and battlefield center

            val lastEvent = statusEvents.maxByOrNull { it.time } ?: return Direction.values().random()
            val robotLocation = Point(lastEvent.status.x, lastEvent.status.y)
            val robotHeadingRadians = lastEvent.status.radarHeadingRadians
            val testPoint = calculateCoordinateAlongHeadingRadians(robotLocation, 10.0, robotHeadingRadians)
            val battlefieldCenter = Point(battleFieldWidth / 2, battleFieldHeight / 2)
            val testCircleRadius = testPoint distanceTo battlefieldCenter
        }

    override fun run() {
        adjustGunForRobotTurn = true
        adjustRadarForGunTurn = true

//        while (true) {
//            // Example code for moving robot around.
//            ahead(100.0)
//            turnGunRight(100.0)
//            back(100.0)
//            turnGunRight(100.0)
//
//            // TODO: Design AI to move more intelligently
//        }


    }

    override fun onScannedRobot(event: ScannedRobotEvent?) {
        if (event == null || isTeammate(event.name)) {
            return
        }

        // TODO: Attack
    }

    private fun turnWeaponSystem(direction: Direction, degrees: Double = Double.POSITIVE_INFINITY) {
        when (direction) {
            Direction.LEFT -> {
                turnRadarLeft(degrees)
                turnGunLeft(degrees)
            }
            Direction.RIGHT -> {
                turnRadarRight(degrees)
                turnGunRight(degrees)
            }
        }
    }
}

enum class Direction {
    LEFT, RIGHT
}

