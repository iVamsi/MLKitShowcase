package com.vamsi.mlkitshowcase.data.scanner

import com.google.common.truth.Truth.assertThat
import org.junit.Test

/**
 * Unit tests for MLKitTextRecognizer
 *
 * Note: Tests that require Android runtime (actual recognition) should be in androidTest. These
 * unit tests focus on testable logic without ML Kit dependencies.
 */
class MLKitTextRecognizerTest {

    @Test
    fun `text recognizer class should be available`() {
        // Given & When & Then
        // This test ensures the class compiles and can be referenced
        val className = MLKitTextRecognizer::class.java.simpleName
        assertThat(className).isEqualTo("MLKitTextRecognizer")
    }

    @Test
    fun `text recognizer should be annotated with singleton`() {
        // Given & When & Then
        val annotations = MLKitTextRecognizer::class.java.annotations
        val hasSingletonAnnotation =
                annotations.any { it.annotationClass.simpleName == "Singleton" }
        assertThat(hasSingletonAnnotation).isTrue()
    }
}
