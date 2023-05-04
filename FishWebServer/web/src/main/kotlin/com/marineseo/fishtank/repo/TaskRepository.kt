package com.marineseo.fishtank.repo

import com.marineseo.fishtank.model.Task
import org.springframework.data.repository.CrudRepository
import org.springframework.stereotype.Repository
import java.util.*

@Repository
interface TaskRepository: CrudRepository<Task, Int> {

    fun findByStateAndExecuteTimeLessThan(state: Int, executeTime: Date): List<Task>

}