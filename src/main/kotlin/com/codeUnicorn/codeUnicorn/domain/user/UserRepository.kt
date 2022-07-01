package com.codeUnicorn.codeUnicorn.domain.user

import java.time.LocalDateTime
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Modifying
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import org.springframework.transaction.annotation.Transactional

// DB Layer 접근자
// JpaRepository 는 기본적으로 CRUD 쿼리 함수 제공
interface UserRepository : JpaRepository<User, Int> {
    fun findByEmail(email: String): User?

    @Transactional
    @Query("select * from user where deleted_at is null", nativeQuery = true)
    fun getUserInfoList(): MutableList<User?>

    @Transactional
    @Modifying
    @Query("update user set nickname = :nickname, updated_at = :updatedAt where id = :id", nativeQuery = true)
    fun updateNickname(@Param("id") id: Int, @Param("nickname") nickname: String, @Param("updatedAt") updatedAt: LocalDateTime): Int?

    @Transactional
    @Query("select * from user where nickname = :nickname", nativeQuery = true)
    fun findByNickname(@Param("nickname") nickname: String): User?

    @Transactional
    @Modifying
    @Query("update user set profile_path = :profilePath, updated_at = :updatedAt where id = :id", nativeQuery = true)
    fun updateProfile(@Param("id") id: Int, @Param("profilePath") profilePath: String, @Param("updatedAt") updatedAt: LocalDateTime): Int?

    @Transactional
    @Modifying
    @Query("update user set deleted_at = :deletedAt where id = :id", nativeQuery = true)
    fun deleteUser(@Param("id") id: Int, @Param("deletedAt") deletedAt: LocalDateTime): Int?
}
