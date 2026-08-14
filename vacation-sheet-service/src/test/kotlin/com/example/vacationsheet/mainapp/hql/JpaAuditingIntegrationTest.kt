package com.example.vacationsheet.mainapp.hql

import com.example.vacationsheet.mainapp.config.JpaAuditingConfig
import com.example.vacationsheet.mainapp.hql.model.ProjectEntity
import com.example.vacationsheet.mainapp.hql.model.UserAccountEntity
import com.example.vacationsheet.mainapp.hql.model.VacationRequestEntity
import com.example.vacationsheet.mainapp.hql.model.VacationRequestState
import com.example.vacationsheet.mainapp.hql.model.VacationType
import com.example.vacationsheet.mainapp.hql.repository.ProjectRepository
import com.example.vacationsheet.mainapp.hql.repository.UserAccountRepository
import com.example.vacationsheet.mainapp.hql.repository.VacationRequestRepository
import jakarta.persistence.EntityManager
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest
import org.springframework.context.annotation.Import
import org.springframework.test.context.DynamicPropertyRegistry
import org.springframework.test.context.DynamicPropertySource
import org.testcontainers.containers.PostgreSQLContainer
import org.testcontainers.junit.jupiter.Container
import org.testcontainers.junit.jupiter.Testcontainers
import java.time.LocalDate
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

@DataJpaTest
@Testcontainers
@Import(JpaAuditingConfig::class)
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class JpaAuditingIntegrationTest {
	@Autowired
	lateinit var userAccountRepository: UserAccountRepository

	@Autowired
	lateinit var projectRepository: ProjectRepository

	@Autowired
	lateinit var vacationRequestRepository: VacationRequestRepository

	@Autowired
	lateinit var entityManager: EntityManager

	@Test
	fun `user audit times are persisted and updated`() {
		val saved = userAccountRepository.saveAndFlush(UserAccountEntity("user@example.com", "Test", "User"))
		val id = requireNotNull(saved.id)
		entityManager.clear()
		val user = userAccountRepository.findById(id).orElseThrow()
		val creationTime = assertNotNull(user.ctime)
		val oldUpdateTime = assertNotNull(user.utime).minusDays(1)

		user.firstName = "Changed"
		user.utime = oldUpdateTime
		userAccountRepository.saveAndFlush(user)
		entityManager.clear()

		val updated = userAccountRepository.findById(id).orElseThrow()
		assertEquals(creationTime, updated.ctime)
		assertTrue(assertNotNull(updated.utime).isAfter(oldUpdateTime))
	}

	@Test
	fun `project audit times are persisted and updated`() {
		val saved = projectRepository.saveAndFlush(ProjectEntity("Project", null))
		val id = requireNotNull(saved.id)
		entityManager.clear()
		val project = projectRepository.findById(id).orElseThrow()
		val creationTime = assertNotNull(project.ctime)
		val oldUpdateTime = assertNotNull(project.utime).minusDays(1)

		project.description = "Changed"
		project.utime = oldUpdateTime
		projectRepository.saveAndFlush(project)
		entityManager.clear()

		val updated = projectRepository.findById(id).orElseThrow()
		assertEquals(creationTime, updated.ctime)
		assertTrue(assertNotNull(updated.utime).isAfter(oldUpdateTime))
	}

	@Test
	fun `vacation request persists relationships and audit times`() {
		val author = userAccountRepository.saveAndFlush(UserAccountEntity("author@example.com", "Test", "Author"))
		val manager = userAccountRepository.saveAndFlush(UserAccountEntity("manager@example.com", "Test", "Manager"))
		val saved = vacationRequestRepository.saveAndFlush(
			VacationRequestEntity(
				title = "Vacation",
				requestState = VacationRequestState.READY,
				vacationType = VacationType.PAYMENT_VACATION,
				startDate = LocalDate.of(2026, 9, 1),
				endDate = LocalDate.of(2026, 9, 14),
				userComments = null,
				author = author,
				manager = manager,
			),
		)
		val requestId = requireNotNull(saved.id)
		val authorId = requireNotNull(author.id)
		val managerId = requireNotNull(manager.id)
		assertNotNull(saved.ctime)
		assertNotNull(saved.utime)

		entityManager.clear()
		userAccountRepository.delete(userAccountRepository.findById(managerId).orElseThrow())
		userAccountRepository.flush()
		entityManager.clear()

		assertEquals(null, vacationRequestRepository.findByIdWithUsers(requestId)?.manager)

		entityManager.clear()
		userAccountRepository.delete(userAccountRepository.findById(authorId).orElseThrow())
		userAccountRepository.flush()
		entityManager.clear()

		assertTrue(vacationRequestRepository.findById(requestId).isEmpty)
	}

	companion object {
		@Container
		@JvmStatic
		val postgres = PostgreSQLContainer<Nothing>("postgres:18")

		@DynamicPropertySource
		@JvmStatic
		fun databaseProperties(registry: DynamicPropertyRegistry) {
			registry.add("spring.datasource.url", postgres::getJdbcUrl)
			registry.add("spring.datasource.username", postgres::getUsername)
			registry.add("spring.datasource.password", postgres::getPassword)
		}
	}
}
