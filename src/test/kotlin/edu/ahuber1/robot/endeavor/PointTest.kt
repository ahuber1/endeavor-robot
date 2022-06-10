package edu.ahuber1.robot.endeavor

import org.junit.jupiter.api.Test

import org.junit.jupiter.api.Assertions.*

internal class PointTest {

    @Test
    fun offset() {
        data class TestCase(val point: Point, val dx: Double, val dy: Double, val expected: Point)

        val testCases = arrayOf(
            TestCase(Point(), 10.0, 20.0, Point(10.0, 20.0)),
            TestCase(Point(10.0, 20.0), -10.0, -20.0, Point()),
        )

        testCases.testAll {
            val actual = point.offset(dx, dy)
            assertEquals(expected, actual)
        }
    }

    @Test
    fun distanceTo() {
        data class TestCase(val p1: Point, val p2: Point, val expected: Double)

        val testCases = arrayOf(
            TestCase(Point(), Point(0.0, 10.0), 10.0),
            TestCase(Point(), Point(10.0, 0.0), 10.0),
            TestCase(Point(), Point(0.0, -10.0), 10.0),
            TestCase(Point(), Point(-10.0, 0.0), 10.0),
            TestCase(Point(), Point(5.0, 5.0), 7.0710678118654755),
            TestCase(Point(), Point(-5.0, -5.0), 7.0710678118654755)
        )

        testCases.testAll {
            assertEquals(expected, p1 distanceTo p2)
            assertEquals(expected, p2 distanceTo p1)
        }
    }

    @Test
    fun isLeftOf() {
        data class TestCase(val p1: Point, val p2: Point, val expected: Boolean)

        val testCases = arrayOf(
            TestCase(Point(), Point(10.0, 0.0), true),
            TestCase(Point(), Point(0.0, 10.0), false),
            TestCase(Point(), Point(-10.0, 0.0), false),
            TestCase(Point(), Point(0.0, -10.0), false),
            TestCase(Point(), Point(), false)
        )

        testCases.testAll {
            val actual = p1.isLeftOf(p2)
            assertEquals(expected, actual)
        }
    }

    @Test
    fun midpoint() {
        val point1 = Point(-10.0, -10.0)
        val point2 = Point(10.0, 10.0)
        val expected = Point()
        val actual = Point.midpoint(point1, point2)
        assertEquals(expected, actual)
    }
}