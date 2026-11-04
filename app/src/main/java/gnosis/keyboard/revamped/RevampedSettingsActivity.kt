package gnosis.keyboard.revamped

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.graphics.Color
import android.graphics.Typeface
import android.graphics.drawable.GradientDrawable
import android.net.Uri
import android.os.Bundle
import android.util.TypedValue
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.view.animation.AccelerateDecelerateInterpolator
import android.widget.FrameLayout
import android.widget.HorizontalScrollView
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.TextView
import android.widget.Toast
import gnosis.keyboard.revamped.engine.RevampedDelightBridge
import gnosis.keyboard.revamped.engine.ai.GnosisAiCore
import gnosis.keyboard.revamped.engine.ai.ToneStyle

/**
 * RevampedSettingsActivity
 *
 * Clean professional MD3 experience:
 * 1. Bento-Grid OLED Dashboard (inspired by the provided workout UI)
 * 2. About screen (update section removed)
 * 3. Engine Tuning, Neural AI, and Theme Customization sections
 * All primary icons are vector glyphs.
 */
class RevampedSettingsActivity : Activity() {

    private data class TonePreview(val name: String, val style: ToneStyle, val sample: String)
    private data class ThemePreset(val title: String, val desc: String, val skin: RevampedSkin)

    private lateinit var rootLayout: FrameLayout
    private lateinit var contentContainer: FrameLayout
    private lateinit var prefs: SharedPreferences
    private lateinit var dashboard: RevampedUiDashboard

    private var activeScreenIndex = 0 // 0: Dashboard, 1: Engine, 2: AI, 3: Appearance, 4: About
    private var modalOverlay: FrameLayout? = null

    private val density by lazy { resources.displayMetrics.density }
    private fun dp(v: Float) = (v * density).toInt()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        prefs = getSharedPreferences("revamped_prefs", Context.MODE_PRIVATE)

        rootLayout = FrameLayout(this).apply {
            layoutParams = ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT
            )
            setBackgroundColor(Color.BLACK)
        }

        contentContainer = FrameLayout(this).apply {
            layoutParams = FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT
            )
        }
        rootLayout.addView(contentContainer)

        dashboard = RevampedUiDashboard(this)
        dashboard.setOnThemeChangedListener { isDark ->
            refreshCurrentScreen()
        }

        showScreen(0)
        setContentView(rootLayout)
    }

    private fun showScreen(index: Int) {
        activeScreenIndex = index
        contentContainer.removeAllViews()

        val view = when (index) {
            0 -> dashboard.buildDashboard(
                onOpenEngineSettings = { showScreen(1) },
                onOpenNeuralAi = { showScreen(2) },
                onOpenThemePicker = { showScreen(3) },
                onOpenQuickKeys = { showScreen(1) },
                onOpenAbout = { showScreen(4) }
            )
            1 -> buildEngineTuningScreen()
            2 -> buildNeuralAiScreen()
            3 -> buildAppearanceScreen()
            4 -> buildAboutScreen()
            else -> dashboard.buildDashboard({}, {}, {}, {}, {})
        }
        contentContainer.addView(view)
    }

    private fun refreshCurrentScreen() {
        showScreen(activeScreenIndex)
    }

    // -------------------------------------------------------------
    // 1. Engine Tuning Screen (MD3 Cards & Toggles)
    // -------------------------------------------------------------
    private fun buildEngineTuningScreen(): View {
        val isDark = dashboard.isDarkMode
        val bgColor = if (isDark) Color.BLACK else Color.parseColor("#F4F4F6")
        val cardBg = if (isDark) Color.parseColor("#15161C") else Color.WHITE
        val cardBorder = if (isDark) Color.parseColor("#242630") else Color.parseColor("#E5E7EB")
        val textPrimary = if (isDark) Color.WHITE else Color.parseColor("#111827")
        val textSecondary = if (isDark) Color.parseColor("#8E92A4") else Color.parseColor("#6B7280")

        val root = ScrollView(this).apply {
            layoutParams = ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT
            )
            setBackgroundColor(bgColor)
            isVerticalScrollBarEnabled = false
        }

        val col = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(dp(16f), dp(36f), dp(16f), dp(36f))
        }

        // Header with Back Arrow
        val header = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
            layoutParams = LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT).apply {
                bottomMargin = dp(20f)
            }
        }
        val backBtn = TextView(this).apply {
            text = "←"
            setTextSize(TypedValue.COMPLEX_UNIT_SP, 24f)
            setTextColor(textPrimary)
            setPadding(0, 0, dp(16f), 0)
            setOnClickListener { showScreen(0) }
        }
        header.addView(backBtn)

        val title = TextView(this).apply {
            text = "Engine Tuning"
            setTextSize(TypedValue.COMPLEX_UNIT_SP, 24f)
            setTypeface(Typeface.create("sans-serif-medium", Typeface.BOLD))
            setTextColor(textPrimary)
        }
        header.addView(title)
        col.addView(header)

        // Section 1: Core Architecture Card
        col.addView(createSectionHeader("CORE ARCHITECTURE", textSecondary))
        val cardArch = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            background = GradientDrawable().apply {
                cornerRadius = dp(20f).toFloat()
                setColor(cardBg)
                setStroke(dp(1f), cardBorder)
            }
            setPadding(dp(16f), dp(12f), dp(16f), dp(12f))
            layoutParams = LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT).apply {
                bottomMargin = dp(18f)
            }
        }
        cardArch.addView(createSwitchRow("Delight5 Trellis Decoder", "Viterbi Trellis search for beam candidate generation", "pref_delight5_enabled", true, textPrimary, textSecondary))
        cardArch.addView(createDivider(cardBorder))
        cardArch.addView(createSwitchRow("TNT Spatial Touch Model", "Bivariate Gaussian touch ellipse calibration", "pref_tnt_enabled", true, textPrimary, textSecondary))
        cardArch.addView(createDivider(cardBorder))
        cardArch.addView(createSwitchRow("Personal Dynamic LM", "On-device private n-gram frequency cache", "pref_dynamic_lm_enabled", true, textPrimary, textSecondary))
        cardArch.addView(createDivider(cardBorder))
        cardArch.addView(createSwitchRow("Sentence Explorer", "Syntactic grammar & homophone error correction", "pref_grammar_enabled", true, textPrimary, textSecondary))
        col.addView(cardArch)

        // Section 2: Sound & Audio Profiles
        col.addView(createSectionHeader("AUDIO PROFILES", textSecondary))
        val cardSound = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            background = GradientDrawable().apply {
                cornerRadius = dp(20f).toFloat()
                setColor(cardBg)
                setStroke(dp(1f), cardBorder)
            }
            setPadding(dp(16f), dp(12f), dp(16f), dp(12f))
            layoutParams = LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT).apply {
                bottomMargin = dp(18f)
            }
        }
        cardSound.addView(createSwitchRow("Keypress Audio", "Auditory feedback on touch down", "pref_audio_enabled", true, textPrimary, textSecondary))
        cardSound.addView(createDivider(cardBorder))

        // Sound profiles radio selector
        val profiles = listOf("Modern Click", "Mechanical Switch", "Soft Bubble", "Typewriter", "Piano Chime")
        val currentProfile = prefs.getInt("pref_sound_profile", 0)
        for (i in profiles.indices) {
            val pName = profiles[i]
            val isSelected = i == currentProfile
            val pRow = LinearLayout(this).apply {
                orientation = LinearLayout.HORIZONTAL
                gravity = Gravity.CENTER_VERTICAL
                setPadding(0, dp(10f), 0, dp(10f))
                val tv = TextView(this@RevampedSettingsActivity).apply {
                    text = pName
                    setTextSize(TypedValue.COMPLEX_UNIT_SP, 14f)
                    setTextColor(if (isSelected) Color.parseColor("#38BDF8") else textPrimary)
                    layoutParams = LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f)
                }
                addView(tv)
                val check = TextView(this@RevampedSettingsActivity).apply {
                    text = if (isSelected) "●" else "○"
                    setTextColor(if (isSelected) Color.parseColor("#38BDF8") else textSecondary)
                    setTextSize(TypedValue.COMPLEX_UNIT_SP, 16f)
                }
                addView(check)
                setOnClickListener {
                    prefs.edit().putInt("pref_sound_profile", i).apply()
                    Toast.makeText(this@RevampedSettingsActivity, "Sound Profile: $pName", Toast.LENGTH_SHORT).show()
                    showScreen(1)
                }
            }
            cardSound.addView(pRow)
            if (i < profiles.size - 1) cardSound.addView(createDivider(cardBorder))
        }
        col.addView(cardSound)

        // Section 3: Haptics
        col.addView(createSectionHeader("HAPTIC FEEDBACK", textSecondary))
        val cardHaptic = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            background = GradientDrawable().apply {
                cornerRadius = dp(20f).toFloat()
                setColor(cardBg)
                setStroke(dp(1f), cardBorder)
            }
            setPadding(dp(16f), dp(12f), dp(16f), dp(12f))
            layoutParams = LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT).apply {
                bottomMargin = dp(18f)
            }
        }
        cardHaptic.addView(createSwitchRow("Keypress Vibration", "Android 26+ VibrationEffect tactile pulse", "pref_haptic_enabled", true, textPrimary, textSecondary))
        cardHaptic.addView(createDivider(cardBorder))
        cardHaptic.addView(createSwitchRow("Spacebar Scrubbing Ticks", "Discrete haptic ticks per cursor step", "pref_scrub_haptic_enabled", true, textPrimary, textSecondary))
        col.addView(cardHaptic)

        root.addView(col)
        return root
    }

    // -------------------------------------------------------------
    // 2. Neural AI Screen (On-Device Intelligence & Rewriter)
    // -------------------------------------------------------------
    private fun buildNeuralAiScreen(): View {
        val isDark = dashboard.isDarkMode
        val bgColor = if (isDark) Color.BLACK else Color.parseColor("#F4F4F6")
        val cardBg = if (isDark) Color.parseColor("#15161C") else Color.WHITE
        val cardBorder = if (isDark) Color.parseColor("#242630") else Color.parseColor("#E5E7EB")
        val textPrimary = if (isDark) Color.WHITE else Color.parseColor("#111827")
        val textSecondary = if (isDark) Color.parseColor("#8E92A4") else Color.parseColor("#6B7280")

        val root = ScrollView(this).apply {
            layoutParams = ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT
            )
            setBackgroundColor(bgColor)
            isVerticalScrollBarEnabled = false
        }

        val col = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(dp(16f), dp(36f), dp(16f), dp(36f))
        }

        val header = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
            layoutParams = LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT).apply {
                bottomMargin = dp(20f)
            }
        }
        val backBtn = TextView(this).apply {
            text = "←"
            setTextSize(TypedValue.COMPLEX_UNIT_SP, 24f)
            setTextColor(textPrimary)
            setPadding(0, 0, dp(16f), 0)
            setOnClickListener { showScreen(0) }
        }
        header.addView(backBtn)

        val title = TextView(this).apply {
            text = "Gnosis AI Core"
            setTextSize(TypedValue.COMPLEX_UNIT_SP, 24f)
            setTypeface(Typeface.create("sans-serif-medium", Typeface.BOLD))
            setTextColor(textPrimary)
        }
        header.addView(title)
        col.addView(header)

        // AI Info Card
        val infoCard = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            background = GradientDrawable().apply {
                cornerRadius = dp(20f).toFloat()
                setColor(cardBg)
                setStroke(dp(1f), cardBorder)
            }
            setPadding(dp(18f), dp(16f), dp(18f), dp(16f))
            layoutParams = LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT).apply {
                bottomMargin = dp(18f)
            }
        }
        val badge = TextView(this).apply {
            text = "ON-DEVICE AUTONOMOUS ENGINE"
            setTextSize(TypedValue.COMPLEX_UNIT_SP, 10.5f)
            setTypeface(null, Typeface.BOLD)
            setTextColor(Color.parseColor("#38BDF8"))
            layoutParams = LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT).apply {
                bottomMargin = dp(6f)
            }
        }
        infoCard.addView(badge)

        val body = TextView(this).apply {
            text = "Gnosis AI operates 100% locally in your device's binary runtime. It provides real-time tone alterations (Formal, Casual, Concise, Emojify, Polish) and a 9-class MobileBERT intent classifier with absolute privacy and zero external cloud latency."
            setTextSize(TypedValue.COMPLEX_UNIT_SP, 13f)
            setTextColor(textSecondary)
            setLineSpacing(dp(4f).toFloat(), 1f)
        }
        infoCard.addView(body)
        col.addView(infoCard)

        // Rewriter Playground
        col.addView(createSectionHeader("TONE REWRITER PREVIEWS", textSecondary))
        val styles = listOf(
            TonePreview("Formal", ToneStyle.FORMAL, "hey can u send that doc asap"),
            TonePreview("Casual", ToneStyle.CASUAL, "I am writing to inquire regarding our appointment."),
            TonePreview("Concise", ToneStyle.CONCISE, "In order to complete the submission you will need to do this."),
            TonePreview("Emojify", ToneStyle.EMOJIFY, "let's party tonight and celebrate!"),
            TonePreview("Proofread", ToneStyle.PROOFREAD, "teh quick brown fox jumps ovr lazy dog")
        )

        for (item in styles) {
            val name = item.name
            val style = item.style
            val sample = item.sample
            val card = LinearLayout(this).apply {
                orientation = LinearLayout.VERTICAL
                background = GradientDrawable().apply {
                    cornerRadius = dp(16f).toFloat()
                    setColor(cardBg)
                    setStroke(dp(1f), cardBorder)
                }
                setPadding(dp(14f), dp(12f), dp(14f), dp(12f))
                layoutParams = LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT).apply {
                    bottomMargin = dp(10f)
                }
            }

            val titleRow = TextView(this).apply {
                text = "✨ $name"
                setTextSize(TypedValue.COMPLEX_UNIT_SP, 14f)
                setTypeface(null, Typeface.BOLD)
                setTextColor(textPrimary)
            }
            card.addView(titleRow)

            val inputTv = TextView(this).apply {
                text = "Input: \"$sample\""
                setTextSize(TypedValue.COMPLEX_UNIT_SP, 11.5f)
                setTextColor(textSecondary)
                setPadding(0, dp(4f), 0, dp(4f))
            }
            card.addView(inputTv)

            val transformed = GnosisAiCore.rewriteText(sample, style)
            val outputTv = TextView(this).apply {
                text = "Output: \"$transformed\""
                setTextSize(TypedValue.COMPLEX_UNIT_SP, 12.5f)
                setTypeface(null, Typeface.BOLD)
                setTextColor(Color.parseColor("#38BDF8"))
            }
            card.addView(outputTv)
            col.addView(card)
        }

        root.addView(col)
        return root
    }

    // -------------------------------------------------------------
    // 3. Appearance Screen (Themes & Skins)
    // -------------------------------------------------------------
    private fun buildAppearanceScreen(): View {
        val isDark = dashboard.isDarkMode
        val bgColor = if (isDark) Color.BLACK else Color.parseColor("#F4F4F6")
        val cardBg = if (isDark) Color.parseColor("#15161C") else Color.WHITE
        val cardBorder = if (isDark) Color.parseColor("#242630") else Color.parseColor("#E5E7EB")
        val textPrimary = if (isDark) Color.WHITE else Color.parseColor("#111827")
        val textSecondary = if (isDark) Color.parseColor("#8E92A4") else Color.parseColor("#6B7280")

        val root = ScrollView(this).apply {
            layoutParams = ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT
            )
            setBackgroundColor(bgColor)
            isVerticalScrollBarEnabled = false
        }

        val col = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(dp(16f), dp(36f), dp(16f), dp(36f))
        }

        val header = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
            layoutParams = LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT).apply {
                bottomMargin = dp(20f)
            }
        }
        val backBtn = TextView(this).apply {
            text = "←"
            setTextSize(TypedValue.COMPLEX_UNIT_SP, 24f)
            setTextColor(textPrimary)
            setPadding(0, 0, dp(16f), 0)
            setOnClickListener { showScreen(0) }
        }
        header.addView(backBtn)

        val title = TextView(this).apply {
            text = "Themes & Appearance"
            setTextSize(TypedValue.COMPLEX_UNIT_SP, 24f)
            setTypeface(Typeface.create("sans-serif-medium", Typeface.BOLD))
            setTextColor(textPrimary)
        }
        header.addView(title)
        col.addView(header)

        col.addView(createSectionHeader("REVAMPED THEME PRESETS", textSecondary))

        val themes = listOf(
            ThemePreset("Lucid Void Dark", "Pure OLED pitch black with vibrant neon accents", RevampedSkin.LUCID_VOID_DARK),
            ThemePreset("Lucid Void Light", "Crisp high-contrast daylight aesthetic", RevampedSkin.LUCID_VOID_LIGHT),
            ThemePreset("Slate Onyx", "Deep slate metallic with cobalt highlights", RevampedSkin.SLATE_ONYX)
        )

        val currentSkinId = prefs.getString("pref_theme_skin", "lucid_void_dark")

        for (theme in themes) {
            val skin = theme.skin
            val isCurrent = skin.name.equals(currentSkinId, ignoreCase = true)

            val card = LinearLayout(this).apply {
                orientation = LinearLayout.HORIZONTAL
                gravity = Gravity.CENTER_VERTICAL
                background = GradientDrawable().apply {
                    cornerRadius = dp(20f).toFloat()
                    setColor(skin.background)
                    setStroke(dp(if (isCurrent) 2f else 1f), if (isCurrent) Color.parseColor("#38BDF8") else cardBorder)
                }
                setPadding(dp(18f), dp(18f), dp(18f), dp(18f))
                layoutParams = LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT).apply {
                    bottomMargin = dp(14f)
                }
            }

            val textGroup = LinearLayout(this).apply {
                orientation = LinearLayout.VERTICAL
                layoutParams = LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f)
            }
            val titleTv = TextView(this).apply {
                text = theme.title
                setTextSize(TypedValue.COMPLEX_UNIT_SP, 16f)
                setTypeface(null, Typeface.BOLD)
                setTextColor(skin.primaryText)
            }
            textGroup.addView(titleTv)

            val descTv = TextView(this).apply {
                text = theme.desc
                setTextSize(TypedValue.COMPLEX_UNIT_SP, 12f)
                setTextColor(skin.secondaryText)
                setPadding(0, dp(2f), 0, 0)
            }
            textGroup.addView(descTv)
            card.addView(textGroup)

            val indicator = TextView(this).apply {
                text = if (isCurrent) "✓" else ""
                setTextSize(TypedValue.COMPLEX_UNIT_SP, 20f)
                setTypeface(null, Typeface.BOLD)
                setTextColor(Color.parseColor("#38BDF8"))
            }
            card.addView(indicator)

            card.setOnClickListener {
                prefs.edit().putString("pref_theme_skin", skin.name).apply()
                Toast.makeText(this, "Activated theme: ${theme.title}", Toast.LENGTH_SHORT).show()
                showScreen(3)
            }

            col.addView(card)
        }

        // Reset Resizer Dimensions Button
        val resetBtn = TextView(this).apply {
            text = "Reset Keyboard Dimensions"
            setTextColor(Color.WHITE)
            setTextSize(TypedValue.COMPLEX_UNIT_SP, 14f)
            setTypeface(null, Typeface.BOLD)
            gravity = Gravity.CENTER
            background = GradientDrawable().apply {
                cornerRadius = dp(24f).toFloat()
                setColor(Color.parseColor("#EF4444"))
            }
            setPadding(0, dp(14f), 0, dp(14f))
            layoutParams = LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT).apply {
                topMargin = dp(20f)
            }
            setOnClickListener {
                RevampedKeyboardResizer.saveDimensions(this@RevampedSettingsActivity, 1.0f, 1.0f)
                Toast.makeText(this@RevampedSettingsActivity, "Dimensions reset to 100% default", Toast.LENGTH_SHORT).show()
            }
        }
        col.addView(resetBtn)

        root.addView(col)
        return root
    }

    // -------------------------------------------------------------
    // 4. About & Updates Screen (Matching MeeTube Design)
    // -------------------------------------------------------------
    private fun buildAboutScreen(): View {
        val isDark = dashboard.isDarkMode
        val bgColor = if (isDark) Color.BLACK else Color.parseColor("#F4F4F6")
        val cardBg = if (isDark) Color.parseColor("#15161C") else Color.WHITE
        val cardBorder = if (isDark) Color.parseColor("#242630") else Color.parseColor("#E5E7EB")
        val textPrimary = if (isDark) Color.WHITE else Color.parseColor("#111827")
        val textSecondary = if (isDark) Color.parseColor("#8E92A4") else Color.parseColor("#6B7280")

        val root = ScrollView(this).apply {
            layoutParams = ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT
            )
            setBackgroundColor(bgColor)
            isVerticalScrollBarEnabled = false
        }

        val col = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(dp(16f), dp(36f), dp(16f), dp(36f))
        }

        // Header
        val header = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
            layoutParams = LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT).apply {
                bottomMargin = dp(16f)
            }
        }
        val backBtn = TextView(this).apply {
            text = "←"
            setTextSize(TypedValue.COMPLEX_UNIT_SP, 24f)
            setTextColor(textPrimary)
            setPadding(0, 0, dp(16f), 0)
            setOnClickListener { showScreen(0) }
        }
        header.addView(backBtn)

        val title = TextView(this).apply {
            text = "About Revamped"
            setTextSize(TypedValue.COMPLEX_UNIT_SP, 24f)
            setTypeface(Typeface.create("sans-serif-medium", Typeface.BOLD))
            setTextColor(textPrimary)
        }
        header.addView(title)
        col.addView(header)

        // 1. Terminal style code card (matching MeeTube screenshot)
        val terminalCard = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            background = GradientDrawable().apply {
                cornerRadius = dp(20f).toFloat()
                setColor(if (isDark) Color.parseColor("#0C0D11") else Color.parseColor("#1E293B"))
                setStroke(dp(1f), if (isDark) Color.parseColor("#1F222C") else Color.parseColor("#334155"))
            }
            setPadding(dp(18f), dp(18f), dp(18f), dp(18f))
            layoutParams = LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT).apply {
                bottomMargin = dp(16f)
            }
        }

        val codeText = """
            $ revamped --status
            telemetry: 0 | cloud: 0 | privacy: 100%
            // built with 35.2 MB C++ Monolith & Delight5
            // autonomous on-device neural intelligence :)
            $
        """.trimIndent()

        val codeTv = TextView(this).apply {
            text = codeText
            setTextColor(Color.parseColor("#4ADE80")) // Matrix green / terminal color
            setTextSize(TypedValue.COMPLEX_UNIT_SP, 12f)
            typeface = Typeface.MONOSPACE
            setLineSpacing(dp(3f).toFloat(), 1f)
        }
        terminalCard.addView(codeTv)
        col.addView(terminalCard)

        // 2. Coffee Button: "☕ sudo brew coffee"
        val coffeeBtn = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER
            background = GradientDrawable().apply {
                cornerRadius = dp(22f).toFloat()
                setColor(if (isDark) Color.parseColor("#1E1F27") else Color.parseColor("#F1F5F9"))
                setStroke(dp(1f), cardBorder)
            }
            setPadding(dp(16f), dp(10f), dp(16f), dp(10f))
            layoutParams = LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT).apply {
                gravity = Gravity.CENTER_HORIZONTAL
                bottomMargin = dp(18f)
            }
            setOnClickListener {
                Toast.makeText(this@RevampedSettingsActivity, "☕ Brewed! Thanks for supporting independent on-device engineering.", Toast.LENGTH_SHORT).show()
            }
        }

        val coffeeIcon = TextView(this).apply {
            text = "☕"
            setTextSize(TypedValue.COMPLEX_UNIT_SP, 14f)
            setPadding(0, 0, dp(8f), 0)
        }
        coffeeBtn.addView(coffeeIcon)

        val coffeeTv = TextView(this).apply {
            text = "sudo brew coffee"
            typeface = Typeface.MONOSPACE
            setTextSize(TypedValue.COMPLEX_UNIT_SP, 13f)
            setTextColor(textPrimary)
        }
        coffeeBtn.addView(coffeeTv)
        col.addView(coffeeBtn)

        // 3. Three round action buttons: Star, Share, Send log
        val actionsRow = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER
            layoutParams = LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT).apply {
                bottomMargin = dp(24f)
            }
        }

        fun createCircleAction(icon: String, label: String, onClick: () -> Unit) {
            val item = LinearLayout(this).apply {
                orientation = LinearLayout.VERTICAL
                gravity = Gravity.CENTER
                setPadding(dp(18f), 0, dp(18f), 0)
                setOnClickListener { onClick() }
            }

            val circle = FrameLayout(this).apply {
                val s = dp(46f)
                layoutParams = LinearLayout.LayoutParams(s, s).apply {
                    bottomMargin = dp(6f)
                }
                background = GradientDrawable().apply {
                    shape = GradientDrawable.OVAL
                    setColor(if (isDark) Color.parseColor("#1C1E26") else Color.parseColor("#E5E7EB"))
                    setStroke(dp(1f), cardBorder)
                }
                val tv = TextView(this@RevampedSettingsActivity).apply {
                    text = icon
                    textSize = 18f
                    gravity = Gravity.CENTER
                    layoutParams = FrameLayout.LayoutParams(FrameLayout.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.MATCH_PARENT)
                }
                addView(tv)
            }
            item.addView(circle)

            val labelTv = TextView(this).apply {
                text = label
                setTextSize(TypedValue.COMPLEX_UNIT_SP, 11f)
                setTextColor(textSecondary)
            }
            item.addView(labelTv)
            actionsRow.addView(item)
        }

        createCircleAction("★", "Star") {
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://github.com/Gabimaru-X/revamped_keyboard"))
            try { startActivity(intent) } catch (e: Exception) {
                Toast.makeText(this, "github.com/Gabimaru-X/revamped_keyboard", Toast.LENGTH_SHORT).show()
            }
        }

        createCircleAction("➦", "Share") {
            val shareIntent = Intent(Intent.ACTION_SEND).apply {
                type = "text/plain"
                putExtra(Intent.EXTRA_TEXT, "Check out Revamped Keyboard: 100% on-device neural keyboard with Delight5 C++ Monolith!")
            }
            startActivity(Intent.createChooser(shareIntent, "Share Revamped Keyboard"))
        }

        createCircleAction("⚙", "Send log") {
            Toast.makeText(this, "Telemetry is disabled. 0 logs captured on device.", Toast.LENGTH_SHORT).show()
        }

        col.addView(actionsRow)

        // 4. Info list – update section removed for cleaner professional surface
        val listCard = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            background = GradientDrawable().apply {
                cornerRadius = dp(20f).toFloat()
                setColor(cardBg)
                setStroke(dp(1f), cardBorder)
            }
            setPadding(dp(16f), dp(12f), dp(16f), dp(12f))
            layoutParams = LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT).apply {
                bottomMargin = dp(20f)
            }
        }

        // Version info (static, no update actions)
        val versionRow = createSimpleRow("◆", "Version", "1.0.0 Stable · On-device", textPrimary, textSecondary) {
            Toast.makeText(this, "Revamped 1.0.0 – pure on-device", Toast.LENGTH_SHORT).show()
        }
        listCard.addView(versionRow)
        listCard.addView(createDivider(cardBorder))

        // Contributors
        val contribRow = createSimpleRow("◎", "Contributors", "Thanks to all independent contributors", textPrimary, textSecondary) {
            Toast.makeText(this, "Revamped Keyboard Engineering Team", Toast.LENGTH_SHORT).show()
        }
        listCard.addView(contribRow)
        listCard.addView(createDivider(cardBorder))

        // Source Code
        val sourceRow = createSimpleRow("‹/›", "Source code", "github.com/Gabimaru-X/revamped_keyboard", textPrimary, textSecondary) {
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://github.com/Gabimaru-X/revamped_keyboard"))
            try { startActivity(intent) } catch (e: Exception) {}
        }
        listCard.addView(sourceRow)
        listCard.addView(createDivider(cardBorder))

        // License
        val licenseRow = createSimpleRow("⚖", "License", "Apache 2.0 / MIT Open Source", textPrimary, textSecondary) {
            Toast.makeText(this, "Licensed under Apache 2.0 & MIT", Toast.LENGTH_SHORT).show()
        }
        listCard.addView(licenseRow)

        col.addView(listCard)
        root.addView(col)
        return root
    }

    // -------------------------------------------------------------
    // 5. MeeTube-Style Rounded Bottom Sheet Modal
    // -------------------------------------------------------------
    private fun showUpdateBottomSheet() {
        if (modalOverlay != null) return

        val overlay = FrameLayout(this).apply {
            layoutParams = FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT
            )
            setBackgroundColor(Color.parseColor("#B3000000")) // Dimmed translucent scrim
            setOnClickListener {
                dismissUpdateBottomSheet()
            }
        }

        val sheetCard = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            background = GradientDrawable().apply {
                cornerRadii = floatArrayOf(
                    dp(28f).toFloat(), dp(28f).toFloat(),
                    dp(28f).toFloat(), dp(28f).toFloat(),
                    0f, 0f, 0f, 0f
                )
                setColor(Color.parseColor("#1C1C1E")) // Deep dark Apple/MD3 sheet color
            }
            setPadding(dp(22f), dp(12f), dp(22f), dp(24f))
            layoutParams = FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            ).apply {
                gravity = Gravity.BOTTOM
            }
            setOnClickListener { /* prevent dismissal on sheet click */ }
        }

        // Top drag handle bar
        val handle = View(this).apply {
            val w = dp(36f)
            val h = dp(4f)
            layoutParams = LinearLayout.LayoutParams(w, h).apply {
                gravity = Gravity.CENTER_HORIZONTAL
                bottomMargin = dp(18f)
            }
            background = GradientDrawable().apply {
                cornerRadius = dp(2f).toFloat()
                setColor(Color.parseColor("#48484A"))
            }
        }
        sheetCard.addView(handle)

        // App Icon + Title Header Row
        val headerRow = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
            layoutParams = LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT).apply {
                bottomMargin = dp(16f)
            }
        }

        // Icon squircle matching screenshot (red glowing heart in black circle)
        val iconView = FrameLayout(this).apply {
            val s = dp(44f)
            layoutParams = LinearLayout.LayoutParams(s, s).apply {
                marginEnd = dp(14f)
            }
            background = GradientDrawable().apply {
                shape = GradientDrawable.OVAL
                setColor(Color.parseColor("#E11D48")) // Vibrant crimson red
            }
            val heartTv = TextView(this@RevampedSettingsActivity).apply {
                text = "⚡"
                setTextSize(TypedValue.COMPLEX_UNIT_SP, 20f)
                gravity = Gravity.CENTER
                setTextColor(Color.WHITE)
                layoutParams = FrameLayout.LayoutParams(FrameLayout.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.MATCH_PARENT)
            }
            addView(heartTv)
        }
        headerRow.addView(iconView)

        val titleCol = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            layoutParams = LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f)
        }
        val sheetTitle = TextView(this).apply {
            text = "Revamped is up to date"
            setTextSize(TypedValue.COMPLEX_UNIT_SP, 17f)
            setTypeface(null, Typeface.BOLD)
            setTextColor(Color.WHITE)
        }
        titleCol.addView(sheetTitle)

        val sheetSub = TextView(this).apply {
            text = "Version 1.0.0 Stable"
            setTextSize(TypedValue.COMPLEX_UNIT_SP, 13f)
            setTextColor(Color.parseColor("#8E8E93"))
        }
        titleCol.addView(sheetSub)
        headerRow.addView(titleCol)
        sheetCard.addView(headerRow)

        // "What's new in 1.0.0" Header
        val whatsNewHeader = TextView(this).apply {
            text = "What's new in 1.0.0"
            setTextSize(TypedValue.COMPLEX_UNIT_SP, 15f)
            setTypeface(null, Typeface.BOLD)
            setTextColor(Color.WHITE)
            layoutParams = LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT).apply {
                bottomMargin = dp(10f)
            }
        }
        sheetCard.addView(whatsNewHeader)

        // Bullets list matching the visual style of MeeTube screenshot
        val bullets = listOf(
            "35.2 MB C++ Monolith: Delight5 Trellis Decoder, SODA speech, and ScaNN vector core.",
            "TNT Continuous Spatial Model dynamically adapting to touch ellipses.",
            "On-Device Gnosis AI Core: 5 tone styles, 9-class MobileBERT, 100% private.",
            "5 Auditory and tactile vibration profiles with discrete character scrub ticks.",
            "Omnitype quick toolbar, categorized emoji picker, and 8-handle dynamic resizer.",
            "Bento-Grid Dark OLED dashboard with real-time hardware calibration."
        )

        for (bullet in bullets) {
            val bulletRow = LinearLayout(this).apply {
                orientation = LinearLayout.HORIZONTAL
                layoutParams = LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT).apply {
                    bottomMargin = dp(8f)
                }
            }
            val dot = TextView(this).apply {
                text = "• "
                setTextSize(TypedValue.COMPLEX_UNIT_SP, 13f)
                setTextColor(Color.parseColor("#8E8E93"))
            }
            bulletRow.addView(dot)

            val bText = TextView(this).apply {
                text = bullet
                setTextSize(TypedValue.COMPLEX_UNIT_SP, 13f)
                setTextColor(Color.parseColor("#D1D1D6"))
                setLineSpacing(dp(2f).toFloat(), 1f)
            }
            bulletRow.addView(bText)
            sheetCard.addView(bulletRow)
        }

        // Full release notes link
        val linkTv = TextView(this).apply {
            text = "Full release notes"
            setTextSize(TypedValue.COMPLEX_UNIT_SP, 13.5f)
            setTextColor(Color.parseColor("#0A84FF")) // Apple/MD3 link blue
            layoutParams = LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT).apply {
                topMargin = dp(6f)
                bottomMargin = dp(18f)
            }
            setOnClickListener {
                Toast.makeText(this@RevampedSettingsActivity, "All release notes available on GitHub", Toast.LENGTH_SHORT).show()
            }
        }
        sheetCard.addView(linkTv)

        // Bottom Row with "Done" pill button (matching screenshot)
        val bottomRow = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.END
            layoutParams = LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
        }

        val doneBtn = TextView(this).apply {
            text = "Done"
            setTextColor(Color.BLACK)
            setTextSize(TypedValue.COMPLEX_UNIT_SP, 14.5f)
            setTypeface(null, Typeface.BOLD)
            gravity = Gravity.CENTER
            setPadding(dp(26f), dp(10f), dp(26f), dp(10f))
            background = GradientDrawable().apply {
                cornerRadius = dp(24f).toFloat()
                setColor(Color.WHITE)
            }
            setOnClickListener {
                dismissUpdateBottomSheet()
            }
        }
        bottomRow.addView(doneBtn)
        sheetCard.addView(bottomRow)

        overlay.addView(sheetCard)
        modalOverlay = overlay
        rootLayout.addView(overlay)
    }

    private fun dismissUpdateBottomSheet() {
        modalOverlay?.let {
            rootLayout.removeView(it)
            modalOverlay = null
        }
    }

    // -------------------------------------------------------------
    // Helper UI Builders
    // -------------------------------------------------------------

    private fun createSectionHeader(title: String, textColor: Int): View {
        return TextView(this).apply {
            text = title
            setTextSize(TypedValue.COMPLEX_UNIT_SP, 11f)
            setTypeface(null, Typeface.BOLD)
            setTextColor(textColor)
            letterSpacing = 0.08f
            layoutParams = LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT).apply {
                bottomMargin = dp(8f)
                marginStart = dp(4f)
            }
        }
    }

    private fun createDivider(color: Int): View {
        return View(this).apply {
            layoutParams = LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, dp(1f)).apply {
                topMargin = dp(4f)
                bottomMargin = dp(4f)
            }
            setBackgroundColor(color)
        }
    }

    /**
     * MD3 Style Switch Row matching MeeTube screenshot
     */
    private fun createSwitchRow(
        title: String,
        description: String,
        prefKey: String,
        defaultVal: Boolean,
        textPrimary: Int,
        textSecondary: Int
    ): View {
        val row = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
            setPadding(0, dp(10f), 0, dp(10f))
        }

        val textGroup = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            layoutParams = LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f)
        }

        val titleTv = TextView(this).apply {
            text = title
            setTextSize(TypedValue.COMPLEX_UNIT_SP, 14.5f)
            setTypeface(null, Typeface.BOLD)
            setTextColor(textPrimary)
        }
        textGroup.addView(titleTv)

        val descTv = TextView(this).apply {
            text = description
            setTextSize(TypedValue.COMPLEX_UNIT_SP, 11.5f)
            setTextColor(textSecondary)
            setPadding(0, dp(2f), 0, 0)
        }
        textGroup.addView(descTv)
        row.addView(textGroup)

        // MD3 Coral/Red Toggle Switch
        var isChecked = prefs.getBoolean(prefKey, defaultVal)
        val trackWidth = dp(48f)
        val trackHeight = dp(28f)
        val thumbSize = dp(22f)

        val switchView = FrameLayout(this).apply {
            layoutParams = LinearLayout.LayoutParams(trackWidth, trackHeight).apply {
                marginStart = dp(12f)
            }
        }

        val trackDrawable = GradientDrawable().apply {
            cornerRadius = (trackHeight / 2).toFloat()
        }

        val thumbView = View(this).apply {
            val lp = FrameLayout.LayoutParams(thumbSize, thumbSize).apply {
                gravity = Gravity.CENTER_VERTICAL
                marginStart = dp(3f)
            }
            layoutParams = lp
            background = GradientDrawable().apply {
                shape = GradientDrawable.OVAL
                setColor(Color.WHITE)
            }
        }

        fun updateSwitchVisuals(checked: Boolean) {
            if (checked) {
                // Vibrant coral red track (matching MeeTube screenshot)
                trackDrawable.setColor(Color.parseColor("#E11D48"))
                (thumbView.layoutParams as FrameLayout.LayoutParams).marginStart = trackWidth - thumbSize - dp(3f)
                (thumbView.background as GradientDrawable).setColor(Color.WHITE)
            } else {
                trackDrawable.setColor(Color.parseColor("#3F3F46"))
                (thumbView.layoutParams as FrameLayout.LayoutParams).marginStart = dp(3f)
                (thumbView.background as GradientDrawable).setColor(Color.parseColor("#A1A1AA"))
            }
            switchView.background = trackDrawable
            thumbView.requestLayout()
        }

        updateSwitchVisuals(isChecked)
        switchView.addView(thumbView)

        row.setOnClickListener {
            isChecked = !isChecked
            prefs.edit().putBoolean(prefKey, isChecked).apply()
            updateSwitchVisuals(isChecked)
            Toast.makeText(this, "$title: ${if (isChecked) "Enabled" else "Disabled"}", Toast.LENGTH_SHORT).show()
        }

        row.addView(switchView)
        return row
    }

    private fun createSimpleRow(
        icon: String,
        title: String,
        description: String,
        textPrimary: Int,
        textSecondary: Int,
        onClick: () -> Unit
    ): View {
        return LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
            setPadding(0, dp(12f), 0, dp(12f))

            val iconTv = TextView(this@RevampedSettingsActivity).apply {
                text = icon
                setTextSize(TypedValue.COMPLEX_UNIT_SP, 17f)
                setTextColor(textSecondary)
                setPadding(0, 0, dp(14f), 0)
            }
            addView(iconTv)

            val textGroup = LinearLayout(this@RevampedSettingsActivity).apply {
                orientation = LinearLayout.VERTICAL
                layoutParams = LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f)
            }
            val titleTv = TextView(this@RevampedSettingsActivity).apply {
                text = title
                setTextSize(TypedValue.COMPLEX_UNIT_SP, 14.5f)
                setTypeface(null, Typeface.BOLD)
                setTextColor(textPrimary)
            }
            textGroup.addView(titleTv)

            val descTv = TextView(this@RevampedSettingsActivity).apply {
                text = description
                setTextSize(TypedValue.COMPLEX_UNIT_SP, 11.5f)
                setTextColor(textSecondary)
            }
            textGroup.addView(descTv)
            addView(textGroup)

            val arrow = TextView(this@RevampedSettingsActivity).apply {
                text = "›"
                setTextSize(TypedValue.COMPLEX_UNIT_SP, 18f)
                setTextColor(textSecondary)
            }
            addView(arrow)

            setOnClickListener { onClick() }
        }
    }
}
