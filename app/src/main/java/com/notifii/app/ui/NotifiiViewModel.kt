package com.notifii.app.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.notifii.app.data.AppCount
import com.notifii.app.data.FREE_RULES
import com.notifii.app.data.FREE_SIRENS
import com.notifii.app.data.Placement
import com.notifii.app.data.Rule
import com.notifii.app.data.Rung
import com.notifii.app.data.VaultEntry
import com.notifii.app.engine.Note
import com.notifii.app.engine.matches
import com.notifii.app.notifii
import com.notifii.app.service.Digest
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

enum class Outcome { DONE, NEEDS_PRO }

data class Preview(val matched: Int, val total: Int, val samples: List<VaultEntry>)

@OptIn(ExperimentalCoroutinesApi::class)
class NotifiiViewModel(app: Application) : AndroidViewModel(app) {
    private val dao = app.notifii.dao
    val prefs = app.notifii.prefs
    val billing = app.notifii.billing

    private fun <T> kotlinx.coroutines.flow.Flow<T>.state(initial: T) = stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), initial)

    val placements: StateFlow<List<Placement>> = dao.placements().state(emptyList())
    val rules: StateFlow<List<Rule>> = dao.rules().state(emptyList())
    val today: StateFlow<List<VaultEntry>?> = dao.since(startOfToday()).state(null)
    val pending: StateFlow<List<VaultEntry>> = dao.pending().state(emptyList())

    /** The noisiest apps of the past week that the user hasn't sorted or dismissed: the triage cards. */
    val triage: StateFlow<List<AppCount>> = combine(
        dao.noisiest(System.currentTimeMillis() - WEEK_MS), placements, prefs.dismissedTriage,
    ) { counts, placed, dismissed ->
        val sorted = placed.filter { it.isApp }.map { it.pkg }.toSet()
        counts.filter { it.pkg !in sorted && it.pkg !in dismissed && it.count >= 2 }.take(3)
    }.state(emptyList())

    val vaultQuery = MutableStateFlow("")
    val vaultFilter = MutableStateFlow<Rung?>(null)
    val vault: StateFlow<List<VaultEntry>?> = combine(vaultQuery, vaultFilter) { q, r -> q.trim() to r }
        .flatMapLatest { (q, r) -> dao.search(q, r) }
        .state(null)

    fun place(pkg: String, person: String, label: String, rung: Rung): Outcome {
        val sirens = placements.value.count { it.rung == Rung.SIREN && !(it.pkg == pkg && it.person == person) }
        if (rung == Rung.SIREN && !prefs.isPro.value && sirens >= FREE_SIRENS) return Outcome.NEEDS_PRO
        viewModelScope.launch(Dispatchers.IO) { dao.place(Placement(pkg, person, label, rung)) }
        return Outcome.DONE
    }

    fun unplace(p: Placement) = viewModelScope.launch(Dispatchers.IO) { dao.unplace(p.pkg, p.person) }

    fun addRule(rule: Rule): Outcome {
        if (!prefs.isPro.value && rules.value.size >= FREE_RULES) return Outcome.NEEDS_PRO
        viewModelScope.launch(Dispatchers.IO) { dao.addRule(rule) }
        return Outcome.DONE
    }

    fun deleteRule(rule: Rule) = viewModelScope.launch(Dispatchers.IO) { dao.deleteRule(rule.id) }

    /** "Would have caught N of the last week's notifications." shortcut: bursts can't be replayed, so they're ignored here. */
    suspend fun preview(rule: Rule): Preview = withContext(Dispatchers.IO) {
        val week = dao.sinceOnce(System.currentTimeMillis() - WEEK_MS)
        val hits = week.filter { matches(rule.copy(burst = false), Note(it.pkg, it.appLabel, it.title, it.text, it.sender, it.postedAt), 1) }
        Preview(hits.size, week.size, hits.take(3))
    }

    fun deliverNow() = viewModelScope.launch(Dispatchers.IO) { Digest.deliver(getApplication()) }

    fun clearVault() = viewModelScope.launch(Dispatchers.IO) { dao.clearVault() }

    private companion object {
        const val WEEK_MS = 7 * 24 * 60 * 60_000L
    }
}
