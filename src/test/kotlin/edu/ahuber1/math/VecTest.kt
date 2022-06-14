package edu.ahuber1.math

import edu.ahuber1.math.Vec.Companion.distance
import edu.ahuber1.testing.assertEqualsWithinDelta
import edu.ahuber1.testing.testAll
import org.junit.jupiter.api.Test

import org.junit.jupiter.api.Assertions.*

internal class VecTest {

    @Test
    fun testTranslate() {
        data class TestCase(val point: Vec, val dx: Double, val dy: Double, val expected: Vec)

        val testCases = arrayOf(
            TestCase(Vec.zero, 10.0, 20.0, Vec(10.0, 20.0)),
            TestCase(Vec(10.0, 20.0), -10.0, -20.0, Vec.zero),
        )

        testCases.testAll {
            val actual = point.translate(dx, dy)
            assertEquals(expected, actual)
        }
    }

    @Test
    fun testPlus() {
        data class TestCase(val point: Vec, val other: Vec, val expected: Vec)

        val testCases = arrayOf(
            TestCase(Vec.zero, Vec(10.0, 20.0), Vec(10.0, 20.0)),
            TestCase(Vec(10.0, 20.0), Vec(-10.0, -20.0), Vec.zero),
        )

        testCases.testAll {
            val actual = point + other
            assertEquals(expected, actual)
        }
    }

    @Test
    fun testMinus() {
        data class TestCase(val point: Vec, val other: Vec, val expected: Vec)

        val testCases = arrayOf(
            TestCase(Vec.zero, Vec(-10.0, -20.0), Vec(10.0, 20.0)),
            TestCase(Vec(10.0, 20.0), Vec(10.0, 20.0), Vec.zero),
        )

        testCases.testAll {
            val actual = point - other
            assertEquals(expected, actual)
        }
    }

    @Test
    fun testDistance() {
        data class TestCase(val p1: Vec, val p2: Vec, val expected: Double)

        val testCases = arrayOf(
            TestCase(Vec.zero, Vec(0.0, 10.0), 10.0),
            TestCase(Vec.zero, Vec(10.0, 0.0), 10.0),
            TestCase(Vec.zero, Vec(0.0, -10.0), 10.0),
            TestCase(Vec.zero, Vec(-10.0, 0.0), 10.0),
            TestCase(Vec.zero, Vec(5.0, 5.0), 7.0710678118654755),
            TestCase(Vec.zero, Vec(-5.0, -5.0), 7.0710678118654755)
        )

        testCases.testAll {
            assertEquals(expected, distance(p1, p2))
            assertEquals(expected, distance(p2, p1))
        }
    }

    @Test
    fun angleRadians() {
        data class TestCase(val from: Vec, val to: Vec, val expected: Double)

        val testCases = arrayOf(
            TestCase(Vec(-10.0, 0.0), Vec(-5.0, 5.0), toRadians(45.0)),
            TestCase(Vec(-10.0, 0.0), Vec(0.0, 10.0), toRadians(45.0)),
            // TODO: Add additional test cases
        )

        testCases.testAll {
            val actual = Vec.angleRadians( from, to)
            assertEqualsWithinDelta(expected, actual, 1e-6)
        }
    }
}