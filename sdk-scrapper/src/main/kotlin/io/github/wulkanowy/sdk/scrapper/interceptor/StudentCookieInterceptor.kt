package io.github.wulkanowy.sdk.scrapper.interceptor

import okhttp3.Interceptor
import okhttp3.Response
import java.net.CookieStore
import java.net.HttpCookie
import java.net.URI

internal class StudentCookieInterceptor(
    private val cookieStore: CookieStore,
    private val schema: String,
    private val host: String,
    private val domainSuffix: String,
    diaryId: Int,
    kindergartenDiaryId: Int,
    studentId: Int,
    schoolYear: Int,
) : Interceptor {

    private val cookiesData = arrayOf(
        "idBiezacyDziennik" to diaryId,
        "idBiezacyUczen" to studentId,
        "idBiezacyDziennikPrzedszkole" to kindergartenDiaryId,
        "biezacyRokSzkolny" to schoolYear,
    )

    override fun intercept(chain: Interceptor.Chain): Response {
        cookiesData.forEach { (name, value) ->
            HttpCookie(name, value.toString()).let {
                it.path = "/"
                it.domain = "uonetplus-uczen$domainSuffix.$host"
                cookieStore.add(URI("$schema://${it.domain}"), it)
            }
        }

        // This is probably used to refresh the cookies in the request (after setting them through cookieJarCabinet above)
        return chain.proceed(chain.request().newBuilder().build())
    }
}
