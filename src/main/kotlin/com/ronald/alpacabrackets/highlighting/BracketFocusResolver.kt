package com.ronald.alpacabrackets.highlighting

object BracketFocusResolver {
    fun resolve(scanResult: RainbowBracketScanResult, caretOffset: Int): BracketPair? {
        return scanResult.pairAt(caretOffset)
            ?: scanResult.innermostPairContaining(caretOffset)
            ?: if (caretOffset > 0) scanResult.pairAt(caretOffset - 1) else null
    }
}
