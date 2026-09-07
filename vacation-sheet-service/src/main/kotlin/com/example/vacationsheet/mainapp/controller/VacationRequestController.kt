package com.example.vacationsheet.mainapp.controller

import com.example.vacationsheet.mainapp.hql.dto.ManagerVacationRequestDto
import com.example.vacationsheet.mainapp.service.VacationRequestService
import com.example.vacationsheet.mainapp.utils.logaspect.LogPublicMethods
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@Tag(name = "Vacation requests")
@RestController
@RequestMapping("/api/vacation_request")
@LogPublicMethods
class VacationRequestController(
	private val vacationRequestService: VacationRequestService,
) {
	@Operation(summary = "List all non-draft vacation requests")
	@GetMapping
	fun findAll(): List<ManagerVacationRequestDto> = vacationRequestService.getAllRequests()
}
