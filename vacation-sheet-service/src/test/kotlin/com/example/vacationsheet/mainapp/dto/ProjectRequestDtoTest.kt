package com.example.vacationsheet.mainapp.dto

import jakarta.validation.Validation
import kotlin.test.Test
import kotlin.test.assertTrue

class ProjectRequestDtoTest {
	private val validator = Validation.buildDefaultValidatorFactory().validator

	@Test
	fun `accepts documented project name characters`() {
		val validNames = listOf("Project123", "Проект123", "project-name.test_value")

		validNames.forEach { name ->
			val violations = validator.validate(ProjectRequestDto(name))
			assertTrue(violations.isEmpty(), "Expected '$name' to be valid, but got $violations")
		}
	}

	@Test
	fun `rejects spaces and unsupported characters`() {
		val invalidNames = listOf("Project name", "Project/name", "Project@name")

		invalidNames.forEach { name ->
			assertTrue(validator.validate(ProjectRequestDto(name)).isNotEmpty(), "Expected '$name' to be invalid")
		}
	}

	@Test
	fun `rejects blank project name`() {
		assertTrue(validator.validate(ProjectRequestDto("")).isNotEmpty())
	}

	@Test
	fun `rejects project name longer than one hundred characters`() {
		assertTrue(validator.validate(ProjectRequestDto("a".repeat(101))).isNotEmpty())
	}
}
