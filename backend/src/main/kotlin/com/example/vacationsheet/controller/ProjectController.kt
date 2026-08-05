package com.example.vacationsheet.controller

import com.example.vacationsheet.dto.ProjectRequest
import com.example.vacationsheet.dto.ProjectResponse
import com.example.vacationsheet.service.ProjectService
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import jakarta.validation.Valid
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.bind.annotation.RestController
import java.util.UUID

@Tag(name = "Projects")
@RestController
@RequestMapping("/api/projects")
class ProjectController(
	private val projectService: ProjectService,
) {
	@Operation(summary = "List all projects with their members")
	@GetMapping
	fun findAll(): List<ProjectResponse> = projectService.findAll()

	@Operation(summary = "Create a project")
	@PostMapping
	@ResponseStatus(HttpStatus.CREATED)
	fun create(@Valid @RequestBody request: ProjectRequest): ProjectResponse = projectService.create(request)

	@Operation(summary = "Update a project")
	@PutMapping("/{id}")
	fun update(@PathVariable id: UUID, @Valid @RequestBody request: ProjectRequest): ProjectResponse =
		projectService.update(id, request)

	@Operation(summary = "Delete a project")
	@DeleteMapping("/{id}")
	@ResponseStatus(HttpStatus.NO_CONTENT)
	fun delete(@PathVariable id: UUID) = projectService.delete(id)

	@Operation(summary = "Attach a user to a project")
	@PutMapping("/{projectId}/users/{userId}")
	fun addMember(@PathVariable projectId: UUID, @PathVariable userId: UUID): ProjectResponse =
		projectService.addMember(projectId, userId)

	@Operation(summary = "Detach a user from a project")
	@DeleteMapping("/{projectId}/users/{userId}")
	fun removeMember(@PathVariable projectId: UUID, @PathVariable userId: UUID): ProjectResponse =
		projectService.removeMember(projectId, userId)
}
