package gnosis.keyboard.revamped

import android.content.Context
import android.graphics.Color
import android.graphics.Typeface
import android.graphics.drawable.Drawable
import android.graphics.drawable.GradientDrawable
import android.graphics.drawable.RippleDrawable
import android.content.res.ColorStateList
import androidx.core.graphics.ColorUtils

/**
 * RevampedSkin: Defines all colors, dimensions, typography, and drawable factories
 * for modern keycaps, glassmorphism, and action buttons.
 */
data class RevampedSkin(
    val id: String,
    val name: String,
    val isLight: Boolean,
    val background: Int,
    val surface: Int,
    val keyColor: Int,
    val keyBorderColor: Int,
    val specialKeyColor: Int,
    val textPrimary: Int,
    val textSecondary: Int,
    val accent: Int,
    val onAccentText: Int,
    val cornerRadiusDp: Float = 13f,
    val borderWidthDp: Float = 1.0f,
    val keySpacingHDp: Float = 3f,
    val keySpacingVDp: Float = 6f,
    val keyHeightDp: Int = 48,
    val fontScale: Float = 0.85f,
    val fontBold: Boolean = false,
    val typeface: Typeface = Typeface.DEFAULT
) {
    val primaryText: Int get() = textPrimary
    val secondaryText: Int get() = textSecondary

    /**
     * Builds the StateListDrawable (or RippleDrawable on API 21+) for standard or special keycaps.
     */
    fun buildKeyDrawable(density: Float, special: Boolean = false, isAction: Boolean = false): Drawable {
        val cornerPx = cornerRadiusDp * density
        val borderPx = (borderWidthDp * density).toInt()

        val baseColor = when {
            isAction -> accent
            special -> specialKeyColor
            else -> keyColor
        }

        val pressedColor = when {
            isAction -> ColorUtils.blendARGB(accent, Color.WHITE, 0.25f)
            isLight -> ColorUtils.blendARGB(baseColor, Color.BLACK, 0.15f)
            else -> ColorUtils.blendARGB(baseColor, Color.WHITE, 0.20f)
        }

        fun createShape(color: Int): GradientDrawable {
            return GradientDrawable().apply {
                shape = GradientDrawable.RECTANGLE
                cornerRadius = cornerPx
                setColor(color)
                if (borderPx > 0 && !isAction) {
                    setStroke(borderPx, keyBorderColor)
                }
            }
        }

        val normalDrawable = createShape(baseColor)
        val pressedDrawable = createShape(pressedColor)

        // Native Material ripple effect
        val rippleColor = ColorStateList.valueOf(
            if (isAction) ColorUtils.setAlphaComponent(Color.WHITE, 70)
            else ColorUtils.setAlphaComponent(accent, 50)
        )
        return RippleDrawable(rippleColor, normalDrawable, pressedDrawable)
    }

    companion object {
        /**
         * Lucid Void Dark (Signature Revamped Dark Theme)
         */
        val LUCID_VOID_DARK = RevampedSkin(
            id = "lucid_void_dark",
            name = "Lucid Void (Dark)",
            isLight = false,
            background = Color.parseColor("#121318"),
            surface = Color.parseColor("#1C1E26"),
            keyColor = Color.parseColor("#282A34"),
            keyBorderColor = Color.parseColor("#383B4A"),
            specialKeyColor = Color.parseColor("#1E202A"),
            textPrimary = Color.parseColor("#FFFFFF"),
            textSecondary = Color.parseColor("#8E92A4"),
            accent = Color.parseColor("#3B82F6"),
            onAccentText = Color.parseColor("#FFFFFF"),
            cornerRadiusDp = 13f,
            borderWidthDp = 1.0f
        )

        /**
         * Lucid Void Light (Signature Revamped Light Theme)
         */
        val LUCID_VOID_LIGHT = RevampedSkin(
            id = "lucid_void_light",
            name = "Lucid Void (Light)",
            isLight = true,
            background = Color.parseColor("#EBECEF"),
            surface = Color.parseColor("#F5F6F8"),
            keyColor = Color.parseColor("#FFFFFF"),
            keyBorderColor = Color.parseColor("#D5D8E0"),
            specialKeyColor = Color.parseColor("#DFE2E8"),
            textPrimary = Color.parseColor("#1A1C23"),
            textSecondary = Color.parseColor("#64748B"),
            accent = Color.parseColor("#2563EB"),
            onAccentText = Color.parseColor("#FFFFFF"),
            cornerRadiusDp = 13f,
            borderWidthDp = 1.0f
        )

        /**
         * Slate / Onyx Theme (OLED Black)
         */
        val SLATE_ONYX = RevampedSkin(
            id = "slate_onyx",
            name = "Slate Onyx",
            isLight = false,
            background = Color.parseColor("#08090C"),
            surface = Color.parseColor("#12141B"),
            keyColor = Color.parseColor("#1C1E27"),
            keyBorderColor = Color.parseColor("#2E3240"),
            specialKeyColor = Color.parseColor("#151720"),
            textPrimary = Color.parseColor("#E6EDF3"),
            textSecondary = Color.parseColor("#7D8590"),
            accent = Color.parseColor("#38BDF8"),
            onAccentText = Color.parseColor("#0F172A"),
            cornerRadiusDp = 12f,
            borderWidthDp = 1.0f
        )
    }
}
