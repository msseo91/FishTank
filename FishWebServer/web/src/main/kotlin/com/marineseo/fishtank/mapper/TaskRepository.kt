package com.marineseo.fishtank.mapper

import com.marineseo.fishtank.model.Task
import org.springframework.data.repository.CrudRepository
import org.springframework.stereotype.Repository
import java.util.Date

@Repository
interface TaskRepository: CrudRepository<Task, Int> {

    fun findByStateAndExecuteTimeGreaterThan(state: Int, executeTime: Date): List<Task>

}