package com.ronald.alpacabrackets.highlighting

import com.intellij.lang.LanguageParserDefinitions
import com.intellij.psi.PsiComment
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiLanguageInjectionHost
import com.intellij.psi.PsiFile
import com.intellij.psi.SyntaxTraverser
import com.intellij.openapi.util.TextRange
import com.intellij.psi.util.CachedValueProvider
import com.intellij.psi.util.CachedValuesManager
import com.intellij.psi.util.PsiModificationTracker

object RainbowBracketScanCache {
    fun get(file: PsiFile): RainbowBracketScanResult {
        return CachedValuesManager.getManager(file.project).getCachedValue(file) {
            CachedValueProvider.Result.create(
                RainbowBracketScanner.scan(scanSegments(file))
                    .mergedWith(JavaGenericAngleBracketSupport.scan(file))
                    .mergedWith(XmlTagBracketSupport.scan(file)),
                file,
                PsiModificationTracker.MODIFICATION_COUNT,
            )
        }
    }

    private fun scanSegments(file: PsiFile): List<BracketScanSegment> {
        val ignoredRanges = collectIgnoredRanges(file)
        if (ignoredRanges.isEmpty()) {
            return listOf(BracketScanSegment(0, file.text))
        }

        val text = file.text
        val segments = mutableListOf<BracketScanSegment>()
        var cursor = 0

        ignoredRanges.forEach { range ->
            if (cursor < range.startOffset) {
                segments.add(
                    BracketScanSegment(
                        startOffset = cursor,
                        text = text.subSequence(cursor, range.startOffset),
                    ),
                )
            }

            cursor = maxOf(cursor, range.endOffset)
        }

        if (cursor < text.length) {
            segments.add(
                BracketScanSegment(
                    startOffset = cursor,
                    text = text.subSequence(cursor, text.length),
                ),
            )
        }

        return segments
    }

    private fun collectIgnoredRanges(file: PsiFile): List<TextRange> {
        val sortedRanges = SyntaxTraverser.psiTraverser(file)
            .filter(::shouldIgnoreElement)
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
                mergedRanges.add(range)
            } else if (range.endOffset > previous.endOffset) {
                mergedRanges[mergedRanges.lastIndex] = TextRange(previous.startOffset, range.endOffset)
            }
        }

        return mergedRanges
    }

    private fun shouldIgnoreElement(element: PsiElement): Boolean {
        if (element is PsiComment || element is PsiLanguageInjectionHost) {
            return true
        }

        val elementType = element.node?.elementType ?: return false
        val parserDefinition = LanguageParserDefinitions.INSTANCE.forLanguage(element.language) ?: return false

        return parserDefinition.commentTokens.contains(elementType) ||
            parserDefinition.stringLiteralElements.contains(elementType)
    }
}
