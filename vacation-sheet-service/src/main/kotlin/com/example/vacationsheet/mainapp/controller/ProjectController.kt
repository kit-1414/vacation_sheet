package com.example.vacationsheet.mainapp.controller

import com.example.vacationsheet.mainapp.dto.ProjectRequestDto
import com.example.vacationsheet.mainapp.hql.dto.ProjectDto
import com.example.vacationsheet.mainapp.service.ProjectService
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

@Tag(name = "Projects")
@RestController
@RequestMapping("/api/projects")
class ProjectController(
	private val projectService: ProjectService,
) {
	@Operation(summary = "List all projects with their members")
	@GetMapping
	fun findAll(): List<ProjectDto> = projectService.findAll()

	@Operation(summary = "Get a project with its members")
	@GetMapping("/{id}")
	fun findById(@PathVariable id: Long): ProjectDto = projectService.findById(id)

	@Operation(summary = "Create a project")
	@PostMapping
	@ResponseStatus(HttpStatus.CREATED)
	fun create(@Valid @RequestBody request: ProjectRequestDto): ProjectDto = projectService.create(request)

	@Operation(summary = "Update a project")
	@PutMapping("/{id}")
	fun update(@PathVariable id: Long, @Valid @RequestBody request: ProjectRequestDto): ProjectDto =
		projectService.update(id, request)

	@Operation(summary = "Delete a project")
	@DeleteMapping("/{id}")
	@ResponseStatus(HttpStatus.NO_CONTENT)
	fun delete(@PathVariable id: Long) = projectService.delete(id)

	@Operation(summary = "Attach a user to a project")
	@PutMapping("/{projectId}/users/{userId}")
	fun addMember(@PathVariable projectId: Long, @PathVariable userId: Long): ProjectDto =
		projectService.addMember(projectId, userId)

	@Operation(summary = "Detach a user from a project")
	@DeleteMapping("/{projectId}/users/{userId}")
	fun removeMember(@PathVariable projectId: Long, @PathVariable userId: Long): ProjectDto =
		projectService.removeMember(projectId, userId)
}
