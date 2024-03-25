package io.github.wulkanowy.sdk.scrapper

import java.net.CookieManager
import java.net.CookiePolicy
import java.net.CookieStore
import java.net.HttpCookie
import java.net.URI

internal class CookieJarCabinet {

    val userCookieManager = MergeCookieManager(
        main = CookieManager().apply {
            setCookiePolicy(CookiePolicy.ACCEPT_ALL)
        },
        second = this::additionalCookieManager,
    )

    val alternativeCookieManager = MergeCookieManager(
        main = CookieManager().apply {
            setCookiePolicy(CookiePolicy.ACCEPT_ALL)
        },
        second = this::additionalCookieManager,
    )

    private var additionalCookieManager: CookieManager? = null

    fun isUserCookiesExist(): Boolean {
        return userCookieManager.cookieStore.cookies.isNotEmpty()
    }

    fun onRegisterUserLogout() {
        clearUserCookieStore()
    }

    fun onUserChange() {
        clearUserCookieStore()
        clearAdditionalCookieStore()
    }

    fun beforeUserLogIn() {
        clearUserCookieStore()
    }

    fun onLoginServiceError() {
        clearUserCookieStore()
    }

    fun addStudentCookie(uri: URI, cookie: HttpCookie) {
        userCookieManager.cookieStore.add(uri, cookie)
    }

    fun setAdditionalCookieManager(cookieManager: CookieManager) {
        additionalCookieManager = cookieManager
    }

    private fun clearUserCookieStore() {
        userCookieManager.cookieStore.removeAll()
    }

    private fun clearAdditionalCookieStore() {
        // if we clear additional cookie manager, user will need to go pass through
        // cloudflare captcha more often
        additionalCookieManager?.cookieStore // ?.removeAll()
    }
}

internal class MergeCookieManager(
    private val main: CookieManager,
    private val second: () -> CookieManager?,
) : CookieManager() {

    override fun get(uri: URI?, requestHeaders: Map<String, List<String>>?): Map<String, List<String>> {
        return merge(
            map1 = main.get(uri, requestHeaders),
            map2 = second()?.get(uri, requestHeaders).orEmpty(),
        )
    }

    override fun put(uri: URI?, responseHeaders: Map<String, List<String>>?) {
        main.put(uri, responseHeaders)
        // if we add cookies to additional cookie manager we also need to clear it in some cases
        // like user change, etc
        // second()?.put(uri, responseHeaders)
    }

    override fun setCookiePolicy(cookiePolicy: CookiePolicy?) {
        main.setCookiePolicy(cookiePolicy)
    }

    override fun getCookieStore(): CookieStore = main.cookieStore
}

private fun merge(map1: Map<String, List<String>>, map2: Map<String, List<String>>): Map<String, List<String>> {
    val output = mutableMapOf<String, List<String>>()
    (map1.keys + map2.keys).forEach {
        output[it] = map1[it].orEmpty() + map2[it].orEmpty()
    }
    return output
}
