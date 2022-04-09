package lv.alexn.wolt.configuration

import lv.alexn.wolt.extension.LogCaptureExtension
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.TestConstructor

@Target(AnnotationTarget.CLASS)
@Retention(AnnotationRetention.RUNTIME)
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles(TestProfile.TEST)
@ExtendWith(value = [LogCaptureExtension::class])
@TestConstructor(autowireMode = TestConstructor.AutowireMode.ALL)
annotation class IntegrationTest
