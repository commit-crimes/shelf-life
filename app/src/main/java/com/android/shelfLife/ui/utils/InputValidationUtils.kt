package com.android.shelfLife.ui.utils

/**
 * Validates the food name.
 *
 * @param foodName The food name to validate.
 * @return The error message if the food name is invalid, null otherwise.
 */
fun validateFoodName(foodName: String): String? {
  val namePattern = Regex("^[a-zA-Z0-9\\s\\-,'()]+\$")
  return when {
    foodName.isBlank() -> "Food name cannot be empty."
    !namePattern.matches(foodName) -> "Food name contains invalid characters."
    else -> null
  }
}

/**
 * Validates the amount.
 *
 * @param amount The amount to validate.
 * @return The error message if the amount is invalid, null otherwise.
 */
fun validateAmount(amount: String): String? {
  return when {
    amount.isBlank() -> "Amount cannot be empty."
    amount.toDoubleOrNull() == null -> "Amount must be a number."
    amount.toDouble() <= 0 -> "Amount must be positive."
    else -> null
  }
}

/**
 * Validates the buy date.
 *
 * @param buyDate The buy date to validate.
 * @return The error message if the buy date is invalid, null otherwise.
 */
fun validateBuyDate(buyDate: String): String? {
  return getDateErrorMessage(buyDate)
}

/**
 * Validates the expire date.
 *
 * @param expireDate The expire date to validate.
 * @param buyDate The buy date to compare with.
 * @param buyDateError The error message for the buy date.
 * @return The error message if the expire date is invalid, null otherwise.
 */
fun validateExpireDate(expireDate: String, buyDate: String, buyDateError: String?): String? {
  var error = getDateErrorMessage(expireDate)
  if (error == null && expireDate.length == 8 && buyDateError == null && buyDate.length == 8) {
    if (!isDateAfterOrEqual(expireDate, buyDate)) {
      error = "Expire Date cannot be before Buy Date"
    }
  }
  if (error == null && expireDate.length == 8) {
    if (!isValidDateNotPast(expireDate)) {
      error = "Expire Date cannot be in the past"
    }
  }
  return error
}

/**
 * Validates the open date.
 *
 * @param openDate The open date to validate.
 * @param buyDate The buy date to compare with.
 * @param buyDateError The error message for the buy date.
 * @param expireDate The expire date to compare with.
 * @param expireDateError The error message for the expire date.
 * @return The error message if the open date is invalid, null otherwise.
 */
fun validateOpenDate(
    openDate: String,
    buyDate: String,
    buyDateError: String?,
    expireDate: String,
    expireDateError: String?
): String? {
  var error = getDateErrorMessage(openDate, isRequired = false)
  if (error == null &&
      openDate.isNotEmpty() &&
      buyDateError == null &&
      openDate.length == 8 &&
      buyDate.length == 8) {
    if (!isDateAfterOrEqual(openDate, buyDate)) {
      error = "Open Date cannot be before Buy Date"
    }
  }
  if (error == null &&
      openDate.isNotEmpty() &&
      expireDateError == null &&
      openDate.length == 8 &&
      expireDate.length == 8) {
    if (!isDateAfterOrEqual(expireDate, openDate)) {
      error = "Open Date cannot be after Expire Date"
    }
  }
  return error
}
