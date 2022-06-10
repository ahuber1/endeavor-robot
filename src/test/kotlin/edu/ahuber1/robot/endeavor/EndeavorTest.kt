package edu.ahuber1.robot.endeavor

import org.junit.jupiter.api.Test

import org.junit.jupiter.api.Assertions.*

internal class EndeavorTest {
    @Test
    fun calculateInitialTurnDirection() {
        data class TestCases(val initialState: Endeavor.InitialState, val expected: Direction)

        val upperLeftRobotLocation = Point(-10.0, 10.0)
        val upperRightRobotLocation = Point(10.0, 10.0)
        val bottomLeftRobotLocation = Point(-10.0, -10.0)
        val bottomRightRobotLocation = Point(10.0, -10.0)

        val testCases = arrayOf(
            // Robot is in upper left
            TestCases(Endeavor.InitialState(Point(), upperLeftRobotLocation, 0.0), Direction.LEFT),
            TestCases(Endeavor.InitialState(Point(), upperLeftRobotLocation, Radians.degrees180), Direction.RIGHT),

            // Robot is in upper right
            TestCases(Endeavor.InitialState(Point(), upperRightRobotLocation, 0.0), Direction.LEFT),
            TestCases(Endeavor.InitialState(Point(), upperRightRobotLocation, Radians.degrees180), Direction.RIGHT),

            // Robot is in bottom left
            TestCases(Endeavor.InitialState(Point(), bottomLeftRobotLocation, 0.0), Direction.RIGHT),
            TestCases(Endeavor.InitialState(Point(), bottomLeftRobotLocation, Radians.degrees180), Direction.LEFT),

            // Robot is in bottom right
            TestCases(Endeavor.InitialState(Point(), bottomRightRobotLocation, 0.0), Direction.RIGHT),
            TestCases(Endeavor.InitialState(Point(), bottomRightRobotLocation, Radians.degrees180), Direction.LEFT),
        )

        testCases.testAll {
            val actual = Endeavor.calculateInitialTurnDirection(initialState)
            assertEquals(expected, actual)
        }
    }
}