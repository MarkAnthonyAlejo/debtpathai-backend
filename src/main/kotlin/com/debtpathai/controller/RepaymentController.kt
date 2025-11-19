package com.debtpathai.controller

import com.debtpathai.dto.RepaymentRequest
import com.debtpathai.model.Debt
import com.debtpathai.service.RepaymentService
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/repayment")
class RepaymentController (
    private val repaymentService: RepaymentService
) {

    @PostMapping
    fun calculateRepayment(@RequestBody request: RepaymentRequest): ResponseEntity<Any> {
        val result = repaymentService.calculateRepaymentPlan(request)
        return ResponseEntity.ok(result)
    }
}