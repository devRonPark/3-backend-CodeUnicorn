package com.codeUnicorn.codeUnicorn.service

import com.codeUnicorn.codeUnicorn.constant.ExceptionMessage
import com.codeUnicorn.codeUnicorn.domain.lecture.LectureDetailInfo
import com.codeUnicorn.codeUnicorn.domain.lecture.LectureRepository
import com.codeUnicorn.codeUnicorn.exception.LectureNotExistException
import com.codeUnicorn.codeUnicorn.exception.MySQLException
import java.io.IOException
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service

@Service
class LectureService {
    @Autowired
    private lateinit var lectureRepository: LectureRepository

    @Throws(LectureNotExistException::class)
    fun getLectureList(): MutableList<LectureDetailInfo?> {
        val lectureList: MutableList<LectureDetailInfo?>
        try {
            // 전체 강의 목록 조회 쿼리 요청
            lectureList = lectureRepository.findAll()
        } catch (e: IOException) {
            throw MySQLException(ExceptionMessage.INTERNAL_SERVER_ERROR)
        }

        // 전체 강의 목록 조회 결과 아무 데이터도 존재하지 않는 경우
        if (lectureList.size == 0) {
            throw LectureNotExistException(ExceptionMessage.RESOURCE_NOT_EXIST)
        }
        return lectureList
    }
}
