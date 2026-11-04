package gnosis.keyboard.revamped

import android.content.Context
import android.content.SharedPreferences
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
            accent = Color.parseColor("#38BDF8"),
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
            name = "Slate Onyx (AMOLED)",
            isLight = false,
            background = Color.parseColor("#000000"),
            surface = Color.parseColor("#101116"),
            keyColor = Color.parseColor("#181922"),
            keyBorderColor = Color.parseColor("#292B3A"),
            specialKeyColor = Color.parseColor("#12131A"),
            textPrimary = Color.parseColor("#E6EDF3"),
            textSecondary = Color.parseColor("#7D8590"),
            accent = Color.parseColor("#22D3EE"),
            onAccentText = Color.parseColor("#000000"),
            cornerRadiusDp = 13f,
            borderWidthDp = 1.0f
        )

        /**
         * Cyber Neon Theme
         */
        val CYBER_NEON = RevampedSkin(
            id = "cyber_neon",
            name = "Cyber Neon",
            isLight = false,
            background = Color.parseColor("#060814"),
            surface = Color.parseColor("#0E1326"),
            keyColor = Color.parseColor("#141C38"),
            keyBorderColor = Color.parseColor("#22305E"),
            specialKeyColor = Color.parseColor("#0F162E"),
            textPrimary = Color.parseColor("#F0F6FC"),
            textSecondary = Color.parseColor("#38BDF8"),
            accent = Color.parseColor("#22D3EE"),
            onAccentText = Color.parseColor("#060814"),
            cornerRadiusDp = 14f,
            borderWidthDp = 1.2f
        )

        /**
         * Midnight Ocean Theme
         */
        val MIDNIGHT_OCEAN = RevampedSkin(
            id = "midnight_ocean",
            name = "Midnight Ocean",
            isLight = false,
            background = Color.parseColor("#0B132B"),
            surface = Color.parseColor("#152244"),
            keyColor = Color.parseColor("#1C2A54"),
            keyBorderColor = Color.parseColor("#2F4588"),
            specialKeyColor = Color.parseColor("#131D3B"),
            textPrimary = Color.parseColor("#FFFFFF"),
            textSecondary = Color.parseColor("#90E0EF"),
            accent = Color.parseColor("#00B4D8"),
            onAccentText = Color.parseColor("#FFFFFF"),
            cornerRadiusDp = 13f,
            borderWidthDp = 1.0f
        )

        /**
         * Sunset Ember Theme
         */
        val SUNSET_EMBER = RevampedSkin(
            id = "sunset_ember",
            name = "Sunset Ember",
            isLight = false,
            background = Color.parseColor("#140D0E"),
            surface = Color.parseColor("#221518"),
            keyColor = Color.parseColor("#2E1C20"),
            keyBorderColor = Color.parseColor("#4D2E36"),
            specialKeyColor = Color.parseColor("#1D1214"),
            textPrimary = Color.parseColor("#FFF1F2"),
            textSecondary = Color.parseColor("#FDA4AF"),
            accent = Color.parseColor("#F97316"),
            onAccentText = Color.parseColor("#FFFFFF"),
            cornerRadiusDp = 13f,
            borderWidthDp = 1.0f
        )

        /**
         * Rose Velvet Theme
         */
        val ROSE_VELVET = RevampedSkin(
            id = "rose_velvet",
            name = "Rose Velvet",
            isLight = false,
            background = Color.parseColor("#160D15"),
            surface = Color.parseColor("#261524"),
            keyColor = Color.parseColor("#331C30"),
            keyBorderColor = Color.parseColor("#572D52"),
            specialKeyColor = Color.parseColor("#20111E"),
            textPrimary = Color.parseColor("#FDF2F8"),
            textSecondary = Color.parseColor("#F472B6"),
            accent = Color.parseColor("#EC4899"),
            onAccentText = Color.parseColor("#FFFFFF"),
            cornerRadiusDp = 13f,
            borderWidthDp = 1.0f
        )

        val ALL_PRESETS = listOf(
            LUCID_VOID_DARK,
            LUCID_VOID_LIGHT,
            SLATE_ONYX,
            CYBER_NEON,
            MIDNIGHT_OCEAN,
            SUNSET_EMBER,
            ROSE_VELVET
        )

        fun findPreset(id: String): RevampedSkin {
            return ALL_PRESETS.firstOrNull { it.id == id } ?: LUCID_VOID_DARK
        }

        fun buildFromPreferences(prefs: SharedPreferences): RevampedSkin {
            val themeId = prefs.getString("pref_theme", "lucid_void_dark") ?: "lucid_void_dark"
            val basePreset = findPreset(themeId)

            val customAccent = prefs.getInt("pref_accent", basePreset.accent)
            val showBorders = prefs.getBoolean("pref_key_borders", false)
            val radius = prefs.getFloat("pref_corner_radius", basePreset.cornerRadiusDp)
            val height = prefs.getInt("pref_keyboard_height", basePreset.keyHeightDp)
            val fontScale = prefs.getFloat("pref_font_scale", basePreset.fontScale)

            return basePreset.copy(
                accent = customAccent,
                borderWidthDp = if (showBorders) 1.2f else 0f,
                cornerRadiusDp = radius,
                keyHeightDp = height,
                fontScale = fontScale
            )
        }
    }
}
