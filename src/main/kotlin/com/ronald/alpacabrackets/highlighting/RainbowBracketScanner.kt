package com.ronald.alpacabrackets.highlighting

enum class BracketHighlightKind {
    MATCHED,
    MISMATCHED,
}

data class BracketPair(
    val openOffset: Int,
    val closeOffset: Int,
    val level: Int,
)

data class BracketHighlight(
    val offset: Int,
    val kind: BracketHighlightKind,
    val level: Int? = null,
    val tooltip: String? = null,
)

data class RainbowBracketScanResult(
    private val highlightsByOffset: Map<Int, BracketHighlight>,
    private val pairsByOffset: Map<Int, BracketPair>,
    private val pairs: List<BracketPair>,
) {
    fun highlightAt(offset: Int): BracketHighlight? = highlightsByOffset[offset]

    fun highlights(): Collection<BracketHighlight> = highlightsByOffset.values

    fun pairAt(offset: Int): BracketPair? = pairsByOffset[offset]

    fun innermostPairContaining(offset: Int): BracketPair? {
        return pairs
            .asSequence()
            .filter { it.openOffset <= offset && offset <= it.closeOffset }
            .minByOrNull { it.closeOffset - it.openOffset }
    }

    fun mergedWith(other: RainbowBracketScanResult): RainbowBracketScanResult {
        if (other.highlightsByOffset.isEmpty() && other.pairs.isEmpty()) {
            return this
        }

        if (highlightsByOffset.isEmpty() && pairs.isEmpty()) {
            return other
        }

        val mergedHighlights = LinkedHashMap<Int, BracketHighlight>(highlightsByOffset.size + other.highlightsByOffset.size)
        mergedHighlights.putAll(highlightsByOffset)
        mergedHighlights.putAll(other.highlightsByOffset)

        val mergedPairsByOffset = LinkedHashMap<Int, BracketPair>(pairsByOffset.size + other.pairsByOffset.size)
        mergedPairsByOffset.putAll(pairsByOffset)
        mergedPairsByOffset.putAll(other.pairsByOffset)

        return RainbowBracketScanResult(
            highlightsByOffset = mergedHighlights,
            pairsByOffset = mergedPairsByOffset,
            pairs = pairs + other.pairs,
        )
    }

    companion object {
        fun empty(): RainbowBracketScanResult {
            return RainbowBracketScanResult(
                highlightsByOffset = emptyMap(),
                pairsByOffset = emptyMap(),
                pairs = emptyList(),
            )
        }
    }
}

data class BracketScanSegment(
    val startOffset: Int,
    val text: CharSequence,
)

object RainbowBracketScanner {
    private val defaultOpenToClose = mapOf(
        '(' to ')',
        '[' to ']',
        '{' to '}',
    )

    fun scan(text: CharSequence): RainbowBracketScanResult {
        return scan(listOf(BracketScanSegment(0, text)), defaultOpenToClose)
    }

    fun scan(segments: Iterable<BracketScanSegment>): RainbowBracketScanResult {
        return scan(segments, defaultOpenToClose)
    }

    fun scan(
        segments: Iterable<BracketScanSegment>,
        openToClose: Map<Char, Char>,
    ): RainbowBracketScanResult {
        val highlights = LinkedHashMap<Int, BracketHighlight>()
        val pairs = mutableListOf<BracketPair>()
        val pairsByOffset = LinkedHashMap<Int, BracketPair>()
        val stack = ArrayDeque<BracketStackEntry>()
        val closeToOpen = openToClose.entries.associate { (open, close) -> close to open }

        segments.forEach { segment ->
            segment.text.forEachIndexed { index, character ->
                val offset = segment.startOffset + index

                when {
                    openToClose.containsKey(character) -> {
                        val level = stack.size
                        stack.addLast(BracketStackEntry(character, offset, level))
                        highlights[offset] = BracketHighlight(offset, BracketHighlightKind.MATCHED, level)
                    }

                    closeToOpen.containsKey(character) -> {
                        val expectedOpen = closeToOpen.getValue(character)
                        val top = stack.removeLastOrNull()

                        if (top != null && top.character == expectedOpen) {
                            val pair = BracketPair(top.offset, offset, top.level)
                            pairs += pair
                            pairsByOffset[top.offset] = pair
                            pairsByOffset[offset] = pair
                            highlights[offset] = BracketHighlight(offset, BracketHighlightKind.MATCHED, top.level)
                        } else {
                            if (top != null) {
                                highlights[top.offset] = BracketHighlight(
                                    offset = top.offset,
                                    kind = BracketHighlightKind.MISMATCHED,
                                    tooltip = "Unclosed '${top.character}': expected '${openToClose.getValue(top.character)}' before '$character'",
                                )
                            }

                            highlights[offset] = BracketHighlight(
                                offset = offset,
                                kind = BracketHighlightKind.MISMATCHED,
                                tooltip = mismatchTooltip(character, top, openToClose),
                            )
                        }
                    }
                }
            }
        }

        while (stack.isNotEmpty()) {
            val unmatched = stack.removeLast()
            highlights[unmatched.offset] = BracketHighlight(
                offset = unmatched.offset,
                kind = BracketHighlightKind.MISMATCHED,
                tooltip = "Unclosed '${unmatched.character}': expected '${openToClose.getValue(unmatched.character)}'",
            )
        }

        return RainbowBracketScanResult(highlights, pairsByOffset, pairs)
    }

    private fun mismatchTooltip(
        character: Char,
        top: BracketStackEntry?,
        openToClose: Map<Char, Char>,
    ): String {
        return if (top == null) {
            "Unexpected closing '$character'"
        } else {
            "Unexpected '$character': '${top.character}' should close with '${openToClose.getValue(top.character)}' first"
        }
    }

    private data class BracketStackEntry(
        val character: Char,
        val offset: Int,
        val level: Int,
    )
}
