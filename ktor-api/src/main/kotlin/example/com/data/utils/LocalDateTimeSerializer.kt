package example.com.data.utils

import example.com.data.model.uiDate
import kotlinx.serialization.KSerializer
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

object LocalDateTimeSerializer : KSerializer<LocalDateTime> {
    private val formatter = DateTimeFormatter.ISO_LOCAL_DATE_TIME

    override val descriptor = PrimitiveSerialDescriptor("LocalDateTime", PrimitiveKind.STRING)

    override fun serialize(encoder: Encoder, value: LocalDateTime) {
        encoder.encodeString(value.format(formatter))
    }

    override fun deserialize(decoder: Decoder): LocalDateTime {
        return LocalDateTime.parse(decoder.decodeString(), formatter)
    }
}

fun LocalDateTime.monthYear(): String {
    return this.format(DateTimeFormatter.ofPattern("MMM yyyy"))
}

fun LocalDateTime.dayMonthTime(): uiDate {
    val day = this.dayOfMonth.toString()
    val dayOfWeek = this.dayOfWeek.toString()
    val time = this.format(DateTimeFormatter.ofPattern("HH:mm"))
    val month = this.format(DateTimeFormatter.ofPattern("MMM"))
    return uiDate(day, dayOfWeek, time, month)
}