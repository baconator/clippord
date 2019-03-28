package ml.ohca.bots

import me.ramswaroop.jbot.core.common.Controller
import me.ramswaroop.jbot.core.common.EventType
import me.ramswaroop.jbot.core.common.JBot
import me.ramswaroop.jbot.core.slack.Bot
import me.ramswaroop.jbot.core.slack.models.Event
import me.ramswaroop.jbot.core.slack.models.Message
import org.languagetool.JLanguageTool
import org.languagetool.language.BritishEnglish
import org.springframework.boot.SpringApplication
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Profile
import org.springframework.web.client.RestTemplate
import org.springframework.web.socket.WebSocketSession
import pl.allegro.finance.tradukisto.ValueConverters
import java.util.*


@JBot
@Profile("slack")
class ClippordTheBot : Bot() {
    companion object {
        @JvmStatic
        fun main(args: Array<String>) {
            SpringApplication.run(JBotApplication::class.java)
        }
    }

    override fun getSlackToken() = System.getenv("CLIPPORD_SLACK_TOKEN")
    private val langTool = JLanguageTool(BritishEnglish())
    override fun getSlackBot(): Bot = this

    @Controller(events = [EventType.MESSAGE])
    fun onReceiveMessage(session: WebSocketSession, event: Event) {
        val matches = langTool.check((event.text ?: "").map {
            if (it in setOf('*', '_', '>', '`', '~')) { // Filter out Slack formatting commands.
                ' '
            } else {
                it
            }
        }.joinToString(""))

        if (matches.isNotEmpty()) {
            val responseBuilder = StringBuilder()
            responseBuilder.append(passiveAggressiveIntro(matches.size) + "\n")
            for ((message, groupedMatches) in matches.groupBy { it.message }) {
                responseBuilder.append("*$message*\n")
                for (match in groupedMatches) {
                    val errorText = event.text.slice(match.fromPos until match.toPos)
                    responseBuilder.append("\"$errorText\" => ${match.suggestedReplacements}\n")
                }
                responseBuilder.append("\n")
            }
            responseBuilder.append(passiveAggressiveOutro(matches.size) + "\n")

            val response = Message(responseBuilder.toString())
            response.threadTs = event.threadTs ?: event.ts // Responds in a thread, continuing the same one if possible.
            reply(session, event, response)
        }
    }

    private fun passiveAggressiveOutro(errorCount: Int) = ""

    private val converter
        get() = ValueConverters.ENGLISH_INTEGER

    private fun passiveAggressiveIntro(errorCount: Int) = (choose {
        "Ruh-roh, there might be a typ-oh!"
    } or {
        "Something doesn't look quite right! Let me help point out some typos :)"
    } or {
        val message = if (errorCount > 1) {
            " or ${converter.asWords(errorCount)}"
        } else {
            ""
        }
        "Don't worry, we all make one or two$message mistakes sometimes."
    } or {
        val message = if (errorCount > 1) {
            "${converter.asWords(errorCount)} typos"
        } else {
            "a typo"
        }
        "Whoops! Looks like you overlooked $message! Let me list them out for you."
    } or {
        val message = if (errorCount > 1) {
            "${converter.asWords(errorCount)} typos"
        } else {
            "typo"
        }
        "What's important is that you tried, not the $message."
    } or {
        "These typos are displayed in real-time on a leaderboard on the 3rd floor! You're #${rand.nextInt(40)}!"
    } or {
        """Writing tip #${rand.nextInt(15) + 20}: I before e, except after c
Or when sounded as 'a' as in 'neighbor' and 'weigh'
Unless the 'c' is part of a 'sh' sound as in 'glacier'
Or it appears in comparatives and superlatives like 'fancier'
And also except when the vowels are sounded as 'e' as in 'seize'
Or 'i' as in 'height'
Or also in '-ing' inflections ending in '-e' as in 'cueing'
Or in compound words as in 'albeit'
Or occasionally in technical words with strong etymological links to their parent languages as in 'cuneiform'
Or in other numerous and random exceptions such as 'science', 'forfeit', and 'weird'.
(source: https://www.merriam-webster.com/words-at-play/i-before-e-except-after-c)"""
    } or {
        "locs_frontend_eng_typoDescriptionMessage"
    } or {
        "TYYYPOOOOOOOOSSSSSSSS (╯°□°）╯︵ ┻━┻"
    } or {
        "Motivational quote #${rand.nextInt(5) + 3}\"The pen is mightier than the sword.\" -Albert Einstein"
    }).getValue()

}

data class OrWrapper<T>(private val random: () -> Random, private val funcs: List<() -> T>) {
    infix fun or(function: () -> T): OrWrapper<T> = this.copy(random = random, funcs = funcs + listOf(function))

    fun getValue(): T = funcs[random().nextInt(funcs.size)]()
}

val rand = Random()
fun <T> choose(function: () -> T): OrWrapper<T> {
    return OrWrapper({ rand }, listOf(function))
}

@SpringBootApplication(scanBasePackages = ["me.ramswaroop.jbot", "ml.ohca.bots"])
open class JBotApplication {
    @Bean
    open fun restTemplate(): RestTemplate = RestTemplate()
}