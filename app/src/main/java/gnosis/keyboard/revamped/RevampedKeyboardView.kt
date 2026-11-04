package gnosis.keyboard.revamped

import android.content.Context
import android.content.SharedPreferences
import android.graphics.Color
import android.graphics.PorterDuff
import android.graphics.Typeface
import android.graphics.drawable.GradientDrawable
import android.util.AttributeSet
import android.util.TypedValue
import android.view.Gravity
import android.view.HapticFeedbackConstants
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView

class RevampedKeyboardView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : LinearLayout(context, attrs, defStyleAttr) {

    // Listener for key presses & actions
    var onKeyClickListener: ((KeyAction) -> Unit)? = null
    var onCursorMoveListener: ((Int) -> Unit)? = null

    sealed class KeyAction {
        data class Character(val text: String) : KeyAction()
        object Backspace : KeyAction()
        object Shift : KeyAction()
        object SwitchLayer : KeyAction()
        object Enter : KeyAction()
        object DoubleSpacePeriod : KeyAction()
        object LanguageSwitch : KeyAction()
        data class ToolbarItem(val id: String) : KeyAction()
        data class Suggestion(val word: String) : KeyAction()
    }

    private var skin: RevampedSkin = RevampedSkin.LUCID_VOID_DARK
    val currentSkin: RevampedSkin get() = skin
    private var isShifted: Boolean = false
    private var isCapsLocked: Boolean = false
    private var currentLayer = 0 // 0: Letters, 1: Numbers/Symbols, 2: Secondary Symbols

    // Preferences
    var showNumberRow: Boolean = true
    var showLanguageKey: Boolean = true
    var doubleSpacePeriodEnabled: Boolean = true
    var showKeyPopups: Boolean = true
    var activeLanguageShortLabel: String = "EN"

    private val keyViews = mutableListOf<KeyViewHolder>()
    private var suggestionContainer: LinearLayout? = null
    private var actionKeyImageView: ImageView? = null
    private var currentImeAction: Int = EditorInfo.IME_ACTION_UNSPECIFIED
    private var isCurrentFieldEmpty: Boolean = false

    private class KeyViewHolder(
        val frame: FrameLayout,
        val labelView: TextView,
        val hintView: TextView?,
        val baseChar: String,
        val isLetter: Boolean
    )

    private val density = resources.displayMetrics.density
    private fun dp(v: Float) = (v * density).toInt()

    init {
        orientation = VERTICAL
        clipChildren = false
        clipToPadding = false
        applySkin(skin)
    }

    fun applySkin(newSkin: RevampedSkin) {
        this.skin = newSkin
        setBackgroundColor(skin.background)
        rebuildLayout()
    }

    fun applyPreferences(prefs: SharedPreferences) {
        this.showNumberRow = prefs.getBoolean("pref_number_row", true)
        this.showLanguageKey = prefs.getBoolean("pref_show_language_key", true)
        this.doubleSpacePeriodEnabled = prefs.getBoolean("pref_double_space_period", true)
        this.showKeyPopups = prefs.getBoolean("pref_key_popups", true)
        val lang = prefs.getString("pref_language", "en") ?: "en"
        this.activeLanguageShortLabel = when (lang) {
            "hi", "deva" -> "HI"
            "hinglish" -> "HING"
            "es" -> "ES"
            "fr" -> "FR"
            "de" -> "DE"
            else -> "EN"
        }
        applySkin(RevampedSkin.buildFromPreferences(prefs))
    }

    fun setShiftState(shifted: Boolean, capsLocked: Boolean = false) {
        this.isShifted = shifted
        this.isCapsLocked = capsLocked
        for (holder in keyViews) {
            if (holder.isLetter) {
                holder.labelView.text = if (isShifted || isCapsLocked) {
                    holder.baseChar.uppercase()
                } else {
                    holder.baseChar.lowercase()
                }
            }
        }
    }

    fun updateActionKeyState(imeOptions: Int, isFieldEmpty: Boolean) {
        this.currentImeAction = imeOptions and EditorInfo.IME_MASK_ACTION
        this.isCurrentFieldEmpty = isFieldEmpty
        updateActionKeyVisual()
    }

    private fun updateActionKeyVisual() {
        val iv = actionKeyImageView ?: return
        val iconRes = when {
            isCurrentFieldEmpty -> R.drawable.ic_chevron_down
            currentImeAction == EditorInfo.IME_ACTION_SEARCH -> R.drawable.ic_search
            currentImeAction == EditorInfo.IME_ACTION_SEND -> R.drawable.ic_send
            currentImeAction == EditorInfo.IME_ACTION_GO -> R.drawable.ic_corner_down_left
            currentImeAction == EditorInfo.IME_ACTION_DONE -> R.drawable.ic_check
            currentImeAction == EditorInfo.IME_ACTION_NEXT -> R.drawable.ic_corner_down_left
            else -> R.drawable.ic_corner_down_left
        }
        iv.setImageResource(iconRes)
        iv.setColorFilter(skin.onAccentText, PorterDuff.Mode.SRC_IN)
    }

    fun updateSuggestions(candidates: List<String>) {
        val container = suggestionContainer ?: return
        container.removeAllViews()

        if (candidates.isEmpty()) {
            container.visibility = View.GONE
            return
        }

        container.visibility = View.VISIBLE
        for ((idx, word) in candidates.take(3).withIndex()) {
            val tv = TextView(context).apply {
                text = word
                gravity = Gravity.CENTER
                setTextSize(TypedValue.COMPLEX_UNIT_SP, 15f)
                setTextColor(if (idx == 1) skin.accent else skin.textPrimary)
                setTypeface(skin.typeface, if (idx == 1) Typeface.BOLD else Typeface.NORMAL)
                layoutParams = LayoutParams(0, LayoutParams.MATCH_PARENT, 1f)
                setPadding(dp(4f), 0, dp(4f), 0)
                isClickable = true
                isFocusable = true
                background = skin.buildKeyDrawable(density, special = true)
                setOnClickListener {
                    performHaptic()
                    onKeyClickListener?.invoke(KeyAction.Suggestion(word))
                }
            }
            container.addView(tv)
        }
    }

    private fun rebuildLayout() {
        removeAllViews()
        keyViews.clear()

        // 1. Omnitype Quick Toolbar
        addView(buildOmnitypeToolbar())

        // 2. Suggestion Strip (3 Slots)
        val strip = LinearLayout(context).apply {
            orientation = HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
            setPadding(dp(8f), dp(2f), dp(8f), dp(2f))
            layoutParams = LayoutParams(LayoutParams.MATCH_PARENT, dp(36f))
            visibility = View.GONE
        }
        suggestionContainer = strip
        addView(strip)

        // 3. Main Key Rows Container
        val rowsContainer = LinearLayout(context).apply {
            orientation = VERTICAL
            clipChildren = false
            clipToPadding = false
            val padH = dp(skin.keySpacingHDp)
            val padV = dp(skin.keySpacingVDp)
            setPadding(padH, padV, padH, padV)
            layoutParams = LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT)
        }

        when (currentLayer) {
            0 -> {
                // Layer 0: Normal Letters
                if (showNumberRow) {
                    val numRow = listOf("1" to "!", "2" to "@", "3" to "#", "4" to "$", "5" to "%",
                                        "6" to "^", "7" to "&", "8" to "*", "9" to "(", "0" to ")")
                    rowsContainer.addView(buildNumberRow(numRow))
                }

                val row0 = listOf("Q" to "1", "W" to "2", "E" to "3", "R" to "4", "T" to "5",
                                  "Y" to "6", "U" to "7", "I" to "8", "O" to "9", "P" to "0")
                val row1 = listOf("A" to "@", "S" to "#", "D" to "$", "F" to "_", "G" to "&",
                                  "H" to "-", "J" to "+", "K" to "(", "L" to ")")
                val row2 = listOf("Z" to "*", "X" to "\"", "C" to "'", "V" to ":",
                                  "B" to ";", "N" to "!", "M" to "?")

                rowsContainer.addView(buildKeyRow(row0))

                val r1Layout = buildKeyRow(row1)
                r1Layout.setPadding(dp(10f), 0, dp(10f), 0)
                rowsContainer.addView(r1Layout)

                rowsContainer.addView(buildRow2(row2))
                rowsContainer.addView(buildRevampedBottomRow())
            }
            1 -> {
                // Layer 1: Numbers & Standard Symbols
                val numRow0 = listOf("1" to "[", "2" to "]", "3" to "{", "4" to "}", "5" to "#",
                                     "6" to "%", "7" to "^", "8" to "*", "9" to "+", "0" to "=")
                val numRow1 = listOf("@" to "_", "#" to "\\", "$" to "|", "_" to "~", "&" to "<",
                                     "-" to ">", "+" to "€", "(" to "£", ")" to "¥", "/" to "•")
                val numRow2 = listOf("*" to "", "\"" to "", "'" to "", ":" to "",
                                     ";" to "", "!" to "", "?" to "")

                rowsContainer.addView(buildKeyRow(numRow0))
                rowsContainer.addView(buildKeyRow(numRow1))
                rowsContainer.addView(buildRow2Symbols(numRow2, switchLayerLabel = "=\\<", switchTo = 2))
                rowsContainer.addView(buildRevampedBottomRow())
            }
            else -> {
                // Layer 2: Advanced Math & Currency Symbols
                val symRow0 = listOf("~" to "", "`" to "", "|" to "", "•" to "", "√" to "",
                                     "π" to "", "÷" to "", "×" to "", "¶" to "", "∆" to "")
                val symRow1 = listOf("£" to "", "¢" to "", "€" to "", "¥" to "", "^" to "",
                                     "°" to "", "=" to "", "{" to "", "}" to "", "\\" to "")
                val symRow2 = listOf("%" to "", "©" to "", "®" to "", "™" to "",
                                     "[" to "", "]" to "", "<" to "")

                rowsContainer.addView(buildKeyRow(symRow0))
                rowsContainer.addView(buildKeyRow(symRow1))
                rowsContainer.addView(buildRow2Symbols(symRow2, switchLayerLabel = "?123", switchTo = 1))
                rowsContainer.addView(buildRevampedBottomRow())
            }
        }

        addView(rowsContainer)
    }

    // -------------------------------------------------------------
    // Omnitype Quick Toolbar
    // -------------------------------------------------------------
    private fun buildOmnitypeToolbar(): View {
        val container = FrameLayout(context).apply {
            layoutParams = LayoutParams(LayoutParams.MATCH_PARENT, dp(60f)).apply {
                bottomMargin = dp(4f)
            }
            setPadding(dp(8f), dp(2f), dp(8f), dp(2f))
        }

        val bar = LinearLayout(context).apply {
            orientation = HORIZONTAL
            gravity = Gravity.CENTER
            layoutParams = FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.MATCH_PARENT,
                FrameLayout.LayoutParams.MATCH_PARENT
            )
            background = GradientDrawable().apply {
                cornerRadius = dp(24f).toFloat()
                setColor(skin.surface)
                if (skin.borderWidthDp > 0) {
                    setStroke(dp(skin.borderWidthDp), skin.keyBorderColor)
                }
            }
            setPadding(dp(6f), dp(2f), dp(6f), dp(2f))
        }

        val items = listOf(
            Triple("ai_core", "Neural", R.drawable.ic_sparkles),
            Triple("emoji", "Emoji", R.drawable.ic_smile),
            Triple("clipboard", "Clip", R.drawable.ic_clipboard),
            Triple("voice", "Voice", R.drawable.ic_mic),
            Triple("resizer", "Resize", R.drawable.ic_scaling),
            Triple("settings", "Settings", R.drawable.ic_settings)
        )

        for (item in items) {
            val chip = LinearLayout(context).apply {
                orientation = HORIZONTAL
                gravity = Gravity.CENTER
                layoutParams = LayoutParams(0, LayoutParams.MATCH_PARENT, 1f)
                isClickable = true
                isFocusable = true
                background = skin.buildKeyDrawable(density, special = true)

                val iv = ImageView(context).apply {
                    setImageResource(item.third)
                    setColorFilter(skin.accent, PorterDuff.Mode.SRC_IN)
                    layoutParams = LayoutParams(dp(16f), dp(16f))
                }
                addView(iv)

                val tv = TextView(context).apply {
                    text = item.second
                    setTextSize(TypedValue.COMPLEX_UNIT_SP, 11f * skin.fontScale)
                    setTextColor(skin.textPrimary)
                    setTypeface(skin.typeface, Typeface.BOLD)
                    setPadding(dp(3f), 0, 0, 0)
                }
                addView(tv)

                setOnClickListener {
                    performHaptic()
                    onKeyClickListener?.invoke(KeyAction.ToolbarItem(item.first))
                }
            }
            bar.addView(chip)
        }

        container.addView(bar)
        return container
    }

    // -------------------------------------------------------------
    // Dedicated Number Row (0-9)
    // -------------------------------------------------------------
    private fun buildNumberRow(keys: List<Pair<String, String>>): LinearLayout {
        val row = LinearLayout(context).apply {
            orientation = HORIZONTAL
            clipChildren = false
            clipToPadding = false
            layoutParams = LayoutParams(LayoutParams.MATCH_PARENT, dp((skin.keyHeightDp * 0.78f))).apply {
                bottomMargin = dp(skin.keySpacingVDp)
            }
        }
        for (pair in keys) {
            row.addView(createLetterKey(pair.first, pair.second, weight = 1.0f))
        }
        return row
    }

    // -------------------------------------------------------------
    // Key Rows Construction
    // -------------------------------------------------------------
    private fun buildKeyRow(keys: List<Pair<String, String>>): LinearLayout {
        val row = LinearLayout(context).apply {
            orientation = HORIZONTAL
            clipChildren = false
            clipToPadding = false
            layoutParams = LayoutParams(LayoutParams.MATCH_PARENT, dp(skin.keyHeightDp.toFloat())).apply {
                bottomMargin = dp(skin.keySpacingVDp)
            }
        }
        for (pair in keys) {
            row.addView(createLetterKey(pair.first, pair.second, weight = 1.0f))
        }
        return row
    }

    private fun buildRow2(keys: List<Pair<String, String>>): LinearLayout {
        val row = LinearLayout(context).apply {
            orientation = HORIZONTAL
            clipChildren = false
            clipToPadding = false
            layoutParams = LayoutParams(LayoutParams.MATCH_PARENT, dp(skin.keyHeightDp.toFloat())).apply {
                bottomMargin = dp(skin.keySpacingVDp)
            }
        }

        // Shift Key (1.5x weight)
        val shiftKey = createSpecialIconKey(
            if (isCapsLocked) R.drawable.ic_chevrons_up else R.drawable.ic_arrow_up,
            weight = 1.5f,
            iconTint = if (isShifted || isCapsLocked) skin.accent else skin.textPrimary
        ) {
            if (isShifted && !isCapsLocked) {
                setShiftState(shifted = true, capsLocked = true)
            } else if (isCapsLocked) {
                setShiftState(shifted = false, capsLocked = false)
            } else {
                setShiftState(shifted = true, capsLocked = false)
            }
            onKeyClickListener?.invoke(KeyAction.Shift)
        }
        row.addView(shiftKey)

        for (pair in keys) {
            row.addView(createLetterKey(pair.first, pair.second, weight = 1.0f))
        }

        // Backspace Key (1.5x weight)
        val backspaceKey = createSpecialIconKey(
            R.drawable.ic_delete,
            weight = 1.5f,
            iconTint = skin.textPrimary
        ) {
            onKeyClickListener?.invoke(KeyAction.Backspace)
        }
        row.addView(backspaceKey)

        return row
    }

    private fun buildRow2Symbols(keys: List<Pair<String, String>>, switchLayerLabel: String, switchTo: Int): LinearLayout {
        val row = LinearLayout(context).apply {
            orientation = HORIZONTAL
            clipChildren = false
            clipToPadding = false
            layoutParams = LayoutParams(LayoutParams.MATCH_PARENT, dp(skin.keyHeightDp.toFloat())).apply {
                bottomMargin = dp(skin.keySpacingVDp)
            }
        }

        val subSwitch = createSpecialKey(switchLayerLabel, weight = 1.5f) {
            currentLayer = switchTo
            rebuildLayout()
            onKeyClickListener?.invoke(KeyAction.SwitchLayer)
        }
        row.addView(subSwitch)

        for (pair in keys) {
            row.addView(createLetterKey(pair.first, pair.second.ifEmpty { null }, weight = 1.0f))
        }

        val backspaceKey = createSpecialIconKey(
            R.drawable.ic_delete,
            weight = 1.5f,
            iconTint = skin.textPrimary
        ) {
            onKeyClickListener?.invoke(KeyAction.Backspace)
        }
        row.addView(backspaceKey)

        return row
    }

    // -------------------------------------------------------------
    // Full Bottom Row Implementation
    // [ ?123 ] [ , ] [ 🌐 ] [    SPACE    ] [ . ] [ Action Key ]
    // -------------------------------------------------------------
    private fun buildRevampedBottomRow(): LinearLayout {
        val row = LinearLayout(context).apply {
            orientation = HORIZONTAL
            clipChildren = false
            clipToPadding = false
            layoutParams = LayoutParams(LayoutParams.MATCH_PARENT, dp(skin.keyHeightDp.toFloat()))
        }

        // 1. Layer Switch (?123 / ABC) - Weight 1.3f
        val layerLabel = if (currentLayer == 0) "?123" else "ABC"
        val switchKey = createSpecialKey(layerLabel, weight = 1.3f) {
            currentLayer = if (currentLayer == 0) 1 else 0
            rebuildLayout()
            onKeyClickListener?.invoke(KeyAction.SwitchLayer)
        }
        row.addView(switchKey)

        // 2. Comma Key (,) - Weight 0.9f
        val commaKey = createLetterKey(",", hint = "!", weight = 0.9f)
        row.addView(commaKey)

        // 3. Language / Globe / Emoji Switcher Key (🌐) - Weight 0.9f (if enabled)
        if (showLanguageKey) {
            val globeKey = createGlobeKey(weight = 0.9f)
            row.addView(globeKey)
        }

        // 4. Spacebar (Wide S P A C E with smooth cursor scrubber)
        val spaceWeight = if (showLanguageKey) 4.4f else 5.3f
        val spaceKey = createSpaceKey(weight = spaceWeight)
        row.addView(spaceKey)

        // 5. Period Key (.) - Weight 0.9f
        val periodKey = createLetterKey(".", hint = "?", weight = 0.9f)
        row.addView(periodKey)

        // 6. Action / Enter Key - Weight 1.6f
        val enterKey = createActionKey(weight = 1.6f) {
            onKeyClickListener?.invoke(KeyAction.Enter)
        }
        row.addView(enterKey)

        return row
    }

    // -------------------------------------------------------------
    // Individual Key Views
    // -------------------------------------------------------------
    private fun createLetterKey(char: String, hint: String?, weight: Float): View {
        val keyBox = FrameLayout(context).apply {
            layoutParams = LayoutParams(0, LayoutParams.MATCH_PARENT, weight).apply {
                val hPad = dp(skin.keySpacingHDp / 2f)
                setMargins(hPad, 0, hPad, 0)
            }
            background = skin.buildKeyDrawable(density, special = false)
            isClickable = true
            isFocusable = true
        }

        val label = TextView(context).apply {
            text = if (isShifted || isCapsLocked) char.uppercase() else char.lowercase()
            setTextSize(TypedValue.COMPLEX_UNIT_SP, 19f * skin.fontScale)
            setTextColor(skin.textPrimary)
            gravity = Gravity.CENTER
            setTypeface(skin.typeface, if (skin.fontBold) Typeface.BOLD else Typeface.NORMAL)
            layoutParams = FrameLayout.LayoutParams(FrameLayout.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.MATCH_PARENT)
        }
        keyBox.addView(label)

        // Secondary Hint at top-right
        var hintView: TextView? = null
        if (!hint.isNullOrEmpty()) {
            hintView = TextView(context).apply {
                text = hint
                setTextSize(TypedValue.COMPLEX_UNIT_SP, 10f * skin.fontScale)
                setTextColor(skin.textSecondary)
                setPadding(0, dp(3f), dp(5f), 0)
                layoutParams = FrameLayout.LayoutParams(
                    FrameLayout.LayoutParams.WRAP_CONTENT,
                    FrameLayout.LayoutParams.WRAP_CONTENT,
                    Gravity.TOP or Gravity.END
                )
            }
            keyBox.addView(hintView)
        }

        keyBox.setOnClickListener {
            performHaptic()
            val textToCommit = label.text.toString()
            onKeyClickListener?.invoke(KeyAction.Character(textToCommit))
        }

        keyBox.setOnLongClickListener {
            if (!hint.isNullOrEmpty()) {
                performHaptic()
                onKeyClickListener?.invoke(KeyAction.Character(hint))
                true
            } else false
        }

        val isAlpha = char.length == 1 && char[0].isLetter()
        keyViews.add(KeyViewHolder(keyBox, label, hintView, char, isLetter = isAlpha))
        return keyBox
    }

    private fun createSpecialKey(label: String, weight: Float, onClick: () -> Unit): View {
        val keyBox = FrameLayout(context).apply {
            layoutParams = LayoutParams(0, LayoutParams.MATCH_PARENT, weight).apply {
                val hPad = dp(skin.keySpacingHDp / 2f)
                setMargins(hPad, 0, hPad, 0)
            }
            background = skin.buildKeyDrawable(density, special = true)
            isClickable = true
            isFocusable = true
        }

        val tv = TextView(context).apply {
            text = label
            setTextSize(TypedValue.COMPLEX_UNIT_SP, 14f * skin.fontScale)
            setTextColor(skin.textPrimary)
            setTypeface(skin.typeface, Typeface.BOLD)
            gravity = Gravity.CENTER
            layoutParams = FrameLayout.LayoutParams(FrameLayout.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.MATCH_PARENT)
        }
        keyBox.addView(tv)

        keyBox.setOnClickListener {
            performHaptic()
            onClick()
        }

        return keyBox
    }

    private fun createSpecialIconKey(drawableRes: Int, weight: Float, iconTint: Int, onClick: () -> Unit): View {
        val keyBox = FrameLayout(context).apply {
            layoutParams = LayoutParams(0, LayoutParams.MATCH_PARENT, weight).apply {
                val hPad = dp(skin.keySpacingHDp / 2f)
                setMargins(hPad, 0, hPad, 0)
            }
            background = skin.buildKeyDrawable(density, special = true)
            isClickable = true
            isFocusable = true
        }

        val iv = ImageView(context).apply {
            setImageResource(drawableRes)
            setColorFilter(iconTint, PorterDuff.Mode.SRC_IN)
            scaleType = ImageView.ScaleType.CENTER_INSIDE
            val pad = dp(13f)
            setPadding(pad, pad, pad, pad)
            layoutParams = FrameLayout.LayoutParams(FrameLayout.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.MATCH_PARENT)
        }
        keyBox.addView(iv)

        keyBox.setOnClickListener {
            performHaptic()
            onClick()
        }

        return keyBox
    }

    private fun createGlobeKey(weight: Float): View {
        val keyBox = FrameLayout(context).apply {
            layoutParams = LayoutParams(0, LayoutParams.MATCH_PARENT, weight).apply {
                val hPad = dp(skin.keySpacingHDp / 2f)
                setMargins(hPad, 0, hPad, 0)
            }
            background = skin.buildKeyDrawable(density, special = true)
            isClickable = true
            isFocusable = true
        }

        val container = LinearLayout(context).apply {
            orientation = VERTICAL
            gravity = Gravity.CENTER
            layoutParams = FrameLayout.LayoutParams(FrameLayout.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.MATCH_PARENT)
        }

        val iv = ImageView(context).apply {
            setImageResource(R.drawable.ic_globe)
            setColorFilter(skin.textPrimary, PorterDuff.Mode.SRC_IN)
            scaleType = ImageView.ScaleType.CENTER_INSIDE
            layoutParams = LayoutParams(dp(18f), dp(18f))
        }
        container.addView(iv)

        keyBox.addView(container)

        keyBox.setOnClickListener {
            performHaptic()
            onKeyClickListener?.invoke(KeyAction.LanguageSwitch)
        }

        keyBox.setOnLongClickListener {
            performHaptic()
            onKeyClickListener?.invoke(KeyAction.ToolbarItem("settings"))
            true
        }

        return keyBox
    }

    private fun createSpaceKey(weight: Float): View {
        val keyBox = FrameLayout(context).apply {
            layoutParams = LayoutParams(0, LayoutParams.MATCH_PARENT, weight).apply {
                val hPad = dp(skin.keySpacingHDp / 2f)
                setMargins(hPad, 0, hPad, 0)
            }
            background = skin.buildKeyDrawable(density, special = false)
            isClickable = true
            isFocusable = true
        }

        val tv = TextView(context).apply {
            text = "S P A C E"
            letterSpacing = 0.08f
            setTextSize(TypedValue.COMPLEX_UNIT_SP, 13f * skin.fontScale)
            setTextColor(skin.textSecondary)
            setTypeface(skin.typeface, Typeface.BOLD)
            gravity = Gravity.CENTER
            layoutParams = FrameLayout.LayoutParams(FrameLayout.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.MATCH_PARENT)
        }
        keyBox.addView(tv)

        // Spacebar Cursor Dragging / Scrubbing & Double-Tap Period
        var downX = 0f
        var lastStepX = 0f
        var isDraggingCursor = false
        val stepDistance = dp(11f)
        var lastSpaceTapTime = 0L

        keyBox.setOnTouchListener { _, event ->
            when (event.actionMasked) {
                MotionEvent.ACTION_DOWN -> {
                    downX = event.x
                    lastStepX = event.x
                    isDraggingCursor = false
                    true
                }
                MotionEvent.ACTION_MOVE -> {
                    val totalDx = event.x - downX
                    if (!isDraggingCursor && Math.abs(totalDx) > stepDistance * 1.5f) {
                        isDraggingCursor = true
                        lastStepX = event.x
                    }
                    if (isDraggingCursor) {
                        val stepDx = event.x - lastStepX
                        if (Math.abs(stepDx) >= stepDistance) {
                            val dir = if (stepDx > 0) 1 else -1
                            onCursorMoveListener?.invoke(dir)
                            lastStepX = event.x
                        }
                    }
                    true
                }
                MotionEvent.ACTION_UP -> {
                    if (!isDraggingCursor) {
                        performHaptic()
                        val now = System.currentTimeMillis()
                        if (doubleSpacePeriodEnabled && (now - lastSpaceTapTime < 350L)) {
                            onKeyClickListener?.invoke(KeyAction.DoubleSpacePeriod)
                            lastSpaceTapTime = 0L
                        } else {
                            lastSpaceTapTime = now
                            onKeyClickListener?.invoke(KeyAction.Character(" "))
                        }
                    }
                    isDraggingCursor = false
                    true
                }
                else -> false
            }
        }

        return keyBox
    }

    private fun createActionKey(weight: Float, onClick: () -> Unit): View {
        val keyBox = FrameLayout(context).apply {
            layoutParams = LayoutParams(0, LayoutParams.MATCH_PARENT, weight).apply {
                val hPad = dp(skin.keySpacingHDp / 2f)
                setMargins(hPad, 0, hPad, 0)
            }
            background = skin.buildKeyDrawable(density, special = true, isAction = true)
            isClickable = true
            isFocusable = true
        }

        val iv = ImageView(context).apply {
            scaleType = ImageView.ScaleType.CENTER_INSIDE
            val pad = dp(12f)
            setPadding(pad, pad, pad, pad)
            setColorFilter(skin.onAccentText, PorterDuff.Mode.SRC_IN)
            layoutParams = FrameLayout.LayoutParams(FrameLayout.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.MATCH_PARENT)
        }
        actionKeyImageView = iv
        keyBox.addView(iv)

        updateActionKeyVisual()

        keyBox.setOnClickListener {
            performHaptic()
            onClick()
        }

        return keyBox
    }

    private fun performHaptic() {
        try {
            performHapticFeedback(
                HapticFeedbackConstants.KEYBOARD_TAP,
                HapticFeedbackConstants.FLAG_IGNORE_GLOBAL_SETTING
            )
        } catch (_: Exception) {}
    }
}
