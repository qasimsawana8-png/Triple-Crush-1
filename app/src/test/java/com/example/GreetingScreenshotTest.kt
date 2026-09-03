package com.example

import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onRoot
import com.example.data.model.Tile
import com.example.data.model.TileCategory
import com.example.data.model.TileType
import com.example.ui.components.TileView
import com.example.ui.theme.MyApplicationTheme
import com.github.takahirom.roborazzi.RobolectricDeviceQualifiers
import com.github.takahirom.roborazzi.captureRoboImage
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode

@RunWith(RobolectricTestRunner::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(qualifiers = RobolectricDeviceQualifiers.Pixel8, sdk = [36])
class GreetingScreenshotTest {

  @get:Rule val composeTestRule = createComposeRule()

  @Test
  fun greeting_screenshot() {
    val sampleTile = Tile(
      id = "test_1",
      typeId = "f1",
      type = TileType("f1", "Strawberry", "🍓", TileCategory.FRUIT, 0xFFFF1744, 0xFFFFEBEE),
      layer = 0,
      col = 0f,
      row = 0f,
      isSelectable = true
    )
    composeTestRule.setContent {
      MyApplicationTheme {
        TileView(tile = sampleTile)
      }
    }

    composeTestRule.onRoot().captureRoboImage(filePath = "src/test/screenshots/greeting.png")
  }
}

