package ml.ohca.bots

import pl.allegro.finance.tradukisto.ValueConverters

fun passiveAggressiveIntro(errorCount: Int, converter: ValueConverters) = (choose {
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