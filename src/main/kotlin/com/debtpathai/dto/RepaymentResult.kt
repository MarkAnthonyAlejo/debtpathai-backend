package com.debtpathai.dto

import java.math.BigDecimal

data class RepaymentResult(
    val months: Int,
    val totalInterestPaid: BigDecimal,
    val schedule: List<PaymentMonth>
)