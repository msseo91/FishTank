package com.marineseo.fishtank.model

import javax.persistence.*

const val TB_USER = "user"

@Entity
@Table(name = TB_USER)
data class User(
    @Id
    var id: String = "",

    @Column
    var name: String = "",

    @Column
    var password: String = "",

    @Column
    var token: String = "",

    @OneToMany
    @JoinColumn(name = "userId")
    val tasks: List<Task> = listOf(),

    @OneToMany
    @JoinColumn(name = "userId")
    val periodicTasks: List<PeriodicTask> = listOf()
)
