package com.ronald.alpacabrackets.highlighting

import com.intellij.testFramework.fixtures.BasePlatformTestCase

class JavaGenericAngleBracketSupportTest : BasePlatformTestCase() {
    fun testHighlightsNestedJavaGenericsButSkipsComparisons() {
        val source = """
            class Demo<T extends Comparable<T>> {
                Map<String, List<Integer>> field;
                boolean ok = 1 < 2 && 3 > 1;
            }
        """.trimIndent()

        val file = myFixture.configureByText("Demo.java", source)
        val result = RainbowBracketScanCache.get(file)

        val declarationOpen = source.indexOf("<T extends")
        val declarationInnerOpen = source.indexOf("<T>>")
        val fieldOpen = source.indexOf("<String")
        val fieldInnerOpen = source.indexOf("<Integer")
        val comparisonLt = source.indexOf("1 < 2") + 2
        val comparisonGt = source.indexOf("3 > 1") + 2

        assertEquals(0, result.highlightAt(declarationOpen)?.level)
        assertEquals(1, result.highlightAt(declarationInnerOpen)?.level)
        assertEquals(0, result.highlightAt(fieldOpen)?.level)
        assertEquals(1, result.highlightAt(fieldInnerOpen)?.level)
        assertNull(result.highlightAt(comparisonLt))
        assertNull(result.highlightAt(comparisonGt))
    }

    fun testCaretResolverFindsInnermostGenericPair() {
        val source = """
            class Demo {
                Map<String, List<Integer>> field;
            }
        """.trimIndent()

        val file = myFixture.configureByText("Demo.java", source)
        val result = RainbowBracketScanCache.get(file)

        val innerOpen = source.indexOf("<Integer")
        val innerClose = source.indexOf(">> field")
        val caretInside = source.indexOf("Integer") + 2

        assertEquals(BracketPair(innerOpen, innerClose, 1), BracketFocusResolver.resolve(result, caretInside))
    }
}
