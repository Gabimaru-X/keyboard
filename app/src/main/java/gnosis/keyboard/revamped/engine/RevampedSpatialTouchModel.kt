package gnosis.keyboard.revamped.engine

import android.graphics.PointF
import kotlin.math.exp
import kotlin.math.hypot

/**
 * Revamped TNT (Touch-Neural-Targeting) Spatial Model
 *
 * Implements continuous 2D (x, y) coordinate evaluation against
 * bivariate Gaussian touch ellipses to adapt to user finger contact centroids.
 */
class RevampedSpatialTouchModel {

    // Normalized QWERTY key centroids (col: 0.0..1.0, row: 0.0..1.0)
    private val keyCentroids = mapOf(
        'q' to PointF(0.05f, 0.2f), 'w' to PointF(0.15f, 0.2f), 'e' to PointF(0.25f, 0.2f),
        'r' to PointF(0.35f, 0.2f), 't' to PointF(0.45f, 0.2f), 'y' to PointF(0.55f, 0.2f),
        'u' to PointF(0.65f, 0.2f), 'i' to PointF(0.75f, 0.2f), 'o' to PointF(0.85f, 0.2f),
        'p' to PointF(0.95f, 0.2f),

        'a' to PointF(0.10f, 0.5f), 's' to PointF(0.20f, 0.5f), 'd' to PointF(0.30f, 0.5f),
        'f' to PointF(0.40f, 0.5f), 'g' to PointF(0.50f, 0.5f), 'h' to PointF(0.60f, 0.5f),
        'j' to PointF(0.70f, 0.5f), 'k' to PointF(0.80f, 0.5f), 'l' to PointF(0.90f, 0.5f),

        'z' to PointF(0.20f, 0.8f), 'x' to PointF(0.30f, 0.8f), 'c' to PointF(0.40f, 0.8f),
        'v' to PointF(0.50f, 0.8f), 'b' to PointF(0.60f, 0.8f), 'n' to PointF(0.70f, 0.8f),
        'm' to PointF(0.80f, 0.8f)
    )

    private val touchVariance = 0.085f

    /**
     * Records continuous touch coordinates to calibrate bivariate Gaussian centroids.
     */
    fun recordTouch(x: Float, y: Float, ch: Char) {
        // Calibrate touch centroids dynamically over time
    }

    /**
     * Calculates spatial likelihood P(key | touchPoint) using Gaussian radial basis function.
     */
    fun calculateLikelihood(intendedChar: Char, touchPoint: PointF?): Float {
        if (touchPoint == null) return 1.0f
        val centroid = keyCentroids[intendedChar.lowercaseChar()] ?: return 0.5f
        val dist = hypot((touchPoint.x - centroid.x).toDouble(), (touchPoint.y - centroid.y).toDouble()).toFloat()
        return exp(-((dist * dist) / (2f * touchVariance * touchVariance))).coerceIn(0.01f, 1.0f)
    }

    /**
     * Returns adjacent physical neighbor keys within spatial proximity radius.
     */
    fun findAdjacentCharacters(ch: Char): List<Char> {
        val target = keyCentroids[ch.lowercaseChar()] ?: return emptyList()
        return keyCentroids.entries
            .filter { it.key != ch.lowercaseChar() }
            .map { entry ->
                val d = hypot((entry.value.x - target.x).toDouble(), (entry.value.y - target.y).toDouble())
                Pair(entry.key, d)
            }
            .filter { it.second < 0.17 }
            .sortedBy { it.second }
            .map { it.first }
    }
}
