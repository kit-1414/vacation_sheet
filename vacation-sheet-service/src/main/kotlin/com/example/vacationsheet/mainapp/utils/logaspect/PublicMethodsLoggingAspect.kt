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

		val prefix = "${methodName}(...)"
		logger.debug("{} Started >>>", prefix)
		return try {
			joinPoint.proceed().also {
				logger.debug("{} Done <<<", prefix)
			}
		} catch (exception: Throwable) {
			logger.error("{} ERROR {} <<<", prefix, exception.toString(), exception)
			throw exception
		}
	}
}
