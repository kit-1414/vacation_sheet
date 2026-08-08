package com.example.vacationsheet.mainapp.controller

import com.example.vacationsheet.mainapp.dto.UserAccountRequestDto
import com.example.vacationsheet.mainapp.hql.dto.UserAccountDto
import com.example.vacationsheet.mainapp.service.UserService
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

@Tag(name = "Users")
@RestController
@RequestMapping("/api/users")
@LogPublicMethods
class UserController(
	private val userService: UserService,
) {
	@Operation(summary = "List all registered users")
	@GetMapping
	fun findAll(): List<UserAccountDto> = userService.findAll()

	@Operation(summary = "Get a registered user")
	@GetMapping("/{id}")
	fun findById(@PathVariable id: Long): UserAccountDto = userService.findById(id)

	@Operation(summary = "Create a user")
	@PostMapping
	@ResponseStatus(HttpStatus.CREATED)
	fun create(@Valid @RequestBody request: UserAccountRequestDto): UserAccountDto = userService.create(request)

	@Operation(summary = "Update a user")
	@PutMapping("/{id}")
	fun update(@PathVariable id: Long, @Valid @RequestBody request: UserAccountRequestDto): UserAccountDto =
		userService.update(id, request)

	@Operation(summary = "Delete a user")
	@DeleteMapping("/{id}")
	@ResponseStatus(HttpStatus.NO_CONTENT)
	fun delete(@PathVariable id: Long) = userService.delete(id)
}
