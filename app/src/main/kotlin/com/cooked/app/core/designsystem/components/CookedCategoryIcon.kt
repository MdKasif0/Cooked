package com.cooked.app.core.designsystem.components

import androidx.annotation.DrawableRes
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.cooked.app.R
import com.cooked.app.core.designsystem.CookedTheme

data class CategoryIconOption(
    val key: String,
    val label: String,
    @get:DrawableRes val iconRes: Int
)

object CategoryIcons {

    val all: List<CategoryIconOption> = listOf(
        CategoryIconOption("target", "Target", R.drawable.ic_cat_target),
        CategoryIconOption("assignment", "Assignment", R.drawable.ic_cat_assignment),
        CategoryIconOption("code", "Code", R.drawable.ic_cat_code),
        CategoryIconOption("work", "Work", R.drawable.ic_cat_work),
        CategoryIconOption("bill", "Bill", R.drawable.ic_cat_bill),
        CategoryIconOption("exam", "Exam", R.drawable.ic_cat_exam),
        CategoryIconOption("renewal", "Renewal", R.drawable.ic_cat_renewal),
        CategoryIconOption("mobile", "Recharge", R.drawable.ic_cat_mobile),
        CategoryIconOption("routine", "Routine", R.drawable.ic_cat_routine),
        CategoryIconOption("call", "Call", R.drawable.ic_cat_call),
        CategoryIconOption("health", "Health", R.drawable.ic_cat_health),
        CategoryIconOption("cart", "Shopping", R.drawable.ic_cat_cart),
        CategoryIconOption("fitness", "Fitness", R.drawable.ic_cat_fitness),
        CategoryIconOption("home", "Home", R.drawable.ic_cat_home),
        CategoryIconOption("creative", "Creative", R.drawable.ic_cat_creative),
        CategoryIconOption("travel", "Travel", R.drawable.ic_cat_travel),
        CategoryIconOption("vehicle", "Commute", R.drawable.ic_cat_vehicle),
        CategoryIconOption("focus", "Cooking", R.drawable.ic_cat_focus),
        CategoryIconOption("flame", "Flame", R.drawable.ic_cat_flame),
        CategoryIconOption("lightning", "Urgent", R.drawable.ic_cat_lightning),
        CategoryIconOption("bell", "Alert", R.drawable.ic_cat_bell)
    )

    @DrawableRes
    fun resolve(keyOrEmoji: String?): Int {
        if (keyOrEmoji.isNullOrBlank()) return R.drawable.ic_cat_target
        val clean = keyOrEmoji.trim()
        return when (clean) {
            "target", "🎯" -> R.drawable.ic_cat_target
            "assignment", "📝", "notes", "📚" -> R.drawable.ic_cat_assignment
            "code", "💻" -> R.drawable.ic_cat_code
            "work", "💼" -> R.drawable.ic_cat_work
            "bill", "💳", "💵" -> R.drawable.ic_cat_bill
            "exam", "🎓", "📐" -> R.drawable.ic_cat_exam
            "renewal", "🔄", "🌐" -> R.drawable.ic_cat_renewal
            "mobile", "📱", "recharge" -> R.drawable.ic_cat_mobile
            "routine", "⏰", "⏱️" -> R.drawable.ic_cat_routine
            "call", "📞" -> R.drawable.ic_cat_call
            "health", "💊", "tooth", "🦷" -> R.drawable.ic_cat_health
            "cart", "🛒" -> R.drawable.ic_cat_cart
            "fitness", "🏋️", "🏋" -> R.drawable.ic_cat_fitness
            "home", "🧹" -> R.drawable.ic_cat_home
            "creative", "🎨" -> R.drawable.ic_cat_creative
            "travel", "✈️", "✈" -> R.drawable.ic_cat_travel
            "vehicle", "🚗", "🛵" -> R.drawable.ic_cat_vehicle
            "focus", "cook", "🍳" -> R.drawable.ic_cat_focus
            "flame", "hot", "🔥" -> R.drawable.ic_cat_flame
            "lightning", "urgent", "⚡", "⚡️", "🚨" -> R.drawable.ic_cat_lightning
            "bell", "🔔" -> R.drawable.ic_cat_bell
            else -> R.drawable.ic_cat_target
        }
    }
}

@Composable
fun CookedCategoryIcon(
    keyOrEmoji: String?,
    contentDescription: String? = null,
    modifier: Modifier = Modifier,
    size: Dp = 22.dp,
    tint: Color = CookedTheme.colors.primary
) {
    val resId = CategoryIcons.resolve(keyOrEmoji)
    Icon(
        painter = painterResource(id = resId),
        contentDescription = contentDescription,
        tint = tint,
        modifier = modifier.size(size)
    )
}

