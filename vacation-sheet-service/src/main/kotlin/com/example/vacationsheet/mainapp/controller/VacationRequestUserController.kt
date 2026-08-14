package com.example.vacationsheet.mainapp.controller

import com.example.vacationsheet.mainapp.dto.VacationRequestRequestDto
import com.example.vacationsheet.mainapp.hql.dto.VacationRequestDto
import com.example.vacationsheet.mainapp.service.CurrentUserService
import com.example.vacationsheet.mainapp.service.VacationRequestService
import com.example.vacationsheet.mainapp.utils.logaspect.LogPublicMethods
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

@Tag(name = "User vacation requests")
@RestController
@RequestMapping("/api/user/actions/vacation_request")
@LogPublicMethods
class VacationRequestUserController(
	private val vacationRequestService: VacationRequestService,
	private val currentUserService: CurrentUserService,
) {
	@Operation(summary = "List the current user's vacation requests")
	@GetMapping
	fun findAll(): List<VacationRequestDto> =
		vacationRequestService.getRequestsByOwnerId(currentUserService.getCurrentUser().id)

	@Operation(summary = "Get the current user's vacation request")
	@GetMapping("/{id}")
	fun findById(@PathVariable id: Long): VacationRequestDto =
		vacationRequestService.findById(id, currentUserService.getCurrentUser())

	@Operation(summary = "Create a vacation request for the current user")
	@PostMapping
	@ResponseStatus(HttpStatus.CREATED)
	fun create(
		@Valid @RequestBody request: VacationRequestRequestDto,
	): VacationRequestDto = vacationRequestService.create( request, currentUserService.getCurrentUser())

	@Operation(summary = "Update the current user's vacation request")
	@PutMapping("/{id}")
	fun update(
		@PathVariable id: Long,
		@Valid @RequestBody request: VacationRequestRequestDto,
	): VacationRequestDto = vacationRequestService.update(id, currentUserService.getCurrentUser(), request)

	@Operation(summary = "Delete the current user's vacation request")
	@DeleteMapping("/{id}")
	@ResponseStatus(HttpStatus.NO_CONTENT)
	fun delete(@PathVariable id: Long) =
		vacationRequestService.delete(id, currentUserService.getCurrentUser())
}
