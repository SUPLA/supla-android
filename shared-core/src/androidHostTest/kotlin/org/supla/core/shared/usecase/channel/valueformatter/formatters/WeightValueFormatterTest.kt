package org.supla.core.shared.usecase.channel.valueformatter.formatters

import org.assertj.core.api.Assertions
import org.junit.Test
import org.supla.core.shared.usecase.channel.valueformatter.types.ValueFormat

class WeightValueFormatterTest {

  @Test
  fun `should format value in grams`() {
    // given
    val value = 250

    // when
    val text = WeightValueFormatter.format(value, ValueFormat.WithUnit)

    // then
    Assertions.assertThat(text).isEqualTo("250 g")
  }

  @Test
  fun `should format value in kilograms`() {
    // given
    val value = 12500

    // when
    val text = WeightValueFormatter.format(value, ValueFormat.WithUnit)

    // then
    Assertions.assertThat(text).isEqualTo("12.50 kg")
  }
}
