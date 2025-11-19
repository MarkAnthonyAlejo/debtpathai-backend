package com.debtpathai.model

import java.math.BigDecimal

//Using BigDecimal for more accurate finance calculations.
//Read Notion notes for more details.

data class Debt(
    val name: String,
    val balance: BigDecimal,
    val apr: BigDecimal,
    val minPayment: BigDecimal
)
