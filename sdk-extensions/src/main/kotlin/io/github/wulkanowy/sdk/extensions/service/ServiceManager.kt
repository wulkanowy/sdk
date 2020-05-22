package io.github.wulkanowy.sdk.extensions.service

import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.create

class ServiceManager {

    fun getSearchService(searchServiceBaseUrl: String): SearchService = getRetrofit(searchServiceBaseUrl).create()

    fun getExtensionsService(url: String): ExtensionsService = getRetrofit(url).create()

    private fun getRetrofit(url: String): Retrofit {
        return Retrofit.Builder()
            .baseUrl("${url.removeSuffix("/")}/")
            .client(OkHttpClient.Builder()
                .addInterceptor(HttpLoggingInterceptor().setLevel(HttpLoggingInterceptor.Level.BODY))
                .build())
            .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }
}
