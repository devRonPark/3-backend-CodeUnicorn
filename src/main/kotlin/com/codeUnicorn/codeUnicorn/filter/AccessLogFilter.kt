package com.codeUnicorn.codeUnicorn.filter

import mu.KotlinLogging
import org.springframework.stereotype.Component
import org.springframework.web.util.ContentCachingRequestWrapper
import org.springframework.web.util.ContentCachingResponseWrapper
import java.util.UUID
import javax.servlet.Filter
import javax.servlet.FilterChain
import javax.servlet.ServletRequest
import javax.servlet.ServletResponse
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse

private val log = KotlinLogging.logger {}

@Component
class AccessLogFilter : Filter {
    override fun doFilter(request: ServletRequest?, response: ServletResponse?, chain: FilterChain?) {
        val httpServletRequest: ContentCachingRequestWrapper =
            ContentCachingRequestWrapper(request as HttpServletRequest)
        val httpServletResponse: ContentCachingResponseWrapper =
            ContentCachingResponseWrapper(response as HttpServletResponse)

        // 전처리
        chain?.doFilter(httpServletRequest, httpServletResponse)
        // doFilter가 실행되면서 실내 내부 Spring 안으로 들어가서야
        // writeToCache 메서드가 실행되서 request의 내용이 content에 담기게 되면서 읽을 수 있게 된다.
        // 그렇기에 log는 doFilter 이후에 처리해준다.

        val traceId: String = UUID.randomUUID().toString()

        // 후처리
        // request
        val accessLog: String = buildRequestLog(traceId, httpServletRequest)
        // HTTP Request Log 남기기
        log.info(accessLog)

        // response
        val resContent: String = String(httpServletResponse.contentAsByteArray)
        val httpStatus: Int = httpServletResponse.status

        httpServletResponse.copyBodyToResponse()

        log.info { "[$traceId] {\"status\": $httpStatus, \"body\": $resContent" }
    }

    private fun buildRequestLog(traceId: String, httpServletRequest: ContentCachingRequestWrapper): String {
        val requestURL: String = getRequestURL(httpServletRequest)
        val remoteAddr: String = getRemoteAddr(httpServletRequest)
        val method: String = getMethod(httpServletRequest)
        val queryString: String? = getQueryString(httpServletRequest)
        val requestBody: String = String(httpServletRequest.contentAsByteArray)
        val sb = StringBuilder()
        sb.append("[").append(traceId).append("] ")
        sb.append("{")
        sb
            .append("\"").append("requestURL").append("\"")
            .append(":")
            .append("\"").append(requestURL).append("\"")
        sb
            .append(",")
            .append("\"").append("remoteAddr").append("\"")
            .append(":")
            .append("\"").append(remoteAddr).append("\"")
        sb
            .append(",")
            .append("\"").append("method").append("\"")
            .append(":")
            .append("\"").append(method).append("\"")
        if (queryString != null) {
            sb
                .append(",")
                .append("\"").append("queryString").append("\"")
                .append(":")
                .append("\"").append(queryString).append("\"")
        }
        if (requestBody.isNotEmpty()) {
            sb
                .append(",")
                .append("\"").append("body").append("\"")
                .append(":")
                .append("\"").append(requestBody).append("\"")
        }
        sb.append("}")
        return sb.toString()
    }

    private fun getQueryString(httpServletRequest: ContentCachingRequestWrapper): String? {
        var queryString: String? = null
        if (httpServletRequest.queryString != null) {
            queryString = httpServletRequest.queryString
        }
        return queryString
    }

    private fun getMethod(httpServletRequest: ContentCachingRequestWrapper): String {
        return httpServletRequest.method
    }

    private fun getRemoteAddr(httpServletRequest: ContentCachingRequestWrapper): String {
        return httpServletRequest.remoteAddr
    }

    private fun getRequestURL(httpServletRequest: ContentCachingRequestWrapper): String {
        return httpServletRequest.requestURL.toString()
    }
}
