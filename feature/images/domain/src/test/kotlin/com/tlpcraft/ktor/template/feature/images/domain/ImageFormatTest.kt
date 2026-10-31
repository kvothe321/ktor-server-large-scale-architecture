package com.tlpcraft.ktor.template.feature.images.domain

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class ImageFormatTest {
    @Test
    fun `detects every supported format by its signature`() {
        assertEquals(ImageFormat.PNG, ImageFormat.detect(TestImages.png))
        assertEquals(ImageFormat.JPEG, ImageFormat.detect(TestImages.jpeg))
        assertEquals(ImageFormat.GIF, ImageFormat.detect(TestImages.gif))
        assertEquals(ImageFormat.WEBP, ImageFormat.detect(TestImages.webp))
    }

    @Test
    fun `rejects unknown and truncated content`() {
        assertNull(ImageFormat.detect(TestImages.text))
        assertNull(ImageFormat.detect(TestImages.png.copyOf(4)))
        assertNull(ImageFormat.detect("RIFF0000WAVE".encodeToByteArray()))
        assertNull(ImageFormat.detect(ByteArray(0)))
    }
}
