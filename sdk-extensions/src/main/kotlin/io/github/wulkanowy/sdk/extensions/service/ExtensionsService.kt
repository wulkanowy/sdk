package io.github.wulkanowy.sdk.extensions.service

import io.github.wulkanowy.sdk.extensions.pojo.LuckyNumber
import io.github.wulkanowy.sdk.extensions.pojo.ServerInfo
import io.reactivex.Single
import retrofit2.http.GET

interface ExtensionsService {

    @GET(".")
    fun getServerInfo(): Single<ServerInfo>

    @GET("lucky_number")
    fun getLuckyNumber(): Single<LuckyNumber>
}
