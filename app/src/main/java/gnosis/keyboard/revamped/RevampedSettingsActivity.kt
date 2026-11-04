package gnosis.keyboard.revamped

import android.app.Activity
import android.app.AlertDialog
import android.content.Context
import android.content.SharedPreferences
import android.graphics.Color
import android.graphics.PorterDuff
import android.graphics.Typeface
import android.graphics.drawable.GradientDrawable
import android.os.Bundle
import android.util.TypedValue
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.SeekBar
import android.widget.TextView
import android.widget.Toast
import gnosis.keyboard.revamped.engine.RevampedDelightBridge
import gnosis.keyboard.revamped.engine.ai.GnosisAiCore
import gnosis.keyboard.revamped.engine.ai.ToneStyle

/**
 * RevampedSettingsActivity
 *
 * Full category-wise settings implementation for Gnosis Revamped Keyboard:
 * 0: Dashboard (Main Categories Hub & Live Test Area)
 * 1: Typing & Intelligence
 * 2: Sound & Haptics
 * 3: Appearance & Themes
 * 4: Languages & Layouts
 * 5: Clipboard & Quick Tools
 * 6: Voice & AI Core
 * 7: Privacy, Reset & About
 */
class RevampedSettingsActivity : Activity() {

    private lateinit var rootLayout: FrameLayout
    private lateinit var contentContainer: FrameLayout
    private lateinit var prefs: SharedPreferences
    private lateinit var dashboard: RevampedUiDashboard

    private var activeScreenIndex = 0

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
                FrameLayout.LayoutParams.MATCH_PARENT,
                FrameLayout.LayoutParams.MATCH_PARENT
            )
        }
        rootLayout.addView(contentContainer)

        dashboard = RevampedUiDashboard(this)
        dashboard.setOnThemeChangedListener {
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
                onOpenTyping = { showScreen(1) },
                onOpenSoundHaptics = { showScreen(2) },
                onOpenAppearance = { showScreen(3) },
                onOpenLanguages = { showScreen(4) },
                onOpenClipboard = { showScreen(5) },
                onOpenAiCore = { showScreen(6) },
                onOpenAbout = { showScreen(7) }
            )
            1 -> buildTypingScreen()
            2 -> buildSoundHapticsScreen()
            3 -> buildAppearanceScreen()
            4 -> buildLanguagesScreen()
            5 -> buildClipboardScreen()
            6 -> buildAiCoreScreen()
            7 -> buildAboutScreen()
            else -> dashboard.buildDashboard({}, {}, {}, {}, {}, {}, {})
        }
        contentContainer.addView(view)
    }

    private fun refreshCurrentScreen() {
        showScreen(activeScreenIndex)
    }

    // -------------------------------------------------------------
    // Screen 1: Typing & Intelligence
    // -------------------------------------------------------------
    private fun buildTypingScreen(): View {
        val (bgColor, cardBg, cardBorder, textPrimary, textSecondary, accentColor) = getThemeColors()

        val root = createScrollContainer(bgColor)
        val col = createContentColumn()

        col.addView(createScreenHeader("Typing & Intelligence", textPrimary))

        // Section 1: Predictions & Correction
        col.addView(createSectionHeader("PREDICTIONS & CORRECTION", textSecondary))
        val cardPred = createCardContainer(cardBg, cardBorder)
        cardPred.addView(createSwitchRow("Next-Word Prediction", "Multi-engine on-device beam candidate search", "pref_predictions_enabled", true, textPrimary, textSecondary, accentColor))
        cardPred.addView(createDivider(cardBorder))
        cardPred.addView(createSwitchRow("Auto-Correction on Space", "Replace typos automatically upon spacebar press", "pref_autocorrect_enabled", false, textPrimary, textSecondary, accentColor))
        cardPred.addView(createDivider(cardBorder))
        cardPred.addView(createSwitchRow("Sentence Explorer Grammar", "Syntactic grammar fixes & homophone error correction", "pref_grammar_enabled", true, textPrimary, textSecondary, accentColor))
        cardPred.addView(createDivider(cardBorder))
        cardPred.addView(createSwitchRow("Hinglish Suggestions", "Bilingual Hinglish word bank & transliterations", "pref_hinglish_enabled", true, textPrimary, textSecondary, accentColor))
        col.addView(cardPred)

        // Section 2: Gestures & Mechanics
        col.addView(createSectionHeader("GESTURES & MECHANICS", textSecondary))
        val cardGestures = createCardContainer(cardBg, cardBorder)
        cardGestures.addView(createSwitchRow("Glide / Gesture Typing", "Swipe across letters seamlessly to form words", "pref_glide_enabled", true, textPrimary, textSecondary, accentColor))
        cardGestures.addView(createDivider(cardBorder))
        cardGestures.addView(createSwitchRow("Gesture Delete", "Swipe left on backspace key to delete whole words", "pref_gesture_delete", true, textPrimary, textSecondary, accentColor))
        cardGestures.addView(createDivider(cardBorder))
        cardGestures.addView(createSwitchRow("Spacebar Cursor Control", "Slide finger horizontally across spacebar to move cursor", "pref_space_cursor", true, textPrimary, textSecondary, accentColor))
        cardGestures.addView(createDivider(cardBorder))
        cardGestures.addView(createSwitchRow("Double-Space Period", "Quickly double-tapping spacebar automatically inserts a period and space", "pref_double_space_period", true, textPrimary, textSecondary, accentColor))
        cardGestures.addView(createDivider(cardBorder))
        cardGestures.addView(createSwitchRow("Auto-Capitalization", "Capitalize the first letter of sentences automatically", "pref_autocap", true, textPrimary, textSecondary, accentColor))
        col.addView(cardGestures)

        // Section 3: Architecture Tuning
        col.addView(createSectionHeader("CORE ARCHITECTURE", textSecondary))
        val cardArch = createCardContainer(cardBg, cardBorder)
        cardArch.addView(createSwitchRow("Delight5 Trellis Decoder", "Viterbi Trellis search for beam candidate generation", "pref_delight5_enabled", true, textPrimary, textSecondary, accentColor))
        cardArch.addView(createDivider(cardBorder))
        cardArch.addView(createSwitchRow("TNT Spatial Touch Model", "Bivariate Gaussian touch ellipse calibration", "pref_tnt_enabled", true, textPrimary, textSecondary, accentColor))
        cardArch.addView(createDivider(cardBorder))
        cardArch.addView(createSwitchRow("Personal Dynamic LM", "On-device private n-gram frequency cache", "pref_dynamic_lm_enabled", true, textPrimary, textSecondary, accentColor))
        col.addView(cardArch)

        root.addView(col)
        return root
    }

    // -------------------------------------------------------------
    // Screen 2: Sound & Haptics
    // -------------------------------------------------------------
    private fun buildSoundHapticsScreen(): View {
        val (bgColor, cardBg, cardBorder, textPrimary, textSecondary, accentColor) = getThemeColors()

        val root = createScrollContainer(bgColor)
        val col = createContentColumn()

        col.addView(createScreenHeader("Sound & Haptics", textPrimary))

        // Section 1: Audio Feedback
        col.addView(createSectionHeader("KEYPRESS AUDIO", textSecondary))
        val cardAudio = createCardContainer(cardBg, cardBorder)
        cardAudio.addView(createSwitchRow("Keypress Audio", "Sub-millisecond auditory feedback on key touch", "pref_audio_enabled", true, textPrimary, textSecondary, accentColor))
        cardAudio.addView(createDivider(cardBorder))

        // Sound profiles radio
        val profiles = listOf("Modern Click", "Mechanical Switch", "Soft Bubble Pop", "Classic Typewriter", "Piano Chime")
        val curProfile = prefs.getInt("pref_sound_profile", 0)
        for (i in profiles.indices) {
            val pName = profiles[i]
            val isSelected = i == curProfile
            val row = createRadioRow(pName, isSelected, textPrimary, textSecondary, accentColor) {
                prefs.edit().putInt("pref_sound_profile", i).apply()
                Toast.makeText(this, "Sound Profile: $pName", Toast.LENGTH_SHORT).show()
                refreshCurrentScreen()
            }
            cardAudio.addView(row)
            if (i < profiles.size - 1) cardAudio.addView(createDivider(cardBorder))
        }
        cardAudio.addView(createDivider(cardBorder))

        // Volume slider
        val curVol = (prefs.getFloat("pref_sound_volume", 0.8f) * 100).toInt()
        cardAudio.addView(createSliderRow(
            title = "Sound Volume",
            valueText = "$curVol%",
            progress = curVol,
            max = 100,
            textPrimary = textPrimary,
            textSecondary = textSecondary,
            accentColor = accentColor,
            onProgressChanged = { v ->
                prefs.edit().putFloat("pref_sound_volume", v / 100f).apply()
            }
        ))
        col.addView(cardAudio)

        // Section 2: Haptic Feedback
        col.addView(createSectionHeader("TACTILE HAPTICS", textSecondary))
        val cardHaptic = createCardContainer(cardBg, cardBorder)
        cardHaptic.addView(createSwitchRow("Keypress Vibration", "VibrationEffect tactile pulse on key down", "pref_haptic_enabled", true, textPrimary, textSecondary, accentColor))
        cardHaptic.addView(createDivider(cardBorder))

        // Intensity slider
        val curDur = prefs.getInt("pref_haptic_intensity", 18)
        cardHaptic.addView(createSliderRow(
            title = "Vibration Duration",
            valueText = "${curDur}ms",
            progress = curDur,
            max = 60,
            textPrimary = textPrimary,
            textSecondary = textSecondary,
            accentColor = accentColor,
            onProgressChanged = { v ->
                prefs.edit().putInt("pref_haptic_intensity", Math.max(5, v)).apply()
            }
        ))
        cardHaptic.addView(createDivider(cardBorder))
        cardHaptic.addView(createSwitchRow("Spacebar Scrubbing Ticks", "Discrete haptic ticks per cursor character step", "pref_scrub_haptic_enabled", true, textPrimary, textSecondary, accentColor))
        col.addView(cardHaptic)

        root.addView(col)
        return root
    }

    // -------------------------------------------------------------
    // Screen 3: Appearance & Themes
    // -------------------------------------------------------------
    private fun buildAppearanceScreen(): View {
        val (bgColor, cardBg, cardBorder, textPrimary, textSecondary, accentColor) = getThemeColors()

        val root = createScrollContainer(bgColor)
        val col = createContentColumn()

        col.addView(createScreenHeader("Appearance & Themes", textPrimary))

        // Section 1: Theme Presets
        col.addView(createSectionHeader("THEME PRESETS", textSecondary))
        val cardThemes = createCardContainer(cardBg, cardBorder)
        val curTheme = prefs.getString("pref_theme", "lucid_void_dark") ?: "lucid_void_dark"

        for ((idx, skinPreset) in RevampedSkin.ALL_PRESETS.withIndex()) {
            val isSelected = skinPreset.id == curTheme
            val row = LinearLayout(this).apply {
                orientation = LinearLayout.HORIZONTAL
                gravity = Gravity.CENTER_VERTICAL
                setPadding(0, dp(12f), 0, dp(12f))
                isClickable = true
                isFocusable = true

                // Swatch preview circle
                val swatch = View(this@RevampedSettingsActivity).apply {
                    val s = dp(28f)
                    layoutParams = LinearLayout.LayoutParams(s, s).apply { marginEnd = dp(12f) }
                    background = GradientDrawable().apply {
                        shape = GradientDrawable.OVAL
                        setColor(skinPreset.background)
                        setStroke(dp(2f), skinPreset.accent)
                    }
                }
                addView(swatch)

                val nameTv = TextView(this@RevampedSettingsActivity).apply {
                    text = skinPreset.name
                    setTextSize(TypedValue.COMPLEX_UNIT_SP, 15f)
                    setTypeface(null, if (isSelected) Typeface.BOLD else Typeface.NORMAL)
                    setTextColor(if (isSelected) accentColor else textPrimary)
                    layoutParams = LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f)
                }
                addView(nameTv)

                val check = TextView(this@RevampedSettingsActivity).apply {
                    text = if (isSelected) "●" else "○"
                    setTextColor(if (isSelected) accentColor else textSecondary)
                    setTextSize(TypedValue.COMPLEX_UNIT_SP, 18f)
                }
                addView(check)

                setOnClickListener {
                    prefs.edit().putString("pref_theme", skinPreset.id).apply()
                    Toast.makeText(this@RevampedSettingsActivity, "Theme applied: ${skinPreset.name}", Toast.LENGTH_SHORT).show()
                    refreshCurrentScreen()
                }
            }
            cardThemes.addView(row)
            if (idx < RevampedSkin.ALL_PRESETS.size - 1) cardThemes.addView(createDivider(cardBorder))
        }
        col.addView(cardThemes)

        // Section 2: Accent Palette
        col.addView(createSectionHeader("ACCENT PALETTE", textSecondary))
        val cardAccents = createCardContainer(cardBg, cardBorder)
        val accents = listOf(
            "Cyan" to Color.parseColor("#22D3EE"),
            "Electric Blue" to Color.parseColor("#3B82F6"),
            "Emerald Green" to Color.parseColor("#10B981"),
            "Sunset Coral" to Color.parseColor("#F97316"),
            "Neon Purple" to Color.parseColor("#A855F7"),
            "Hot Pink" to Color.parseColor("#EC4899"),
            "Amber Gold" to Color.parseColor("#F59E0B"),
            "Pure White" to Color.parseColor("#FFFFFF")
        )
        val curAccent = prefs.getInt("pref_accent", Color.parseColor("#22D3EE"))

        val paletteRow = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER
            setPadding(0, dp(8f), 0, dp(8f))
        }
        for ((name, colorVal) in accents) {
            val isSelected = colorVal == curAccent
            val dot = FrameLayout(this).apply {
                val s = dp(34f)
                layoutParams = LinearLayout.LayoutParams(s, s).apply {
                    setMargins(dp(4f), 0, dp(4f), 0)
                }
                background = GradientDrawable().apply {
                    shape = GradientDrawable.OVAL
                    setColor(colorVal)
                    if (isSelected) {
                        setStroke(dp(3f), Color.WHITE)
                    }
                }
                setOnClickListener {
                    prefs.edit().putInt("pref_accent", colorVal).apply()
                    Toast.makeText(this@RevampedSettingsActivity, "Accent: $name", Toast.LENGTH_SHORT).show()
                    refreshCurrentScreen()
                }
            }
            paletteRow.addView(dot)
        }
        cardAccents.addView(paletteRow)
        col.addView(cardAccents)

        // Section 3: Keyboard Layout & Scaling
        col.addView(createSectionHeader("KEYBOARD LAYOUT & SCALING", textSecondary))
        val cardLayout = createCardContainer(cardBg, cardBorder)
        cardLayout.addView(createSwitchRow("Dedicated Number Row", "Display 1234567890 digits permanently on top row", "pref_number_row", true, textPrimary, textSecondary, accentColor))
        cardLayout.addView(createDivider(cardBorder))
        cardLayout.addView(createSwitchRow("Show Key Borders", "Display subtle sleek keycap stroke borders", "pref_key_borders", false, textPrimary, textSecondary, accentColor))
        cardLayout.addView(createDivider(cardBorder))

        // Corner radius slider
        val curRadius = prefs.getFloat("pref_corner_radius", 13f).toInt()
        cardLayout.addView(createSliderRow(
            title = "Key Corner Radius",
            valueText = "${curRadius}dp",
            progress = curRadius,
            max = 24,
            textPrimary = textPrimary,
            textSecondary = textSecondary,
            accentColor = accentColor,
            onProgressChanged = { v ->
                prefs.edit().putFloat("pref_corner_radius", Math.max(6, v).toFloat()).apply()
            }
        ))
        cardLayout.addView(createDivider(cardBorder))

        // Height slider
        val curHeight = prefs.getInt("pref_keyboard_height", 48)
        cardLayout.addView(createSliderRow(
            title = "Keyboard Height",
            valueText = "${curHeight}dp",
            progress = curHeight,
            max = 64,
            textPrimary = textPrimary,
            textSecondary = textSecondary,
            accentColor = accentColor,
            onProgressChanged = { v ->
                prefs.edit().putInt("pref_keyboard_height", Math.max(38, v)).apply()
            }
        ))
        cardLayout.addView(createDivider(cardBorder))

        // Font scale slider
        val curScale = (prefs.getFloat("pref_font_scale", 0.85f) * 100).toInt()
        cardLayout.addView(createSliderRow(
            title = "Key Font Scale",
            valueText = "$curScale%",
            progress = curScale,
            max = 120,
            textPrimary = textPrimary,
            textSecondary = textSecondary,
            accentColor = accentColor,
            onProgressChanged = { v ->
                prefs.edit().putFloat("pref_font_scale", Math.max(70, v) / 100f).apply()
            }
        ))
        cardLayout.addView(createDivider(cardBorder))
        cardLayout.addView(createSwitchRow("Keypress Character Popup", "Show enlarged character preview bubble on touch", "pref_key_popups", true, textPrimary, textSecondary, accentColor))
        col.addView(cardLayout)

        root.addView(col)
        return root
    }

    // -------------------------------------------------------------
    // Screen 4: Languages & Layouts
    // -------------------------------------------------------------
    private fun buildLanguagesScreen(): View {
        val (bgColor, cardBg, cardBorder, textPrimary, textSecondary, accentColor) = getThemeColors()

        val root = createScrollContainer(bgColor)
        val col = createContentColumn()

        col.addView(createScreenHeader("Languages & Layouts", textPrimary))

        // Section 1: Active Language
        col.addView(createSectionHeader("ACTIVE LANGUAGE", textSecondary))
        val cardLang = createCardContainer(cardBg, cardBorder)
        val languages = listOf(
            "en" to "English (US)",
            "en_gb" to "English (UK)",
            "hinglish" to "Hinglish (Chat English)",
            "hi" to "Hindi (Devanagari)",
            "es" to "Spanish (Español)",
            "fr" to "French (Français)",
            "de" to "German (Deutsch)"
        )
        val curLang = prefs.getString("pref_language", "en") ?: "en"
        for ((idx, pair) in languages.withIndex()) {
            val isSelected = pair.first == curLang
            val row = createRadioRow(pair.second, isSelected, textPrimary, textSecondary, accentColor) {
                prefs.edit().putString("pref_language", pair.first).apply()
                Toast.makeText(this, "Language: ${pair.second}", Toast.LENGTH_SHORT).show()
                refreshCurrentScreen()
            }
            cardLang.addView(row)
            if (idx < languages.size - 1) cardLang.addView(createDivider(cardBorder))
        }
        col.addView(cardLang)

        // Section 2: Keyboard Layout
        col.addView(createSectionHeader("KEYBOARD LAYOUT", textSecondary))
        val cardLayout = createCardContainer(cardBg, cardBorder)
        val layouts = listOf("QWERTY", "QWERTZ", "AZERTY", "DVORAK", "Devanagari Native")
        val curLayout = prefs.getString("pref_layout_type", "QWERTY") ?: "QWERTY"
        for ((idx, lay) in layouts.withIndex()) {
            val isSelected = lay == curLayout
            val row = createRadioRow(lay, isSelected, textPrimary, textSecondary, accentColor) {
                prefs.edit().putString("pref_layout_type", lay).apply()
                Toast.makeText(this, "Layout: $lay", Toast.LENGTH_SHORT).show()
                refreshCurrentScreen()
            }
            cardLayout.addView(row)
            if (idx < layouts.size - 1) cardLayout.addView(createDivider(cardBorder))
        }
        col.addView(cardLayout)

        // Section 3: Bottom Row Settings
        col.addView(createSectionHeader("BOTTOM ROW CONTROLS", textSecondary))
        val cardBottom = createCardContainer(cardBg, cardBorder)
        cardBottom.addView(createSwitchRow("Dedicated Language Key (🌐)", "Display language/globe switch button to the left of spacebar", "pref_show_language_key", true, textPrimary, textSecondary, accentColor))
        col.addView(cardBottom)

        root.addView(col)
        return root
    }

    // -------------------------------------------------------------
    // Screen 5: Clipboard & Quick Tools
    // -------------------------------------------------------------
    private fun buildClipboardScreen(): View {
        val (bgColor, cardBg, cardBorder, textPrimary, textSecondary, accentColor) = getThemeColors()

        val root = createScrollContainer(bgColor)
        val col = createContentColumn()

        col.addView(createScreenHeader("Clipboard & Quick Tools", textPrimary))

        col.addView(createSectionHeader("CLIPBOARD MANAGER", textSecondary))
        val cardClip = createCardContainer(cardBg, cardBorder)
        cardClip.addView(createSwitchRow("Persistent Clipboard History", "Keep copied snippets handy in the quick toolbar", "pref_clipboard_enabled", true, textPrimary, textSecondary, accentColor))
        cardClip.addView(createDivider(cardBorder))
        cardClip.addView(createSwitchRow("Auto-Paste Recent Chips", "Show recently copied text directly in the candidate bar", "pref_clipboard_suggest", true, textPrimary, textSecondary, accentColor))
        cardClip.addView(createDivider(cardBorder))

        // Clear clipboard action button
        val clearBtn = createActionButton("Clear Clipboard History", isDestructive = true) {
            AlertDialog.Builder(this)
                .setTitle("Clear Clipboard")
                .setMessage("Are you sure you want to delete all stored clipboard snippets?")
                .setPositiveButton("Clear") { _, _ ->
                    val cm = getSystemService(Context.CLIPBOARD_SERVICE) as? android.content.ClipboardManager
                    cm?.clearPrimaryClip()
                    Toast.makeText(this, "Clipboard cleared", Toast.LENGTH_SHORT).show()
                }
                .setNegativeButton("Cancel", null)
                .show()
        }
        cardClip.addView(clearBtn)
        col.addView(cardClip)

        root.addView(col)
        return root
    }

    // -------------------------------------------------------------
    // Screen 6: Voice & AI Core
    // -------------------------------------------------------------
    private fun buildAiCoreScreen(): View {
        val (bgColor, cardBg, cardBorder, textPrimary, textSecondary, accentColor) = getThemeColors()

        val root = createScrollContainer(bgColor)
        val col = createContentColumn()

        col.addView(createScreenHeader("Voice & AI Core", textPrimary))

        // Section 1: Voice Dictation
        col.addView(createSectionHeader("VOICE DICTATION", textSecondary))
        val cardVoice = createCardContainer(cardBg, cardBorder)
        cardVoice.addView(createSwitchRow("Speech-to-Text Input", "Transcribe voice dictation into current input field", "pref_voice_enabled", true, textPrimary, textSecondary, accentColor))
        col.addView(cardVoice)

        // Section 2: Neural AI Tone Rewrite
        col.addView(createSectionHeader("ON-DEVICE NEURAL REWRITE", textSecondary))
        val cardAi = createCardContainer(cardBg, cardBorder)
        cardAi.addView(createSwitchRow("AI Tone Rewrite Strip", "Quick toolbar chip to rewrite text in 5 distinct tone styles", "pref_ai_core_enabled", true, textPrimary, textSecondary, accentColor))
        cardAi.addView(createDivider(cardBorder))

        // Interactive Tone Rewrite Sandbox
        val sandboxHeader = TextView(this).apply {
            text = "INTERACTIVE TONE PLAYGROUND"
            setTextSize(TypedValue.COMPLEX_UNIT_SP, 11f)
            setTextColor(accentColor)
            setTypeface(null, Typeface.BOLD)
            setPadding(0, dp(10f), 0, dp(6f))
        }
        cardAi.addView(sandboxHeader)

        val sampleText = TextView(this).apply {
            text = "I need this report done ASAP because the client is mad."
            setTextSize(TypedValue.COMPLEX_UNIT_SP, 13f)
            setTextColor(textPrimary)
            setPadding(dp(10f), dp(8f), dp(10f), dp(8f))
            background = GradientDrawable().apply {
                cornerRadius = dp(10f).toFloat()
                setColor(if (dashboard.isDarkMode) Color.parseColor("#1F202B") else Color.parseColor("#E5E7EB"))
            }
        }
        cardAi.addView(sampleText)

        val resultText = TextView(this).apply {
            text = "Tap a tone below to preview live on-device transformation:"
            setTextSize(TypedValue.COMPLEX_UNIT_SP, 12f)
            setTextColor(textSecondary)
            setPadding(0, dp(8f), 0, dp(8f))
        }
        cardAi.addView(resultText)

        val toneStyles = listOf(
            "Casual" to ToneStyle.CASUAL,
            "Professional" to ToneStyle.PROFESSIONAL,
            "Witty" to ToneStyle.WITTY,
            "Concise" to ToneStyle.CONCISE,
            "Empathetic" to ToneStyle.EMPATHETIC
        )

        val chipsRow = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            setPadding(0, dp(4f), 0, dp(4f))
        }
        for ((tName, style) in toneStyles) {
            val chip = TextView(this).apply {
                text = tName
                setTextSize(TypedValue.COMPLEX_UNIT_SP, 11f)
                setTextColor(accentColor)
                setTypeface(null, Typeface.BOLD)
                setPadding(dp(10f), dp(6f), dp(10f), dp(6f))
                background = GradientDrawable().apply {
                    cornerRadius = dp(14f).toFloat()
                    setColor(if (dashboard.isDarkMode) Color.parseColor("#1C2030") else Color.parseColor("#EEF2F6"))
                    setStroke(dp(1f), accentColor)
                }
                layoutParams = LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT).apply {
                    marginEnd = dp(6f)
                }
                setOnClickListener {
                    val rewritten = GnosisAiCore.rewriteText(sampleText.text.toString(), style)
                    resultText.text = "Result ($tName):\n\"$rewritten\""
                    resultText.setTextColor(accentColor)
                }
            }
            chipsRow.addView(chip)
        }
        cardAi.addView(chipsRow)
        col.addView(cardAi)

        // Section 3: Native Monolith Engines
        col.addView(createSectionHeader("NATIVE ON-DEVICE ENGINES", textSecondary))
        val cardEngines = createCardContainer(cardBg, cardBorder)
        val engines = listOf(
            "libintegrated_shared_object.so" to "Delight5 / ScaNN / TNT Core",
            "libfilterframework_jni.so" to "Pipeline Filter Engine",
            "libgboard_pipeline_jni.so" to "Neural Pipeline Runtime",
            "libgrammar-checker_jni.so" to "Sentence Explorer Grammar Engine"
        )
        for ((soName, desc) in engines) {
            val eRow = LinearLayout(this).apply {
                orientation = LinearLayout.HORIZONTAL
                gravity = Gravity.CENTER_VERTICAL
                setPadding(0, dp(8f), 0, dp(8f))

                val dot = View(this@RevampedSettingsActivity).apply {
                    val s = dp(8f)
                    layoutParams = LinearLayout.LayoutParams(s, s).apply { marginEnd = dp(10f) }
                    background = GradientDrawable().apply {
                        shape = GradientDrawable.OVAL
                        setColor(Color.parseColor("#10B981")) // Green active dot
                    }
                }
                addView(dot)

                val colT = LinearLayout(this@RevampedSettingsActivity).apply {
                    orientation = LinearLayout.VERTICAL
                    layoutParams = LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f)
                }
                colT.addView(TextView(this@RevampedSettingsActivity).apply {
                    text = soName
                    setTextSize(TypedValue.COMPLEX_UNIT_SP, 13f)
                    setTypeface(Typeface.MONOSPACE, Typeface.BOLD)
                    setTextColor(textPrimary)
                })
                colT.addView(TextView(this@RevampedSettingsActivity).apply {
                    text = desc
                    setTextSize(TypedValue.COMPLEX_UNIT_SP, 11f)
                    setTextColor(textSecondary)
                })
                addView(colT)
            }
            cardEngines.addView(eRow)
        }
        col.addView(cardEngines)

        root.addView(col)
        return root
    }

    // -------------------------------------------------------------
    // Screen 7: Privacy, Reset & About
    // -------------------------------------------------------------
    private fun buildAboutScreen(): View {
        val (bgColor, cardBg, cardBorder, textPrimary, textSecondary, accentColor) = getThemeColors()

        val root = createScrollContainer(bgColor)
        val col = createContentColumn()

        col.addView(createScreenHeader("Privacy, Reset & About", textPrimary))

        // Section 1: Privacy & Data Reset
        col.addView(createSectionHeader("DATA & PRIVACY", textSecondary))
        val cardData = createCardContainer(cardBg, cardBorder)

        val clearLearnedBtn = createActionButton("Clear Learned Words & Dynamic LM", isDestructive = false) {
            AlertDialog.Builder(this)
                .setTitle("Clear Learned Words")
                .setMessage("Reset personalized words and local n-gram frequency cache? This cannot be undone.")
                .setPositiveButton("Reset") { _, _ ->
                    prefs.edit().remove("dynamic_lm_cache").apply()
                    Toast.makeText(this, "Learned words cleared", Toast.LENGTH_SHORT).show()
                }
                .setNegativeButton("Cancel", null)
                .show()
        }
        cardData.addView(clearLearnedBtn)
        cardData.addView(createDivider(cardBorder))

        val resetAllBtn = createActionButton("Reset All Settings to Default", isDestructive = true) {
            AlertDialog.Builder(this)
                .setTitle("Reset All Settings")
                .setMessage("Reset all keyboard preferences, themes, and sound profiles to factory defaults?")
                .setPositiveButton("Reset Everything") { _, _ ->
                    prefs.edit().clear().apply()
                    Toast.makeText(this, "All settings restored to default", Toast.LENGTH_SHORT).show()
                    showScreen(0)
                }
                .setNegativeButton("Cancel", null)
                .show()
        }
        cardData.addView(resetAllBtn)
        col.addView(cardData)

        // Section 2: Application Info
        col.addView(createSectionHeader("APPLICATION INFORMATION", textSecondary))
        val cardInfo = createCardContainer(cardBg, cardBorder)
        val infoItems = listOf(
            "Application" to "Gnosis Revamped Keyboard",
            "Version" to "v1.0.2",
            "Build Code" to "3",
            "Target SDK" to "Android 15 (API 36)",
            "Compile SDK" to "API 37",
            "Architecture" to "100% Offline Native Kotlin & C++",
            "Privacy Standard" to "Zero Network Requests · Zero Telemetry"
        )
        for ((k, v) in infoItems) {
            val r = LinearLayout(this).apply {
                orientation = LinearLayout.HORIZONTAL
                gravity = Gravity.CENTER_VERTICAL
                setPadding(0, dp(8f), 0, dp(8f))

                val tvK = TextView(this@RevampedSettingsActivity).apply {
                    text = k
                    setTextSize(TypedValue.COMPLEX_UNIT_SP, 13f)
                    setTextColor(textSecondary)
                    layoutParams = LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f)
                }
                addView(tvK)

                val tvV = TextView(this@RevampedSettingsActivity).apply {
                    text = v
                    setTextSize(TypedValue.COMPLEX_UNIT_SP, 13f)
                    setTypeface(null, Typeface.BOLD)
                    setTextColor(textPrimary)
                }
                addView(tvV)
            }
            cardInfo.addView(r)
        }
        col.addView(cardInfo)

        root.addView(col)
        return root
    }

    // -------------------------------------------------------------
    // Common UI Builder Helpers
    // -------------------------------------------------------------
    private data class ThemeColors(
        val bg: Int,
        val cardBg: Int,
        val cardBorder: Int,
        val textPrimary: Int,
        val textSecondary: Int,
        val accent: Int
    )

    private fun getThemeColors(): ThemeColors {
        val isDark = dashboard.isDarkMode
        return ThemeColors(
            bg = if (isDark) Color.parseColor("#08090C") else Color.parseColor("#F4F4F6"),
            cardBg = if (isDark) Color.parseColor("#14151C") else Color.WHITE,
            cardBorder = if (isDark) Color.parseColor("#242634") else Color.parseColor("#E5E7EB"),
            textPrimary = if (isDark) Color.WHITE else Color.parseColor("#111827"),
            textSecondary = if (isDark) Color.parseColor("#8E92A4") else Color.parseColor("#6B7280"),
            accent = if (isDark) Color.parseColor("#22D3EE") else Color.parseColor("#2563EB")
        )
    }

    private fun createScrollContainer(bgColor: Int): ScrollView {
        return ScrollView(this).apply {
            layoutParams = ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT
            )
            setBackgroundColor(bgColor)
            isVerticalScrollBarEnabled = false
        }
    }

    private fun createContentColumn(): LinearLayout {
        return LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(dp(16f), dp(36f), dp(16f), dp(48f))
        }
    }

    private fun createScreenHeader(title: String, textPrimary: Int): View {
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

        val titleTv = TextView(this).apply {
            text = title
            setTextSize(TypedValue.COMPLEX_UNIT_SP, 22f)
            setTypeface(Typeface.create("sans-serif-medium", Typeface.BOLD))
            setTextColor(textPrimary)
        }
        header.addView(titleTv)
        return header
    }

    private fun createCardContainer(cardBg: Int, cardBorder: Int): LinearLayout {
        return LinearLayout(this).apply {
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
    }

    private fun createSectionHeader(title: String, textColor: Int): View {
        return TextView(this).apply {
            text = title
            setTextSize(TypedValue.COMPLEX_UNIT_SP, 11f)
            setTypeface(null, Typeface.BOLD)
            setTextColor(textColor)
            letterSpacing = 0.08f
            setPadding(dp(4f), dp(4f), dp(4f), dp(6f))
        }
    }

    private fun createDivider(color: Int): View {
        return View(this).apply {
            layoutParams = LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, dp(1f)).apply {
                setMargins(0, dp(4f), 0, dp(4f))
            }
            setBackgroundColor(color)
        }
    }

    private fun createSwitchRow(
        title: String,
        description: String,
        prefKey: String,
        defaultVal: Boolean,
        textPrimary: Int,
        textSecondary: Int,
        accentColor: Int
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

        var isChecked = prefs.getBoolean(prefKey, defaultVal)
        val trackWidth = dp(46f)
        val trackHeight = dp(26f)
        val thumbSize = dp(20f)

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
                trackDrawable.setColor(accentColor)
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
        switchView.setOnClickListener {
            isChecked = !isChecked
            prefs.edit().putBoolean(prefKey, isChecked).apply()
            updateSwitchVisuals(isChecked)
        }
        row.setOnClickListener {
            switchView.performClick()
        }

        row.addView(switchView)
        return row
    }

    private fun createRadioRow(
        title: String,
        isSelected: Boolean,
        textPrimary: Int,
        textSecondary: Int,
        accentColor: Int,
        onClick: () -> Unit
    ): View {
        return LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
            setPadding(0, dp(10f), 0, dp(10f))
            isClickable = true
            isFocusable = true

            val tv = TextView(this@RevampedSettingsActivity).apply {
                text = title
                setTextSize(TypedValue.COMPLEX_UNIT_SP, 14.5f)
                setTextColor(if (isSelected) accentColor else textPrimary)
                setTypeface(null, if (isSelected) Typeface.BOLD else Typeface.NORMAL)
                layoutParams = LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f)
            }
            addView(tv)

            val check = TextView(this@RevampedSettingsActivity).apply {
                text = if (isSelected) "●" else "○"
                setTextColor(if (isSelected) accentColor else textSecondary)
                setTextSize(TypedValue.COMPLEX_UNIT_SP, 18f)
            }
            addView(check)

            setOnClickListener { onClick() }
        }
    }

    private fun createSliderRow(
        title: String,
        valueText: String,
        progress: Int,
        max: Int,
        textPrimary: Int,
        textSecondary: Int,
        accentColor: Int,
        onProgressChanged: (Int) -> Unit
    ): View {
        val col = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(0, dp(8f), 0, dp(8f))
        }

        val topRow = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
        }

        val titleTv = TextView(this).apply {
            text = title
            setTextSize(TypedValue.COMPLEX_UNIT_SP, 14f)
            setTypeface(null, Typeface.BOLD)
            setTextColor(textPrimary)
            layoutParams = LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f)
        }
        topRow.addView(titleTv)

        val valTv = TextView(this).apply {
            text = valueText
            setTextSize(TypedValue.COMPLEX_UNIT_SP, 13f)
            setTextColor(accentColor)
            setTypeface(null, Typeface.BOLD)
        }
        topRow.addView(valTv)
        col.addView(topRow)

        val seekBar = SeekBar(this).apply {
            this.max = max
            this.progress = progress
            thumbTintList = android.content.res.ColorStateList.valueOf(accentColor)
            progressTintList = android.content.res.ColorStateList.valueOf(accentColor)
            setPadding(0, dp(10f), 0, dp(6f))
            setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
                override fun onProgressChanged(sb: SeekBar?, p: Int, fromUser: Boolean) {
                    if (fromUser) {
                        onProgressChanged(p)
                        valTv.text = when {
                            valueText.endsWith("%") -> "$p%"
                            valueText.endsWith("ms") -> "${p}ms"
                            valueText.endsWith("dp") -> "${p}dp"
                            else -> "$p"
                        }
                    }
                }
                override fun onStartTrackingTouch(sb: SeekBar?) {}
                override fun onStopTrackingTouch(sb: SeekBar?) {}
            })
        }
        col.addView(seekBar)

        return col
    }

    private fun createActionButton(label: String, isDestructive: Boolean, onClick: () -> Unit): View {
        return TextView(this).apply {
            text = label
            setTextSize(TypedValue.COMPLEX_UNIT_SP, 14f)
            setTypeface(null, Typeface.BOLD)
            gravity = Gravity.CENTER
            setTextColor(if (isDestructive) Color.parseColor("#EF4444") else Color.parseColor("#38BDF8"))
            setPadding(dp(12f), dp(12f), dp(12f), dp(12f))
            background = GradientDrawable().apply {
                cornerRadius = dp(12f).toFloat()
                setColor(if (isDestructive) Color.parseColor("#1F1315") else Color.parseColor("#101A29"))
                setStroke(dp(1f), if (isDestructive) Color.parseColor("#EF4444") else Color.parseColor("#38BDF8"))
            }
            layoutParams = LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT).apply {
                topMargin = dp(8f)
                bottomMargin = dp(4f)
            }
            setOnClickListener { onClick() }
        }
    }
}
