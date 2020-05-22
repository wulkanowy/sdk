package io.github.wulkanowy.sdk.extensions.service

import io.github.wulkanowy.sdk.extensions.pojo.SearchResult
import io.reactivex.Single
import retrofit2.http.GET
import retrofit2.http.Query

interface SearchService {

    @GET("/")
    fun searchById(@Query("id") id: String): Single<List<SearchResult>>
}
