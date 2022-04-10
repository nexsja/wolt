package lv.alexn.wolt.extension

import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.test.web.servlet.MockHttpServletRequestDsl
import org.springframework.test.web.servlet.MockMvcResultMatchersDsl

fun MockMvcResultMatchersDsl.status(status: HttpStatus) {
    status { isEqualTo(status.value()) }
}

fun MockMvcResultMatchersDsl.body(body: String) {
    content { string(body) }
}

fun MockHttpServletRequestDsl.json(json: String) {
    content = json
    contentType = MediaType.APPLICATION_JSON
}
