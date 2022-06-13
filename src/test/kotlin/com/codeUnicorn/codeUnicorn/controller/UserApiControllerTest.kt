package com.codeUnicorn.codeUnicorn.controller

import com.codeUnicorn.codeUnicorn.dto.RequestUserDto
import com.codeUnicorn.codeUnicorn.service.S3FileUploadService
import com.codeUnicorn.codeUnicorn.service.UserService
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.boot.test.mock.mockito.MockBean
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders
import org.springframework.test.web.servlet.result.MockMvcResultHandlers
import org.springframework.test.web.servlet.result.MockMvcResultMatchers

@WebMvcTest(UserApiController::class)
class UserApiControllerTest {
    @Autowired
    private lateinit var mockMvc: MockMvc

    @MockBean
    private lateinit var userService: UserService

    @MockBean
    private lateinit var s3FileUploadService: S3FileUploadService

    @DisplayName("만약 이메일이 \"\" 으로 들어온다면")
    @Test
    fun loginFailTest1() {
        // 실패 케이스 1 : 이메일이 "" 으로 들어온 경우
        // given
        val userRequest = RequestUserDto("", "론이다님")

        val json = jacksonObjectMapper().writeValueAsString(userRequest)
        println(json)

        // when
        val performLogin = mockMvc.perform(
            MockMvcRequestBuilders.post("/users/login")
                .content(json)
                .contentType("application/json")
                .accept("application/json")
        )

        // then
        performLogin.andExpect(MockMvcResultMatchers.status().isBadRequest)
            .andExpect(MockMvcResultMatchers.jsonPath("\$.status").value("400"))
            .andExpect(MockMvcResultMatchers.jsonPath("\$.method").value("POST"))
            .andExpect(MockMvcResultMatchers.jsonPath("\$.path").value("/users/login"))
            .andDo(MockMvcResultHandlers.print())
    }

    @DisplayName("만약 닉네임 값이 60자 이상이라면")
    @Test
    fun loginFailTest2() {
        // 실패 케이스 2 : 닉네임 값이 60자 이상인 경우
        // given
        val userRequest =
            RequestUserDto("gildong@naver.com", "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa")

        val json = jacksonObjectMapper().writeValueAsString(userRequest)
        println(json)

        // when
        val performLogin = mockMvc.perform(
            MockMvcRequestBuilders.post("/users/login")
                .content(json)
                .contentType("application/json")
                .accept("application/json")
        )

        // then
        performLogin
            .andExpect(MockMvcResultMatchers.status().isBadRequest)
            .andExpect(MockMvcResultMatchers.jsonPath("\$.status").value("400"))
            .andExpect(MockMvcResultMatchers.jsonPath("\$.method").value("POST"))
            .andExpect(MockMvcResultMatchers.jsonPath("\$.message").value("닉네임은 1 ~ 60자 이어야 합니다."))
            .andExpect(MockMvcResultMatchers.jsonPath("\$.path").value("/users/login"))
            .andDo(MockMvcResultHandlers.print())
    }

    @DisplayName("만약 이메일 값이 naver 혹은 gmail이 아니라면")
    @Test
    fun loginFailTest3() {
        // 실패 케이스 2 : 이메일 값이 naver 혹은 gmail 이 아닌 경우
        // given
        val userRequest =
            RequestUserDto("gildong@gildong.com", "홍길동")

        val json = jacksonObjectMapper().writeValueAsString(userRequest)
        println(json)

        // when
        val performLogin = mockMvc.perform(
            MockMvcRequestBuilders.post("/users/login")
                .content(json)
                .contentType("application/json")
                .accept("application/json")
        )
        // then
        performLogin
            .andExpect(MockMvcResultMatchers.status().isBadRequest)
            .andExpect(MockMvcResultMatchers.jsonPath("\$.status").value("400"))
            .andExpect(MockMvcResultMatchers.jsonPath("\$.method").value("POST"))
            .andExpect(
                MockMvcResultMatchers.jsonPath("\$.message").value("이메일은 반드시 @gmail.com 혹은 @naver.com 를 포함해야 합니다.")
            )
            .andExpect(MockMvcResultMatchers.jsonPath("\$.path").value("/users/login"))
            .andDo(MockMvcResultHandlers.print())
    }

    @DisplayName("만약 이메일 값이 형식에 어긋난다면")
    @Test
    fun loginFailTest4() {
        // 실패 케이스 4 : 이메일 값이 형식에 어긋나는 경우
        // given
        val userRequest =
            RequestUserDto("gildong", "홍길동")

        val json = jacksonObjectMapper().writeValueAsString(userRequest)
        println(json)

        // when
        val performLogin =
            mockMvc.perform(
                MockMvcRequestBuilders.post("/users/login")
                    .content(json)
                    .contentType("application/json")
                    .accept("application/json")
            )
        // then
        performLogin
            .andExpect(MockMvcResultMatchers.status().isBadRequest)
            .andExpect(MockMvcResultMatchers.jsonPath("\$.status").value("400"))
            .andExpect(MockMvcResultMatchers.jsonPath("\$.method").value("POST"))
            .andExpect(
                MockMvcResultMatchers.jsonPath("\$.message").value("이메일은 반드시 @gmail.com 혹은 @naver.com 를 포함해야 합니다.")
            )
            .andExpect(MockMvcResultMatchers.jsonPath("\$.path").value("/users/login"))
            .andDo(MockMvcResultHandlers.print())
    }
}
