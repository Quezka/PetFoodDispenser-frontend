package it.quezka.petfooddispenser

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {
    
    @Provides
    @Singleton
    fun provideNetworkManagerFactory(): NetworkManagerFactory {
        return NetworkManagerFactory { ip -> NetworkManager(ip) }
    }
}
