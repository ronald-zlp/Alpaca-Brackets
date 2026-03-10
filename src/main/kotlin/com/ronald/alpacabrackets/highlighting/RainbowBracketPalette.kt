package com.ronald.alpacabrackets.highlighting

import com.intellij.openapi.editor.markup.EffectType
import com.intellij.openapi.editor.markup.TextAttributes
import com.intellij.openapi.editor.colors.TextAttributesKey
import com.intellij.ui.JBColor
import java.awt.Color
import java.awt.Font

object RainbowBracketPalette {
    private data class PaletteEntry(val light: Int, val dark: Int)

    private val palette = listOf(
        PaletteEntry(0xC62828, 0xFF6B6B),
        PaletteEntry(0xEF6C00, 0xFFD166),
        PaletteEntry(0x2E7D32, 0x06D6A0),
        PaletteEntry(0x00897B, 0x4CC9F0),
        PaletteEntry(0x1565C0, 0x4895EF),
        PaletteEntry(0x6A1B9A, 0xB5179E),
    )

    private val levelKeys = palette.mapIndexed { index, entry ->
        TextAttributesKey.createTextAttributesKey(
            "RAINBOW_BRACKETS_LEVEL_$index",
            TextAttributes(
                JBColor(Color(entry.light), Color(entry.dark)),
                null,
                null,
                null,
                Font.BOLD,
            ),
        )
    }

    private val mismatchAttributes = TextAttributes(
        JBColor(Color(0xB71C1C), Color(0xFF4D6D)),
        null,
        JBColor(Color(0xB71C1C), Color(0xFF4D6D)),
        EffectType.WAVE_UNDERSCORE,
        Font.BOLD,
    )

    val mismatchKey: TextAttributesKey = TextAttributesKey.createTextAttributesKey(
        "RAINBOW_BRACKETS_MISMATCH",
        mismatchAttributes,
    )

    val activePairAttributes = TextAttributes(
        null,
        JBColor(Color(0xFFF3CD), Color(0x3A2F0B)),
        JBColor(Color(0xD4A017), Color(0xFFD166)),
        EffectType.ROUNDED_BOX,
        Font.BOLD,
    )

    fun attributesFor(highlight: BracketHighlight): TextAttributes {
        return when (highlight.kind) {
            BracketHighlightKind.MATCHED -> keyForLevel(requireNotNull(highlight.level)).defaultAttributes.clone()
            BracketHighlightKind.MISMATCHED -> mismatchAttributes.clone()
        }
    }

    fun keyFor(highlight: BracketHighlight): TextAttributesKey {
        return when (highlight.kind) {
            BracketHighlightKind.MATCHED -> keyForLevel(requireNotNull(highlight.level))
            BracketHighlightKind.MISMATCHED -> mismatchKey
        }
    }

    private fun keyForLevel(level: Int): TextAttributesKey {
        return levelKeys[Math.floorMod(level, levelKeys.size)]
    }
}
