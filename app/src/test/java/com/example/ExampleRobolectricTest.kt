package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.data.model.BoardProfiles
import com.example.data.model.EspBoardType
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class ExampleRobolectricTest {

  @Test
  fun `read string from context`() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val appName = context.getString(R.string.app_name)
    assertEquals("ESP Manager", appName)
  }

  @Test
  fun `verify esp board profiles`() {
    val esp01Pins = BoardProfiles.getPinsForBoard(EspBoardType.ESP_01)
    assertEquals(4, esp01Pins.size)

    val esp12fPins = BoardProfiles.getPinsForBoard(EspBoardType.ESP_12F)
    assertTrue(esp12fPins.size >= 10)

    val esp32Pins = BoardProfiles.getPinsForBoard(EspBoardType.ESP32)
    assertTrue(esp32Pins.size >= 20)
  }
}
