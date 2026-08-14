package com.example.vacationsheet.mainapp.controller

import com.example.vacationsheet.mainapp.dto.VacationRequestRequestDto
import com.example.vacationsheet.mainapp.hql.dto.VacationRequestDto
import com.example.vacationsheet.mainapp.service.UserService
import com.example.vacationsheet.mainapp.service.VacationRequestService
import com.example.vacationsheet.mainapp.utils.logaspect.LogPublicMethods
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import jakarta.validation.Valid
import org.springframework.http.HttpStatus
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.security.oauth2.core.user.OAuth2User
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.server.ResponseStatusException

@Tag(name = "User vacation requests")
@RestController
@RequestMapping("/api/user/actions/vacation_request")
@LogPublicMethods
class VacationRequestUserController(
	private val vacationRequestService: VacationRequestService,
	private val userService: UserService,
) {
	@Operation(summary = "List the current user's vacation requests")
	@GetMapping
	fun findAll(@AuthenticationPrincipal principal: OAuth2User): List<VacationRequestDto> =
		vacationRequestService.getRequestsByOwnerId(currentUserId(principal))

	@Operation(summary = "Get the current user's vacation request")
	@GetMapping("/{id}")
	fun findById(@PathVariable id: Long, @AuthenticationPrincipal principal: OAuth2User): VacationRequestDto =
		vacationRequestService.findById(id, currentUserId(principal))

	@Operation(summary = "Create a vacation request for the current user")
	@PostMapping
	@ResponseStatus(HttpStatus.CREATED)
	fun create(
		@Valid @RequestBody request: VacationRequestRequestDto,
		@AuthenticationPrincipal principal: OAuth2User,
	): VacationRequestDto = vacationRequestService.create(currentUserId(principal), request)

	@Operation(summary = "Update the current user's vacation request")
	@PutMapping("/{id}")
	fun update(
		@PathVariable id: Long,
		@Valid @RequestBody request: VacationRequestRequestDto,
		@AuthenticationPrincipal principal: OAuth2User,
	): VacationRequestDto = vacationRequestService.update(id, currentUserId(principal), request)

	@Operation(summary = "Delete the current user's vacation request")
	@DeleteMapping("/{id}")
	@ResponseStatus(HttpStatus.NO_CONTENT)
	fun delete(@PathVariable id: Long, @AuthenticationPrincipal principal: OAuth2User) =
		vacationRequestService.delete(id, currentUserId(principal))

	private fun currentUserId(principal: OAuth2User): Long {
		val email = principal.getAttribute<Any>("default_email")?.toString()
			?: throw ResponseStatusException(HttpStatus.FORBIDDEN)
		return userService.findCurrent(email).id
	}
}
