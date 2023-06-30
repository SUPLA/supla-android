package org.supla.android.profile

class NoSuchProfileException(val profileId: Long) :
  Exception("There is no profile with the identifier $profileId")
