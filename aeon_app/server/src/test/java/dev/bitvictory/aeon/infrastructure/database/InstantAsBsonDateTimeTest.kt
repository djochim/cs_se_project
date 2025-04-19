package dev.bitvictory.aeon.infrastructure.database

import io.kotest.matchers.shouldBe
import io.mockk.confirmVerified
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.junit5.MockKExtension
import io.mockk.mockk
import io.mockk.verify
import kotlinx.datetime.Instant
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.SerializationException
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import org.bson.BsonDateTime
import org.bson.BsonValue
import org.bson.codecs.kotlinx.BsonDecoder
import org.bson.codecs.kotlinx.BsonEncoder
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.api.extension.ExtendWith
import kotlin.test.Test

@OptIn(ExperimentalSerializationApi::class)
@ExtendWith(MockKExtension::class)
class InstantAsBsonDateTimeTest {

	@Test
	fun `serialize with BsonEncoder and valid Instant`(@MockK bsonEncoder: BsonEncoder) {
		val time = Instant.fromEpochSeconds(1000)

		every { bsonEncoder.encodeBsonValue(any()) } returns Unit

		InstantAsBsonDateTime.serialize(bsonEncoder, time)

		verify {
			bsonEncoder.encodeBsonValue(
				BsonDateTime(time.toEpochMilliseconds())
			)
		}
		confirmVerified(bsonEncoder)
	}

	@Test
	fun `serialize with non BsonEncoder`(@MockK encoder: Encoder) {
		val time = Instant.fromEpochSeconds(1000)

		assertThrows<SerializationException> {
			InstantAsBsonDateTime.serialize(encoder, time)
		}
	}

	@Test
	fun `deserialize with BsonDecoder and valid BsonDateTime`(@MockK decoder: BsonDecoder) {
		val time = Instant.fromEpochSeconds(1000)
		val bsonValue = mockk<BsonValue>()
		val bsonDateTime = BsonDateTime(time.toEpochMilliseconds())

		every { decoder.decodeBsonValue() } returns bsonValue
		every { bsonValue.asDateTime() } returns bsonDateTime

		val value = InstantAsBsonDateTime.deserialize(decoder)

		verify {
			decoder.decodeBsonValue()
			bsonValue.asDateTime()
		}
		confirmVerified(decoder, bsonValue)

		value shouldBe time
	}

	@Test
	fun `deserialize with non BsonDecoder`(@MockK decoder: Decoder) {

		assertThrows<SerializationException> {
			InstantAsBsonDateTime.deserialize(decoder)
		}
	}

}