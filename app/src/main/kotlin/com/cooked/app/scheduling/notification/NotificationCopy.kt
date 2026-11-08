package com.cooked.app.scheduling.notification

import com.cooked.app.domain.model.Personality
import kotlin.math.abs

enum class NagStage {
    FIRST,
    NAG_EARLY,       // Nags 1-2
    NAG_PERSISTENT,  // Nags 3-4
    FINAL_CALL       // Nag 5+ or Critical
}

object NotificationCopy {

    fun getCopy(
        personality: Personality,
        stage: NagStage,
        title: String,
        nagCount: Int = 0
    ): String {
        val variants = when (personality) {
            Personality.MILD -> when (stage) {
                NagStage.FIRST -> MildVariants.first
                NagStage.NAG_EARLY -> MildVariants.early
                NagStage.NAG_PERSISTENT -> MildVariants.persistent
                NagStage.FINAL_CALL -> MildVariants.finalCall
            }
            Personality.SPICY -> when (stage) {
                NagStage.FIRST -> SpicyVariants.first
                NagStage.NAG_EARLY -> SpicyVariants.early
                NagStage.NAG_PERSISTENT -> SpicyVariants.persistent
                NagStage.FINAL_CALL -> SpicyVariants.finalCall
            }
            Personality.UNHINGED -> when (stage) {
                NagStage.FIRST -> UnhingedVariants.first
                NagStage.NAG_EARLY -> UnhingedVariants.early
                NagStage.NAG_PERSISTENT -> UnhingedVariants.persistent
                NagStage.FINAL_CALL -> UnhingedVariants.finalCall
            }
        }

        val seed = abs(title.hashCode() * 31 + nagCount)
        val template = variants[seed % variants.size]
        return template.replace("{title}", title)
    }

    private object MildVariants {
        val first = listOf(
            "Time for {title}.",
            "Gentle reminder: {title} is due now.",
            "Ready when you are: {title}.",
            "Friendly nudge to take care of {title}.",
            "Small step forward: time for {title}.",
            "A quick reminder that {title} is due.",
            "Time to check off {title}.",
            "You've got this: {title}."
        )
        val early = listOf(
            "Just checking back on {title}.",
            "Still on your list: {title}.",
            "Quick reminder: {title} is waiting.",
            "Whenever you have a moment for {title}.",
            "Keeping {title} on your radar.",
            "Second reminder for {title}.",
            "Hope all is well! Still need to do {title}.",
            "Don't forget: {title}."
        )
        val persistent = listOf(
            "A reminder that {title} is still pending.",
            "Still here to help you finish {title}.",
            "Please take a moment to complete {title}.",
            "Checking in again regarding {title}.",
            "Keeping {title} fresh in mind.",
            "Just a few minutes on {title} can make a big difference.",
            "Whenever you can wrap up {title}.",
            "Persistent reminder for {title}."
        )
        val finalCall = listOf(
            "Important: {title} is now overdue.",
            "Priority alert: {title} needs your attention.",
            "High priority: please complete {title}.",
            "Final reminder to take care of {title}.",
            "Critical follow-up on {title}.",
            "Please attend to {title} as soon as possible.",
            "Urgent task: {title}.",
            "Action required for {title}."
        )
    }

    private object SpicyVariants {
        val first = listOf(
            "Clock's ticking on {title}.",
            "Alright, it's go time: {title}.",
            "You said you'd do {title}. I believed you.",
            "Time to lock in: {title}.",
            "Look what's due: {title}.",
            "No excuses, it's time for {title}.",
            "Future you is begging you to do {title}.",
            "Heat is on for {title}."
        )
        val early = listOf(
            "me again. {title}.",
            "still yapping about {title}.",
            "Did you think I forgot about {title}?",
            "Swiping me away won't do {title}.",
            "I can do this all day: {title}.",
            "Friendly reminder that {title} isn't doing itself.",
            "Still waiting on {title}. Respectfully.",
            "Hello? {title} is calling."
        )
        val persistent = listOf(
            "Bro, do {title} already.",
            "The skillet is heating up for {title}.",
            "I'm not mad about {title}, just professionally persistent.",
            "At this rate, we're getting cooked by {title}.",
            "Just tap Done on {title} and I'll go away.",
            "Are we really stalling on {title}?",
            "Don't make me bring out the loud alarms for {title}.",
            "Procrastinating on {title} is a dangerous game."
        )
        val finalCall = listOf(
            "CRITICAL: You are officially cooked if you ignore {title}.",
            "Emergency meeting: {title} is way overdue.",
            "Alarm siren sound effect: DO {title} NOW.",
            "This is DEFCON 1 for {title}.",
            "I am no longer asking nicely about {title}.",
            "Lock your phone, open your eyes, do {title}.",
            "The deadline is on fire: {title}.",
            "Final warning before peak heat: {title}."
        )
    }

    private object UnhingedVariants {
        val first = listOf(
            "THE SKILLET HATH SPOKEN: {title}.",
            "Wake up bestie, {title} is due.",
            "Drop everything, {title} approaches with malicious intent.",
            "A disturbance in the force: {title} is due.",
            "Ding ding ding! Round 1 of {title}.",
            "My internal flames are flickering for {title}.",
            "Behold: {title} is here to test your resolve.",
            "The prophecy said you'd do {title} right now."
        )
        val early = listOf(
            "I LIVE IN YOUR NOTIFICATION SHADE UNTIL {title} IS DONE.",
            "Swiping me away only makes me stronger: {title}.",
            "You cannot escape the heat of {title}.",
            "I have returned from the shadow realm to ask about {title}.",
            "Knock knock. Who's there? {title}.",
            "I will vibrate your pocket into another dimension: {title}.",
            "Your phone is an instrument of destiny. Complete {title}.",
            "Why are you reading this instead of doing {title}?"
        )
        val persistent = listOf(
            "I AM ONCE AGAIN ASKING YOU TO DO {title}.",
            "THE COOKED SCORE IS SIZZLING. DO {title}.",
            "DO NOT MAKE ME SUMMON THE ELDER FLAMES FOR {title}.",
            "Are you spiritually allergic to completing {title}?",
            "At this point {title} is haunting my operating system.",
            "The notification bar is my home now. Thanks to {title}.",
            "We are reaching temperatures previously thought impossible for {title}.",
            "I am screaming internally (and externally) about {title}."
        )
        val finalCall = listOf(
            "ABSOLUTE MAXIMUM OVERHEAT: COMPLETE {title} OR FACE COMBUSTION.",
            "SOUND THE HORNS OF GONDOR: {title} IS FULLY CRISPY.",
            "THIS IS NOT A DRILL. WE ARE AT LEVEL 10 COOKED FOR {title}.",
            "I HAVE BECOME DEATH, THE DESTROYER OF PROCRASTINATION: {title}.",
            "THE UNIVERSE IMPLODES IN 5... 4... DO {title} NOW.",
            "FULL CODE RED CRISPY MELTDOWN FOR {title}.",
            "DO {title} OR I WILL PERSONALLY LEARN HOW TO WALK.",
            "MAXIMUM EMERGENCY: DO {title} IMMEDIATELY."
        )
    }
}
