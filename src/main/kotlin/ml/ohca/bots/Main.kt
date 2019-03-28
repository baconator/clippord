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


@JBot
@Profile("slack")
class ClippordTheBot : Bot() {
    companion object {
        @JvmStatic
        fun main(args: Array<String>) {
            SpringApplication.run(JBotApplication::class.java)
        }
    }

    override fun getSlackToken() = System.getenv("CLIPPORD_SLACK_TOKEN")!!
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
            responseBuilder.append(passiveAggressiveIntro(matches.size, converter) + "\n")
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

}

@SpringBootApplication(scanBasePackages = ["me.ramswaroop.jbot", "ml.ohca.bots"])
open class JBotApplication {
    @Bean
    open fun restTemplate(): RestTemplate = RestTemplate()
}