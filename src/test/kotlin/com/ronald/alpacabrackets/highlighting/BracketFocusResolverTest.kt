package com.ronald.alpacabrackets.highlighting

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class BracketFocusResolverTest {
    @Test
    fun `resolves pair directly under caret`() {
        val result = RainbowBracketScanner.scan("({[]})")

        assertEquals(BracketPair(2, 3, 2), BracketFocusResolver.resolve(result, 2))
        assertEquals(BracketPair(2, 3, 2), BracketFocusResolver.resolve(result, 3))
    }

    @Test
    fun `resolves innermost enclosing pair inside code`() {
        val result = RainbowBracketScanner.scan("a(b[c]d)e")

        assertEquals(BracketPair(3, 5, 1), BracketFocusResolver.resolve(result, 4))
        assertEquals(BracketPair(1, 7, 0), BracketFocusResolver.resolve(result, 6))
    }

    @Test
    fun `returns null when caret is outside brackets`() {
        val result = RainbowBracketScanner.scan("abc")

        assertNull(BracketFocusResolver.resolve(result, 1))
    }
}
