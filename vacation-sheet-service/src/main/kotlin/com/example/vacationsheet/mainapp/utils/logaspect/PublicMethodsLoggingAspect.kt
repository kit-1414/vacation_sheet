package com.example.vacationsheet.mainapp.utils.logaspect

import org.aspectj.lang.ProceedingJoinPoint
import org.aspectj.lang.annotation.Around
import org.aspectj.lang.annotation.Aspect
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component

@Aspect
@Component
class PublicMethodsLoggingAspect {
	@Around("@within(com.example.vacationsheet.mainapp.utils.logaspect.LogPublicMethods)")
	fun logMethod(joinPoint: ProceedingJoinPoint): Any? {
		val className = joinPoint.target::class.simpleName ?: joinPoint.signature.declaringType.simpleName
		val methodName = joinPoint.signature.name
		val logger = LoggerFactory.getLogger(joinPoint.target::class.java)

		logger.debug("{}:{}() Started >>>", className, methodName)
		return try {
			joinPoint.proceed().also {
				logger.debug("{}:{}() Done <<<", className, methodName)
			}
		} catch (exception: Throwable) {
			logger.error("{}:{}() ERROR {} <<<", className, methodName, exception.toString(), exception)
			throw exception
		}
	}
}
