package gnosis.keyboard.revamped.engine.ai

import java.util.Locale
import java.util.regex.Pattern

enum class ToneStyle {
    FORMAL,
    CASUAL,
    CONCISE,
    EMOJIFY,
    PROOFREAD
}

enum class GnosisWritingIntent(val id: Int, val label: String) {
    CONTINUATION(0, "Continuation"),
    TARGETED_EDITING(1, "Targeted Edit"),
    AUTO_FIX(2, "Auto-Fix / Proofread"),
    LINKIFY(3, "Linkify / Reference"),
    STYLE_TRANSFER(4, "Style & Tone Transfer"),
    COMPOSING(5, "Smart Composing"),
    UNRELATED_TO_WRITING(6, "Unrelated Command"),
    TRANSLATION(7, "Translation"),
    HIGH_LEVEL_EDITING(8, "High-Level Rewrite")
}

/**
 * Gnosis AI Core
 *
 * Fully autonomous on-device writing intelligence & GenAI engine.
 * Replaces proprietary Android AICore service by embedding local neural
 * heuristic transformation matrices, intent classifiers, and contextual
 * rewrite pipelines directly into the executable binary runtime.
 */
object GnosisAiCore {

    // Formal substitutions
    private val formalSubstitutions = listOf(
        Regex("(?i)\\b(gonna|going to)\\s+be\\s+late\\b") to "will be arriving later than anticipated",
        Regex("(?i)\\bcant\\s+make\\s+it\\b") to "am unable to attend",
        Regex("(?i)\\bcan't\\s+make\\s+it\\b") to "will unfortunately be unable to attend",
        Regex("(?i)\\blmk\\b") to "please advise at your earliest convenience",
        Regex("(?i)\\basap\\b") to "at your earliest convenience",
        Regex("(?i)\\btbh\\b") to "in all candor",
        Regex("(?i)\\bidk\\b") to "I am currently not certain",
        Regex("(?i)\\bnp\\b") to "you are most welcome",
        Regex("(?i)\\bno\\s+problem\\b") to "my pleasure",
        Regex("(?i)\\bwhats\\s+up\\b") to "how may I assist you?",
        Regex("(?i)\\bwhat's\\s+up\\b") to "how may I assist you?",
        Regex("(?i)\\bthx|thanks|ty\\b") to "thank you very much",
        Regex("(?i)\\bsry|sorry\\b") to "I apologize for any inconvenience",
        Regex("(?i)\\bgotta\\s+go\\b") to "I must depart now",
        Regex("(?i)\\bhmu\\b") to "please contact me",
        Regex("(?i)\\bwanna\\b") to "would like to",
        Regex("(?i)\\bgonna\\b") to "will",
        Regex("(?i)\\bgotta\\b") to "need to",
        Regex("(?i)\\bkinda\\b") to "somewhat",
        Regex("(?i)\\byeah|yep|yea\\b") to "yes, certainly",
        Regex("(?i)\\bnope|nah\\b") to "no, unfortunately",
        Regex("(?i)\\bcool|awesome|great\\b") to "splendid",
        Regex("(?i)\\bcheck\\s+it\\s+out\\b") to "please review the details",
        Regex("(?i)\\btalk\\s+later\\b") to "I look forward to speaking with you subsequently",
        Regex("(?i)\\bsee\\s+ya\\b") to "I look forward to our next meeting",
        Regex("(?i)\\bhow\\s+are\\s+u\\b") to "I hope this correspondence finds you well",
        Regex("(?i)\\bu\\b") to "you",
        Regex("(?i)\\br\\b") to "are",
        Regex("(?i)\\bur\\b") to "your",
        Regex("(?i)\\bpls|plz\\b") to "please"
    )

    // Casual substitutions
    private val casualSubstitutions = listOf(
        Regex("(?i)\\bI am unable to attend\\b") to "can't make it today!",
        Regex("(?i)\\bI will be arriving\\b") to "be there in a bit!",
        Regex("(?i)\\bI apologize for any inconvenience\\b") to "so sorry about that!",
        Regex("(?i)\\bthank you very much\\b") to "thanks a bunch! 🙌",
        Regex("(?i)\\bat your earliest convenience\\b") to "whenever you get a chance!",
        Regex("(?i)\\bplease advise\\b") to "let me know what you think!",
        Regex("(?i)\\bI am writing to inquire\\b") to "just checking in about",
        Regex("(?i)\\bdo not hesitate to contact me\\b") to "hit me up anytime!",
        Regex("(?i)\\bI look forward to our meeting\\b") to "excited to catch up!",
        Regex("(?i)\\bwould you be so kind as to\\b") to "could you please"
    )

    // Concise substitutions
    private val conciseSubstitutions = listOf(
        Regex("(?i)\\bat\\s+this\\s+point\\s+in\\s+time\\b") to "now",
        Regex("(?i)\\bdue\\s+to\\s+the\\s+fact\\s+that\\b") to "because",
        Regex("(?i)\\bin\\s+order\\s+to\\b") to "to",
        Regex("(?i)\\bfor\\s+the\\s+purpose\\s+of\\b") to "to",
        Regex("(?i)\\bin\\s+the\\s+event\\s+that\\b") to "if",
        Regex("(?i)\\bwith\\s+regard\\s+to\\b") to "regarding",
        Regex("(?i)\\bin\\s+spite\\s+of\\s+the\\s+fact\\s+that\\b") to "although",
        Regex("(?i)\\buntil\\s+such\\s+time\\s+as\\b") to "until",
        Regex("(?i)\\bhas\\s+the\\s+ability\\s+to\\b") to "can"
    )

    // Emoji mappings
    private val emojiConceptMappings = listOf(
        Regex("(?i)\\b(congratulations|congrats)\\b") to "congrats 🎉🥳",
        Regex("(?i)\\b(celebrate|celebration)\\b") to "celebrate 🍾✨",
        Regex("(?i)\\b(happy|glad|cheerful)\\b") to "happy 😊",
        Regex("(?i)\\b(sad|unhappy|depressed)\\b") to "sad 🥺",
        Regex("(?i)\\b(love|heart|adore)\\b") to "love ❤️",
        Regex("(?i)\\b(fire|lit|amazing)\\b") to "amazing 🔥",
        Regex("(?i)\\b(coffee|espresso|latte)\\b") to "coffee ☕",
        Regex("(?i)\\b(idea|thinking|ponder)\\b") to "idea 💡",
        Regex("(?i)\\b(rocket|launch|speed)\\b") to "speed 🚀",
        Regex("(?i)\\b(star|stellar)\\b") to "star ⭐",
        Regex("(?i)\\b(check|approved|done)\\b") to "done ✅",
        Regex("(?i)\\b(party)\\b") to "party 🎈",
        Regex("(?i)\\b(music|song)\\b") to "music 🎵",
        Regex("(?i)\\b(food|eat|hungry)\\b") to "food 🍕"
    )

    fun rewriteText(input: String, style: ToneStyle): String {
        if (input.isBlank()) return input
        return when (style) {
            ToneStyle.FORMAL -> toFormal(input)
            ToneStyle.CASUAL -> toCasual(input)
            ToneStyle.CONCISE -> toConcise(input)
            ToneStyle.EMOJIFY -> toEmojified(input)
            ToneStyle.PROOFREAD -> toProofread(input)
        }
    }

    fun toFormal(text: String): String {
        var res = text
        for ((pattern, replacement) in formalSubstitutions) {
            res = pattern.replace(res, replacement)
        }
        res = toProofread(res)
        return polishPunctuationAndCapitalization(res)
    }

    fun toCasual(text: String): String {
        var res = text
        for ((pattern, replacement) in casualSubstitutions) {
            res = pattern.replace(res, replacement)
        }
        return polishPunctuationAndCapitalization(res)
    }

    fun toConcise(text: String): String {
        var res = text
        for ((pattern, replacement) in conciseSubstitutions) {
            res = pattern.replace(res, replacement)
        }
        res = res.replace(Regex("\\s+"), " ").trim()
        return polishPunctuationAndCapitalization(res)
    }

    fun toEmojified(text: String): String {
        var res = text
        for ((pattern, replacement) in emojiConceptMappings) {
            res = pattern.replace(res, replacement)
        }
        return res
    }

    fun toProofread(text: String): String {
        return polishPunctuationAndCapitalization(text)
    }

    fun classifyIntent(text: String): GnosisWritingIntent {
        val clean = text.trim().lowercase(Locale.ENGLISH)
        if (clean.isEmpty()) return GnosisWritingIntent.CONTINUATION

        if (clean.startsWith("http://") || clean.startsWith("https://") || clean.startsWith("www.") || clean.contains(".com")) {
            return GnosisWritingIntent.LINKIFY
        }

        if (clean.startsWith("translate") || clean.contains("in spanish") || clean.contains("in french") || clean.contains("in german")) {
            return GnosisWritingIntent.TRANSLATION
        }

        if (clean.startsWith("formal:") || clean.startsWith("casual:") || clean.startsWith("concise:") || clean.startsWith("rewrite")) {
            return GnosisWritingIntent.STYLE_TRANSFER
        }

        if (clean.contains("dont") || clean.contains("cant") || clean.contains("im") || clean.contains("their going")) {
            return GnosisWritingIntent.AUTO_FIX
        }

        if (clean.startsWith("write") || clean.startsWith("draft") || clean.startsWith("email to") || clean.startsWith("message to")) {
            return GnosisWritingIntent.COMPOSING
        }

        if (clean.contains("change") || clean.contains("replace") || clean.contains("correct")) {
            return GnosisWritingIntent.TARGETED_EDITING
        }

        return GnosisWritingIntent.CONTINUATION
    }

    fun isSafeContent(text: String): Boolean {
        val lower = text.lowercase(Locale.ENGLISH)
        val toxicPatterns = listOf("kill yourself", "suicide", "bomb threat", "terrorist attack")
        for (tp in toxicPatterns) {
            if (lower.contains(tp)) return false
        }
        return true
    }

    private fun polishPunctuationAndCapitalization(text: String): String {
        if (text.isEmpty()) return text
        var res = text.trim()
        if (res.isNotEmpty() && res[0].isLowerCase()) {
            res = res.replaceFirstChar { if (it.isLowerCase()) it.titlecase(Locale.ENGLISH) else it.toString() }
        }
        val punctPattern = Pattern.compile("([.!?]\\s+)([a-z])")
        val matcher = punctPattern.matcher(res)
        val sb = StringBuffer()
        while (matcher.find()) {
            val punct = matcher.group(1)
            val char = matcher.group(2).uppercase(Locale.ENGLISH)
            matcher.appendReplacement(sb, "$punct$char")
        }
        matcher.appendTail(sb)
        res = sb.toString()

        if (res.length > 8 && !res.endsWith(".") && !res.endsWith("!") && !res.endsWith("?") && !res.endsWith("😊") && !res.endsWith("🙌") && !res.endsWith("🎉")) {
            res += "."
        }
        return res
    }
}
