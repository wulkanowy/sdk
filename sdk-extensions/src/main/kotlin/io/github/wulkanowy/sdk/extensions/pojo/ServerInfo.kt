package io.github.wulkanowy.sdk.extensions.pojo

import com.google.gson.annotations.SerializedName

data class ServerInfo(

    @SerializedName("integrations")
    val integrations: List<String>
)
