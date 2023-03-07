package com.marineseo.fishtank.repo

import com.marineseo.fishtank.model.PeriodicTask
import org.springframework.data.repository.CrudRepository
import org.springframework.stereotype.Repository


@Repository
interface PeriodicTaskRepository: CrudRepository<PeriodicTask, Int> {
    fun findAllByUserId(userId: String): List<PeriodicTask>

}