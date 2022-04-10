package lv.alexn.wolt.extension

import org.junit.jupiter.api.Named
import org.junit.jupiter.api.extension.ExtensionContext
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.ArgumentsProvider
import kotlin.streams.asStream

interface NamedTestCase {

    val name: String?
}

open class KArgumentsProvider<T>(private vararg val args: T) : ArgumentsProvider {

    override fun provideArguments(context: ExtensionContext) =
        args.asSequence().map { testCase ->
            if (testCase is NamedTestCase && testCase.name != null) {
                Arguments.of(Named.of(testCase.name, testCase))
            } else {
                Arguments.of(testCase)
            }
        }.asStream()
}
