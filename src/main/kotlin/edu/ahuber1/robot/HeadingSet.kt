package edu.ahuber1.robot

/**
 * A container for a vertical and horizontal [Heading]
 *
 * @constructor Creates a new [HeadingSet] with the provided vertical and horizontal [Heading]
 * @param vertical The vertical [Heading]. This must be either [Heading.NORTH] or [Heading.SOUTH].
 * @param horizontal The horizontal [Heading]. This must be either [Heading.EAST] or [Heading.WEST].
 * @throws IllegalArgumentException If the vertical [Heading] is neither [Heading.NORTH] nor [Heading.SOUTH],
 *                                  or if the horizontal [Heading] is neither [Heading.EAST] nor [Heading.WEST].
 *
 * @property vertical The vertical [Heading]. This is either [Heading.NORTH] or [Heading.SOUTH].
 * @property horizontal The horizontal [Heading]. This is either [Heading.EAST] or [Heading.WEST].
 *
 * @see [Heading.isVertical]
 * @see [Heading.isHorizontal]
 */
public data class HeadingSet(val vertical: Heading, val horizontal: Heading) : Iterable<Heading> {
    init {
        require(vertical.isVertical)
        require(horizontal.isHorizontal)
    }

    /**
     * Returns a [HeadingSet] where the vertical and horizontal [Heading] are inverted.
     *
     * @see [Heading.opposite]
     */
    public inline val opposite: HeadingSet
        get() = HeadingSet(vertical.opposite, horizontal.opposite)

    /**
     * Returns `true` if [other] is equal to [vertical] or [horizontal].
     */
    public operator fun contains(other: Heading): Boolean {
        return vertical == other || horizontal == other
    }

    /**
     * Returns an [Iterator] that yields [vertical] then [horizontal].
     */
    override fun iterator(): Iterator<Heading> {
        return HeadingIterator(this)
    }

    private class HeadingIterator(private val headingSet: HeadingSet) : Iterator<Heading> {
        private var returnCount = 0

        override fun hasNext(): Boolean {
            return returnCount < 2
        }

        override fun next(): Heading {
            val heading = when(returnCount) {
                0 -> headingSet.vertical
                1 -> headingSet.horizontal
                else -> throw NoSuchElementException()
            }

            returnCount++
            return heading
        }
    }
}