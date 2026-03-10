package com.ronald.alpacabrackets.highlighting

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.assertTrue

class RainbowBracketScannerTest {
    @Test
    fun `nested brackets receive stable rainbow levels`() {
        val result = RainbowBracketScanner.scan("({[]})")

        assertEquals(BracketHighlightKind.MATCHED, result.highlightAt(0)?.kind)
        assertEquals(0, result.highlightAt(0)?.level)
        assertEquals(1, result.highlightAt(1)?.level)
        assertEquals(2, result.highlightAt(2)?.level)
        assertEquals(2, result.highlightAt(3)?.level)
        assertEquals(1, result.highlightAt(4)?.level)
        assertEquals(0, result.highlightAt(5)?.level)
    }

    @Test
    fun `adjacent groups restart from level zero`() {
        val result = RainbowBracketScanner.scan("[](){}")

        assertEquals(0, result.highlightAt(0)?.level)
        assertEquals(0, result.highlightAt(1)?.level)
        assertEquals(0, result.highlightAt(2)?.level)
        assertEquals(0, result.highlightAt(3)?.level)
        assertEquals(0, result.highlightAt(4)?.level)
        assertEquals(0, result.highlightAt(5)?.level)
    }

    @Test
    fun `mismatched brackets are marked as mismatched`() {
        val result = RainbowBracketScanner.scan("([)]")

        assertEquals(BracketHighlightKind.MISMATCHED, result.highlightAt(0)?.kind)
        assertEquals(BracketHighlightKind.MISMATCHED, result.highlightAt(1)?.kind)
        assertEquals(BracketHighlightKind.MISMATCHED, result.highlightAt(2)?.kind)
        assertEquals(BracketHighlightKind.MISMATCHED, result.highlightAt(3)?.kind)
    }

    @Test
    fun `non bracket characters stay untouched`() {
        val result = RainbowBracketScanner.scan("abc")

        assertNull(result.highlightAt(0))
        assertNull(result.highlightAt(1))
        assertNull(result.highlightAt(2))
    }

    @Test
    fun `scanner preserves nesting across skipped regions`() {
        val result = RainbowBracketScanner.scan(
            listOf(
                BracketScanSegment(0, "function("),
                BracketScanSegment(20, "[value]"),
                BracketScanSegment(40, ")"),
            ),
        )

        assertEquals(0, result.highlightAt(8)?.level)
        assertEquals(1, result.highlightAt(20)?.level)
        assertEquals(1, result.highlightAt(26)?.level)
        assertEquals(0, result.highlightAt(40)?.level)
    }

    @Test
    fun `matched brackets expose pair locations`() {
        val result = RainbowBracketScanner.scan("a(b[c]d)e")

        assertEquals(BracketPair(1, 7, 0), result.pairAt(1))
        assertEquals(BracketPair(3, 5, 1), result.pairAt(5))
        assertEquals(BracketPair(3, 5, 1), result.innermostPairContaining(4))
    }

    @Test
    fun `mismatched brackets expose readable tooltips`() {
        val result = RainbowBracketScanner.scan("([)")

        assertTrue(result.highlightAt(1)?.tooltip?.contains("expected ']' before ')'") == true)
        assertTrue(result.highlightAt(2)?.tooltip?.contains("should close with ']' first") == true)
    }
}
