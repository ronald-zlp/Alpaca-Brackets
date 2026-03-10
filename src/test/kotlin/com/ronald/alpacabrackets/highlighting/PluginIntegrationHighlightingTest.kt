package com.ronald.alpacabrackets.highlighting

import com.intellij.openapi.editor.markup.HighlighterLayer
import com.intellij.psi.PsiDocumentManager
import com.intellij.testFramework.fixtures.BasePlatformTestCase
import com.intellij.util.ui.UIUtil
import kotlin.test.assertTrue

class PluginIntegrationHighlightingTest : BasePlatformTestCase() {
    fun testEditorAddsBracketHighlightsInJavaEditor() {
        val source = """
            import java.util.List;

            class Demo {
                void test(List<String> input) {
                    if ((input.get(0)) != null) {
                        call(input);
                    }
                }
            }
        """.trimIndent()

        myFixture.configureByText("Demo.java", source)
        PsiDocumentManager.getInstance(project).commitAllDocuments()
        UIUtil.dispatchAllInvocationEvents()

        val methodBodyOpen = source.indexOf("{")
        val genericOpen = source.indexOf("<String")
        val callOpen = source.indexOf("call(") + 4

        assertTrue(hasRainbowHighlighterAt(methodBodyOpen))
        assertTrue(hasRainbowHighlighterAt(genericOpen))
        assertTrue(hasRainbowHighlighterAt(callOpen))
    }

    private fun hasRainbowHighlighterAt(offset: Int): Boolean {
        return myFixture.editor.markupModel.allHighlighters.any {
            it.startOffset == offset &&
                it.endOffset == offset + 1 &&
                it.layer == HighlighterLayer.ADDITIONAL_SYNTAX &&
                it.getUserData(ALPACA_RAINBOW_HIGHLIGHTER_KEY) == true
        }
    }
}
