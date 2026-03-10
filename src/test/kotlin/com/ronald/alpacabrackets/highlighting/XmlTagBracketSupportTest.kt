package com.ronald.alpacabrackets.highlighting

import com.intellij.testFramework.fixtures.BasePlatformTestCase

class XmlTagBracketSupportTest : BasePlatformTestCase() {
    fun testHighlightsNestedXmlTagsByTagDepth() {
        val source = """
            <root>
              <outer attr="(ignored)">
                <inner>text</inner>
              </outer>
              <br/>
            </root>
        """.trimIndent()

        val file = myFixture.configureByText("demo.xml", source)
        val result = RainbowBracketScanCache.get(file)

        val rootOpen = source.indexOf("<root")
        val outerOpen = source.indexOf("<outer")
        val innerOpen = source.indexOf("<inner")
        val innerCloseOpen = source.indexOf("</inner")
        val brOpen = source.indexOf("<br/")
        val attrParen = source.indexOf("(ignored)")

        assertEquals(0, result.highlightAt(rootOpen)?.level)
        assertEquals(1, result.highlightAt(outerOpen)?.level)
        assertEquals(2, result.highlightAt(innerOpen)?.level)
        assertEquals(2, result.highlightAt(innerCloseOpen)?.level)
        assertEquals(1, result.highlightAt(brOpen)?.level)
        assertNull(result.highlightAt(attrParen))
    }

    fun testCaretResolverFindsInnermostXmlTagScope() {
        val source = """
            <root>
              <outer>
                <inner>text</inner>
              </outer>
            </root>
        """.trimIndent()

        val file = myFixture.configureByText("demo.xml", source)
        val result = RainbowBracketScanCache.get(file)

        val innerOpen = source.indexOf("<inner")
        val innerClose = source.indexOf("</inner>") + "</inner>".length - 1
        val caretInsideInner = source.indexOf("text") + 1

        assertEquals(BracketPair(innerOpen, innerClose, 2), BracketFocusResolver.resolve(result, caretInsideInner))
    }
}
