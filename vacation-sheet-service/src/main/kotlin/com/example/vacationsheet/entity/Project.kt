package com.example.vacationsheet.entity

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.JoinColumn
import jakarta.persistence.JoinTable
import jakarta.persistence.ManyToMany
import jakarta.persistence.Table
import java.time.Instant
import java.util.UUID

@Entity
@Table(name = "projects")
class Project(
	@Column(nullable = false, length = 120)
	var name: String,

	@Column(length = 2000)
	var description: String?,

	@Column(name = "created_at", nullable = false)
	val createdAt: Instant = Instant.now(),

	@Column(name = "updated_at", nullable = false)
	var updatedAt: Instant = Instant.now(),

	@ManyToMany
	@JoinTable(
		name = "project_members",
		joinColumns = [JoinColumn(name = "project_id")],
		inverseJoinColumns = [JoinColumn(name = "user_account_id")],
	)
	val members: MutableSet<UserAccount> = linkedSetOf(),

	@Id
	@GeneratedValue(strategy = GenerationType.UUID)
	val id: UUID? = null,
)
