package lv.alexn.wolt.extension

import org.junit.jupiter.api.extension.ExtensionContext
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.ArgumentsProvider
import kotlin.streams.asStream

open class KArgumentsProvider<T>(private vararg val args: T) : ArgumentsProvider {
    override fun provideArguments(context: ExtensionContext) =
        args.asSequence().map { Arguments.of(it) }.asStream()
}
