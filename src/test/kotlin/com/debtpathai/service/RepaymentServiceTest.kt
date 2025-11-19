package com.debtpathai.service

import com.debtpathai.dto.RepaymentRequest
import com.debtpathai.model.Debt
import org.junit.jupiter.api.Test
import java.math.BigDecimal
import kotlin.test.assertEquals

class RepaymentServiceTest {

    private val repaymentService = RepaymentService()

    @Test
    fun `test avalanche sorting`() {
        //Arrange: create debts
        val debts = listOf(
        Debt("Card A", BigDecimal(1500), BigDecimal(24), BigDecimal(50)),
        Debt("Card B", BigDecimal(1000), BigDecimal(19), BigDecimal(35)),
        Debt("Card C", BigDecimal(500), BigDecimal(16), BigDecimal(25))
        )

        val request = RepaymentRequest(
            debts = debts,
            extraPayment = BigDecimal(100),
            method = "avalanche"
        )

        //Act: call the service
        val result = repaymentService.calculateRepaymentPlan(request)

        //Assert: for now, check placeholder
        assertEquals(listOf("Debt logic goes here"), result)
    }

    @Test
    fun `test snowball sorting`() {
        val debts = listOf(
            Debt("Card A", BigDecimal(1500), BigDecimal(24), BigDecimal(50)),
            Debt("Card B", BigDecimal(1000), BigDecimal(35), BigDecimal(25)),
            Debt("Card C", BigDecimal(500), BigDecimal(16), BigDecimal(25))
        )

        val request = RepaymentRequest(
            debts = debts,
            extraPayment = BigDecimal(100),
            method = "snowball"
        )

        val result = repaymentService.calculateRepaymentPlan(request)

        assertEquals(listOf("Debt logic goes here"), result)
    }
}