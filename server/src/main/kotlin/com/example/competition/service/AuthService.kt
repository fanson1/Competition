package com.example.competition.service

import at.favre.lib.crypto.bcrypt.BCrypt
import com.example.competition.model.AuthTokens
import com.example.competition.model.Users
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.select
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.deleteWhere
import org.jetbrains.exposed.sql.transactions.transaction
import java.security.SecureRandom
import java.util.Base64

object AuthService {
    private val random = SecureRandom()

    fun hashPassword(password: String): String {
        return BCrypt.withDefaults().hashToString(12, password.toCharArray())
    }

    fun verifyPassword(password: String, hash: String): Boolean {
        return BCrypt.verifyer().verify(password.toCharArray(), hash).verified
    }

    fun generateToken(): String {
        val bytes = ByteArray(32)
        random.nextBytes(bytes)
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes)
    }

    fun createToken(userId: String): String {
        val token = generateToken()
        val now = System.currentTimeMillis()
        val expiresAt = now + 30 * 24 * 60 * 60 * 1000L // 30 days

        transaction {
            AuthTokens.insert {
                it[AuthTokens.token] = token
                it[AuthTokens.userId] = userId
                it[AuthTokens.createdAt] = now
                it[AuthTokens.expiresAt] = expiresAt
            }
        }
        return token
    }

    fun validateToken(token: String): String? {
        return transaction {
            val row = AuthTokens.selectAll().where { AuthTokens.token eq token }.singleOrNull()
            if (row != null && row[AuthTokens.expiresAt] > System.currentTimeMillis()) {
                row[AuthTokens.userId]
            } else {
                null
            }
        }
    }

    fun revokeToken(token: String) {
        transaction {
            AuthTokens.deleteWhere { AuthTokens.token eq token }
        }
    }

    fun getUserByUsername(username: String): UserRow? {
        return transaction {
            Users.selectAll().where { Users.username eq username }.singleOrNull()?.let {
                UserRow(
                    id = it[Users.id],
                    username = it[Users.username],
                    passwordHash = it[Users.passwordHash],
                    nickname = it[Users.nickname],
                    avatarEmoji = it[Users.avatarEmoji],
                    createdAt = it[Users.createdAt],
                    lastLoginAt = it[Users.lastLoginAt]
                )
            }
        }
    }

    fun getUserById(id: String): UserRow? {
        return transaction {
            Users.selectAll().where { Users.id eq id }.singleOrNull()?.let {
                UserRow(
                    id = it[Users.id],
                    username = it[Users.username],
                    passwordHash = it[Users.passwordHash],
                    nickname = it[Users.nickname],
                    avatarEmoji = it[Users.avatarEmoji],
                    createdAt = it[Users.createdAt],
                    lastLoginAt = it[Users.lastLoginAt]
                )
            }
        }
    }

    fun getAllUsers(): List<UserRow> {
        return transaction {
            Users.selectAll().map {
                UserRow(
                    id = it[Users.id],
                    username = it[Users.username],
                    passwordHash = it[Users.passwordHash],
                    nickname = it[Users.nickname],
                    avatarEmoji = it[Users.avatarEmoji],
                    createdAt = it[Users.createdAt],
                    lastLoginAt = it[Users.lastLoginAt]
                )
            }
        }
    }

    fun createUser(id: String, username: String, password: String, nickname: String): UserRow {
        val hash = hashPassword(password)
        val now = System.currentTimeMillis()

        transaction {
            Users.insert {
                it[Users.id] = id
                it[Users.username] = username
                it[Users.passwordHash] = hash
                it[Users.nickname] = nickname
                it[Users.avatarEmoji] = "😀"
                it[Users.createdAt] = now
                it[Users.lastLoginAt] = now
            }
        }

        return UserRow(id, username, hash, nickname, "😀", now, now)
    }

    fun usernameExists(username: String): Boolean {
        return transaction {
            Users.selectAll().where { Users.username eq username }.count() > 0
        }
    }

    data class UserRow(
        val id: String,
        val username: String,
        val passwordHash: String,
        val nickname: String,
        val avatarEmoji: String,
        val createdAt: Long,
        val lastLoginAt: Long
    )
}
