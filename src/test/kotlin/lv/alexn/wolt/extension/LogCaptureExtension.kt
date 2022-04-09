package lv.alexn.wolt.extension

import ch.qos.logback.classic.Logger
import ch.qos.logback.classic.PatternLayout
import ch.qos.logback.classic.pattern.EnsureExceptionHandling
import ch.qos.logback.classic.spi.ILoggingEvent
import ch.qos.logback.core.Context
import ch.qos.logback.core.UnsynchronizedAppenderBase
import org.assertj.core.api.AssertProvider
import org.assertj.core.api.Assertions
import org.assertj.core.api.ListAssert
import org.junit.jupiter.api.extension.AfterAllCallback
import org.junit.jupiter.api.extension.AfterEachCallback
import org.junit.jupiter.api.extension.BeforeAllCallback
import org.junit.jupiter.api.extension.BeforeEachCallback
import org.junit.jupiter.api.extension.ExtensionContext
import org.junit.jupiter.api.extension.ParameterContext
import org.junit.jupiter.api.extension.support.TypeBasedParameterResolver
import org.slf4j.LoggerFactory

typealias LogPredicate = (ILoggingEvent) -> Boolean

private val rootLogger: Logger = LoggerFactory.getLogger(org.slf4j.Logger.ROOT_LOGGER_NAME) as Logger
private val rootContext: Context = rootLogger.loggerContext

operator fun LogPredicate.not(): LogPredicate = { !this(it) }

object Pattern {
    const val SIMPLE = "%5level - %message"
}

class LogCaptureExtension :
    BeforeAllCallback, BeforeEachCallback,
    AfterEachCallback, AfterAllCallback,
    TypeBasedParameterResolver<Logs>() {

    override fun beforeAll(context: ExtensionContext) {
        context.attachAppender()
    }

    override fun beforeEach(context: ExtensionContext) {
        context.attachAppender()
    }

    override fun afterEach(context: ExtensionContext) {
        context.detachAppender()
    }

    override fun afterAll(context: ExtensionContext) {
        context.detachAppender()
    }

    override fun resolveParameter(
        parameterContext: ParameterContext,
        extensionContext: ExtensionContext
    ) = Logs(extensionContext.appender.events)

    private fun ExtensionContext.attachAppender() {
        rootLogger.addAppender(LoggingEventBufferAppender(uniqueId))
    }

    private fun ExtensionContext.detachAppender() {
        rootLogger.detachAppender(uniqueId)
    }

    private val ExtensionContext.appender: LoggingEventBufferAppender
        get() = rootLogger.getAppender(uniqueId) as LoggingEventBufferAppender
}

private class LoggingEventBufferAppender(name: String? = null) :
    UnsynchronizedAppenderBase<ILoggingEvent>() {

    val events = mutableListOf<ILoggingEvent>()

    init {
        context = rootContext
        this.name = name
        start()
    }

    override fun append(eventObject: ILoggingEvent) {
        events.add(eventObject)
    }
}

data class Logs(private val events: List<ILoggingEvent>) : AssertProvider<ListAssert<ILoggingEvent>> {

    override fun assertThat(): ListAssert<ILoggingEvent> = Assertions.assertThat(events)

    fun with(predicate: LogPredicate, pattern: String): List<String> =
        filter(predicate).withLayout(pattern)

    fun with(predicate: LogPredicate) = Logs(events.filter(predicate))

    private fun filter(predicate: LogPredicate) = Logs(events.filter(predicate))

    /**
     * @see [PatternLayout]
     * @see [EnsureExceptionHandling]
     */
    private fun withLayout(pattern: String): List<String> =
        PatternLayout().apply {
            /** get rid of [EnsureExceptionHandling] to avoid stack traces */
            setPostCompileProcessor(null)
            context = rootContext
            setPattern(pattern)
            start()
        }.run {
            events.map { doLayout(it) }
        }
}
