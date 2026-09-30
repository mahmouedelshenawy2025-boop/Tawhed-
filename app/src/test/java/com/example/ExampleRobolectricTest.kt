package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.widget.TawheedWidgetProvider
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class ExampleRobolectricTest {

  @Test
  fun `read string from context`() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val appName = context.getString(R.string.app_name)
    assertEquals("لا إله إلا الله", appName)
  }

  @Test
  fun `verify phrases contain both shahadas`() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val phrase0 = TawheedWidgetProvider.getFormattedPhrase(context, 0)
    val phrase1 = TawheedWidgetProvider.getFormattedPhrase(context, 1)

    assertTrue(phrase0.contains("لا إله إلا الله"))
    assertTrue(phrase1.contains("محمد رسول الله"))
  }
}
