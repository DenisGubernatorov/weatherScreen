package com.weatherscreen.di

import com.weatherscreen.data.repository.WeatherRepositoryImpl
import com.weatherscreen.domain.repository.WeatherRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
interface RepositoryModule {

    @Binds
    @Singleton
    fun bindWeatherRepository(
        impl: WeatherRepositoryImpl
    ): WeatherRepository


}