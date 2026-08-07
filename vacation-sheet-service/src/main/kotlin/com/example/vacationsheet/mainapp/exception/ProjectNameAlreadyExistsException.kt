package com.example.vacationsheet.mainapp.exception

class ProjectNameAlreadyExistsException(name: String) : RuntimeException("Project with name '$name' already exists")
