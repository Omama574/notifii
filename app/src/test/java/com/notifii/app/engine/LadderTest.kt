package com.notifii.app.engine

import com.notifii.app.data.Placement
import com.notifii.app.data.Rule
import com.notifii.app.data.Rung
import com.notifii.app.data.Why
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class LadderTest {
    private fun note(text: String, pkg: String = "com.upwork", sender: String? = null, title: String = "Upwork") =
        Note(pkg, "Upwork", title, text, sender, postedAt = 0)

    private val upworkLater = Placement("com.upwork", "", "Upwork", Rung.LATER)

    @Test fun unsortedAppRings() {
        assertEquals(Rung.RING, decide(note("hi"), emptyList(), emptyList(), 1, night = false).rung)
    }

    @Test fun appPlacementApplies() {
        assertEquals(Rung.LATER, decide(note("newsletter"), listOf(upworkLater), emptyList(), 1, false).rung)
    }

    @Test fun exceptionBeatsPlacement() {
        val rule = Rule(id = 7, pkg = "com.upwork", appLabel = "Upwork", sender = null, words = "invited you", rung = Rung.SIREN)
        val d = decide(note("Acme invited you to an interview"), listOf(upworkLater), listOf(rule), 1, false)
        assertEquals(Rung.SIREN, d.rung)
        assertEquals(7L, d.ruleId)
        assertEquals(Why.EXCEPTION, d.why)
    }

    @Test fun loudestMatchingExceptionWins() {
        val hide = Rule(id = 1, pkg = null, appLabel = null, sender = null, words = "order", rung = Rung.HIDE)
        val siren = Rule(id = 2, pkg = null, appLabel = null, sender = null, words = "urgent", rung = Rung.SIREN)
        assertEquals(Rung.SIREN, decide(note("urgent order"), emptyList(), listOf(hide, siren), 1, false).rung)
    }

    @Test fun personBeatsApp() {
        val mom = Placement("com.whatsapp", "Mom", "Mom", Rung.SIREN)
        val wa = Placement("com.whatsapp", "", "WhatsApp", Rung.LATER)
        val n = Note("com.whatsapp", "WhatsApp", "Mom", "call me", "Mom", 0)
        assertEquals(Rung.SIREN, decide(n, listOf(wa, mom), emptyList(), 1, false).rung)
    }

    @Test fun blankSenderIsNotAPerson() {
        val d = decide(note("hi", sender = ""), listOf(upworkLater), emptyList(), 1, false)
        assertEquals(Rung.LATER, d.rung)
        assertEquals(Why.APP, d.why)
        assertEquals("Upwork", d.subject)
    }

    @Test fun nightHoldsRingButNotSiren() {
        val held = decide(note("hi"), emptyList(), emptyList(), 1, night = true)
        assertEquals(Rung.LATER, held.rung)
        assertTrue(held.night)
        val siren = Placement("com.upwork", "", "Upwork", Rung.SIREN)
        assertEquals(Rung.SIREN, decide(note("hi"), listOf(siren), emptyList(), 1, night = true).rung)
    }

    @Test fun amountThreshold() {
        val rule = Rule(pkg = null, appLabel = null, sender = null, minAmount = 20.0, rung = Rung.SIREN)
        assertTrue(matches(rule, note("New order: \$24.50 tip included"), 1))
        assertFalse(matches(rule, note("New order: \$4.10"), 1))
        assertFalse(matches(rule, note("New order, no price"), 1))
        assertEquals(1200.0, largestAmount("Paid ₹1,200 and ₹50")!!, 0.0)
        assertEquals(30.0, largestAmount("refund of 30 USD")!!, 0.0)
        assertNull(largestAmount("call me at 5"))
    }

    @Test fun burstNeedsThree() {
        val rule = Rule(pkg = null, appLabel = null, sender = "Mom", burst = true, rung = Rung.SIREN)
        val n = Note("com.whatsapp", "WhatsApp", "Mom", "pick up", "Mom", 0)
        assertFalse(matches(rule, n, 2))
        assertTrue(matches(rule, n, 3))
    }

    @Test fun nightWindowCrossesMidnight() {
        assertTrue(inWindow(23 * 60 + 30, 23 * 60, 7 * 60))
        assertTrue(inWindow(3 * 60, 23 * 60, 7 * 60))
        assertFalse(inWindow(12 * 60, 23 * 60, 7 * 60))
        assertTrue(inWindow(10 * 60, 9 * 60, 17 * 60))
        assertFalse(inWindow(17 * 60, 9 * 60, 17 * 60))
    }


    @Test fun wordSuggestions() {
        assertEquals(listOf("interview", "invited"), suggestWords("Acme invited you to an interview", max = 2))
    }
}
