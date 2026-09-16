package com.eeseka.lynk.shared.presentation.util

import assertk.assertThat
import assertk.assertions.isEqualTo
import kotlin.test.Test

class MailtoUriTest {

    @Test
    fun `address subject and body are joined into one link`() {
        val uri = buildMailtoUri(email = "help@example.com", subject = "Hello", body = "Hi")

        assertThat(uri).isEqualTo("mailto:help@example.com?subject=Hello&body=Hi")
    }

    @Test
    fun `spaces become percent 20 not plus`() {
        val uri = buildMailtoUri(email = "help@example.com", subject = "Need help", body = "")

        assertThat(uri).isEqualTo("mailto:help@example.com?subject=Need%20help&body=")
    }

    @Test
    fun `new lines are encoded`() {
        val uri = buildMailtoUri(email = "help@example.com", subject = "", body = "Line one\nLine two")

        assertThat(uri).isEqualTo("mailto:help@example.com?subject=&body=Line%20one%0ALine%20two")
    }

    @Test
    fun `characters that would break the link are encoded`() {
        val uri = buildMailtoUri(email = "help@example.com", subject = "a&b=c?d+e", body = "")

        assertThat(uri).isEqualTo("mailto:help@example.com?subject=a%26b%3Dc%3Fd%2Be&body=")
    }

    @Test
    fun `unreserved characters pass through`() {
        val uri = buildMailtoUri(email = "help@example.com", subject = "Az09-._~", body = "")

        assertThat(uri).isEqualTo("mailto:help@example.com?subject=Az09-._~&body=")
    }

    @Test
    fun `non ascii characters are encoded byte by byte`() {
        val uri = buildMailtoUri(email = "help@example.com", subject = "₦500", body = "")

        assertThat(uri).isEqualTo("mailto:help@example.com?subject=%E2%82%A6500&body=")
    }
}
