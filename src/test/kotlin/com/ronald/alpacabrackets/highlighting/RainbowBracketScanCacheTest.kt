package com.ronald.alpacabrackets.highlighting

import com.intellij.testFramework.fixtures.BasePlatformTestCase

class RainbowBracketScanCacheTest : BasePlatformTestCase() {
    fun testIgnoresBracketsInsideStringsAndComments() {
        val source = """
            class Demo {
                void test() {
                    if ((true)) {
                        String text = "([{}])";
                        // comment [ignored]
                        call();
                    }
                }
            }
        """.trimIndent()

        val file = myFixture.configureByText("Demo.java", source)
        val result = RainbowBracketScanCache.get(file)

        val codeOpen = source.indexOf("((true))")
        val codeInnerOpen = source.indexOf("((true))") + 1
        val stringOpen = source.indexOf("([{}])")
        val commentOpen = source.indexOf("[ignored]")
        val callOpen = source.indexOf("call()") + 4
        val callClose = source.indexOf("call()") + 5

        val outerCodeHighlight = requireNotNull(result.highlightAt(codeOpen))
        val innerCodeHighlight = requireNotNull(result.highlightAt(codeInnerOpen))
        val callOpenHighlight = requireNotNull(result.highlightAt(callOpen))
        val callCloseHighlight = requireNotNull(result.highlightAt(callClose))

        assertEquals(BracketHighlightKind.MATCHED, outerCodeHighlight.kind)
        assertEquals(BracketHighlightKind.MATCHED, innerCodeHighlight.kind)
        assertEquals(outerCodeHighlight.level!! + 1, innerCodeHighlight.level)
        assertNull(result.highlightAt(stringOpen))
        assertNull(result.highlightAt(commentOpen))
        assertEquals(BracketHighlightKind.MATCHED, callOpenHighlight.kind)
        assertEquals(BracketHighlightKind.MATCHED, callCloseHighlight.kind)
        assertEquals(callOpenHighlight.level, callCloseHighlight.level)
    }
}
