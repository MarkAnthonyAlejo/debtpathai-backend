package com.debtpathai.dto

import com.debtpathai.model.Debt
import java.math.BigDecimal

//Data Transfer Object (DTO) handles API request/response shapes

data class RepaymentRequest(
    val debts: List<Debt>,
    val extraPayment: BigDecimal,
    val method: String
)

//Example of what the frontend might send
// {
//  "debts": [
//    { "name": "Credit Card", "balance": 1500, "apr": 18.5, "minPayment": 50 }
//  ],
//  "extraPayment": 200,
//  "method": "snowball"
//}