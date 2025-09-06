package com.vamsi.mlkitshowcase.ui.theme

import androidx.compose.ui.graphics.Color
import com.google.common.truth.Truth.assertThat
import org.junit.Test

class ColorTest {

    @Test
    fun `Purple80 should have correct color value`() {
        // Given & When
        val color = Purple80

        // Then
        assertThat(color).isEqualTo(Color(0xFFD0BCFF))
    }

    @Test
    fun `PurpleGrey80 should have correct color value`() {
        // Given & When
        val color = PurpleGrey80

        // Then
        assertThat(color).isEqualTo(Color(0xFFCCC2DC))
    }

    @Test
    fun `Pink80 should have correct color value`() {
        // Given & When
        val color = Pink80

        // Then
        assertThat(color).isEqualTo(Color(0xFFEFB8C8))
    }

    @Test
    fun `Purple40 should have correct color value`() {
        // Given & When
        val color = Purple40

        // Then
        assertThat(color).isEqualTo(Color(0xFF6650a4))
    }

    @Test
    fun `PurpleGrey40 should have correct color value`() {
        // Given & When
        val color = PurpleGrey40

        // Then
        assertThat(color).isEqualTo(Color(0xFF625b71))
    }

    @Test
    fun `Pink40 should have correct color value`() {
        // Given & When
        val color = Pink40

        // Then
        assertThat(color).isEqualTo(Color(0xFF7D5260))
    }

    @Test
    fun `light theme colors should be properly defined`() {
        // Given & When
        val lightColors = listOf(Purple40, PurpleGrey40, Pink40)

        // Then
        lightColors.forEach { color ->
            assertThat(color).isNotNull()
            assertThat(color).isInstanceOf(Color::class.java)
        }
    }

    @Test
    fun `dark theme colors should be properly defined`() {
        // Given & When
        val darkColors = listOf(Purple80, PurpleGrey80, Pink80)

        // Then
        darkColors.forEach { color ->
            assertThat(color).isNotNull()
            assertThat(color).isInstanceOf(Color::class.java)
        }
    }

    @Test
    fun `colors should have correct alpha values`() {
        // Given & When & Then
        assertThat(Purple80.alpha).isEqualTo(1.0f)
        assertThat(PurpleGrey80.alpha).isEqualTo(1.0f)
        assertThat(Pink80.alpha).isEqualTo(1.0f)
        assertThat(Purple40.alpha).isEqualTo(1.0f)
        assertThat(PurpleGrey40.alpha).isEqualTo(1.0f)
        assertThat(Pink40.alpha).isEqualTo(1.0f)
    }
}