package com.notifii.app.ui

import android.content.Context
import androidx.annotation.StringRes
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.notifii.app.R
import com.notifii.app.data.Rule
import com.notifii.app.data.Rung
import com.notifii.app.data.VaultEntry
import com.notifii.app.data.Why
import com.notifii.app.engine.trimAmount

// Everything the engine decides is turned into words here, in the phone's language.

@StringRes private fun Rung.labelRes() = when (this) {
    Rung.SIREN -> R.string.rung_siren
    Rung.RING -> R.string.rung_ring
    Rung.LATER -> R.string.rung_later
    Rung.HIDE -> R.string.rung_hide
}

@StringRes private fun Rung.blurbRes() = when (this) {
    Rung.SIREN -> R.string.rung_siren_blurb
    Rung.RING -> R.string.rung_ring_blurb
    Rung.LATER -> R.string.rung_later_blurb
    Rung.HIDE -> R.string.rung_hide_blurb
}

@Composable fun Rung.label() = stringResource(labelRes())
@Composable fun Rung.blurb() = stringResource(blurbRes())
fun Rung.label(context: Context) = context.getString(labelRes())

/** "From Upwork · containing “invited you” → Siren" */
fun describe(context: Context, rule: Rule): String {
    val parts = buildList {
        rule.appLabel?.let { add(context.getString(R.string.desc_from, it)) }
        rule.sender?.let { add(context.getString(R.string.desc_by, it)) }
        if (rule.wordList.isNotEmpty()) {
            add(context.getString(R.string.desc_containing, rule.wordList.joinToString(", ") { context.getString(R.string.desc_word, it) }))
        }
        rule.minAmount?.let { add(context.getString(R.string.desc_amount, trimAmount(it))) }
        if (rule.burst) add(context.getString(R.string.desc_burst))
    }
    val condition = parts.joinToString(" · ").replaceFirstChar { it.uppercase() }.ifEmpty { context.getString(R.string.desc_anything) }
    return context.getString(R.string.desc_rule, condition, rule.rung.label(context))
}

/** The answer to "Why did this ring / wait / disappear?" */
fun reasonText(context: Context, e: VaultEntry, rules: List<Rule>): String {
    // Night only ever holds Ring, so that's the rung the person or app was really on.
    val placedOn = if (e.night) Rung.RING else e.rung
    val base = when (e.why) {
        Why.EXCEPTION -> rules.firstOrNull { it.id == e.ruleId }?.let { context.getString(R.string.why_exception, describe(context, it)) }
            ?: context.getString(R.string.why_exception_gone)
        Why.PERSON, Why.APP -> context.getString(R.string.why_on_rung, e.subject, placedOn.label(context))
        Why.UNSORTED -> context.getString(R.string.why_unsorted, e.subject)
    }
    return if (e.night) context.getString(R.string.why_night, base) else base
}
