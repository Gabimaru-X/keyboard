package gnosis.keyboard.revamped

import android.app.Activity
import android.content.Context
import android.graphics.Color
import android.graphics.PorterDuff
import android.graphics.Typeface
import android.graphics.drawable.GradientDrawable
import android.util.TypedValue
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.TextView

/**
 * Revamped UI Dashboard
 *
 * Category-wise main hub implementing the Gnosis Keyboard settings architecture:
 * - Pure OLED black / Light surface responsive theming
 * - Category cards for Typing, Sound & Haptics, Appearance, Languages, Clipboard, AI Core, Privacy
 * - Interactive live typing sandbox test field
 */
class RevampedUiDashboard(private val activity: Activity) {

    private val density = activity.resources.displayMetrics.density
    private fun dp(v: Float) = (v * density).toInt()

    var isDarkMode: Boolean = true
        private set

    private var onThemeChangedListener: ((Boolean) -> Unit)? = null

    fun setOnThemeChangedListener(listener: (Boolean) -> Unit) {
        this.onThemeChangedListener = listener
    }

    fun toggleTheme(): Boolean {
        isDarkMode = !isDarkMode
        onThemeChangedListener?.invoke(isDarkMode)
        return isDarkMode
    }

    fun buildDashboard(
        onOpenTyping: () -> Unit,
        onOpenSoundHaptics: () -> Unit,
        onOpenAppearance: () -> Unit,
        onOpenLanguages: () -> Unit,
        onOpenClipboard: () -> Unit,
        onOpenAiCore: () -> Unit,
        onOpenAbout: () -> Unit
    ): View {
        val bgColor = if (isDarkMode) Color.parseColor("#08090C") else Color.parseColor("#F4F4F6")
        val cardBg = if (isDarkMode) Color.parseColor("#14151C") else Color.parseColor("#FFFFFF")
        val cardBorder = if (isDarkMode) Color.parseColor("#242634") else Color.parseColor("#E5E7EB")
        val textPrimary = if (isDarkMode) Color.WHITE else Color.parseColor("#111827")
        val textSecondary = if (isDarkMode) Color.parseColor("#8E92A4") else Color.parseColor("#6B7280")
        val accentColor = if (isDarkMode) Color.parseColor("#22D3EE") else Color.parseColor("#2563EB")
        val badgeBg = if (isDarkMode) Color.parseColor("#1C2030") else Color.parseColor("#EEF2F6")

        val rootFrame = FrameLayout(activity).apply {
            layoutParams = ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT
            )
            setBackgroundColor(bgColor)
        }

        val scroll = ScrollView(activity).apply {
            layoutParams = FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT
            )
            isVerticalScrollBarEnabled = false
        }

        val content = LinearLayout(activity).apply {
            orientation = LinearLayout.VERTICAL
            layoutParams = ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            )
            setPadding(dp(16f), dp(36f), dp(16f), dp(48f))
        }

        // -------------------------------------------------------------
        // 1. Header
        // -------------------------------------------------------------
        val headerRow = LinearLayout(activity).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
            layoutParams = LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            ).apply { bottomMargin = dp(16f) }
        }

        val titleCol = LinearLayout(activity).apply {
            orientation = LinearLayout.VERTICAL
            layoutParams = LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f)
        }

        val titleTv = TextView(activity).apply {
            text = "Gnosis Revamped"
            setTextSize(TypedValue.COMPLEX_UNIT_SP, 26f)
            setTypeface(Typeface.create("sans-serif-medium", Typeface.BOLD))
            setTextColor(textPrimary)
        }
        titleCol.addView(titleTv)

        val subTv = TextView(activity).apply {
            text = "Modern Native IME Settings"
            setTextSize(TypedValue.COMPLEX_UNIT_SP, 13f)
            setTextColor(textSecondary)
        }
        titleCol.addView(subTv)

        headerRow.addView(titleCol)

        // Day/Night toggle
        val themeBtn = FrameLayout(activity).apply {
            val s = dp(42f)
            layoutParams = LinearLayout.LayoutParams(s, s)
            background = GradientDrawable().apply {
                shape = GradientDrawable.OVAL
                setColor(cardBg)
                setStroke(dp(1f), cardBorder)
            }
            val iv = ImageView(activity).apply {
                setImageResource(if (isDarkMode) R.drawable.ic_sun else R.drawable.ic_moon)
                setColorFilter(accentColor, PorterDuff.Mode.SRC_IN)
                scaleType = ImageView.ScaleType.CENTER_INSIDE
                setPadding(dp(10f), dp(10f), dp(10f), dp(10f))
            }
            addView(iv)
            setOnClickListener { toggleTheme() }
        }
        headerRow.addView(themeBtn)
        content.addView(headerRow)

        // -------------------------------------------------------------
        // 2. Interactive Live Test Input Field
        // -------------------------------------------------------------
        val testCard = LinearLayout(activity).apply {
            orientation = LinearLayout.VERTICAL
            background = GradientDrawable().apply {
                cornerRadius = dp(20f).toFloat()
                setColor(cardBg)
                setStroke(dp(1.2f), accentColor)
            }
            setPadding(dp(16f), dp(12f), dp(16f), dp(12f))
            layoutParams = LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            ).apply { bottomMargin = dp(20f) }
        }

        val testLabel = TextView(activity).apply {
            text = "LIVE KEYBOARD TEST AREA"
            setTextSize(TypedValue.COMPLEX_UNIT_SP, 10.5f)
            setTextColor(accentColor)
            setTypeface(null, Typeface.BOLD)
            letterSpacing = 0.08f
            setPadding(0, 0, 0, dp(4f))
        }
        testCard.addView(testLabel)

        val testInput = EditText(activity).apply {
            hint = "Tap here to test typing, sounds & haptics..."
            setHintTextColor(textSecondary)
            setTextColor(textPrimary)
            setTextSize(TypedValue.COMPLEX_UNIT_SP, 15f)
            background = null
            setPadding(0, dp(4f), 0, dp(4f))
        }
        testCard.addView(testInput)
        content.addView(testCard)

        // -------------------------------------------------------------
        // 3. Category Cards
        // -------------------------------------------------------------
        fun createCategoryCard(
            title: String,
            description: String,
            badge: String,
            iconRes: Int,
            onClick: () -> Unit
        ): View {
            val card = LinearLayout(activity).apply {
                orientation = LinearLayout.HORIZONTAL
                gravity = Gravity.CENTER_VERTICAL
                layoutParams = LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT
                ).apply { bottomMargin = dp(12f) }
                background = GradientDrawable().apply {
                    cornerRadius = dp(20f).toFloat()
                    setColor(cardBg)
                    setStroke(dp(1f), cardBorder)
                }
                setPadding(dp(16f), dp(16f), dp(16f), dp(16f))
                isClickable = true
                isFocusable = true
            }

            // Circular Icon badge
            val iconFrame = FrameLayout(activity).apply {
                val s = dp(44f)
                layoutParams = LinearLayout.LayoutParams(s, s).apply { marginEnd = dp(14f) }
                background = GradientDrawable().apply {
                    cornerRadius = dp(14f).toFloat()
                    setColor(badgeBg)
                    setStroke(dp(1f), accentColor)
                }
                val iv = ImageView(activity).apply {
                    setImageResource(iconRes)
                    setColorFilter(accentColor, PorterDuff.Mode.SRC_IN)
                    scaleType = ImageView.ScaleType.CENTER_INSIDE
                    setPadding(dp(10f), dp(10f), dp(10f), dp(10f))
                    layoutParams = FrameLayout.LayoutParams(FrameLayout.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.MATCH_PARENT)
                }
                addView(iv)
            }
            card.addView(iconFrame)

            // Text column
            val textCol = LinearLayout(activity).apply {
                orientation = LinearLayout.VERTICAL
                layoutParams = LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f)
            }

            val titleRow = LinearLayout(activity).apply {
                orientation = LinearLayout.HORIZONTAL
                gravity = Gravity.CENTER_VERTICAL
            }

            val cardTitle = TextView(activity).apply {
                text = title
                setTextSize(TypedValue.COMPLEX_UNIT_SP, 16f)
                setTypeface(null, Typeface.BOLD)
                setTextColor(textPrimary)
            }
            titleRow.addView(cardTitle)

            val badgeTv = TextView(activity).apply {
                text = badge
                setTextSize(TypedValue.COMPLEX_UNIT_SP, 10f)
                setTextColor(accentColor)
                setTypeface(null, Typeface.BOLD)
                setPadding(dp(6f), dp(2f), dp(6f), dp(2f))
                background = GradientDrawable().apply {
                    cornerRadius = dp(8f).toFloat()
                    setColor(badgeBg)
                }
                layoutParams = LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT).apply {
                    marginStart = dp(8f)
                }
            }
            titleRow.addView(badgeTv)
            textCol.addView(titleRow)

            val cardDesc = TextView(activity).apply {
                text = description
                setTextSize(TypedValue.COMPLEX_UNIT_SP, 12f)
                setTextColor(textSecondary)
                setPadding(0, dp(3f), 0, 0)
            }
            textCol.addView(cardDesc)
            card.addView(textCol)

            // Chevron
            val chevron = ImageView(activity).apply {
                setImageResource(R.drawable.ic_chevron_right)
                setColorFilter(textSecondary, PorterDuff.Mode.SRC_IN)
                layoutParams = LinearLayout.LayoutParams(dp(20f), dp(20f)).apply {
                    marginStart = dp(8f)
                }
            }
            card.addView(chevron)

            card.setOnClickListener { onClick() }
            return card
        }

        // Category 1: Typing & Intelligence
        content.addView(createCategoryCard(
            title = "Typing & Intelligence",
            description = "Predictions, auto-correct, grammar, gestures & spacebar",
            badge = "Smart Engine",
            iconRes = R.drawable.ic_keyboard,
            onClick = onOpenTyping
        ))

        // Category 2: Sound & Haptics
        content.addView(createCategoryCard(
            title = "Sound & Haptics",
            description = "Keypress audio, 5 sound profiles, vibration & scrub ticks",
            badge = "Audio",
            iconRes = R.drawable.ic_volume_2,
            onClick = onOpenSoundHaptics
        ))

        // Category 3: Appearance & Themes
        content.addView(createCategoryCard(
            title = "Appearance & Themes",
            description = "7 theme presets, custom accent colors, key borders & scaling",
            badge = "Design Studio",
            iconRes = R.drawable.ic_palette,
            onClick = onOpenAppearance
        ))

        // Category 4: Languages & Layouts
        content.addView(createCategoryCard(
            title = "Languages & Layouts",
            description = "Active language, keyboard layout, language switch key",
            badge = "Multilingual",
            iconRes = R.drawable.ic_globe,
            onClick = onOpenLanguages
        ))

        // Category 5: Clipboard & Quick Tools
        content.addView(createCategoryCard(
            title = "Clipboard & Quick Tools",
            description = "History tracking, auto-paste chips & clipboard manager",
            badge = "Tools",
            iconRes = R.drawable.ic_clipboard,
            onClick = onOpenClipboard
        ))

        // Category 6: Voice & AI Core
        content.addView(createCategoryCard(
            title = "Voice & AI Core",
            description = "Speech dictation, on-device AI tone rewrite & native engines",
            badge = "Neural AI",
            iconRes = R.drawable.ic_sparkles,
            onClick = onOpenAiCore
        ))

        // Category 7: Privacy, Reset & About
        content.addView(createCategoryCard(
            title = "Privacy, Reset & About",
            description = "Clear learned words, reset preferences, version & credits",
            badge = "Info",
            iconRes = R.drawable.ic_shield,
            onClick = onOpenAbout
        ))

        scroll.addView(content)
        rootFrame.addView(scroll)
        return rootFrame
    }
}
