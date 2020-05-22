package io.github.wulkanowy.sdk.extensions

import io.github.wulkanowy.sdk.extensions.pojo.SearchResult
import io.github.wulkanowy.sdk.extensions.pojo.ServerInfo
import io.github.wulkanowy.sdk.extensions.service.ServiceManager
import io.reactivex.Single

class Extensions {

    private val changeManager = resettableManager()

    var searchServiceBaseUrl = "https://search.school-extensions.workers.dev/"
        set(value) {
            field = value
            changeManager.reset()
        }

    var schoolServerBaseUrl = ""
        set(value) {
            field = value
            changeManager.reset()
        }

    private val serviceManager = ServiceManager()

    private val searchService by lazy { serviceManager.getSearchService(searchServiceBaseUrl) }

    private val extensionsService by resettableLazy(changeManager) { serviceManager.getExtensionsService(schoolServerBaseUrl) }

    fun searchServerBySchoolId(id: String): Single<List<SearchResult>> = searchService.searchById(id)

    fun getServerInfo(url: String = schoolServerBaseUrl): Single<ServerInfo> {
        if (url != schoolServerBaseUrl) schoolServerBaseUrl = url

        return extensionsService.getServerInfo()
    }

    fun getLuckyNumber() = extensionsService.getLuckyNumber()
}
