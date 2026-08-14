package com.example.vacationsheet.mainapp.exception

import org.springframework.http.HttpStatus
import org.springframework.http.ProblemDetail
import org.springframework.web.bind.MethodArgumentNotValidException
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.RestControllerAdvice

@RestControllerAdvice
class RestExceptionHandler {
	@ExceptionHandler(ResourceNotFoundException::class)
	fun handleNotFound(exception: ResourceNotFoundException): ProblemDetail =
		ProblemDetail.forStatusAndDetail(HttpStatus.NOT_FOUND, exception.message ?: "Resource not found")

	@ExceptionHandler(ProjectNameAlreadyExistsException::class)
	fun handleProjectNameConflict(exception: ProjectNameAlreadyExistsException): ProblemDetail =
		ProblemDetail.forStatusAndDetail(HttpStatus.CONFLICT, exception.message ?: "Project name already exists")

	@ExceptionHandler(UserEmailAlreadyExistsException::class)
	fun handleUserEmailConflict(exception: UserEmailAlreadyExistsException): ProblemDetail =
		ProblemDetail.forStatusAndDetail(HttpStatus.CONFLICT, exception.message ?: "User email already exists")

	@ExceptionHandler(VacationRequestAccessDeniedException::class)
	fun handleVacationRequestAccessDenied(exception: VacationRequestAccessDeniedException): ProblemDetail =
		ProblemDetail.forStatusAndDetail(HttpStatus.FORBIDDEN, exception.message ?: "Vacation request access denied")

	@ExceptionHandler(VacationRequestModificationNotAllowedException::class)
	fun handleVacationRequestConflict(exception: VacationRequestModificationNotAllowedException): ProblemDetail =
		ProblemDetail.forStatusAndDetail(HttpStatus.CONFLICT, exception.message ?: "Vacation request cannot be modified")

	@ExceptionHandler(InvalidVacationRequestException::class)
	fun handleInvalidVacationRequest(exception: InvalidVacationRequestException): ProblemDetail =
		ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, exception.message ?: "Invalid vacation request")

	@ExceptionHandler(MethodArgumentNotValidException::class)
	fun handleValidation(exception: MethodArgumentNotValidException): ProblemDetail {
		val problem = ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, "Request validation failed")
		problem.setProperty(
			"errors",
			exception.bindingResult.fieldErrors.associate { it.field to (it.defaultMessage ?: "Invalid value") },
		)
		return problem
	}
}
