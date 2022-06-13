package edu.ahuber1.math

import edu.ahuber1.testing.testAll
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals

class DoubleExtensionsTests {
    @Test
    fun testSquared() {
        data class TestCases(val value: Double, val expected: Double)

        val testCases = generateSequence(-10.0) { it + 0.5 }
            .takeWhile { it <= 10 }
            .map { TestCases(it, it * it) }
            .toList()
            .toTypedArray()

        testCases.testAll {
            val actual = value.squared
            assertEquals(expected, actual)
        }
    }

    @Test
    fun testEqualsWithinDelta() {
        data class TestCase(val value: Double, val other: Double, val delta: Double, val expected: Boolean)

        val testCases = arrayOf(
            TestCase(0.1234, 0.1234, 1e-5, true),
            TestCase(0.1234, 0.1234, 1e-4, true),
            TestCase(0.1234, 0.1234, 1e-3, true),
            TestCase(0.1234, 0.1234, 1e-2, true),
            TestCase(0.1234, 0.1234, 1e-1, true),
            TestCase(0.1234, 0.1234, 1.0, true),

            TestCase(0.1234, 0.123, 1e-4, false),
            TestCase(0.1234, 0.12, 1e-3, false),
            TestCase(0.1234, 0.1, 1e-2, false),
            TestCase(0.1234, 0.0, 1e-1, false)
        )

        testCases.testAll {
            val actual = value.equalsWithinDelta(other, delta)
            assertEquals(expected, actual)
        }
    }
}