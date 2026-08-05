package com.example.vacationsheet.dto

import com.example.vacationsheet.entity.Project
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Size
import java.time.Instant
import java.util.UUID

data class ProjectRequest(
	@field:NotBlank
	@field:Size(max = 120)
	val name: String,

	@field:Size(max = 2000)
	val description: String? = null,
)

data class ProjectResponse(
	val id: UUID,
	val name: String,
	val description: String?,
	val members: List<UserResponse>,
	val createdAt: Instant,
	val updatedAt: Instant,
)

fun Project.toResponse() = ProjectResponse(
	id = requireNotNull(id),
	name = name,
	description = description,
	members = members.map { it.toResponse() }.sortedBy { it.displayName ?: it.email },
	createdAt = createdAt,
	updatedAt = updatedAt,
)
