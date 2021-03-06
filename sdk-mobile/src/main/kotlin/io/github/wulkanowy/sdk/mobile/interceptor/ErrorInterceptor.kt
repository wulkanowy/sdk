package io.github.wulkanowy.sdk.mobile.interceptor

import io.github.wulkanowy.sdk.mobile.exception.InvalidSymbolException
import okhttp3.Interceptor
import okhttp3.Response
import java.io.IOException

class ErrorInterceptor : Interceptor {

    override fun intercept(chain: Interceptor.Chain): Response {
        val response = chain.proceed(chain.request())
        val body = response.peekBody(Long.MAX_VALUE).string()

        when {
            body == "Bad Request" -> throw IOException(body)
            body.contains("Podany symbol grupujący jest nieprawidłowy") -> throw InvalidSymbolException()
        }

        return response
    }
}
