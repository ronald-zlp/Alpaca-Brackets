package com.ronald.alpacabrackets.highlighting

import com.intellij.openapi.editor.Editor
import com.intellij.openapi.editor.EditorFactory
import com.intellij.openapi.editor.event.CaretEvent
import com.intellij.openapi.editor.event.CaretListener
import com.intellij.openapi.editor.event.DocumentEvent
import com.intellij.openapi.editor.event.DocumentListener
import com.intellij.openapi.editor.event.EditorFactoryEvent
import com.intellij.openapi.editor.event.EditorFactoryListener
import com.intellij.openapi.editor.markup.HighlighterLayer
import com.intellij.openapi.editor.markup.HighlighterTargetArea
import com.intellij.openapi.editor.markup.RangeHighlighter
import com.intellij.openapi.project.Project
import com.intellij.openapi.startup.StartupActivity
import com.intellij.openapi.util.Key
import com.intellij.psi.PsiDocumentManager

internal val ALPACA_RAINBOW_HIGHLIGHTER_KEY: Key<Boolean> = Key.create("alpaca.rainbow.highlighter.marker")

class AlpacaBracketFocusStartupActivity : StartupActivity.DumbAware {
    override fun runActivity(project: Project) {
        val highlighter = AlpacaBracketEditorHighlighter(project)
        val multicaster = EditorFactory.getInstance().eventMulticaster

        multicaster.addCaretListener(highlighter, project)
        multicaster.addDocumentListener(highlighter, project)
        EditorFactory.getInstance().addEditorFactoryListener(highlighter, project)

        EditorFactory.getInstance().allEditors
            .filter { it.project == project }
            .forEach {
                highlighter.refreshRainbow(it)
                highlighter.refreshActivePair(it)
            }
    }
}

private class AlpacaBracketEditorHighlighter(
    private val project: Project,
) : CaretListener, DocumentListener, EditorFactoryListener {
    override fun caretPositionChanged(event: CaretEvent) {
        refreshActivePair(event.editor)
    }

    override fun documentChanged(event: DocumentEvent) {
        val editors = EditorFactory.getInstance().getEditors(event.document, project)
        val psiDocumentManager = PsiDocumentManager.getInstance(project)

        psiDocumentManager.performLaterWhenAllCommitted {
            editors
                .filterNot(Editor::isDisposed)
                .forEach {
                    refreshRainbow(it)
                    refreshActivePair(it)
                }
        }
    }

    override fun editorCreated(event: EditorFactoryEvent) {
        refreshRainbow(event.editor)
        refreshActivePair(event.editor)
    }

    override fun editorReleased(event: EditorFactoryEvent) {
        clearRainbowHighlights(event.editor)
        clearActivePairHighlights(event.editor)
    }

    fun refreshRainbow(editor: Editor) {
        if (editor.project != project || editor.isDisposed) {
            return
        }

        clearRainbowHighlights(editor)

        val psiFile = PsiDocumentManager.getInstance(project).getPsiFile(editor.document) ?: return
        if (psiFile.textLength > MAX_FILE_LENGTH) {
            return
        }

        val highlighters = RainbowBracketScanCache.get(psiFile)
            .highlights()
            .map { highlight ->
                val highlighter = editor.markupModel.addRangeHighlighter(
                    highlight.offset,
                    highlight.offset + 1,
                    HighlighterLayer.ADDITIONAL_SYNTAX,
                    RainbowBracketPalette.attributesFor(highlight),
                    HighlighterTargetArea.EXACT_RANGE,
                )
                highlighter.errorStripeTooltip = highlight.tooltip
                highlighter.putUserData(ALPACA_RAINBOW_HIGHLIGHTER_KEY, true)
                highlighter
            }

        editor.putUserData(RAINBOW_HIGHLIGHTERS, highlighters)
    }

    fun refreshActivePair(editor: Editor) {
        if (editor.project != project || editor.isDisposed) {
            return
        }

        clearActivePairHighlights(editor)

        val psiFile = PsiDocumentManager.getInstance(project).getPsiFile(editor.document) ?: return
        val scanResult = RainbowBracketScanCache.get(psiFile)
        val pair = BracketFocusResolver.resolve(scanResult, editor.caretModel.offset) ?: return

        val highlighters = listOf(
            createActivePairHighlighter(editor, pair.openOffset, pair.openOffset + 1),
            createActivePairHighlighter(editor, pair.closeOffset, pair.closeOffset + 1),
        )

        editor.putUserData(ACTIVE_PAIR_HIGHLIGHTERS, highlighters)
    }

    private fun createActivePairHighlighter(editor: Editor, startOffset: Int, endOffset: Int): RangeHighlighter {
        return editor.markupModel.addRangeHighlighter(
            startOffset,
            endOffset,
            HighlighterLayer.SELECTION - 1,
            RainbowBracketPalette.activePairAttributes,
            HighlighterTargetArea.EXACT_RANGE,
        )
    }

    private fun clearRainbowHighlights(editor: Editor) {
        editor.getUserData(RAINBOW_HIGHLIGHTERS)
            ?.forEach(editor.markupModel::removeHighlighter)
        editor.putUserData(RAINBOW_HIGHLIGHTERS, null)
    }

    private fun clearActivePairHighlights(editor: Editor) {
        editor.getUserData(ACTIVE_PAIR_HIGHLIGHTERS)
            ?.forEach(editor.markupModel::removeHighlighter)
        editor.putUserData(ACTIVE_PAIR_HIGHLIGHTERS, null)
    }

    private companion object {
        const val MAX_FILE_LENGTH = 250_000

        val RAINBOW_HIGHLIGHTERS: Key<List<RangeHighlighter>> = Key.create("alpaca.rainbow.highlighters")
        val ACTIVE_PAIR_HIGHLIGHTERS: Key<List<RangeHighlighter>> = Key.create("alpaca.active.pair.highlighters")
    }
}
