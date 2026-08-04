package com.example.vacationsheet

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.context.properties.ConfigurationPropertiesScan
import org.springframework.boot.runApplication

@SpringBootApplication
@ConfigurationPropertiesScan
class VacationSheetApplication

fun main(args: Array<String>) {
	runApplication<VacationSheetApplication>(*args)
}
