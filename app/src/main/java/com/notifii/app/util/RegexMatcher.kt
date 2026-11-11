package com.notifii.app.util

import java.util.regex.Pattern
import java.util.regex.PatternSyntaxException
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class RegexMatcher @Inject constructor() {

    private val cache = mutableMapOf<String, Pattern?>()

    fun matches(input: String, patternStr: String): Boolean {
        if (patternStr.isBlank()) return false
        val pattern = cache.getOrPut(patternStr) {
            try {
                Pattern.compile(patternStr, Pattern.CASE_INSENSITIVE)
            } catch (e: PatternSyntaxException) {
                null
            }
        } ?: return false

        return pattern.matcher(input).find()
    }
}
