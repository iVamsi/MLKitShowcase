package com.vamsi.mlkitshowcase.presentation.document

import android.content.Intent
import android.content.Context
import android.net.Uri
import com.google.common.truth.Truth.assertThat
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment

@RunWith(RobolectricTestRunner::class)
class DocumentOpenTargetTest {

    @Test
    fun `pdf target creates read-granted view intent`() {
        val context = testContext()
        val uri = "content://scan/result.pdf"

        val intent = DocumentOpenTarget.Pdf(uri).toViewIntent(context)

        assertThat(intent.action).isEqualTo(Intent.ACTION_VIEW)
        assertThat(intent.data).isEqualTo(Uri.parse(uri))
        assertThat(intent.type).isEqualTo("application/pdf")
        assertThat(intent.clipData?.itemCount).isEqualTo(1)
        assertThat(intent.clipData?.getItemAt(0)?.uri).isEqualTo(intent.data)
        assertThat(intent.flags and Intent.FLAG_GRANT_READ_URI_PERMISSION).isNotEqualTo(0)
    }

    @Test
    fun `image target creates read-granted view intent`() {
        val context = testContext()
        val uri = "content://scan/page-1"

        val intent = DocumentOpenTarget.PageImage(uri).toViewIntent(context)

        assertThat(intent.action).isEqualTo(Intent.ACTION_VIEW)
        assertThat(intent.data).isEqualTo(Uri.parse(uri))
        assertThat(intent.type).isEqualTo("image/jpeg")
        assertThat(intent.clipData?.itemCount).isEqualTo(1)
        assertThat(intent.clipData?.getItemAt(0)?.uri).isEqualTo(intent.data)
        assertThat(intent.flags and Intent.FLAG_GRANT_READ_URI_PERMISSION).isNotEqualTo(0)
    }

    private fun testContext(): Context = RuntimeEnvironment.getApplication()
}
