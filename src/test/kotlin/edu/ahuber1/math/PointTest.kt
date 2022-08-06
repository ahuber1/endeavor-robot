package edu.ahuber1.math

import edu.ahuber1.testing.assertEqualsWithinDelta
import edu.ahuber1.testing.testAll
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

internal class PointTest {

    @Test
    fun testTranslate() {
        data class TestCase(val point: Point, val dx: Double, val dy: Double, val expected: Point)

        val testCases = arrayOf(
            TestCase(Point.zero, 10.0, 20.0, Point(10.0, 20.0)),
            TestCase(Point(10.0, 20.0), -10.0, -20.0, Point.zero),
        )

        testCases.testAll {
            val actual = point.translate(dx, dy)
            assertEquals(expected, actual)
        }
    }

    @Test
    fun testPlus() {
        data class TestCase(val point: Point, val other: Point, val expected: Point)

        val testCases = arrayOf(
            TestCase(Point.zero, Point(10.0, 20.0), Point(10.0, 20.0)),
            TestCase(Point(10.0, 20.0), Point(-10.0, -20.0), Point.zero),
        )

        testCases.testAll {
            val actual = point + other
            assertEquals(expected, actual)
        }
    }

    @Test
    fun testMinus() {
        data class TestCase(val point: Point, val other: Point, val expected: Point)

        val testCases = arrayOf(
            TestCase(Point.zero, Point(-10.0, -20.0), Point(10.0, 20.0)),
            TestCase(Point(10.0, 20.0), Point(10.0, 20.0), Point.zero),
        )

        testCases.testAll {
            val actual = point - other
            assertEquals(expected, actual)
        }
    }

    @Test
    fun testDistanceTo() {
        data class TestCase(val p1: Point, val p2: Point, val expected: Double)

        val testCases = arrayOf(
            TestCase(Point.zero, Point(0.0, 10.0), 10.0),
            TestCase(Point.zero, Point(10.0, 0.0), 10.0),
            TestCase(Point.zero, Point(0.0, -10.0), 10.0),
            TestCase(Point.zero, Point(-10.0, 0.0), 10.0),
            TestCase(Point.zero, Point(5.0, 5.0), 7.0710678118654755),
            TestCase(Point.zero, Point(-5.0, -5.0), 7.0710678118654755)
        )

        testCases.testAll {
            assertEquals(expected, p1 distanceTo p2)
            assertEquals(expected, p2 distanceTo p1)
        }
    }

    @Test
    fun angleRadians() {
        data class TestCase(val from: Point, val to: Point, val expected: Double)

        val testCases = arrayOf(
            TestCase(Point(-10.0, 0.0), Point(-5.0, 5.0), toRadians(45.0)),
            TestCase(Point(-10.0, 0.0), Point(0.0, 10.0), toRadians(45.0)),
            // TODO: Add additional test cases
        )

        testCases.testAll {
            val actual = Point.angleRadians( from, to)
            assertEqualsWithinDelta(expected, actual, 1e-6)
        }
    }
}