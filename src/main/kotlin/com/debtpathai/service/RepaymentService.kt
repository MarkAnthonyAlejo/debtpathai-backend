package com.debtpathai.service

import com.debtpathai.dto.PaymentDetail
import com.debtpathai.dto.PaymentMonth
import com.debtpathai.dto.RepaymentRequest
import com.debtpathai.dto.RepaymentResult
import com.debtpathai.model.Debt
import org.springframework.stereotype.Service
import java.math.BigDecimal
import java.math.RoundingMode

@Service
class RepaymentService {

    private val SCALE = 2
    private val MONTHS_IN_YEAR = BigDecimal(12)
    private val PERCENT = BigDecimal(100)
    private val MAX_MONTHS = 600 //safety cap (50 years) to avoid infinite loops

    fun calculateRepaymentPlan(request: RepaymentRequest): RepaymentResult{
        //Defensive checks
        require(request.debts.isNotEmpty()){"No debts provided"}
        require(request.extraPayment >= BigDecimal.ZERO){"Extra Payment must be >= 0"}
        val method = request.method.lowercase().trim()

        //Work on a mutable copy of debts (Do Not Mutate Incoming Objects)
        data class MutableDebt(
            var name: String,
            var balance: BigDecimal,
            var apr: BigDecimal,
            var minPayment: BigDecimal
        )

        val workingDebts = request.debts.map {
            MutableDebt(it.name,
                it.balance.setScale(SCALE, RoundingMode.HALF_UP),
                it.apr,
                it.minPayment.setScale(SCALE, RoundingMode.HALF_UP))
        }

        //Sorting comparator: determine priority order for extra payment
        fun sortedPriorityList(): List<MutableDebt> {
            return when (method) {
                "avalanche" -> workingDebts.filter {it.balance > BigDecimal.ZERO}
                    .sortedByDescending { it.apr }

                "snowball" -> workingDebts.filter {it.balance > BigDecimal.ZERO }
                    .sortedBy { it.balance }

                else -> throw IllegalArgumentException("Invalid Method: $method")
            }
        }

        val schedule = mutableListOf<PaymentMonth>()
        var month = 0
        var totalInterestPaid = BigDecimal.ZERO.setScale(SCALE, RoundingMode.HALF_UP)

        //loop month by month
        while (workingDebts.any {it.balance > BigDecimal.ZERO} && month < MAX_MONTHS) {
            month++
            //First compute interest for all active debts and add to balance
            for(d in workingDebts) {
                if(d.balance <= BigDecimal.ZERO) continue
                //monthly rate = apr% / 12
                val monthlyRate = d.apr.divide(PERCENT, 10, RoundingMode.HALF_UP).divide(MONTHS_IN_YEAR, 10 , RoundingMode.HALF_UP)
                val interest = d.balance.multiply(monthlyRate).setScale(SCALE, RoundingMode.HALF_UP)
                d.balance = d.balance.add(interest).setScale(SCALE, RoundingMode.HALF_UP)
                totalInterestPaid = totalInterestPaid.add(interest).setScale(SCALE, RoundingMode.HALF_UP)
            }

            //Available extra to allocate this month
            var extra = request.extraPayment.setScale(SCALE, RoundingMode.HALF_UP)

            val paymentsThisMonth = mutableListOf<PaymentDetail>()

            //Pay minimums for all debts first, but possibly less if balance is smaller
            for(d in workingDebts) {
                if(d.balance <= BigDecimal.ZERO) {
                    paymentsThisMonth.add(
                        PaymentDetail(d.name, BigDecimal.ZERO.setScale(SCALE),
                            BigDecimal.ZERO.setScale(SCALE), BigDecimal.ZERO.setScale(SCALE),
                            BigDecimal.ZERO.setScale(SCALE))
                    )
                    continue
                }

                //base payment is min(minPayment, balance)
                val basePayment = if(d.minPayment >= d.balance) d.balance else d.minPayment
                //temporarily subtract basePayment from balance (we will later add extra)
                d.balance = d.balance.subtract(basePayment).setScale(SCALE, RoundingMode.HALF_UP)
                paymentsThisMonth.add(
                    PaymentDetail(
                        debtName = d.name,
                        payment = basePayment,
                        interest = BigDecimal.ZERO, //well update interest/ principal fields later per debt
                        principal = basePayment,
                        remainingBalance = d.balance
                    )
                )
            }

            //Now apply extra to prioritized debts in order (rolling over within same month)
            val priority = sortedPriorityList()
            var extraAllocationRecords = mutableMapOf<String, BigDecimal>() //name -> extra allocated this month

            for(pd in priority) {
                if(extra <= BigDecimal.ZERO) break
                if(pd.balance <= BigDecimal.ZERO) continue

                val amountNeeded = pd.balance //how much to zero it out after basePayment applied
                val allocation = if(extra >= amountNeeded) amountNeeded else extra

                //subtract allocation from debt balance
                pd.balance = pd.balance.subtract(allocation).setScale(SCALE, RoundingMode.HALF_UP)
                extra = extra.subtract(allocation).setScale(SCALE, RoundingMode.HALF_UP)
                extraAllocationRecords[pd.name] = allocation
            }

            //Build final PaymentDetail list with correct interest/principal & payment totals
            val finalizedPayments = paymentsThisMonth.map { p ->
                val extraAlloc = extraAllocationRecords[p.debtName] ?: BigDecimal.ZERO.setScale(SCALE)
                //interest portion: we don't keep interest per-debt above because we already tracked totalInterestPaid above,
                //but we can compute approximate interest for the month as: total payment - principal applied.
                val paymentTotal = p.payment.add(extraAlloc).setScale(SCALE, RoundingMode.HALF_UP)
                val principalPaid = p.principal.add(extraAlloc).setScale(SCALE, RoundingMode.HALF_UP)
                //find the working debt to get the remaining balance
                val remaining = workingDebts.first {it.name == p.debtName}.balance.setScale(SCALE, RoundingMode.HALF_UP)
                //We do not store per-debt interest here (set to total payment - principal), but better to compute explicit interest if needed
                val interestPortion = BigDecimal.ZERO.setScale(SCALE, RoundingMode.HALF_UP) //placeholder if you want per-debt interest
                PaymentDetail(
                    debtName = p.debtName,
                    payment = paymentTotal,
                    interest = interestPortion,
                    principal = principalPaid,
                    remainingBalance = remaining
                )
            }

            schedule.add(PaymentMonth(month, finalizedPayments))
        }

       return RepaymentResult(
           months = month,
           totalInterestPaid = totalInterestPaid.setScale(SCALE, RoundingMode.HALF_UP),
           schedule = schedule
       )
    }
}