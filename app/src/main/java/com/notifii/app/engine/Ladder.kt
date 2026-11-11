package com.notifii.app.engine

import com.notifii.app.data.Placement
import com.notifii.app.data.Rule
import com.notifii.app.data.Rung

/** A notification reduced to what decisions need. */
data class Note(
    val pkg: String,
    val appLabel: String,
    val title: String,
    val text: String,
    val sender: String?,
    val postedAt: Long,
)

data class Decision(val rung: Rung, val reason: String, val ruleId: Long? = null)

const val BURST_COUNT = 3
const val BURST_WINDOW_MS = 2 * 60_000L

/**
 * The whole product in one function. Order: exceptions (loudest match wins, so you never miss),
 * then the person's rung, then the app's rung, then Ring. Night turns Ring into Later.
 */
fun decide(
    note: Note,
    placements: List<Placement>,
    rules: List<Rule>,
    recentCount: Int,
    night: Boolean,
): Decision {
    val matched = rules.filter { matches(it, note, recentCount) }
    val person = note.sender?.takeIf { it.isNotBlank() }?.let { s -> placements.firstOrNull { it.pkg == note.pkg && it.person.equals(s, ignoreCase = true) } }
    val app = placements.firstOrNull { it.pkg == note.pkg && it.isApp }

    val base = when {
        matched.isNotEmpty() -> {
            val loudest = matched.minBy { it.rung.ordinal }
            Decision(loudest.rung, "Exception: ${describe(loudest)}", loudest.id)
        }
        person != null -> Decision(person.rung, "${person.person} is on ${person.rung.label}")
        app != null -> Decision(app.rung, "${note.appLabel} is on ${app.rung.label}")
        else -> Decision(Rung.RING, "${note.appLabel} isn’t sorted yet, so it rings normally")
    }
    return if (night && base.rung == Rung.RING) base.copy(rung = Rung.LATER, reason = "${base.reason}. Night is on, so it waits for morning")
    else base
}

fun matches(rule: Rule, note: Note, recentCount: Int): Boolean {
    if (rule.pkg != null && rule.pkg != note.pkg) return false
    if (rule.sender != null) {
        val s = rule.sender
        val fromThem = note.sender?.equals(s, ignoreCase = true) == true || note.title.contains(s, ignoreCase = true)
        if (!fromThem) return false
    }
    val body = "${note.title} ${note.text}"
    val words = rule.wordList
    if (words.isNotEmpty() && words.none { body.contains(it, ignoreCase = true) }) return false
    if (rule.minAmount != null) {
        val amount = largestAmount(body) ?: return false
        if (amount < rule.minAmount) return false
    }
    if (rule.burst && recentCount < BURST_COUNT) return false
    return true
}

private val CURRENCY_FIRST = Regex("""(?:[$€£₹¥]|rs\.?|inr|usd|eur)\s?(\d{1,3}(?:[,\s]\d{3})*(?:\.\d+)?|\d+(?:\.\d+)?)""", RegexOption.IGNORE_CASE)
private val CURRENCY_LAST = Regex("""(\d{1,3}(?:,\d{3})*(?:\.\d+)?|\d+(?:\.\d+)?)\s?(?:usd|eur|inr|rs|dollars|rupees)\b""", RegexOption.IGNORE_CASE)

/** The biggest money amount in the text ("$24.50", "₹1,200", "30 USD"), or null. */
fun largestAmount(text: String): Double? =
    (CURRENCY_FIRST.findAll(text) + CURRENCY_LAST.findAll(text))
        .mapNotNull { it.groupValues[1].replace(",", "").replace(" ", "").toDoubleOrNull() }
        .maxOrNull()

/** "From Upwork · contains “invited you” → Siren" */
fun describe(rule: Rule): String {
    val parts = buildList {
        rule.appLabel?.let { add("from $it") }
        rule.sender?.let { add("by $it") }
        if (rule.wordList.isNotEmpty()) add("containing " + rule.wordList.joinToString(" or ") { "“$it”" })
        rule.minAmount?.let { add("amount over ${trimAmount(it)}") }
        if (rule.burst) add("$BURST_COUNT+ within 2 minutes")
    }
    val condition = parts.joinToString(" · ").replaceFirstChar { it.uppercase() }.ifEmpty { "Anything" }
    return "$condition → ${rule.rung.label}"
}

fun trimAmount(v: Double): String = if (v % 1.0 == 0.0) v.toLong().toString() else v.toString()

/** Night window in minutes since midnight; works across midnight (23:00 → 07:00). */
fun inWindow(minuteOfDay: Int, start: Int, end: Int): Boolean =
    if (start <= end) minuteOfDay in start until end else minuteOfDay >= start || minuteOfDay < end

/** Distinctive words worth suggesting for an exception: longest first, no duplicates or filler. */
fun suggestWords(text: String, max: Int = 3): List<String> {
    val filler = setOf("this", "that", "with", "from", "your", "have", "will", "just", "there", "about", "what", "when", "they", "been", "were")
    return Regex("""[\p{L}\p{N}']{4,}""").findAll(text).map { it.value }
        .filter { it.lowercase() !in filler && it.any(Char::isLetter) }
        .distinctBy { it.lowercase() }
        .sortedByDescending { it.length }
        .take(max)
        .toList()
}
