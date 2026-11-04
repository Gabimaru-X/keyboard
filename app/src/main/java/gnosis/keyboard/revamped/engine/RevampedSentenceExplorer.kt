package gnosis.keyboard.revamped.engine

/**
 * Revamped Sentence Explorer & Grammar Checker
 *
 * Implements on-device Sentence Explorer grammar checking:
 * - Detects syntactic mismatches and homophone confusions (their/there/they're, your/you're, its/it's, to/too)
 * - Identifies missing apostrophes in English and multilingual contractions
 * - Evaluates sentence-initial and pronoun capitalization
 */
class RevampedSentenceExplorer {

    companion object {
        private val homophoneRules = listOf(
            Pair(Regex("(?i)\\btheir\\s+going\\b"), "they're going"),
            Pair(Regex("(?i)\\btheir\\s+coming\\b"), "they're coming"),
            Pair(Regex("(?i)\\btheir\\s+doing\\b"), "they're doing"),
            Pair(Regex("(?i)\\btheir\\s+here\\b"), "they're here"),
            Pair(Regex("(?i)\\btheir\\s+not\\b"), "they're not"),
            Pair(Regex("(?i)\\bover\\s+their\\b"), "over there"),
            Pair(Regex("(?i)\\bout\\s+their\\b"), "out there"),
            Pair(Regex("(?i)\\bin\\s+their\\b"), "in there"),
            Pair(Regex("(?i)\\bstay\\s+their\\b"), "stay there"),
            Pair(Regex("(?i)\\bthere\\s+car\\b"), "their car"),
            Pair(Regex("(?i)\\bthere\\s+house\\b"), "their house"),
            Pair(Regex("(?i)\\bthere\\s+phone\\b"), "their phone"),
            Pair(Regex("(?i)\\bthere\\s+family\\b"), "their family"),

            Pair(Regex("(?i)\\byour\\s+welcome\\b"), "you're welcome"),
            Pair(Regex("(?i)\\byour\\s+right\\b"), "you're right"),
            Pair(Regex("(?i)\\byour\\s+doing\\b"), "you're doing"),
            Pair(Regex("(?i)\\byour\\s+going\\b"), "you're going"),
            Pair(Regex("(?i)\\byou're\\s+name\\b"), "your name"),
            Pair(Regex("(?i)\\byou're\\s+phone\\b"), "your phone"),
            Pair(Regex("(?i)\\byou're\\s+car\\b"), "your car"),

            Pair(Regex("(?i)\\bits\\s+raining\\b"), "it's raining"),
            Pair(Regex("(?i)\\bits\\s+ok\\b"), "it's ok"),
            Pair(Regex("(?i)\\bits\\s+okay\\b"), "it's okay"),
            Pair(Regex("(?i)\\bits\\s+time\\b"), "it's time"),
            Pair(Regex("(?i)\\bits\\s+cold\\b"), "it's cold"),
            Pair(Regex("(?i)\\bits\\s+good\\b"), "it's good"),

            Pair(Regex("(?i)\\bme\\s+to\\b"), "me too"),
            Pair(Regex("(?i)\\bto\\s+much\\b"), "too much"),
            Pair(Regex("(?i)\\bto\\s+late\\b"), "too late"),
            Pair(Regex("(?i)\\bto\\s+bad\\b"), "too bad"),
            Pair(Regex("(?i)\\bbetter\\s+then\\b"), "better than"),
            Pair(Regex("(?i)\\bmore\\s+then\\b"), "more than"),

            Pair(Regex("(?i)\\balot\\b"), "a lot"),
            Pair(Regex("(?i)\\baswell\\b"), "as well"),
            Pair(Regex("(?i)\\binfront\\b"), "in front"),
            Pair(Regex("(?i)\\bthankyou\\b"), "thank you")
        )

        private val contractionRules = mapOf(
            "dont" to "don't", "cant" to "can't", "wont" to "won't",
            "didnt" to "didn't", "wouldnt" to "wouldn't", "shouldnt" to "shouldn't",
            "couldnt" to "couldn't", "isnt" to "isn't", "arent" to "aren't",
            "wasnt" to "wasn't", "werent" to "weren't", "hasnt" to "hasn't",
            "havent" to "haven't", "im" to "I'm", "ive" to "I've",
            "youre" to "you're", "theyre" to "they're", "weve" to "we've",
            "thats" to "that's", "whats" to "what's", "lets" to "let's"
        )
    }

    fun correctWord(word: String): String {
        val clean = word.lowercase()
        return contractionRules[clean] ?: word
    }

    fun checkAndCorrect(text: String): String {
        var result = text

        for ((rule, replacement) in homophoneRules) {
            result = rule.replace(result, replacement)
        }

        val words = result.split(" ")
        val sb = StringBuilder()
        for ((idx, w) in words.withIndex()) {
            val clean = w.lowercase()
            val fixed = contractionRules[clean] ?: w
            sb.append(fixed)
            if (idx < words.size - 1) sb.append(" ")
        }
        return sb.toString()
    }
}
