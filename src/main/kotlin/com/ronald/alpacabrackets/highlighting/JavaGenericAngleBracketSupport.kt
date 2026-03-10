package com.ronald.alpacabrackets.highlighting

import com.intellij.openapi.util.TextRange
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiFile
import com.intellij.psi.PsiJavaFile
import com.intellij.psi.PsiReferenceParameterList
import com.intellij.psi.PsiTypeParameterList
import com.intellij.psi.SyntaxTraverser

object JavaGenericAngleBracketSupport {
    private val angleBracketPairs = mapOf('<' to '>')

    fun scan(file: PsiFile): RainbowBracketScanResult {
        if (file !is PsiJavaFile) {
            return RainbowBracketScanResult.empty()
        }

        val genericRanges = collectGenericRanges(file)
        if (genericRanges.isEmpty()) {
            return RainbowBracketScanResult.empty()
        }

        val text = file.text
        val segments = genericRanges.map {
            BracketScanSegment(
                startOffset = it.startOffset,
                text = text.subSequence(it.startOffset, it.endOffset),
            )
        }

        return RainbowBracketScanner.scan(segments, angleBracketPairs)
    }

    private fun collectGenericRanges(file: PsiJavaFile): List<TextRange> {
        val sortedRanges = SyntaxTraverser.psiTraverser(file)
            .filter { it is PsiTypeParameterList || it is PsiReferenceParameterList }
            .map(PsiElement::getTextRange)
            .sortedBy(TextRange::getStartOffset)
            .toList()

        if (sortedRanges.isEmpty()) {
            return emptyList()
        }

        val mergedRanges = mutableListOf<TextRange>()

        sortedRanges.forEach { range ->
            val previous = mergedRanges.lastOrNull()
            if (previous == null || previous.endOffset < range.startOffset) {
                mergedRanges += range
            } else if (range.endOffset > previous.endOffset) {
                mergedRanges[mergedRanges.lastIndex] = TextRange(previous.startOffset, range.endOffset)
            }
        }

        return mergedRanges
    }
}
