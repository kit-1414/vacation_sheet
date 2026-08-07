package com.example.vacationsheet.mainapp.exception

class UserEmailAlreadyExistsException(email: String) : RuntimeException("User with email '$email' already exists")
