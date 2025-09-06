package com.vamsi.mlkitshowcase.di

import com.vamsi.mlkitshowcase.data.scanner.MLKitBarcodeScanner
import com.vamsi.mlkitshowcase.data.scanner.MLKitTextRecognizer
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    @Singleton
    fun provideBarcodeScanner(): MLKitBarcodeScanner {
        return MLKitBarcodeScanner()
    }

    @Provides
    @Singleton
    fun provideTextRecognizer(): MLKitTextRecognizer {
        return MLKitTextRecognizer()
    }
}