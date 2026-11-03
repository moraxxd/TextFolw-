package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.data.languages.SourceLanguage
import com.example.data.languages.TargetLanguage
import com.example.data.translation.HinglishPreprocessor
import com.example.data.translation.tribal.TribalTranslationEngine
import com.example.service.ime.TextFlowInputMethodService
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
    assertEquals("TextFlow", appName)
  }

  @Test
  fun `test hinglish preprocessing`() {
    val input = "namaste aap kaise ho"
    val output = HinglishPreprocessor.normalizeHinglish(input)
    assertTrue(HinglishPreprocessor.containsDevanagari(output))
  }

  @Test
  fun `test gondi to english and marathi translation`() {
    val engine = TribalTranslationEngine()

    // Test Gondi greeting
    val resultEnglish = engine.translate("सेवाल", SourceLanguage.GONDI, TargetLanguage.ENGLISH)
    assertTrue(resultEnglish.isSuccess)
    assertTrue(resultEnglish.getOrNull()?.contains("Greetings") == true || resultEnglish.getOrNull()?.contains("Hello") == true)

    val resultMarathi = engine.translate("सेवाल", SourceLanguage.GONDI, TargetLanguage.MARATHI)
    assertTrue(resultMarathi.isSuccess)
    assertTrue(resultMarathi.getOrNull()?.contains("नमस्कार") == true)
  }

  @Test
  fun `test kolami to marathi translation`() {
    val engine = TribalTranslationEngine()

    val resultMarathi = engine.translate("राम-राम", SourceLanguage.KOLAMI, TargetLanguage.MARATHI)
    assertTrue(resultMarathi.isSuccess)
    assertTrue(resultMarathi.getOrNull()?.contains("राम-राम") == true || resultMarathi.getOrNull()?.contains("नमस्कार") == true)
  }

  @Test
  fun `test pending text insertion buffer`() {
    TextFlowInputMethodService.pendingInsertText.value = "Test tribal text"
    assertEquals("Test tribal text", TextFlowInputMethodService.pendingInsertText.value)
  }
}
