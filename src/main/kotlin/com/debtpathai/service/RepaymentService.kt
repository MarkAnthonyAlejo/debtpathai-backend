package com.debtpathai.service

import com.debtpathai.dto.RepaymentRequest
import com.debtpathai.model.Debt
import org.springframework.stereotype.Service

@Service
class RepaymentService {
    fun calculateRepaymentPlan(request: RepaymentRequest): List<String>{
        val sortedDebts = when (request.method.lowercase()){
            "avalanche" -> request.debts.sortedByDescending { it.apr }
            "snowball" -> request.debts.sortedBy { it.balance }
            else -> throw IllegalArgumentException("Invalid method")
        }
        //Placeholder for now
        return listOf("Debt logic goes here")
    }
}