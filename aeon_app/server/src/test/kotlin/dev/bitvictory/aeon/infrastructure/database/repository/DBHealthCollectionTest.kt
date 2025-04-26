package dev.bitvictory.aeon.infrastructure.database.repository

import com.mongodb.kotlin.client.coroutine.MongoDatabase
import dev.bitvictory.aeon.infrastructure.database.Database
import dev.bitvictory.aeon.model.primitive.UptimeStatus
import io.kotest.matchers.shouldBe
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.junit5.MockKExtension
import io.mockk.mockk
import kotlinx.coroutines.runBlocking
import org.bson.Document
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.extension.ExtendWith
import kotlin.test.Test

@ExtendWith(MockKExtension::class)
class DBHealthCollectionTest {

	@MockK
	lateinit var database: Database

	@MockK
	lateinit var mongoDatabase: MongoDatabase

	lateinit var dbHealthCollection: DBHealthCollection

	@BeforeEach
	fun setUp() {
		every { database.value } returns mongoDatabase
		dbHealthCollection = DBHealthCollection(database)
	}

	@Nested
	inner class HealthCheckTests {
		@Test
		fun `Successful database health check`() {
			runBlocking {
				val document = mockk<Document>()
				coEvery { mongoDatabase.runCommand(any()) } returns document
				coEvery { document.getOrDefault("ok", -1) } returns 1.0

				val result = dbHealthCollection.getHealth()

				coVerify {
					mongoDatabase.runCommand(any())
					document.getOrDefault("ok", -1)
				}
				result.status shouldBe UptimeStatus.UP
			}
		}

		@Test
		fun `Database down status`() {
			runBlocking {
				val document = mockk<Document>()
				coEvery { mongoDatabase.runCommand(any()) } returns document
				coEvery { document.getOrDefault("ok", -1) } returns 0.0

				val result = dbHealthCollection.getHealth()

				coVerify {
					mongoDatabase.runCommand(any())
					document.getOrDefault("ok", -1)
				}
				result.status shouldBe UptimeStatus.DOWN
			}
		}

		@Test
		fun `Database command failure`() {
			runBlocking {
				coEvery { mongoDatabase.runCommand(any()) } throws Exception("Command failed")

				val result = dbHealthCollection.getHealth()

				coVerify {
					mongoDatabase.runCommand(any())
				}

				result.status shouldBe UptimeStatus.DOWN
			}
		}
	}

}