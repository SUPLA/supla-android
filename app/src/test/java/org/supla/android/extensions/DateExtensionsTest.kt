package org.supla.android.extensions

import org.assertj.core.api.Assertions.assertThat
import org.junit.Test
import java.util.Calendar

class DateExtensionsTest {

  @Test
  fun `check shift days during spring time change`() {
    val date = date(2025, Calendar.MARCH, 31)
    assertThat(date.shift(-7)).isEqualTo(date(2025, Calendar.MARCH, 24))
  }

  @Test
  fun `check shift days during autumn time change`() {
    val date = date(2024, Calendar.OCTOBER, 28)
    assertThat(date.shift(-7)).isEqualTo(date(2024, Calendar.OCTOBER, 21))
  }
}
