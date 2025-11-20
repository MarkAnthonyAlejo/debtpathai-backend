package com.debtpathai.service

import com.debtpathai.dto.RepaymentRequest
import com.debtpathai.model.Debt
import org.junit.jupiter.api.Test
import java.math.BigDecimal
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class RepaymentServiceTest {

    private val repaymentService = RepaymentService()

    @Test
    fun `snowball should prioritize smallest balance first`(){
        val debts = listOf(
            Debt("Card A", BigDecimal(1500), BigDecimal(18), BigDecimal(50)),
            Debt("Card B", BigDecimal(500), BigDecimal(15), BigDecimal(35)),
            Debt("Card C", BigDecimal(1000), BigDecimal(22), BigDecimal(25)),
        )

        val request = RepaymentRequest(
            debts = debts,
            extraPayment = BigDecimal(200),
            method = "snowball"
        )

        val result = repaymentService.calculateRepaymentPlan(request)

        val month1 = result.schedule.first()

        //The smallest balance is Card B (500)
        val bPayment = month1.payments.first {it.debtName == "Card B"}

        assertTrue(bPayment.principal > BigDecimal(35)) // min payment was 35, so extra applied
    }

    @Test
    fun `avalanche should priotize highest APR first`() {
        val debts = listOf(
            Debt("Card A", BigDecimal(1500), BigDecimal(18), BigDecimal(50)),
            Debt("Card B", BigDecimal(500), BigDecimal(10), BigDecimal(35)),
            Debt("Card C", BigDecimal(1000), BigDecimal(29), BigDecimal(25)), //highest APR
        )

        val request = RepaymentRequest(
            debts = debts,
            extraPayment = BigDecimal(200),
            method = "avalanche"
        )

        val result = repaymentService.calculateRepaymentPlan(request)

        val month1 = result.schedule.first()

        val highestAPRPayment = month1.payments.first{it.debtName == "Card C"}

        assertTrue(highestAPRPayment.principal > BigDecimal(25))
    }

    @Test
    fun `balances must go down over time`() {
        val debts = listOf(
            Debt("Loan A", BigDecimal(1000), BigDecimal(12), BigDecimal(50)),
        )

        val result = repaymentService.calculateRepaymentPlan(
            RepaymentRequest(
                debts = debts,
                extraPayment = BigDecimal(100),
                method = "snowball"
            )
        )

        val month1 = result.schedule[0].payments.first().remainingBalance
        val month2 = result.schedule[1].payments.first().remainingBalance

        assertTrue(month2 < month1)
    }

    @Test
    fun `total interest must be greater than zero`() {
        val result = repaymentService.calculateRepaymentPlan(
            RepaymentRequest(
                debts = listOf(
                    Debt("Test", BigDecimal(500), BigDecimal(20), BigDecimal(50)),
                ),
                extraPayment = BigDecimal(0),
                method = "snowball"
            )
        )

        assertTrue(result.totalInterestPaid > BigDecimal.ZERO)
    }

    @Test
    fun `schedule must not be empty`(){
        val result = repaymentService.calculateRepaymentPlan(
            RepaymentRequest(
                debts = listOf(
                    Debt("Test", BigDecimal(500), BigDecimal(20), BigDecimal(50))
                ),
                extraPayment = BigDecimal(50),
                method = "snowball"
            )
        )

        assertTrue(result.schedule.isNotEmpty())
        assertTrue(result.months > 0)
    }
}