package com.debtpathai.dto

data class PaymentMonth(
    val month: Int,
    val payments: List<PaymentDetail>
)