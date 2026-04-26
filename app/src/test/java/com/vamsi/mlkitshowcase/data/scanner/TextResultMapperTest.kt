package com.vamsi.mlkitshowcase.data.scanner

import com.google.common.truth.Truth.assertThat
import org.junit.Test

class TextResultMapperTest {

    @Test
    fun `maps text hierarchy counts without inventing confidence`() {
        val text = RecognizedTextSnapshot(
            text = "Hello world",
            blocks = listOf(
                RecognizedTextBlockSnapshot(
                    text = "Hello world",
                    lines = listOf(
                        RecognizedTextLineSnapshot(
                            text = "Hello world",
                            elements = listOf(
                                RecognizedTextElementSnapshot(
                                    text = "Hello",
                                    symbolCount = 1
                                )
                            )
                        )
                    )
                )
            )
        )

        val result = TextResultMapper.map(text)

        assertThat(result.text).isEqualTo("Hello world")
        assertThat(result.blocks).hasSize(1)
        assertThat(result.lineCount).isEqualTo(1)
        assertThat(result.elementCount).isEqualTo(1)
        assertThat(result.symbolCount).isEqualTo(1)
    }

    @Test
    fun `trims recognized text and skips blank blocks`() {
        val text = RecognizedTextSnapshot(
            text = "  Receipt total  ",
            blocks = listOf(RecognizedTextBlockSnapshot(text = "   "))
        )

        val result = TextResultMapper.map(text)

        assertThat(result.text).isEqualTo("Receipt total")
        assertThat(result.blocks).isEmpty()
        assertThat(result.lineCount).isEqualTo(0)
    }
}
