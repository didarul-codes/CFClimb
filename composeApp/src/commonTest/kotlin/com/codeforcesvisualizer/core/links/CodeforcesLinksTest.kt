package com.codeforcesvisualizer.core.links

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue

class CodeforcesLinksTest {

    @Test
    fun profileLinks() {
        val tourist = CodeforcesLink.Profile("tourist")
        assertEquals(tourist, parseCodeforcesLink("https://codeforces.com/profile/tourist"))
        assertEquals(tourist, parseCodeforcesLink("http://www.codeforces.com/profile/tourist/"))
        assertEquals(tourist, parseCodeforcesLink("https://m2.codeforces.com/profile/tourist?locale=ru#top"))
        assertEquals(tourist, parseCodeforcesLink("https://codeforces.com/submissions/tourist"))
        assertEquals(tourist, parseCodeforcesLink("https://codeforces.com/contests/with/tourist"))
        assertEquals(CodeforcesLink.Profile("Um_nik"), parseCodeforcesLink("cfclimb://profile/Um_nik"))
    }

    @Test
    fun contestLinks() {
        val round = CodeforcesLink.Contest(2266)
        assertEquals(round, parseCodeforcesLink("https://codeforces.com/contest/2266"))
        assertEquals(round, parseCodeforcesLink("https://codeforces.com/contest/2266/problem/A"))
        assertEquals(round, parseCodeforcesLink("https://m1.codeforces.com/contests/2266"))
        assertEquals(round, parseCodeforcesLink("CFCLIMB://contest/2266"))
    }

    @Test
    fun findsTheLinkInsideSharedText() {
        assertEquals(
            CodeforcesLink.Profile("jiangly"),
            parseCodeforcesLink("Look at this: https://codeforces.com/profile/jiangly!")
        )
        assertEquals(
            CodeforcesLink.Contest(2266),
            parseCodeforcesLink("Codeforces Round (Div. 3)\n(https://codeforces.com/contest/2266).")
        )
    }

    @Test
    fun ignoresPagesTheAppCannotShow() {
        assertNull(parseCodeforcesLink("https://notcodeforces.com/profile/tourist"))
        assertNull(parseCodeforcesLink("https://codeforces.com.evil.example/profile/tourist"))
        assertNull(parseCodeforcesLink("https://codeforces.com/gym/100001"))
        assertNull(parseCodeforcesLink("https://codeforces.com/blog/entry/1"))
        assertNull(parseCodeforcesLink("https://codeforces.com/profile/"))
        assertNull(parseCodeforcesLink("https://codeforces.com/contest/abc"))
        assertNull(parseCodeforcesLink("https://codeforces.com/profile/<script>"))
        assertNull(parseCodeforcesLink("tourist"))
    }

    @Test
    fun deepLinksHoldTheLinkUntilItIsShown() {
        val deepLinks = DeepLinks()

        assertFalse(deepLinks.open("https://codeforces.com/blog/entry/1"))
        assertNull(deepLinks.pending.value)

        assertTrue(deepLinks.open("https://codeforces.com/contest/2266"))
        assertTrue(deepLinks.open("https://codeforces.com/profile/tourist"))
        // Consuming the older link doesn't drop the newer one.
        deepLinks.consume(CodeforcesLink.Contest(2266))
        assertEquals(CodeforcesLink.Profile("tourist"), deepLinks.pending.value)

        deepLinks.consume(CodeforcesLink.Profile("tourist"))
        assertNull(deepLinks.pending.value)
    }
}
