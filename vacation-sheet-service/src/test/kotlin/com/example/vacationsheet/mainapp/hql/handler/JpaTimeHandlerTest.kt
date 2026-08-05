package com.example.vacationsheet.mainapp.hql.handler

import com.example.vacationsheet.mainapp.hql.model.ProjectEntity
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull

class JpaTimeHandlerTest {
	private val handler = JpaTimeHandler()

	@Test
	fun `pre persist initializes creation and update time`() {
		val entity = ProjectEntity(name = "Project", description = null)

		handler.prePersist(entity)

		assertNotNull(entity.ctime)
		assertNotNull(entity.utime)
	}

	@Test
	fun `pre update preserves creation time`() {
		val entity = ProjectEntity(name = "Project", description = null)
		handler.prePersist(entity)
		val creationTime = entity.ctime

		handler.preUpdate(entity)

		assertEquals(creationTime, entity.ctime)
		assertNotNull(entity.utime)
	}
}
