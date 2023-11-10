package org.supla.android.data.source.remote

enum class ConfigResult(val value: Int) {
  RESULT_FALSE(0),
  RESULT_TRUE(1),
  DATA_ERROR(2),
  TYPE_NOT_SUPPORTED(3),
  FUNCTION_NOT_SUPPORTED(4),
  LOCAL_CONFIG_DISABLED(5),
  NOT_ALLOWED(6),
  DEVICE_NOT_FOUND(7)
}
