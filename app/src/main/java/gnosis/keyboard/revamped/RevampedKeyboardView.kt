package gnosis.keyboard.revamped

import android.content.Context
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
import android.widget.FrameLayout
import android.widget.HorizontalScrollView
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import gnosis.keyboard.revamped.engine.RevampedSpatialTouchModel

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
        data class ToolbarItem(val id: String) : KeyAction()
        data class Suggestion(val word: String) : KeyAction()
    }

    private var skin: RevampedSkin = RevampedSkin.LUCID_VOID_DARK
    val currentSkin: RevampedSkin get() = skin
    private var isShifted: Boolean = false
    private var isCapsLocked: Boolean = false
    private var currentLayer = 0 // 0: Letters, 1: Numbers/Symbols

    private val keyViews = mutableListOf<KeyViewHolder>()
    private var suggestionContainer: LinearLayout? = null

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

        if (currentLayer == 0) {
            // QWERTY Layout
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
        } else {
            // Numbers & Symbols Layout
            val numRow0 = listOf("1" to "[", "2" to "]", "3" to "{", "4" to "}", "5" to "#",
                                 "6" to "%", "7" to "^", "8" to "*", "9" to "+", "0" to "=")
            val numRow1 = listOf("-" to "_", "/" to "\\", ":" to "|", ";" to "~", "(" to "<",
                                 ")" to ">", "$" to "€", "&" to "£", "@" to "¥", "\"" to "•")
            val numRow2 = listOf("." to ",", "," to "?", "?" to "!", "!" to "'", "'" to "`")

            rowsContainer.addView(buildKeyRow(numRow0))
            rowsContainer.addView(buildKeyRow(numRow1))
            rowsContainer.addView(buildRow2(numRow2))
            rowsContainer.addView(buildRevampedBottomRow())
        }

        addView(rowsContainer)
    }

    // -------------------------------------------------------------
    // Omnitype Quick Toolbar – matches the redesign mockup
    // Single floating dark capsule with vertical icon + label items
    // -------------------------------------------------------------
    private fun buildOmnitypeToolbar(): View {
        val container = FrameLayout(context).apply {
            layoutParams = LayoutParams(LayoutParams.MATCH_PARENT, dp(64f)).apply {
                bottomMargin = dp(6f)
            }
            setPadding(dp(12f), dp(4f), dp(12f), dp(4f))
        }

        val bar = LinearLayout(context).apply {
            orientation = HORIZONTAL
            gravity = Gravity.CENTER
            layoutParams = FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.MATCH_PARENT,
                FrameLayout.LayoutParams.MATCH_PARENT
            )
            background = GradientDrawable().apply {
                cornerRadius = dp(20f).toFloat()
                setColor(if (skin.isLight) Color.parseColor("#F1F5F9") else Color.parseColor("#1C1E26"))
                setStroke(dp(1f), if (skin.isLight) Color.parseColor("#E2E8F0") else Color.parseColor("#2A2D3A"))
            }
            setPadding(dp(6f), dp(6f), dp(6f), dp(6f))
        }

        fun createToolbarItem(drawableRes: Int, label: String, actionId: String): View {
            return LinearLayout(context).apply {
                orientation = VERTICAL
                gravity = Gravity.CENTER
                layoutParams = LinearLayout.LayoutParams(0, LayoutParams.MATCH_PARENT, 1f)
                isClickable = true
                isFocusable = true
                setOnClickListener {
                    performHaptic()
                    onKeyClickListener?.invoke(KeyAction.ToolbarItem(actionId))
                }

                val iv = ImageView(context).apply {
                    setImageResource(drawableRes)
                    setColorFilter(skin.textPrimary, PorterDuff.Mode.SRC_IN)
                    layoutParams = LinearLayout.LayoutParams(dp(20f), dp(20f)).apply {
                        gravity = Gravity.CENTER_HORIZONTAL
                    }
                }
                addView(iv)

                val tv = TextView(context).apply {
                    text = label
                    setTextSize(TypedValue.COMPLEX_UNIT_SP, 10f)
                    setTextColor(skin.textSecondary)
                    gravity = Gravity.CENTER
                    setPadding(0, dp(3f), 0, 0)
                }
                addView(tv)
            }
        }

        bar.addView(createToolbarItem(R.drawable.ic_sparkles, "Neural", "neural_ai"))
        bar.addView(createToolbarItem(R.drawable.ic_smile, "Emoji", "emoji"))
        bar.addView(createToolbarItem(R.drawable.ic_clipboard, "Clip", "clipboard"))
        bar.addView(createToolbarItem(R.drawable.ic_mic, "Voice", "voice"))
        bar.addView(createToolbarItem(R.drawable.ic_maximize_2, "Resize", "resizer"))
        bar.addView(createToolbarItem(R.drawable.ic_settings, "Settings", "settings"))

        container.addView(bar)
        return container
    }

    // -------------------------------------------------------------
    // Keycap Constructors
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
            val keyView = createLetterKey(pair.first, pair.second, weight = 1.0f)
            row.addView(keyView)
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
        val shiftKey = createSpecialKey(if (currentLayer == 0) "⇧" else "=\\<", weight = 1.5f) {
            if (currentLayer == 0) {
                isShifted = !isShifted
                setShiftState(isShifted, isCapsLocked)
                onKeyClickListener?.invoke(KeyAction.Shift)
            }
        }
        row.addView(shiftKey)

        for (pair in keys) {
            row.addView(createLetterKey(pair.first, pair.second, weight = 1.0f))
        }

        // Backspace Key (1.5x weight)
        val backspaceKey = createSpecialKey("⌫", weight = 1.5f) {
            onKeyClickListener?.invoke(KeyAction.Backspace)
        }
        row.addView(backspaceKey)

        return row
    }

    private fun buildRevampedBottomRow(): LinearLayout {
        val row = LinearLayout(context).apply {
            orientation = HORIZONTAL
            clipChildren = false
            clipToPadding = false
            layoutParams = LayoutParams(LayoutParams.MATCH_PARENT, dp(skin.keyHeightDp.toFloat()))
        }

        // 1. Layer Switch (?123 / ABC) - Weight 1.8f
        val layerLabel = if (currentLayer == 0) "?123" else "ABC"
        val switchKey = createSpecialKey(layerLabel, weight = 1.8f) {
            currentLayer = if (currentLayer == 0) 1 else 0
            rebuildLayout()
            onKeyClickListener?.invoke(KeyAction.SwitchLayer)
        }
        row.addView(switchKey)

        // 2. Spacebar (Wide S P A C E with smooth cursor scrubber) - Weight 6.4f
        val spaceKey = createSpaceKey(weight = 6.4f)
        row.addView(spaceKey)

        // 3. Action / Enter Key - Weight 1.8f
        val enterKey = createActionKey("↵", weight = 1.8f) {
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
            text = if (isShifted) char.uppercase() else char.lowercase()
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

        keyViews.add(KeyViewHolder(keyBox, label, hintView, char, isLetter = true))
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

        // Spacebar Cursor Dragging / Scrubbing
        var downX = 0f
        var lastStepX = 0f
        var isDraggingCursor = false
        val stepDistance = dp(11f)

        keyBox.setOnTouchListener { _, event ->
            when (event.action) {
                MotionEvent.ACTION_DOWN -> {
                    downX = event.x
                    lastStepX = event.x
                    isDraggingCursor = false
                    true
                }
                MotionEvent.ACTION_MOVE -> {
                    val delta = event.x - downX
                    if (!isDraggingCursor && Math.abs(delta) > dp(8f)) {
                        isDraggingCursor = true
                    }
                    if (isDraggingCursor) {
                        val stepDelta = event.x - lastStepX
                        if (Math.abs(stepDelta) >= stepDistance) {
                            val dir = if (stepDelta > 0) 1 else -1
                            onCursorMoveListener?.invoke(dir)
                            performHaptic()
                            lastStepX = event.x
                        }
                    }
                    true
                }
                MotionEvent.ACTION_UP -> {
                    if (!isDraggingCursor) {
                        performHaptic()
                        onKeyClickListener?.invoke(KeyAction.Character(" "))
                    }
                    isDraggingCursor = false
                    true
                }
                else -> false
            }
        }

        return keyBox
    }

    private fun createActionKey(symbol: String, weight: Float, onClick: () -> Unit): View {
        val keyBox = FrameLayout(context).apply {
            layoutParams = LayoutParams(0, LayoutParams.MATCH_PARENT, weight).apply {
                val hPad = dp(skin.keySpacingHDp / 2f)
                setMargins(hPad, 0, hPad, 0)
            }
            background = skin.buildKeyDrawable(density, special = true, isAction = true)
            isClickable = true
            isFocusable = true
        }

        val tv = TextView(context).apply {
            text = symbol
            setTextSize(TypedValue.COMPLEX_UNIT_SP, 16f * skin.fontScale)
            setTextColor(skin.onAccentText)
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

    private fun performHaptic() {
        try {
            performHapticFeedback(
                HapticFeedbackConstants.KEYBOARD_TAP,
                HapticFeedbackConstants.FLAG_IGNORE_GLOBAL_SETTING
            )
        } catch (_: Exception) {}
    }
}
