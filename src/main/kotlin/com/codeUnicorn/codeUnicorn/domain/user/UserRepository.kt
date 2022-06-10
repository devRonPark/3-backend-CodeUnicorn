package com.codeUnicorn.codeUnicorn.domain.user

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Modifying
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import org.springframework.transaction.annotation.Transactional

// DB Layer 접근자
// JpaRepository 는 기본적으로 CRUD 쿼리 함수 제공
interface UserRepository : JpaRepository<User, Int> {
    fun findByEmail(email: String): User?

    @Modifying
    @Transactional
    @Query("update User u set u.nickname = :nickname where u.id = :id")
    fun updateNickname(@Param("id") id: Int, @Param("nickname") nickname: String): Int?

    @Transactional
    @Query("select u from User u where u.nickname = :nickname")
    fun findByNickname(@Param("nickname") nickname: String): User?

    @Transactional
    @Query("update User u set u.profile_path = :profile_path where u.id = :id")
    fun updateProfile(@Param("id") id: Int, @Param("profile_path") profilePath: String): Int?
}
