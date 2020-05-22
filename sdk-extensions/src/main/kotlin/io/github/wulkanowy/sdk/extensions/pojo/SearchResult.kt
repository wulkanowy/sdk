package io.github.wulkanowy.sdk.extensions.pojo

import com.google.gson.annotations.SerializedName

data class SearchResult(

    @SerializedName("id")
    val id: String,

    @SerializedName("name")
    val name: String,

    @SerializedName("url")
    val url: String
)
