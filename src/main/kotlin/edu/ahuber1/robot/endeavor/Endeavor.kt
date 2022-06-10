package edu.ahuber1.robot.endeavor

import robocode.ScannedRobotEvent
import robocode.TeamRobot

class Endeavor : TeamRobot() {

    private val initialTurnDirection: Direction
        get() {
            // Shortest rotational difference to the angle between robot and battlefield center
            val lastEvent = statusEvents.maxByOrNull { it.time } ?: return Direction.values().random()
            val robotLocation = Point(lastEvent.status.x, lastEvent.status.y)
            val robotHeadingRadians = lastEvent.status.radarHeadingRadians
            val battlefieldCenter = Point(battleFieldWidth / 2, battleFieldHeight / 2)
            val initialState = InitialState(battlefieldCenter, robotLocation, robotHeadingRadians)
            return calculateInitialTurnDirection(initialState)
        }

    override fun run() {
        adjustGunForRobotTurn = true
        adjustRadarForGunTurn = true
        turnWeaponSystem(initialTurnDirection)

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

    internal data class InitialState(
        val battlefieldCenter: Point,
        val robotLocation: Point,
        val robotHeadingRadians: Double
    ) {
        val distance = battlefieldCenter distanceTo robotLocation
    }

    internal data class TestArc(
        val initialState: InitialState,
        val offsetRadians: Double,
        val offsetDirection: Direction
    ) {
        private val arcAngleRadians = initialState.robotHeadingRadians + offsetRadians

        val arcLength = calculateArcLengthRadians(offsetRadians, initialState.distance)

        val endPoint =
            calculateCoordinateAlongHeadingRadians(initialState.robotLocation, initialState.distance, arcAngleRadians)
    }

    companion object {

        internal fun calculateInitialTurnDirection(initialState: InitialState): Direction {
            val testPoint = calculateCoordinateAlongHeadingRadians(
                initialState.robotLocation,
                initialState.distance,
                initialState.robotHeadingRadians
            )

            val angle1 = calculateArcAngleRadians(initialState.robotLocation, testPoint, initialState.battlefieldCenter)
            val angle2 = inverseRadians(angle1)

            val testArcs = buildList {
                listOf(-angle1, -angle2).forEach { add(TestArc(initialState, it, Direction.LEFT)) }
                listOf(angle1, angle2).forEach { add(TestArc(initialState, it, Direction.RIGHT)) }
            }

            val sortedTestArcs = testArcs.sortedWith(
                compareBy(
                    { it.arcLength },
                    { it.endPoint distanceTo initialState.battlefieldCenter },
                )
            )

            return sortedTestArcs.first().offsetDirection
        }
    }
}

enum class Direction {
    LEFT, RIGHT
}

