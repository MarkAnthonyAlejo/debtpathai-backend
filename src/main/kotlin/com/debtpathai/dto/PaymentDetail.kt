package com.debtpathai.dto

import java.math.BigDecimal

data class PaymentDetail(
    val debtName: String,
    val payment: BigDecimal,
    val interest: BigDecimal,
    val principal: BigDecimal,
    val remainingBalance: BigDecimal
)