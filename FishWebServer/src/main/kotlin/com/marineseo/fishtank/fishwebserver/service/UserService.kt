package com.marineseo.fishtank.fishwebserver.service

import com.marineseo.fishtank.fishwebserver.mapper.DatabaseMapper
import com.marineseo.fishtank.fishwebserver.model.User
import com.marineseo.fishtank.fishwebserver.util.makeRandomString
import org.springframework.stereotype.Service

const val TOKEN_LENGTH = 64
@Service
class UserService(
    private val mapper: DatabaseMapper
) {
   private val tokenMap = mutableMapOf<String, User>()

    fun signIn(id: String, password: String): User? {
        val user = mapper.getUser(id)
        return if(user.password == password) {
            user.apply { token = generateUserToken() }
            tokenMap[user.token] = user
            user
        } else {
            null
        }
    }

    private fun generateUserToken(): String {
        return makeRandomString(TOKEN_LENGTH)
    }

    fun getUserByToken(token: String): User? {
        return tokenMap[token]
    }
}