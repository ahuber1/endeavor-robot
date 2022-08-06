package edu.ahuber1.robot

public object Constants {
    public const val ENCIRCLING_STEP_ANGLE: Double = 45.0
    public const val TANK_LENGTH: Double = 100.0
    public const val DISTANCE_DELTA: Double = 5.0
    public const val ANGLE_DELTA: Double = ENCIRCLING_STEP_ANGLE / 2
    public const val FULL_ROTATION_ANGLE_COUNT: Int = 1 + (360 / ENCIRCLING_STEP_ANGLE.toInt())
    public const val ENCIRCLE_COUNT_DELTA: Double = 0.25

    public val ENCIRCLE_COUNT_RANGE: ClosedFloatingPointRange<Double> = 0.25..2.00
    public val SAFE_DISTANCE_RANGE: IntRange = 100..300
}