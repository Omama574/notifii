package com.notifii.app.data

import android.content.Context
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

/** Settings as StateFlows so the UI and the listener service see changes instantly. */
class Prefs(context: Context) {
    private val sp = context.getSharedPreferences("notifii", Context.MODE_PRIVATE)

    private inner class Bool(private val key: String, default: Boolean) {
        val flow = MutableStateFlow(sp.getBoolean(key, default))
        fun set(v: Boolean) { sp.edit().putBoolean(key, v).apply(); flow.value = v }
    }

    private inner class Num(private val key: String, default: Int) {
        val flow = MutableStateFlow(sp.getInt(key, default))
        fun set(v: Int) { sp.edit().putInt(key, v).apply(); flow.value = v }
    }

    private val pro = Bool("pro", false)
    val isPro: StateFlow<Boolean> = pro.flow
    fun setPro(v: Boolean) = pro.set(v)

    private val night = Bool("night", false)
    val nightOn: StateFlow<Boolean> = night.flow
    fun setNightOn(v: Boolean) = night.set(v)

    /** Minutes since midnight. */
    private val nightStartN = Num("night_start", 23 * 60)
    val nightStart: StateFlow<Int> = nightStartN.flow
    fun setNightStart(v: Int) = nightStartN.set(v)

    private val nightEndN = Num("night_end", 7 * 60)
    val nightEnd: StateFlow<Int> = nightEndN.flow
    fun setNightEnd(v: Int) = nightEndN.set(v)

    private val flip = Bool("flip", true)
    val flipToSilence: StateFlow<Boolean> = flip.flow
    fun setFlipToSilence(v: Boolean) = flip.set(v)

    private val speak = Bool("speak", true)
    val readAloud: StateFlow<Boolean> = speak.flow
    fun setReadAloud(v: Boolean) = speak.set(v)

    /** 0 = forever. */
    private val keep = Num("keep_days", 90)
    val keepDays: StateFlow<Int> = keep.flow
    fun setKeepDays(v: Int) = keep.set(v)

    private val _dismissed = MutableStateFlow(sp.getStringSet("triage_dismissed", emptySet()).orEmpty().toSet())
    /** Apps the user said "Not now" to on a triage card. */
    val dismissedTriage: StateFlow<Set<String>> = _dismissed
    fun dismissTriage(pkg: String) {
        val next = _dismissed.value + pkg
        sp.edit().putStringSet("triage_dismissed", next).apply()
        _dismissed.value = next
    }
}

/** Digest delivery times (minutes since midnight). shortcut: fixed in v1; make editable if users ask. */
val DIGEST_TIMES = listOf(12 * 60, 18 * 60)

/** Free tier: one Siren placement and one exception, so everyone can try the hero feature. */
const val FREE_SIRENS = 1
const val FREE_RULES = 1
