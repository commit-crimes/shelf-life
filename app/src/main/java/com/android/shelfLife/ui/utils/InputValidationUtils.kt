package com.android.shelfLife.ui.utils

import com.android.shelfLife.R

/**
 * Validates the food name.
 *
 * @param foodName The food name to validate.
 * @return The resource ID of the error message if the food name is invalid, null otherwise.
 */
fun validateFoodName(foodName: String): Int? {
  val namePattern = Regex("^[a-zA-Z0-9\\s\\-,'()]+\$")
  return when {
    foodName.isBlank() -> R.string.food_name_empty_error
    !namePattern.matches(foodName) -> R.string.food_name_invalid_error
    else -> null
  }
}

/**
 * Validates the amount.
 *
 * @param amount The amount to validate.
 * @return The resource ID of the error message if the amount is invalid, null otherwise.
 */
fun validateAmount(amount: String): Int? {
  return when {
    amount.isBlank() -> R.string.amount_empty_error
    amount.toDoubleOrNull() == null -> R.string.amount_not_number_error
    amount.toDouble() <= 0 -> R.string.amount_negative_error
    else -> null
  }
}

/**
 * Validates the buy date.
 *
 * @param buyDate The buy date to validate.
 * @return The resource ID of the error message if the buy date is invalid, null otherwise.
 */
fun validateBuyDate(buyDate: String): Int? {
  return getDateErrorMessageResId(buyDate)
}

/**
 * Validates the expire date.
 *
 * @param expireDate The expire date to validate.
 * @param buyDate The buy date to compare with.
 * @param buyDateErrorResId The resource ID of the error message for the buy date.
 * @return The resource ID of the error message if the expire date is invalid, null otherwise.
 */
fun validateExpireDate(expireDate: String, buyDate: String, buyDateErrorResId: Int?): Int? {
  var errorResId = getDateErrorMessageResId(expireDate)
  if (errorResId == null && buyDateErrorResId == null) {
    if (!isDateAfterOrEqual(expireDate, buyDate)) {
      errorResId = R.string.expire_date_before_buy_date_error
    } else if (!isValidDateNotPast(expireDate)) {
      errorResId = R.string.expire_date_in_past_error
    }
  }
  return errorResId
}

/**
 * Validates the open date.
 *
 * @param openDate The open date to validate.
 * @param buyDate The buy date to compare with.
 * @param buyDateErrorResId The resource ID of the error message for the buy date.
 * @param expireDate The expire date to compare with.
 * @param expireDateErrorResId The resource ID of the error message for the expire date.
 * @return The resource ID of the error message if the open date is invalid, null otherwise.
 */
fun validateOpenDate(
    openDate: String,
    buyDate: String,
    buyDateErrorResId: Int?,
    expireDate: String,
    expireDateErrorResId: Int?
): Int? {
  var errorResId = getDateErrorMessageResId(openDate, isRequired = false)
  if (errorResId == null &&
      openDate.isNotEmpty() &&
      buyDateErrorResId == null &&
      expireDateErrorResId == null) {
    if (!isDateAfterOrEqual(openDate, buyDate)) {
      errorResId = R.string.open_date_before_buy_date_error
    } else if (!isDateAfterOrEqual(expireDate, openDate)) {
      errorResId = R.string.open_date_after_expire_date_error
    }
  }
  return errorResId
}
