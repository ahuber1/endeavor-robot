package edu.ahuber1.testing

import edu.ahuber1.math.equalsWithinDelta
import org.junit.jupiter.api.fail
import kotlin.math.abs

inline fun <T> Array<T>.testAll(block: (T.() -> Unit)) {
    this.forEachIndexed { index, testCase ->
        println("Running test ${index + 1} of ${this.size} -> $testCase")
        testCase.block()
    }
}

fun assertEqualsWithinDelta(expected: Double, actual: Double, delta: Double, description: String = "") {
    if (expected.equalsWithinDelta(actual, delta)) {
        return
    }

    fun Double.toPlainString(): String = this.toBigDecimal().toPlainString()

    fail {
        buildString {
            val expectedString = expected.toPlainString()
            val actualString = actual.toPlainString()
            val deltaString = delta.toPlainString()

            val difference = abs(expected - actual)
            val differenceString = difference.toPlainString()

            appendLine("The value $expectedString does not equal $actualString within a delta of $deltaString")
            appendLine("    Expected:    $expectedString")
            appendLine("    Actual:      $actualString")
            appendLine("    Difference:  $differenceString")
            appendLine("    Delta:       $deltaString")
            appendLine("    Description: \"$description\"")
        }
    }
}
