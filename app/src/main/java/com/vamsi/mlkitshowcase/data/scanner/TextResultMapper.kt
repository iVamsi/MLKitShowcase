package com.vamsi.mlkitshowcase.data.scanner

import android.graphics.Rect
import com.google.mlkit.vision.text.Text
import com.vamsi.mlkitshowcase.domain.model.TextBlockResult
import com.vamsi.mlkitshowcase.domain.model.TextScanResult

object TextResultMapper {

    fun map(text: Text): TextScanResult {
        return map(text.toSnapshot())
    }

    fun map(text: RecognizedTextSnapshot): TextScanResult {
        val blocks = text.blocks
            .filter { it.text.isNotBlank() }
            .map { block ->
                TextBlockResult(
                    text = block.text.trim(),
                    boundingBox = block.boundingBox,
                    lineCount = block.lines.size
                )
            }

        val lines = text.blocks.flatMap { it.lines }
        val elements = lines.flatMap { it.elements }
        val symbolCount = elements.sumOf { it.symbolCount }

        return TextScanResult(
            text = text.text.trim(),
            blocks = blocks,
            lineCount = lines.size,
            elementCount = elements.size,
            symbolCount = symbolCount
        )
    }

    private fun Text.toSnapshot(): RecognizedTextSnapshot {
        return RecognizedTextSnapshot(
            text = text,
            blocks = textBlocks.map { block ->
                RecognizedTextBlockSnapshot(
                    text = block.text,
                    boundingBox = block.boundingBox,
                    lines = block.lines.map { line ->
                        RecognizedTextLineSnapshot(
                            text = line.text,
                            boundingBox = line.boundingBox,
                            elements = line.elements.map { element ->
                                RecognizedTextElementSnapshot(
                                    text = element.text,
                                    symbolCount = element.symbols.size
                                )
                            }
                        )
                    }
                )
            }
        )
    }
}

data class RecognizedTextSnapshot(
    val text: String,
    val blocks: List<RecognizedTextBlockSnapshot>,
)

data class RecognizedTextBlockSnapshot(
    val text: String,
    val boundingBox: Rect? = null,
    val lines: List<RecognizedTextLineSnapshot> = emptyList(),
)

data class RecognizedTextLineSnapshot(
    val text: String,
    val boundingBox: Rect? = null,
    val elements: List<RecognizedTextElementSnapshot> = emptyList(),
)

data class RecognizedTextElementSnapshot(
    val text: String,
    val symbolCount: Int = 0,
)
