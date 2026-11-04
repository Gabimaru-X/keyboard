package gnosis.keyboard.revamped.engine

import android.content.Context
import org.json.JSONObject
import java.io.File
import java.util.concurrent.ConcurrentHashMap

/**
 * Revamped Dynamic Language Model (Dynamic LM)
 *
 * Tracks vocabulary frequencies and bigram context transitions dynamically
 * on-device with zero external telemetry and strict local privacy boundaries.
 */
class RevampedDynamicLm(private val context: Context? = null) {

    private val unigramFrequencies = ConcurrentHashMap<String, Int>()
    private val bigramTransitions = ConcurrentHashMap<String, ConcurrentHashMap<String, Int>>()
    private val suppressedCorrections = ConcurrentHashMap.newKeySet<String>()

    init {
        loadPersistedData()
    }

    fun learnWord(word: String, prevWord: String? = null) {
        val clean = word.trim().lowercase()
        if (clean.length < 2 || clean.any { it.isDigit() }) return

        unigramFrequencies.compute(clean) { _, v -> (v ?: 0) + 1 }

        if (!prevWord.isNullOrEmpty()) {
            val cleanPrev = prevWord.trim().lowercase()
            if (cleanPrev.isNotEmpty()) {
                bigramTransitions
                    .computeIfAbsent(cleanPrev) { ConcurrentHashMap() }
                    .compute(clean) { _, v -> (v ?: 0) + 1 }
            }
        }
    }

    fun learnNgram(prevWord: String, currentWord: String) {
        learnWord(currentWord, prevWord)
    }

    fun getBigramProbability(prevWord: String, currentWord: String): Float {
        val cleanPrev = prevWord.trim().lowercase()
        val cleanCurr = currentWord.trim().lowercase()
        val map = bigramTransitions[cleanPrev] ?: return 0.0f
        val count = map[cleanCurr] ?: return 0.0f
        val total = map.values.sum().toFloat().coerceAtLeast(1.0f)
        return count / total
    }

    fun predictNext(prevWord: String): String? {
        val cleanPrev = prevWord.trim().lowercase()
        val map = bigramTransitions[cleanPrev] ?: return null
        return map.entries.maxByOrNull { it.value }?.key
    }

    fun predictNextWords(prefix: String): List<String> {
        val clean = prefix.trim().lowercase()
        val list = mutableListOf<String>()
        val next = predictNext(clean)
        if (next != null) list.add(next)

        unigramFrequencies.entries
            .filter { it.key.startsWith(clean) }
            .sortedByDescending { it.value }
            .take(3)
            .forEach { if (!list.contains(it.key)) list.add(it.key) }

        return list
    }

    fun getUnigramFrequency(word: String): Int {
        return unigramFrequencies[word.trim().lowercase()] ?: 0
    }

    fun suppressCorrection(word: String) {
        suppressedCorrections.add(word.trim().lowercase())
    }

    fun isSuppressed(word: String): Boolean {
        return suppressedCorrections.contains(word.trim().lowercase())
    }

    fun getVocabularySize(): Int = unigramFrequencies.size

    private fun loadPersistedData() {
        if (context == null) return
        try {
            val file = File(context.filesDir, "revamped_dynamic_lm.json")
            if (!file.exists()) return
            val json = JSONObject(file.readText())
            val unigrams = json.optJSONObject("unigrams")
            if (unigrams != null) {
                for (k in unigrams.keys()) {
                    unigramFrequencies[k] = unigrams.getInt(k)
                }
            }
        } catch (_: Exception) {}
    }

    fun persistData() {
        if (context == null) return
        try {
            val file = File(context.filesDir, "revamped_dynamic_lm.json")
            val json = JSONObject()
            val unigrams = JSONObject()
            for ((k, v) in unigramFrequencies) {
                unigrams.put(k, v)
            }
            json.put("unigrams", unigrams)
            file.writeText(json.toString())
        } catch (_: Exception) {}
    }
}
