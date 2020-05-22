package io.github.wulkanowy.sdk.extensions.pojo

import com.google.gson.annotations.SerializedName

data class LuckyNumber(

    @SerializedName("lucky_number")
    val number: String
)
