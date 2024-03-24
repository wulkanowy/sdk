package io.github.wulkanowy.sdk.scrapper.interceptor

import io.github.wulkanowy.sdk.scrapper.CookieJarCabinet
import io.github.wulkanowy.sdk.scrapper.Scrapper.LoginType
import io.github.wulkanowy.sdk.scrapper.Scrapper.LoginType.ADFS
import io.github.wulkanowy.sdk.scrapper.Scrapper.LoginType.ADFSCards
import io.github.wulkanowy.sdk.scrapper.Scrapper.LoginType.ADFSLight
import io.github.wulkanowy.sdk.scrapper.Scrapper.LoginType.ADFSLightCufs
import io.github.wulkanowy.sdk.scrapper.Scrapper.LoginType.ADFSLightScoped
import io.github.wulkanowy.sdk.scrapper.Scrapper.LoginType.STANDARD
import io.github.wulkanowy.sdk.scrapper.exception.VulcanClientError
import io.github.wulkanowy.sdk.scrapper.getScriptParam
import io.github.wulkanowy.sdk.scrapper.login.LoginResult
import io.github.wulkanowy.sdk.scrapper.login.NotLoggedInException
import io.github.wulkanowy.sdk.scrapper.login.UrlGenerator
import io.github.wulkanowy.sdk.scrapper.repository.AccountRepository.Companion.SELECTOR_ADFS
import io.github.wulkanowy.sdk.scrapper.repository.AccountRepository.Companion.SELECTOR_ADFS_CARDS
import io.github.wulkanowy.sdk.scrapper.repository.AccountRepository.Companion.SELECTOR_ADFS_LIGHT
import io.github.wulkanowy.sdk.scrapper.repository.AccountRepository.Companion.SELECTOR_STANDARD
import kotlinx.coroutines.runBlocking
import okhttp3.HttpUrl
import okhttp3.Interceptor
import okhttp3.MediaType
import okhttp3.Protocol
import okhttp3.Request
import okhttp3.Response
import okhttp3.ResponseBody
import okio.Buffer
import okio.BufferedSource
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import org.jsoup.select.Elements
import org.slf4j.LoggerFactory
import retrofit2.HttpException
import java.io.IOException
import java.net.HttpURLConnection
import java.util.concurrent.locks.ReentrantLock
import kotlin.concurrent.withLock

private val HOSTS = arrayOf("uonetplus-wiadomosciplus", "uonetplus-uczenplus", "uonetplus-uczen")

internal data class ModuleHeaders(
    val token: String,
    val appGuid: String,
    val appVersion: String,
)

internal class AutoLoginInterceptor(
    private val loginType: LoginType,
    private val headersByHost: MutableMap<String, ModuleHeaders> = mutableMapOf(),
    private val loginLock: ReentrantLock = ReentrantLock(true),
    private val cookieJarCabinet: CookieJarCabinet,
    private val emptyCookieJarIntercept: Boolean = false,
    private val notLoggedInCallback: suspend () -> LoginResult,
    private val fetchModuleCookies: (UrlGenerator.Site) -> Pair<HttpUrl, Document>,
) : Interceptor {

    companion object {
        @JvmStatic
        private val logger = LoggerFactory.getLogger(this::class.java)
    }

    private var lastError: Throwable? = null

    override fun intercept(chain: Interceptor.Chain): Response {
        val uri = chain.request().url
        val url = uri.toString()

        return try {
            val request = chain.request()
            checkRequest()
            val response = try {
                chain.proceed(request.attachModuleHeaders())
            } catch (e: Throwable) {
                if (e is VulcanClientError) {
                    checkHttpErrorResponse(e, url)
                }
                throw e
            }
            if (response.body?.contentType()?.subtype != "json") {
                val body = response.peekBody(Long.MAX_VALUE).byteStream()
                val html = Jsoup.parse(body, null, url)
                checkResponse(html, url)
                saveModuleHeaders(html, uri)
            }
            lastError = null
            response
        } catch (e: NotLoggedInException) {
            if (loginLock.tryLock()) {
                logger.debug("Not logged in. Login in...")
                try {
                    val loginResult = runBlocking { notLoggedInCallback() }
                    headersByHost.clear()

                    val messages = getModuleCookies(UrlGenerator.Site.MESSAGES)
                    val student = when (loginResult.isStudentSchoolUseEduOne) {
                        true -> getModuleCookies(UrlGenerator.Site.STUDENT_PLUS)
                        else -> getModuleCookies(UrlGenerator.Site.STUDENT)
                    }

                    when {
                        "wiadomosciplus" in uri.host -> messages.getOrThrow()
                        "uczenplus" in uri.host -> student.getOrThrow()
                        "uczen" in uri.host -> student.getOrThrow()
                        else -> logger.info("Resource don't need further login anyway")
                    }

                    chain.proceed(chain.request().attachModuleHeaders())
                } catch (e: IOException) {
                    lastError = e
                    logger.debug("Error occurred on login")
                    throw e
                } catch (e: HttpException) {
                    lastError = e
                    logger.debug("Error occurred on login")
                    e.toOkHttpResponse(chain.request())
                } catch (e: Throwable) {
                    lastError = e
                    throw IOException("Unknown exception on login", e)
                } finally {
                    logger.debug("Login finished. Release lock")
                    loginLock.unlock()
                }
            } else {
                logger.debug("Wait for user to be logged in...")
                loginLock.withLock {
                    lastError?.let {
                        when (it) {
                            is IOException -> throw it
                            else -> throw IOException("Unknown error on login", it)
                        }
                    } ?: logger.debug("There is no last exception")
                }
                logger.debug("User logged in. Retry after login...")

                chain.proceed(chain.request().attachModuleHeaders())
            }
        }
    }

    private fun getModuleCookies(site: UrlGenerator.Site): Result<Pair<HttpUrl, Document>?> {
        return runCatching { fetchModuleCookies(site) }
            .onFailure { logger.error("Error in $site login", it) }
            .onSuccess { (url, doc) -> saveModuleHeaders(doc, url) }
    }

    private fun saveModuleHeaders(doc: Document, url: HttpUrl): Pair<HttpUrl, Document>? {
        val htmlContent = doc.select("script").html()
        val moduleHeaders = ModuleHeaders(
            token = getScriptParam("antiForgeryToken", htmlContent),
            appGuid = getScriptParam("appGuid", htmlContent),
            appVersion = getScriptParam("version", htmlContent).ifBlank {
                getScriptParam("appVersion", htmlContent)
            },
        )

        if (moduleHeaders.token.isBlank()) {
            logger.info("There is no token found on $url")
            return null
        }
        val host = HOSTS.find { it in url.host } ?: return null
        headersByHost[host] = moduleHeaders
        return url to doc
    }

    private fun Request.attachModuleHeaders(headersByHost: Map<String, ModuleHeaders> = this@AutoLoginInterceptor.headersByHost): Request {
        val headers = headersByHost.entries.find { (host) -> host in url.host }?.value ?: return this
        logger.info("X-V-AppVersion: ${headers.appVersion}")
        return newBuilder()
            .addHeader("X-V-RequestVerificationToken", headers.token)
            .addHeader("X-V-AppGuid", headers.appGuid)
            .addHeader("X-V-AppVersion", headers.appVersion)
            .build()
    }

    private fun checkRequest() {
        if (emptyCookieJarIntercept && !cookieJarCabinet.isUserCookiesExist()) {
            throw NotLoggedInException("No cookie found! You are not logged in yet")
        }
    }

    private fun checkResponse(doc: Document, url: String) {
        // if (chain.request().url().toString().contains("/Start.mvc/Get")) {
        if (url.contains("/Start.mvc/")) { // /Index return error too in 19.09.0000.34977
            doc.select(".errorBlock").let {
                if (it.isNotEmpty()) throw NotLoggedInException(it.select(".errorTitle").text())
            }
        }

        val loginSelectors = when (loginType) {
            STANDARD -> doc.select(SELECTOR_STANDARD)
            ADFS -> doc.select(SELECTOR_ADFS)
            ADFSLight, ADFSLightCufs, ADFSLightScoped -> doc.select(SELECTOR_ADFS_LIGHT)
            ADFSCards -> doc.select(SELECTOR_ADFS_CARDS)
            else -> Elements()
        }
        if (loginSelectors.isNotEmpty()) {
            throw NotLoggedInException("User not logged in")
        }

        val bodyContent = doc.body().text()
        when {
            // uonetplus-uczen
            "The custom error module" in bodyContent -> {
                throw NotLoggedInException(bodyContent)
            }
        }
    }

    private fun checkHttpErrorResponse(error: VulcanClientError, url: String) {
        val isCodeMatch = error.httpCode == HttpURLConnection.HTTP_CONFLICT
        val isSubdomainMatch = "uonetplus-wiadomosciplus" in url || "uonetplus-uczenplus" in url
        if (isCodeMatch && isSubdomainMatch) {
            throw NotLoggedInException(error.message.orEmpty())
        }
    }

    /**
     * @see [https://github.com/square/retrofit/issues/3110#issuecomment-536248102]
     */
    private fun HttpException.toOkHttpResponse(request: Request) = Response.Builder()
        .code(code())
        .message(message())
        .request(request)
        .protocol(Protocol.HTTP_1_1)
        .body(
            body = response()?.errorBody() ?: object : ResponseBody() {
                override fun contentLength() = 0L

                override fun contentType(): MediaType? = null

                override fun source(): BufferedSource = Buffer()
            },
        )
        .build()
}
