package com.codeUnicorn.codeUnicorn.domain.course

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository
import org.springframework.transaction.annotation.Transactional

@Repository
interface CourseInfoRepository : JpaRepository<CourseInfo, Int> {

    // 전체 코스 정보 조회(페이징)
    @Transactional
    @Query(value = "select * from course limit :paging, 9", nativeQuery = true)
    fun findByAllCourse(@Param("paging") paging: Int): List<CourseInfo?>

    // 특정 코스 정보 조회(페이징)
    @Transactional
    @Query(
        value = "select * from course where category = :category limit :paging, 9",
        nativeQuery = true
    )
    fun findByCourse(@Param("category") category: String, @Param("paging") page: Int): List<CourseInfo?>

    // 전체 코스 갯수
    @Transactional
    @Query(value = "select count(*) from course", nativeQuery = true)
    fun findByAllCourseCount(): Int

    // 특정 코스 갯수
    @Transactional
    @Query(value = "select count(*) from course where category = :category", nativeQuery = true)
    fun findByCourseCount(@Param("category") category: String): Int

    // 모든 코스 정보 조회
    @Transactional
    @Query(value = "select * from course", nativeQuery = true)
    fun findByAllCourseList(): List<CourseInfo>

    // 인기 순 코스 목록 조회
    @Transactional
    @Query(value = "select * from course order by like_count desc limit :paging, 9", nativeQuery = true)
    fun findSortedByPopularCourseList(@Param("paging") page: Int): List<CourseInfo?>

    @Transactional
    @Query(value = "select * from course order by like_count desc limit :paging, 9", nativeQuery = true)
    fun findSortedByPopularAllCourseList(@Param("paging") page: Int): List<CourseInfo?>

    @Transactional
    @Query(value = "select * from course where category = :category order by like_count desc limit :paging, 9", nativeQuery = true)
    fun findSortedByPopularCategorizedCourseList(@Param("category") category: String, @Param("paging") page: Int): List<CourseInfo?>

    // 최신 순 코스 목록 조회
    @Transactional
    @Query(value = "select * from course order by created_at desc limit :paging, 9", nativeQuery = true)
    fun findSortedByNewCourseList(@Param("paging") page: Int): List<CourseInfo?>

    @Transactional
    @Query(value = "select * from course order by created_at desc limit :paging, 9", nativeQuery = true)
    fun findSortedByNewAllCourseList(@Param("paging") page: Int): List<CourseInfo?>

    @Transactional
    @Query(value = "select * from course where category = :category order by created_at desc limit :paging, 9", nativeQuery = true)
    fun findSortedByNewCategorizedCourseList(@Param("category") category: String, @Param("paging") page: Int): List<CourseInfo?>
}
