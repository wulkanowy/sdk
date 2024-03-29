package io.github.wulkanowy.sdk.scrapper.register

import org.jsoup.nodes.Element
import pl.droidsonroids.jspoon.annotation.Selector

internal class SentUnlockAccountResponse {

    @Selector("html")
    lateinit var html: Element

    @Selector("title")
    lateinit var title: String

    @Selector(".ErrorMessage, #ErrorTextLabel, .UnlockAccountSummary p, #box .box-p, #lblStatus, #lblMessage")
    lateinit var message: String
}
