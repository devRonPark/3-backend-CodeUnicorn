package com.codeUnicorn.codeUnicorn.api


import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RestController

@RestController
class IndexController {

    @GetMapping(path = ["/"])

            fun index(): ResponseEntity<String> {
            val hello = "Hello World!"
            return ResponseEntity.ok(hello)
        }
}