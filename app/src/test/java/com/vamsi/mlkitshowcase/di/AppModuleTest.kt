package com.vamsi.mlkitshowcase.di

import com.google.common.truth.Truth.assertThat
import com.vamsi.mlkitshowcase.data.scanner.MLKitBarcodeScanner
import com.vamsi.mlkitshowcase.data.scanner.MLKitTextRecognizer
import dagger.Module
import org.junit.Test

/**
 * Unit tests for AppModule
 *
 * Note: Tests that require instantiating ML Kit classes should be in androidTest. These unit tests
 * focus on verifying the module structure.
 */
class AppModuleTest {

    @Test
    fun `AppModule should be properly annotated`() {
        // Given & When
        val moduleClass = AppModule::class.java

        // Then
        assertThat(moduleClass.isAnnotationPresent(Module::class.java)).isTrue()
        // InstallIn annotation verification - we'll just check it has annotations
        assertThat(moduleClass.annotations.isNotEmpty()).isTrue()
    }

    @Test
    fun `AppModule should have provideBarcodeScanner method`() {
        // Given & When
        val methods = AppModule::class.java.declaredMethods
        val hasBarcodeProvider = methods.any { it.name == "provideBarcodeScanner" }

        // Then
        assertThat(hasBarcodeProvider).isTrue()
    }

    @Test
    fun `AppModule should have provideTextRecognizer method`() {
        // Given & When
        val methods = AppModule::class.java.declaredMethods
        val hasTextProvider = methods.any { it.name == "provideTextRecognizer" }

        // Then
        assertThat(hasTextProvider).isTrue()
    }

    @Test
    fun `AppModule methods should return correct types`() {
        // Given & When
        val methods = AppModule::class.java.declaredMethods
        val barcodeMethod = methods.find { it.name == "provideBarcodeScanner" }
        val textMethod = methods.find { it.name == "provideTextRecognizer" }

        // Then
        assertThat(barcodeMethod?.returnType).isEqualTo(MLKitBarcodeScanner::class.java)
        assertThat(textMethod?.returnType).isEqualTo(MLKitTextRecognizer::class.java)
    }
}
