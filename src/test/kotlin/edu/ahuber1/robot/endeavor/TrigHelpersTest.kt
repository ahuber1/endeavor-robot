package edu.ahuber1.robot.endeavor

import org.junit.jupiter.api.Test

import org.junit.jupiter.api.Assertions.*

internal class TrigHelpersTest {

    @Test
    fun testCalculateCoordinateAlongHeadingRadians() {
        data class TestCase(val origin: Point, val distance: Double, val heading: Double, val expected: Point)

        val distance45Degrees = Point() distanceTo Point(5.0, 5.0)

        val testCases = arrayOf(
            TestCase(Point(), 10.0, 0.0, Point(10.0, 0.0)),
            TestCase(Point(), distance45Degrees, Radians.degrees45, Point(5.0, 5.0)),
            TestCase(Point(), 10.0, Radians.degrees90, Point(0.0, 10.0)),
            TestCase(Point(), distance45Degrees, -Radians.degrees45, Point(5.0, -5.0)),
            TestCase(Point(), 10.0, -Radians.degrees90, Point(0.0, -10.0))
        )

        testCases.testAll {
            val actual = calculateCoordinateAlongHeadingRadians(origin, distance, heading)
            val (expectedX, expectedY) = expected
            println("expected=$expected, actual=$actual")
            assertEqualsWithinDelta(expectedX, actual.x, 1e-14, "x")
            assertEqualsWithinDelta(expectedY, actual.y, 1e-14, "y")
        }
    }

    @Test
    fun testCalculateArcAngleRadians() {
        data class TestCase(val c: Point, val p1: Point, val p2: Point, val expected: Double)

        val distance45Degrees = Point() distanceTo Point(5.0, 5.0)

        val testCases = arrayOf(
            TestCase(Point(), Point(0.0, 10.0), Point(10.0, 0.0), Radians.degrees90),
            TestCase(Point(), Point(0.0, 10.0), Point(distance45Degrees, distance45Degrees), Radians.degrees45),
            TestCase(Point(), Point(0.0, 10.0), Point(-10.0, 0.0), Radians.degrees90),
            TestCase(Point(), Point(0.0, 10.0), Point(-distance45Degrees, distance45Degrees), Radians.degrees45),
        )

        testCases.testAll {
            val actual = calculateArcAngleRadians(c, p1, p2)
            assertEqualsWithinDelta(expected, actual, 1e-14)
        }
    }

    @Test
    fun testCalculateArcLengthRadians() {
        data class TestCase(val angle: Double, val radius: Double, val expected: Double)

        val radius = 10.0
        val circumference = 2 * Math.PI * radius

        val testCases = arrayOf(
            TestCase(0.0, radius, 0.0),
            TestCase(Radians.degrees45, radius, circumference / 8),
            TestCase(Radians.degrees90, radius, circumference / 4)
        )

        testCases.testAll {
            val actual = calculateArcLengthRadians(angle, radius)
            assertEquals(expected, actual)
        }
    }

    @Test
    fun testInverseRadians() {
        data class TestCase(val radians: Double, val expected: Double)

        val testCases = arrayOf(
            TestCase(0.0, Radians.degrees360),
            TestCase(Radians.degrees45, Radians.degrees315),
            TestCase(Radians.degrees90, Radians.degrees270),
            TestCase(Radians.degrees135, Radians.degrees225),
            TestCase(Radians.degrees180, Radians.degrees180),
            TestCase(Radians.degrees225, Radians.degrees135),
            TestCase(Radians.degrees270, Radians.degrees90),
            TestCase(Radians.degrees315, Radians.degrees45),
            TestCase(Radians.degrees360, 0.0),
        )

        testCases.testAll {
            val actual = inverseRadians(radians)
            assertEquals(expected, actual)
        }
    }
}