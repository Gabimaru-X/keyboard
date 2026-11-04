package gnosis.keyboard.revamped

import android.content.Context
import org.json.JSONArray
import org.json.JSONObject

/**
 * Revamped Quick Keys Manager
 *
 * Imported directly from Gnosis Keyboard:
 * Manages customizable quick-action toolbar chips, snippet insertions,
 * and quick utility functions (Emoji, Clipboard, Resizer, Voice, Settings).
 */
object RevampedQuickKeys {

    data class QuickKeyItem(
        val id: String,
        val label: String,
        val iconName: String,
        val actionType: ActionType,
        val payload: String = ""
    )

    data class QuickKeySnippet(
        val id: String,
        val text: String
    )

    enum class ActionType {
        EMOJI,
        CLIPBOARD,
        RESIZER,
        VOICE,
        NEURAL_AI,
        SETTINGS,
        SNIPPET
    }

    val DEFAULT_QUICK_KEYS = listOf(
        QuickKeyItem("emoji", "Emoji", "ic_smile", ActionType.EMOJI),
        QuickKeyItem("neural_ai", "Neural AI", "ic_wand_sparkles", ActionType.NEURAL_AI),
        QuickKeyItem("clipboard", "Clipboard", "ic_clipboard", ActionType.CLIPBOARD),
        QuickKeyItem("voice", "Voice", "ic_mic", ActionType.VOICE),
        QuickKeyItem("resizer", "Resize", "ic_scaling", ActionType.RESIZER),
        QuickKeyItem("settings", "Settings", "ic_settings", ActionType.SETTINGS)
    )

    fun loadQuickKeys(context: Context): List<QuickKeyItem> {
        val prefs = context.getSharedPreferences("revamped_prefs", Context.MODE_PRIVATE)
        val raw = prefs.getString("quick_keys_json", null) ?: return DEFAULT_QUICK_KEYS
        return try {
            val arr = JSONArray(raw)
            val list = mutableListOf<QuickKeyItem>()
            for (i in 0 until arr.length()) {
                val obj = arr.getJSONObject(i)
                list.add(
                    QuickKeyItem(
                        id = obj.getString("id"),
                        label = obj.getString("label"),
                        iconName = obj.optString("iconName", "ic_sparkles"),
                        actionType = ActionType.valueOf(obj.getString("actionType")),
                        payload = obj.optString("payload", "")
                    )
                )
            }
            if (list.isEmpty()) DEFAULT_QUICK_KEYS else list
        } catch (_: Exception) {
            DEFAULT_QUICK_KEYS
        }
    }

    fun saveQuickKeys(context: Context, items: List<QuickKeyItem>) {
        val prefs = context.getSharedPreferences("revamped_prefs", Context.MODE_PRIVATE)
        val arr = JSONArray()
        for (item in items) {
            val obj = JSONObject().apply {
                put("id", item.id)
                put("label", item.label)
                put("iconName", item.iconName)
                put("actionType", item.actionType.name)
                put("payload", item.payload)
            }
            arr.put(obj)
        }
        prefs.edit().putString("quick_keys_json", arr.toString()).apply()
    }

    fun getQuickKeys(context: Context): List<QuickKeySnippet> {
        val items = loadQuickKeys(context)
        val snippets = items.filter { it.actionType == ActionType.SNIPPET }.map {
            QuickKeySnippet(it.id, if (it.payload.isNotEmpty()) it.payload else it.label)
        }
        if (snippets.isEmpty()) {
            return listOf(
                QuickKeySnippet("1", "👋 Hello!"),
                QuickKeySnippet("2", "📍 Be right there"),
                QuickKeySnippet("3", "👍 Looks great!")
            )
        }
        return snippets
    }

    fun addQuickKey(context: Context, text: String) {
        val current = loadQuickKeys(context).toMutableList()
        current.add(QuickKeyItem(System.currentTimeMillis().toString(), text, "ic_sparkles", ActionType.SNIPPET, text))
        saveQuickKeys(context, current)
    }
}
