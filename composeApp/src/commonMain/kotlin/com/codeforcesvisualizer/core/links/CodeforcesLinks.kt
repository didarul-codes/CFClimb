package com.codeforcesvisualizer.core.links

import androidx.compose.runtime.Composable
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

/** A Codeforces page the app can show itself. */
sealed interface CodeforcesLink {
    data class Profile(val handle: String) : CodeforcesLink
    data class Contest(val contestId: Int) : CodeforcesLink
}

private val UrlPattern = Regex("""(?i)\b(?:https?://|cfclimb://)\S+""")
private val HandlePattern = Regex("""[A-Za-z0-9_.\-]{2,24}""")
private const val TRAILING_PUNCTUATION = ".,;:!?)]}>\"'"

/**
 * Finds the first Codeforces profile or contest link in [text], which can be a bare URL or a
 * shared message around one. Accepts codeforces.com with its www and m1–m3 mirrors, and the
 * app's own `cfclimb://profile/{handle}` and `cfclimb://contest/{id}` links.
 */
fun parseCodeforcesLink(text: String): CodeforcesLink? {
    val url = UrlPattern.find(text)?.value?.trimEnd { it in TRAILING_PUNCTUATION } ?: return null
    val scheme = url.substringBefore("://").lowercase()
    val rest = url.substringAfter("://").substringBefore('#').substringBefore('?')
    val parts = rest.split('/').filter { it.isNotEmpty() }
    if (parts.isEmpty()) return null

    val path = if (scheme == "cfclimb") {
        parts
    } else {
        val host = parts.first().lowercase().substringBefore(':')
        if (host != "codeforces.com" && !host.endsWith(".codeforces.com")) return null
        parts.drop(1)
    }
    return pathToLink(path)
}

private fun pathToLink(path: List<String>): CodeforcesLink? {
    val first = path.getOrNull(0)?.lowercase() ?: return null
    val second = path.getOrNull(1) ?: return null
    return when (first) {
        "profile", "submissions" -> handleLink(second)
        "contest", "contests" -> when {
            second.equals("with", ignoreCase = true) -> path.getOrNull(2)?.let(::handleLink)
            else -> second.toIntOrNull()?.takeIf { it > 0 }?.let { CodeforcesLink.Contest(it) }
        }
        else -> null
    }
}

private fun handleLink(handle: String): CodeforcesLink? =
    if (HandlePattern.matches(handle)) CodeforcesLink.Profile(handle) else null

/**
 * Holds a link opened from outside the app until the UI has navigated to it, so a link that
 * arrives before the screen exists isn't lost.
 */
class DeepLinks {
    private val _pending = MutableStateFlow<CodeforcesLink?>(null)
    val pending: StateFlow<CodeforcesLink?> = _pending

    /** Returns false when [text] holds no link the app can show. */
    fun open(text: String): Boolean {
        val link = parseCodeforcesLink(text) ?: return false
        _pending.value = link
        return true
    }

    /** Clears [link] once shown, unless a newer link has replaced it. */
    fun consume(link: CodeforcesLink) {
        _pending.compareAndSet(link, null)
    }
}

/** Whether Codeforces links on this device open in the app, and a way to change that. */
class LinkHandlingSetting(val enabled: Boolean, val open: () -> Unit)

/** Returns null where the app can't offer this setting. */
@Composable
expect fun rememberLinkHandlingSetting(): LinkHandlingSetting?
