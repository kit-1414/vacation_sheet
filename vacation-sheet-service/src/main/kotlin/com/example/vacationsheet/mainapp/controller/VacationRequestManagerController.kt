package com.example.vacationsheet.mainapp.controller

import com.example.vacationsheet.mainapp.dto.VacationRequestManagerActionDto
import com.example.vacationsheet.mainapp.hql.dto.ManagerVacationRequestDto
import com.example.vacationsheet.mainapp.service.CurrentUserService
import com.example.vacationsheet.mainapp.service.VacationRequestService
import com.example.vacationsheet.mainapp.utils.logaspect.LogPublicMethods
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import jakarta.validation.Valid
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@Tag(name = "Manager vacation requests")
@RestController
@RequestMapping("/api/manager/actions/vacation_request")
@LogPublicMethods
class VacationRequestManagerController(
	private val vacationRequestService: VacationRequestService,
	private val currentUserService: CurrentUserService,
) {
	@Operation(summary = "List vacation requests available for review")
	@GetMapping
	fun findAll(): List<ManagerVacationRequestDto> = vacationRequestService.getRequestsForManager()

	@Operation(summary = "Get a vacation request available for review")
	@GetMapping("/{id}")
	fun findById(@PathVariable id: Long): ManagerVacationRequestDto =
		vacationRequestService.findByIdForManager(id)

	@Operation(summary = "Review a vacation request")
	@PutMapping("/{id}")
	fun review(
		@PathVariable id: Long,
		@Valid @RequestBody action: VacationRequestManagerActionDto,
	): ManagerVacationRequestDto = vacationRequestService.review(id, currentUserService.getCurrentUser(), action)
}
