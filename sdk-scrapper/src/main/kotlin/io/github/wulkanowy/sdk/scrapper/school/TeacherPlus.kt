package io.github.wulkanowy.sdk.scrapper.school

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class TeacherPlus(
    @SerialName("przedmiot") val subject: String,
    @SerialName("imie") val firstName: String,
    @SerialName("nazwisko") val lastName: String,
    @SerialName("wychowawca") val isWychowawca: Boolean,
    @SerialName("globalKeySkrzynka") val mailboxGlobalKey: String?,
)

@Serializable
data class TeacherPlusResponse(
    @SerialName("nauczyciele") val teachers: List<TeacherPlus>,
)
