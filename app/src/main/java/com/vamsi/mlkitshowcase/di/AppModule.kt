package com.vamsi.mlkitshowcase.di

import com.vamsi.mlkitshowcase.data.scanner.MLKitBarcodeScanner
import com.vamsi.mlkitshowcase.data.scanner.MLKitTextRecognizer
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    fun provideBarcodeScanner(): MLKitBarcodeScanner {
        return MLKitBarcodeScanner()
    }

    @Provides
    fun provideTextRecognizer(): MLKitTextRecognizer {
        return MLKitTextRecognizer()
    }
}
