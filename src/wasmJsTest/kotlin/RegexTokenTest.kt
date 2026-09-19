import com.github.h0tk3y.betterParse.combinators.leftAssociative
import com.github.h0tk3y.betterParse.combinators.use
import com.github.h0tk3y.betterParse.grammar.Grammar
import com.github.h0tk3y.betterParse.grammar.parseToEnd
import com.github.h0tk3y.betterParse.lexer.DefaultTokenizer
import com.github.h0tk3y.betterParse.lexer.literalToken
import com.github.h0tk3y.betterParse.lexer.regexToken
import com.github.h0tk3y.betterParse.parser.Parser
import kotlin.test.Test
import kotlin.test.assertEquals

/**
 * The wasmJs [com.github.h0tk3y.betterParse.lexer.RegexToken] cannot reuse the sticky-flag trick of
 * the JS target, which reaches into the `Regex` internals through `asDynamic`. It anchors patterns
 * with `\A` against a relative view of the input instead, so the anchoring and the offsetting are
 * both platform-specific and pinned down here.
 */
class RegexTokenTest {
    private val word = regexToken("\\w+")

    @Test
    fun matchesAtTheStartOfTheInput() {
        assertEquals(3, word.match("foo bar", 0))
    }

    @Test
    fun matchesAtAnOffset() {
        assertEquals(3, word.match("foo bar", 4))
    }

    /** The anchoring contract: a pattern that only matches further along must not be found. */
    @Test
    fun doesNotMatchAheadOfTheGivenIndex() {
        assertEquals(0, word.match("   foo", 0))
        assertEquals(0, regexToken("bar").match("foo bar", 0))
    }

    /** The relative view reports the remaining length; a greedy pattern must stop at the input end. */
    @Test
    fun doesNotMatchPastTheEndOfTheInput() {
        assertEquals(3, word.match("foo", 0))
        assertEquals(0, word.match("foo", 3))
    }

    @Test
    fun matchesRegexBuiltTokens() {
        assertEquals(3, regexToken(Regex("[a-z]+")).match("foo bar", 0))
        assertEquals(0, regexToken(Regex("[a-z]+")).match("FOO", 0))
    }

    /** A pattern already anchored with `\A` must not be anchored twice. */
    @Test
    fun matchesExplicitlyAnchoredPatterns() {
        assertEquals(3, regexToken("\\Afoo").match("foo bar", 0))
    }

    @Test
    fun tokenizesFromEveryOffset() {
        val ws = regexToken("\\s+", ignore = true)
        val tokenizer = DefaultTokenizer(listOf(word, ws))

        val matches = tokenizer.tokenize("foo bar baz").toList()

        assertEquals(listOf("foo", "bar", "baz"), matches.filter { it.type == word }.map { it.text })
        assertEquals(listOf(0, 4, 8), matches.filter { it.type == word }.map { it.offset })
    }

    /** End-to-end: the tokenizer feeds a real grammar on Wasm. */
    @Test
    fun parsesAGrammar() {
        val grammar = object : Grammar<Int>() {
            val num by regexToken("\\d+")
            val plus by literalToken("+")
            val ws by regexToken("\\s+", ignore = true)

            override val rootParser: Parser<Int> by
                leftAssociative(num use { text.toInt() }, plus) { l, _, r -> l + r }
        }

        assertEquals(6, grammar.parseToEnd("1 + 2 + 3"))
    }
}
