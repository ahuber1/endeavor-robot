package edu.ahuber1.robot

internal enum class RotationDirection { CLOCKWISE, COUNTERCLOCKWISE }

internal inline val RotationDirection.opposite: RotationDirection
    get() {
        return when (this) {
            RotationDirection.CLOCKWISE -> RotationDirection.COUNTERCLOCKWISE
            RotationDirection.COUNTERCLOCKWISE -> RotationDirection.CLOCKWISE
        }
    }