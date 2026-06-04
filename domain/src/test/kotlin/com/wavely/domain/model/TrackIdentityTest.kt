package com.wavely.domain.model

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class TrackIdentityTest {

    @Test
    fun `asKey then fromKey round-trips`() {
        val id = TrackIdentity(filePath = "/music/song.mp3", sizeBytes = 4096, dateAddedSec = 1700000000)
        val restored = TrackIdentity.fromKey(id.asKey())
        assertEquals(id, restored)
    }

    @Test
    fun `fromKey returns null on malformed key`() {
        assertNull(TrackIdentity.fromKey("not-a-valid-key"))
        assertNull(TrackIdentity.fromKey("/path|notanumber|123"))
    }

    @Test
    fun `identity ignores content uri and id - same file different id stays equal`() {
        // Two scans of the same file (path/size/date unchanged) produce equal identities,
        // even though MediaStore _ID / content URI changed (risk T-1).
        val a = TrackIdentity("/m/a.mp3", 100, 50)
        val b = TrackIdentity("/m/a.mp3", 100, 50)
        assertEquals(a, b)
        assertEquals(a.asKey(), b.asKey())
    }
}
