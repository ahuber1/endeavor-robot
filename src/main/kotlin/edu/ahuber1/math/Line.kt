package edu.ahuber1.math

public data class Line(val start: Vec, val end: Vec) {
    val slope: Double
        get() = (end.y - start.y) / (end.x - start.x)
}


