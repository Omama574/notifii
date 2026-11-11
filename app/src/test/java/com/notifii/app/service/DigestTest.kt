package com.notifii.app.service

import org.junit.Assert.assertEquals
import org.junit.Test
import java.util.Calendar

class DigestTest {
    private fun at(h: Int, m: Int, dayOffset: Int = 0) = Calendar.getInstance().apply {
        set(2026, Calendar.NOVEMBER, 12, h, m, 0); set(Calendar.MILLISECOND, 0)
        add(Calendar.DAY_OF_YEAR, dayOffset)
    }.timeInMillis

    @Test fun morningGoesToNoon() = assertEquals(at(12, 0), Digest.nextTime(at(9, 0), nightOn = false, nightEnd = 7 * 60))

    @Test fun afternoonGoesToSix() = assertEquals(at(18, 0), Digest.nextTime(at(12, 0), false, 7 * 60))

    @Test fun eveningRollsToTomorrowNoon() = assertEquals(at(12, 0, 1), Digest.nextTime(at(20, 0), false, 7 * 60))

    @Test fun nightEndIsADeliveryTime() = assertEquals(at(7, 0, 1), Digest.nextTime(at(23, 30), nightOn = true, nightEnd = 7 * 60))
}
