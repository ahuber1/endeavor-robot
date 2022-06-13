package edu.ahuber1.math

import edu.ahuber1.testing.assertEqualsWithinDelta
import edu.ahuber1.testing.testAll
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

internal class TrigonometryTest {

    @Test
    fun testCalculateCoordinateAlongHeadingRadians() {
        data class TestCase(val origin: Vec, val distance: Double, val headingRadians: Double, val expected: Vec)

        val distance45Degrees = Vec.distance(Vec(), Vec(5.0, 5.0))

        val testCases = arrayOf(
            TestCase(Vec(), 10.0, 0.0, Vec(10.0, 0.0)),
            TestCase(Vec(), distance45Degrees, toRadians(45.0), Vec(5.0, 5.0)),
            TestCase(Vec(), 10.0, toRadians(90.0), Vec(0.0, 10.0)),
            TestCase(Vec(), distance45Degrees, -toRadians(45.0), Vec(5.0, -5.0)),
            TestCase(Vec(), 10.0, -toRadians(90.0), Vec(0.0, -10.0))
        )

        testCases.testAll {
            val actual = getCoordinateAlongHeadingRadians(origin, distance, headingRadians)
            val (expectedX, expectedY) = expected
            println("expected=$expected, actual=$actual")
            assertEqualsWithinDelta(expectedX, actual.x, 1e-14, "x")
            assertEqualsWithinDelta(expectedY, actual.y, 1e-14, "y")
        }
    }

    @Test
    fun testCalculateArcAngleRadians() {
        data class TestCase(val c: Vec, val p1: Vec, val p2: Vec, val expected: Double)

        val distance45Degrees = Vec.distance(Vec(), Vec(5.0, 5.0))

        val testCases = arrayOf(
            TestCase(Vec(), Vec(0.0, 10.0), Vec(10.0, 0.0), toRadians(90.0)),
            TestCase(Vec(), Vec(0.0, 10.0), Vec(distance45Degrees, distance45Degrees), toRadians(45.0)),
            TestCase(Vec(), Vec(0.0, 10.0), Vec(-10.0, 0.0), toRadians(90.0)),
            TestCase(Vec(), Vec(0.0, 10.0), Vec(-distance45Degrees, distance45Degrees), toRadians(45.0)),
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
            TestCase(toRadians(45.0), radius, circumference / 8),
            TestCase(toRadians(90.0), radius, circumference / 4)
        )

        testCases.testAll {
            val actual = calculateArcLengthRadians(angle, radius)
            assertEquals(expected, actual)
        }
    }

    @Test
    fun testNormalizeRadians() {
        data class TestCase(val radians: Double, val expected: Double)

        val testCases = arrayOf(
            TestCase(toRadians(-45.0), toRadians(315.0)),
            TestCase(toRadians(-405.0), toRadians(315.0)),
            TestCase(0.0, 0.0),
            TestCase(toRadians(45.0), toRadians(45.0))
        )

        testCases.testAll {
            val actual = normalizeRadians(radians)
            assertEquals(expected, actual)
        }
    }

    @Test
    fun testToRadians() {
        data class TestCase(val degrees: Double, val expected: Double)

        val testCases = arrayOf(
            TestCase(-360.0, -2.0 * Math.PI),
            TestCase(-270.0, -(3 * Math.PI) / 2.0),
            TestCase(-180.0, -Math.PI),
            TestCase(-90.0, -Math.PI / 2.0),
            TestCase(-60.0, -Math.PI / 3.0),
            TestCase(-45.0, -Math.PI / 4.0),
            TestCase(-30.0, -Math.PI / 6.0),
            TestCase(0.0, 0.0),
            TestCase(30.0, Math.PI / 6.0),
            TestCase(45.0, Math.PI / 4.0),
            TestCase(60.0, Math.PI / 3.0),
            TestCase(90.0, Math.PI / 2.0),
            TestCase(180.0, Math.PI),
            TestCase(270.0, (3 * Math.PI) / 2.0),
            TestCase(360.0, 2.0 * Math.PI),
        )

        testCases.testAll {
            val actual = toRadians(degrees)
            assertEquals(expected, actual)
        }
    }
}
