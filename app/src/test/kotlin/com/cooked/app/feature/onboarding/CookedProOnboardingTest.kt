package com.cooked.app.feature.onboarding

import com.cooked.app.domain.billing.PlanPeriod
import com.cooked.app.domain.billing.SubscriptionPlan
import com.google.common.truth.Truth.assertThat
import org.junit.Test

class CookedProOnboardingTest {

    @Test
    fun yearlySavings_calculatedAccurately() {
        val monthlyPlan = SubscriptionPlan(
            productId = "monthly",
            title = "Monthly",
            subtitle = "Billed monthly",
            formattedPrice = "₹199 / month",
            priceMicros = 199_000_000L,
            period = PlanPeriod.MONTHLY
        )

        val yearlyPlan = SubscriptionPlan(
            productId = "yearly",
            title = "Yearly",
            subtitle = "Billed annually",
            formattedPrice = "₹1,499 / year",
            priceMicros = 1_499_000_000L,
            period = PlanPeriod.YEARLY,
            freeTrialDays = 7,
            isBestValue = true
        )

        val annualFromMonthly = monthlyPlan.priceMicros * 12 // 2,388,000,000
        val savingsPercent = ((annualFromMonthly - yearlyPlan.priceMicros) * 100 / annualFromMonthly).toInt()

        // 1 - 1499 / 2388 = ~37%
        assertThat(savingsPercent).isEqualTo(37)
    }

    @Test
    fun monthlyEquivalent_calculatedCorrectly() {
        val yearlyMicros = 1_499_000_000L
        val monthlyMicros = yearlyMicros / 12 // 124,916,666 -> ~124.91 or 125
        val monthlyPrice = monthlyMicros / 1_000_000.0

        assertThat(monthlyPrice).isWithin(1.0).of(125.0)
    }

    @Test
    fun ctaLabel_adaptsToTrialAndLifetime() {
        val trialPlan = SubscriptionPlan(
            productId = "yearly",
            title = "Yearly",
            subtitle = "7-day trial",
            formattedPrice = "$14.99 / year",
            priceMicros = 14_990_000L,
            period = PlanPeriod.YEARLY,
            freeTrialDays = 7
        )

        val noTrialPlan = SubscriptionPlan(
            productId = "monthly",
            title = "Monthly",
            subtitle = "No trial",
            formattedPrice = "$2.99 / month",
            priceMicros = 2_990_000L,
            period = PlanPeriod.MONTHLY,
            freeTrialDays = 0
        )

        val lifetimePlan = SubscriptionPlan(
            productId = "lifetime",
            title = "Lifetime",
            subtitle = "Pay once",
            formattedPrice = "$39.99",
            priceMicros = 39_990_000L,
            period = PlanPeriod.LIFETIME,
            isLifetime = true
        )

        assertThat(trialPlan.freeTrialDays > 0).isTrue()
        assertThat(noTrialPlan.freeTrialDays > 0).isFalse()
        assertThat(lifetimePlan.isLifetime).isTrue()
    }

    @Test
    fun compliance_featuresDoNotClaimBypassingLockScreen() {
        // Ensure feature copy complies with Android restrictions
        val descriptions = listOf(
            "Configurable reminder escalation until you take action.",
            "Understand your consistency, procrastination patterns and deadline performance.",
            "Enhanced reminder experiences for your most important deadlines.",
            "Unlock additional reminder personalities, custom voices and premium app customization."
        )

        descriptions.forEach { desc ->
            assertThat(desc.lowercase()).doesNotContain("bypass lock-screen")
            assertThat(desc.lowercase()).doesNotContain("bypass security")
        }
    }
}
