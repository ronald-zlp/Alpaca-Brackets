package com.ronald.alpacabrackets.highlighting

import com.intellij.psi.PsiFile
import com.intellij.psi.SyntaxTraverser
import com.intellij.psi.xml.XmlTag

object XmlTagBracketSupport {
    fun scan(file: PsiFile): RainbowBracketScanResult {
        val tags = SyntaxTraverser.psiTraverser(file)
            .toList()
            .mapNotNull { it as? XmlTag }

        if (tags.isEmpty()) {
            return RainbowBracketScanResult.empty()
        }

        val highlights = LinkedHashMap<Int, BracketHighlight>()
        val pairsByOffset = LinkedHashMap<Int, BracketPair>()
        val pairs = mutableListOf<BracketPair>()

        tags.forEach { tag ->
            val tagOffsets = resolveTagOffsets(tag) ?: return@forEach
            val level = nestingLevel(tag)
            val pair = BracketPair(tagOffsets.scopeOpen, tagOffsets.scopeClose, level)

            pairs += pair

            tagOffsets.highlightOffsets.forEach { offset ->
                highlights[offset] = BracketHighlight(
                    offset = offset,
                    kind = BracketHighlightKind.MATCHED,
                    level = level,
                )
                pairsByOffset[offset] = pair
            }
        }

        return RainbowBracketScanResult(
            highlightsByOffset = highlights,
            pairsByOffset = pairsByOffset,
            pairs = pairs,
        )
    }

    private fun resolveTagOffsets(tag: XmlTag): TagOffsets? {
        val text = tag.text
        val firstOpenIndex = text.indexOf('<')
        if (firstOpenIndex < 0) {
            return null
        }

        val firstCloseIndex = text.indexOf('>', firstOpenIndex + 1)
        if (firstCloseIndex < 0) {
            return null
        }

        val lastCloseIndex = text.lastIndexOf('>')
        if (lastCloseIndex < 0) {
            return null
        }

        val lastOpenIndex = text.lastIndexOf('<', lastCloseIndex)
        if (lastOpenIndex < 0) {
            return null
        }

        val baseOffset = tag.textRange.startOffset
        val highlightOffsets = linkedSetOf(
            baseOffset + firstOpenIndex,
            baseOffset + firstCloseIndex,
        )

        val hasSeparateClosingTag = lastOpenIndex > firstCloseIndex
        if (hasSeparateClosingTag) {
            highlightOffsets += baseOffset + lastOpenIndex
            highlightOffsets += baseOffset + lastCloseIndex
        }

        return TagOffsets(
            scopeOpen = baseOffset + firstOpenIndex,
            scopeClose = if (hasSeparateClosingTag) baseOffset + lastCloseIndex else baseOffset + firstCloseIndex,
            highlightOffsets = highlightOffsets,
        )
    }

    private fun nestingLevel(tag: XmlTag): Int {
        var level = 0
        var parent = tag.parent

        while (parent != null) {
            if (parent is XmlTag) {
                level += 1
            }
            parent = parent.parent
        }

        return level
    }

    private data class TagOffsets(
        val scopeOpen: Int,
        val scopeClose: Int,
        val highlightOffsets: Set<Int>,
    )
}
