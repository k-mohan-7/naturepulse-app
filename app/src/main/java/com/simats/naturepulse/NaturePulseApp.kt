package com.simats.naturepulse

import android.app.Application
import coil.ImageLoader
import coil.ImageLoaderFactory
import coil.disk.DiskCache
import coil.memory.MemoryCache
import com.simats.naturepulse.core.datastore.PreferencesManager
import com.simats.naturepulse.core.network.NetworkModule
import com.simats.naturepulse.data.remote.ApiService
import com.simats.naturepulse.data.repository.AuthRepository
import com.simats.naturepulse.data.repository.NotificationRepository
import com.simats.naturepulse.data.repository.ReportRepository
import com.simats.naturepulse.data.repository.UserRepository

/**
 * Manual DI container — creates singletons once and exposes them
 * to ViewModels via ViewModelFactory.
 * Implements ImageLoaderFactory so Coil uses our configured OkHttpClient globally.
 */
class NaturePulseApp : Application(), ImageLoaderFactory {

    val prefs: PreferencesManager by lazy { PreferencesManager(this) }
    val api: ApiService by lazy { NetworkModule.provideApiService(prefs) }
    val authRepo: AuthRepository by lazy { AuthRepository(api) }
    val reportRepo: ReportRepository by lazy { ReportRepository(api) }
    val userRepo: UserRepository by lazy { UserRepository(api) }
    val notifRepo: NotificationRepository by lazy { NotificationRepository(api) }

    override fun onCreate() {
        super.onCreate()
        // Configure osmdroid user-agent
        org.osmdroid.config.Configuration.getInstance().userAgentValue = packageName
    }

    override fun newImageLoader(): ImageLoader {
        return ImageLoader.Builder(this)
            .okHttpClient { NetworkModule.provideOkHttpClient(prefs) }
            .memoryCache {
                MemoryCache.Builder(this)
                    .maxSizePercent(0.25)
                    .build()
            }
            .diskCache {
                DiskCache.Builder()
                    .directory(cacheDir.resolve("image_cache"))
                    .maxSizePercent(0.05)
                    .build()
            }
            .crossfade(true)
            .respectCacheHeaders(false)
            .build()
    }
}

/** Helper extension to get the app from any Context */
val android.content.Context.app get() = applicationContext as NaturePulseApp
