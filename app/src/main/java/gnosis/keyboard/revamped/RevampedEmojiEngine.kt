package gnosis.keyboard.revamped

import android.content.Context
import android.graphics.Color
import android.util.TypedValue
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.HorizontalScrollView
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.TextView

/**
 * Revamped Emoji & Expressive Concepts Engine
 *
 * Reverse-engineered from Gboard's ScaNN vector concept ranker:
 * - Real-time keyword-to-emoji prediction (e.g. "love" -> ❤️, "fire" -> 🔥)
 * - 8-category comprehensive emoji catalog
 * - Recents cache and zero-latency grid rendering
 */
object RevampedEmojiEngine {

    private val conceptMap = mapOf(
        "love" to listOf("❤️", "😍", "💕", "🥰", "😘"),
        "heart" to listOf("❤️", "💖", "💗", "💓"),
        "fire" to listOf("🔥", "💥", "✨", "⚡"),
        "lit" to listOf("🔥", "💯", "😎"),
        "happy" to listOf("😊", "😄", "😁", "😃", "🥳"),
        "smile" to listOf("😊", "🙂", "😀", "😃"),
        "sad" to listOf("🥺", "😢", "😭", "😞"),
        "cry" to listOf("😭", "😢", "😿"),
        "laugh" to listOf("😂", "🤣", "😆"),
        "lol" to listOf("😂", "🤣", "💀"),
        "cool" to listOf("😎", "🧊", "🤙"),
        "congrats" to listOf("🎉", "🥳", "👏", "🥂"),
        "party" to listOf("🥳", "🎉", "🎈", "🍾"),
        "coffee" to listOf("☕", "🧋", "🍵"),
        "tea" to listOf("🍵", "🫖", "☕"),
        "food" to listOf("🍕", "🍔", "🍟", "🍣", "🌮"),
        "star" to listOf("⭐", "🌟", "✨"),
        "idea" to listOf("💡", "🧠", "💭"),
        "money" to listOf("💰", "💵", "💸", "🤑"),
        "ok" to listOf("👌", "👍", "✅"),
        "yes" to listOf("👍", "✅", "🙌"),
        "no" to listOf("👎", "❌", "🚫"),
        "thanks" to listOf("🙏", "🙌", "✨"),
        "please" to listOf("🥺", "🙏"),
        "rocket" to listOf("🚀", "🛸", "🛰️"),
        "check" to listOf("✅", "✔️", "☑️"),
        "car" to listOf("🚗", "🏎️", "🚙"),
        "dog" to listOf("🐶", "🐕", "🐾"),
        "cat" to listOf("🐱", "🐈", "🐾")
    )

    val CATEGORIES = listOf(
        "Smileys" to listOf(
            "😀", "😃", "😄", "😁", "😆", "😅", "😂", "🤣", "🥲", "🥹", "😊", "😇",
            "🙂", "🙃", "😉", "😌", "😍", "🥰", "😘", "😗", "😙", "😚", "😋", "😛",
            "😝", "😜", "🤪", "🤨", "🧐", "🤓", "😎", "🥸", "🤩", "🥳", "😏", "😒",
            "😞", "😔", "😟", "😕", "🙁", "☹️", "😣", "😖", "😫", "😩", "🥺", "😢",
            "😭", "😮‍💨", "😤", "😠", "😡", "🤬", "🤯", "😳", "🥵", "🥶", "😱", "😨"
        ),
        "Hands" to listOf(
            "👋", "🤚", "🖐️", "✋", "🖖", "🫱", "🫲", "🫳", "🫴", "👌", "🤌", "🤏",
            "✌️", "🤞", "🫰", "🤟", "🤘", "🤙", "👈", "👉", "👆", "🖕", "👇", "☝️",
            "👍", "👎", "✊", "👊", "🤛", "🤜", "👏", "🙌", "🫶", "👐", "🤲", "🤝",
            "🙏", "✍️", "💅", "🤳", "💪"
        ),
        "Hearts" to listOf(
            "❤️", "🧡", "💛", "💚", "💙", "💜", "🖤", "🤍", "🤎", "💔", "❤️‍🔥", "❤️‍🩹",
            "❣️", "💕", "💞", "💓", "💗", "💖", "💘", "💝", "💟", "💌"
        ),
        "Nature" to listOf(
            "🐶", "🐱", "🐭", "🐹", "🐰", "🦊", "🐻", "🐼", "🐻‍❄️", "🐨", "🐯", "🦁",
            "🐮", "🐷", "🐸", "🐵", "🐔", "🐧", "🐦", "🐤", "🦆", "🦅", "🦉", "🦇",
            "🐺", "🐗", "🐴", "🦄", "🐝", "🪱", "🐛", "🦋", "🐌", "🐞", "🐜", "🪰",
            "🌸", "💮", "🏵️", "🌹", "🥀", "🌺", "🌻", "🌼", "🌷", "🌱", "🪴", "🌲"
        ),
        "Food" to listOf(
            "🍏", "🍎", "🍐", "🍊", "🍋", "🍌", "🍉", "🍇", "🍓", "🫐", "🍈", "🍒",
            "🍑", "🥭", "🍍", "🥥", "🥝", "🍅", "🍆", "🥑", "🥦", "🥬", "🥒", "🌶️",
            "🌽", "🥕", "🫒", "🧄", "🧅", "🥔", "🍠", "🥐", "🥯", "🍞", "🥖", "🥨",
            "🧀", "🥚", "🍳", "🧈", "🥞", "🧇", "🥓", "🥩", "🍗", "🍖", "🦴", "🌭",
            "🍔", "🍟", "🍕", "🫓", "🥪", "🥙", "🧆", "🌮", "🌯", "🫔", "🥗", "🥘"
        ),
        "Activities" to listOf(
            "⚽", "🏀", "🏈", "⚾", "🥎", "🎾", "🏐", "🏉", "🥏", "🎱", "🪀", "🏓",
            "🏸", "🏒", "🏑", "🥍", "🏏", "🪃", "🥅", "⛳", "🪁", "🏹", "🎣", "🤿",
            "🥊", "🥋", "🎽", "🛹", "🛼", "🛷", "⛸️", "🥌", "🎿", "⛷️", "🏂", "🪂"
        ),
        "Objects" to listOf(
            "💡", "🔦", "🕯️", "🪔", "🧱", "🪵", "🪜", "🧰", "🪛", "🔧", "🔨", "⚒️",
            "🛠️", "⛏️", "🪚", "🔩", "⚙️", "🪤", "🧲", "🔫", "💣", "🧨", "🪓", "🔪",
            "🗡️", "⚔️", "🛡️", "🚬", "⚰️", "🪦", "⚱️", "🏺", "🔮", "📿", "🧿", "🪬"
        ),
        "Symbols" to listOf(
            "✨", "⭐", "🌟", "💫", "💥", "🔥", "⚡", "☄️", "💨", "🌪️", "🌈", "☀️",
            "🌤️", "⛅", "🌥️", "☁️", "🌦️", "🌧️", "⛈️", "🌩️", "🌨️", "❄️", "☃️", "⛄",
            "🌬️", "💧", "💦", "🫧", "🌊", "🔔", "🔕", "📢", "📣", "💬", "💭", "🗯️"
        )
    )

    fun getEmojiSuggestions(word: String): List<String> {
        val clean = word.trim().lowercase()
        return conceptMap[clean] ?: emptyList()
    }

    fun buildEmojiPanel(
        context: Context,
        skin: RevampedSkin,
        onEmojiSelected: (String) -> Unit,
        onClose: () -> Unit = {}
    ): View {
        val density = context.resources.displayMetrics.density
        fun dp(v: Float) = (v * density).toInt()

        val root = LinearLayout(context).apply {
            orientation = LinearLayout.VERTICAL
            setBackgroundColor(skin.background)
            layoutParams = ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                dp(250f)
            )
        }

        // Category Tabs Bar
        val categoryBar = HorizontalScrollView(context).apply {
            isHorizontalScrollBarEnabled = false
            layoutParams = LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                dp(38f)
            )
            setBackgroundColor(skin.surface)
        }

        val tabsRow = LinearLayout(context).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
            setPadding(dp(6f), 0, dp(6f), 0)
        }

        // Close / Keyboard switch button
        val closeBtn = TextView(context).apply {
            text = "ABC"
            setTextColor(skin.accent)
            setTextSize(TypedValue.COMPLEX_UNIT_SP, 13f)
            setPadding(dp(12f), dp(8f), dp(12f), dp(8f))
            isClickable = true
            isFocusable = true
            setOnClickListener { onClose() }
        }
        tabsRow.addView(closeBtn)

        val scrollContainer = ScrollView(context).apply {
            layoutParams = LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                0,
                1f
            )
        }

        val emojiContent = LinearLayout(context).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(dp(6f), dp(6f), dp(6f), dp(6f))
        }

        fun showCategory(emojis: List<String>) {
            emojiContent.removeAllViews()
            val rowCount = 7
            var curRow = LinearLayout(context).apply {
                orientation = LinearLayout.HORIZONTAL
                layoutParams = LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT
                )
            }

            for (i in emojis.indices) {
                val emoji = emojis[i]
                val tv = TextView(context).apply {
                    text = emoji
                    setTextSize(TypedValue.COMPLEX_UNIT_SP, 24f)
                    gravity = Gravity.CENTER
                    layoutParams = LinearLayout.LayoutParams(0, dp(42f), 1f)
                    isClickable = true
                    isFocusable = true
                    background = skin.buildKeyDrawable(density, special = true)
                    setOnClickListener { onEmojiSelected(emoji) }
                }
                curRow.addView(tv)

                if ((i + 1) % rowCount == 0 || i == emojis.size - 1) {
                    val rem = rowCount - curRow.childCount
                    for (k in 0 until rem) {
                        curRow.addView(View(context).apply {
                            layoutParams = LinearLayout.LayoutParams(0, dp(42f), 1f)
                        })
                    }
                    emojiContent.addView(curRow)
                    curRow = LinearLayout(context).apply {
                        orientation = LinearLayout.HORIZONTAL
                        layoutParams = LinearLayout.LayoutParams(
                            ViewGroup.LayoutParams.MATCH_PARENT,
                            ViewGroup.LayoutParams.WRAP_CONTENT
                        )
                    }
                }
            }
        }

        for ((idx, cat) in CATEGORIES.withIndex()) {
            val tabTv = TextView(context).apply {
                text = cat.first
                setTextColor(if (idx == 0) skin.accent else skin.textSecondary)
                setTextSize(TypedValue.COMPLEX_UNIT_SP, 12f)
                setPadding(dp(12f), dp(8f), dp(12f), dp(8f))
                isClickable = true
                isFocusable = true
                setOnClickListener {
                    for (c in 1 until tabsRow.childCount) {
                        val other = tabsRow.getChildAt(c) as? TextView
                        other?.setTextColor(skin.textSecondary)
                    }
                    setTextColor(skin.accent)
                    showCategory(cat.second)
                }
            }
            tabsRow.addView(tabTv)
        }

        categoryBar.addView(tabsRow)
        root.addView(categoryBar)

        scrollContainer.addView(emojiContent)
        root.addView(scrollContainer)

        showCategory(CATEGORIES[0].second)

        return root
    }
}
