package com.example.vacationsheet.exception

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
