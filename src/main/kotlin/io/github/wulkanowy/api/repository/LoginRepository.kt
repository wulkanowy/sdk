package io.github.wulkanowy.api.repository

import io.github.wulkanowy.api.login.ADFSFormResponse
import io.github.wulkanowy.api.login.CertificateResponse
import io.github.wulkanowy.api.register.HomepageResponse
import io.github.wulkanowy.api.service.LoginService
import io.reactivex.Single
import java.net.URLEncoder

class LoginRepository(
        private val schema: String,
        val host: String,
        private val symbol: String,
        private val api: LoginService
) {

    private val firstEndpointUrl by lazy {
        val url = URLEncoder.encode("$schema://uonetplus.$host/$symbol/LoginEndpoint.aspx", "UTF-8")
        "/$symbol/FS/LS?wa=wsignin1.0&wtrealm=$url&wctx=$url"
    }

    fun sendCredentials(credentials: Map<String, String>): Single<CertificateResponse> {
        return api.sendCredentials(firstEndpointUrl, credentials)
    }

    fun sendCertificate(certificate: CertificateResponse, url: String = certificate.action): Single<HomepageResponse> { // response for adfs
        return api.sendCertificate(url, mapOf(
                "wa" to certificate.wa,
                "wresult" to certificate.wresult,
                "wctx" to certificate.wctx
        ))
    }

    // ADFS

    fun isADFS(): Boolean {
        return when(host) {
            "vulcan.net.pl" -> false
            "fakelog.cf" -> false
            "fakelog.localhost:3000" -> false
            else -> true
        }
    }

    fun getADFSFormState(): Single<ADFSFormResponse> {
        return api.getForm(firstEndpointUrl)
    }

    fun sendADFSFormStandardChoice(url: String, formState: Map<String, String>): Single<ADFSFormResponse> {
        return api.sendADFSFormStandardChoice("$schema://adfs.$host/$url", formState)
    }

    fun sendADFSCredentials(url: String, credentials: Map<String, String>): Single<CertificateResponse> {
        return api.sendADFSCredentials("$schema://adfs.$host/$url", credentials)
    }

    fun sendADFSFirstCertificate(certificate: CertificateResponse, url: String = certificate.action): Single<CertificateResponse> {
        return api.sendADFSFirstCertificate(url, mapOf(
                "wa" to certificate.wa,
                "wresult" to certificate.wresult,
                "wctx" to certificate.wctx
        ))
    }
}