package edu.ahuber1.robot

public object Values {
    public const val SAFE_DISTANCE: Double = 100.0
    public const val ENCIRCLING_STEP_ANGLE: Double = 45.0
    public const val TANK_LENGTH: Double = 100.0
    public const val MAX_ANGLE_PROGRESSION_COUNT: Int = 1 + (360 / ENCIRCLING_STEP_ANGLE.toInt())
    public const val ENCIRCLE_COUNT: Int = 1 // TODO: Consider making this random
    public const val ENCIRCLE_POINT_COUNT: Int = MAX_ANGLE_PROGRESSION_COUNT * ENCIRCLE_COUNT // TODO: Convert to property
    public const val DISTANCE_DELTA: Double = 5.0
}