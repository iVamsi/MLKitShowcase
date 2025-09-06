package com.vamsi.mlkitshowcase

import com.google.common.truth.Truth.assertThat
import dagger.hilt.android.HiltAndroidApp
import org.junit.Test

class MLKitShowcaseApplicationTest {

    @Test
    fun `application should be properly configured for Hilt`() {
        // Given & When - Check application annotations and structure
        val applicationClass = MLKitShowcaseApplication::class.java

        // Then
        assertThat(applicationClass.isAnnotationPresent(HiltAndroidApp::class.java)).isTrue()
    }

    @Test
    fun `application should extend Android Application`() {
        // Given & When - Check class hierarchy
        val applicationClass = MLKitShowcaseApplication::class.java

        // Then
        assertThat(android.app.Application::class.java.isAssignableFrom(applicationClass)).isTrue()
    }

    @Test
    fun `application class should be instantiable`() {
        // Given & When & Then - Should be able to create instance
        val application = MLKitShowcaseApplication()
        assertThat(application).isNotNull()
        assertThat(application).isInstanceOf(android.app.Application::class.java)
    }
}
