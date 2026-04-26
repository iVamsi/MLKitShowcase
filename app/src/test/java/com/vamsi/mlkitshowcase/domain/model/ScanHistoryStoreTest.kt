package com.vamsi.mlkitshowcase.domain.model

import com.google.common.truth.Truth.assertThat
import org.junit.Test

class ScanHistoryStoreTest {

    @Test
    fun `keeps newest five scan results in memory`() {
        val store = ScanHistoryStore(capacity = 5)

        repeat(6) { index ->
            store.add(
                ScanHistoryEntry(
                    mode = ScannerMode.BARCODE,
                    title = "Result $index",
                    subtitle = "QR_CODE"
                )
            )
        }

        val items = store.items.value
        assertThat(items).hasSize(5)
        assertThat(items.map { it.title }).containsExactly(
            "Result 5",
            "Result 4",
            "Result 3",
            "Result 2",
            "Result 1"
        ).inOrder()
    }
}
