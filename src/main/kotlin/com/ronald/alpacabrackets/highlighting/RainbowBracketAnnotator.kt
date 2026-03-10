package com.ronald.alpacabrackets.highlighting

import com.intellij.lang.annotation.AnnotationHolder
import com.intellij.lang.annotation.Annotator
import com.intellij.lang.annotation.HighlightSeverity
import com.intellij.openapi.project.DumbAware
import com.intellij.openapi.util.TextRange
import com.intellij.psi.PsiElement

class RainbowBracketAnnotator : Annotator, DumbAware {
    override fun annotate(element: PsiElement, holder: AnnotationHolder) {
        val node = element.node ?: return
        if (node.firstChildNode != null) {
            return
        }

        val file = element.containingFile ?: return
        if (file.textLength > MAX_FILE_LENGTH) {
            return
        }

        val scanResult = RainbowBracketScanCache.get(file)
        val baseOffset = element.textRange.startOffset

        element.text.forEachIndexed { index, _ ->
            val offset = baseOffset + index
            val highlight = scanResult.highlightAt(offset) ?: return@forEachIndexed

            val builder = when (highlight.kind) {
                BracketHighlightKind.MATCHED -> holder.newSilentAnnotation(HighlightSeverity.INFORMATION)
                BracketHighlightKind.MISMATCHED -> holder.newSilentAnnotation(HighlightSeverity.WARNING)
            }

            builder
                .range(TextRange.from(offset, 1))
                .textAttributes(RainbowBracketPalette.keyFor(highlight))
                .also {
                    if (!highlight.tooltip.isNullOrBlank()) {
                        it.tooltip(highlight.tooltip)
                    }
                }
                .create()
        }
    }

    private companion object {
        const val MAX_FILE_LENGTH = 250_000
    }
}
