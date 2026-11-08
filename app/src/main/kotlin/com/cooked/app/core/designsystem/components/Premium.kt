package com.cooked.app.core.designsystem.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.cooked.app.core.designsystem.CookedMotion
import com.cooked.app.core.designsystem.CookedRadius
import com.cooked.app.core.designsystem.CookedTheme
import com.cooked.app.core.designsystem.FontFeatureSettingsTabular
import com.cooked.app.core.designsystem.PillShape
import com.cooked.app.core.designsystem.rememberAreAnimationsDisabled

/**
 * Large, very soft, warm-tinted shadow used to lift cards off the ivory canvas
 * (the reference designs separate surfaces with diffused light, not outlines).
 * No-op in dark mode, where tonal steps do the separating.
 */
@Composable
fun Modifier.softShadow(
    shape: Shape = RoundedCornerShape(CookedRadius.Card),
    elevation: Dp = 18.dp,
    alpha: Float = 0.10f
): Modifier {
    val colors = CookedTheme.colors
    return if (colors.isDark) {
        this
    } else {
        this.shadow(
            elevation = elevation,
            shape = shape,
            clip = false,
            ambientColor = colors.shadow.copy(alpha = alpha * 0.5f),
            spotColor = colors.shadow.copy(alpha = alpha)
        )
    }
}

/** Press-scale feedback shared by premium tappables. */
@Composable
private fun rememberPressScale(interactionSource: MutableInteractionSource, pressed: Float = 0.96f): Float {
    val isPressed by interactionSource.collectIsPressedAsState()
    val animationsDisabled = rememberAreAnimationsDisabled()
    val scale by animateFloatAsState(
        targetValue = if (isPressed) pressed else 1f,
        animationSpec = if (animationsDisabled) CookedMotion.fadeSpec(0) else CookedMotion.layoutSpring(),
        label = "pressScale"
    )
    return scale
}

enum class CircleButtonStyle { Surface, Ink, Accent, Ghost }

/**
 * 44dp circular icon button (bell, back, share, settings). Mirrors the round white
 * controls in the references; [badge] draws a small neon notification dot.
 */
@Composable
fun CircleIconButton(
    icon: ImageVector,
    contentDescription: String?,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    style: CircleButtonStyle = CircleButtonStyle.Surface,
    size: Dp = 44.dp,
    badge: Boolean = false
) {
    CircleButtonContainer(onClick = onClick, modifier = modifier, style = style, size = size, badge = badge) { tint ->
        Icon(imageVector = icon, contentDescription = contentDescription, tint = tint, modifier = Modifier.size(size * 0.45f))
    }
}

@Composable
fun CircleIconButton(
    painter: Painter,
    contentDescription: String?,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    style: CircleButtonStyle = CircleButtonStyle.Surface,
    size: Dp = 44.dp,
    badge: Boolean = false
) {
    CircleButtonContainer(onClick = onClick, modifier = modifier, style = style, size = size, badge = badge) { tint ->
        Icon(painter = painter, contentDescription = contentDescription, tint = tint, modifier = Modifier.size(size * 0.45f))
    }
}

@Composable
private fun CircleButtonContainer(
    onClick: () -> Unit,
    modifier: Modifier,
    style: CircleButtonStyle,
    size: Dp,
    badge: Boolean,
    content: @Composable (Color) -> Unit
) {
    val colors = CookedTheme.colors
    val (container, tint) = when (style) {
        CircleButtonStyle.Surface -> (if (colors.isDark) colors.surfaceContainer else colors.surface) to colors.onSurface
        CircleButtonStyle.Ink -> colors.hero to colors.onHero
        CircleButtonStyle.Accent -> colors.accent to colors.onAccent
        CircleButtonStyle.Ghost -> Color.Transparent to colors.onSurface
    }
    val interaction = remember { MutableInteractionSource() }
    val scale = rememberPressScale(interaction)

    Box(modifier = modifier.size(size)) {
        Box(
            modifier = Modifier
                .size(size)
                .graphicsLayer { scaleX = scale; scaleY = scale }
                .then(if (style == CircleButtonStyle.Surface) Modifier.softShadow(CircleShape, 10.dp, 0.08f) else Modifier)
                .clip(CircleShape)
                .background(container)
                .clickable(interactionSource = interaction, indication = null, role = Role.Button, onClick = onClick),
            contentAlignment = Alignment.Center
        ) {
            content(tint)
        }
        if (badge) {
            Box(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .offset(x = (-6).dp, y = 8.dp)
                    .size(9.dp)
                    .clip(CircleShape)
                    .background(if (colors.isDark) colors.accent else colors.heatFor(com.cooked.app.core.designsystem.HeatLevel.COOKED).color)
            )
        }
    }
}

/**
 * The single most important card on a screen: ink panel (both themes) with a faint neon
 * glow in one corner, as in the "AI Price Forecast" card of the reference.
 */
@Composable
fun HeroCard(
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null,
    glow: Boolean = true,
    contentPadding: Dp = 22.dp,
    content: @Composable ColumnScope.() -> Unit
) {
    val colors = CookedTheme.colors
    val shape = RoundedCornerShape(CookedRadius.Card)
    val interaction = remember { MutableInteractionSource() }
    val scale = if (onClick != null) rememberPressScale(interaction, 0.985f) else 1f
    Column(
        modifier = modifier
            .graphicsLayer { scaleX = scale; scaleY = scale }
            .softShadow(shape, 24.dp, 0.22f)
            .clip(shape)
            .background(colors.hero)
            .drawBehind {
                if (glow) {
                    drawCircle(
                        brush = Brush.radialGradient(
                            colors = listOf(colors.accent.copy(alpha = 0.13f), Color.Transparent),
                            center = Offset(size.width * 0.92f, size.height * 0.05f),
                            radius = size.width * 0.75f
                        ),
                        radius = size.width * 0.75f,
                        center = Offset(size.width * 0.92f, size.height * 0.05f)
                    )
                }
            }
            .then(
                if (onClick != null) {
                    Modifier.clickable(interactionSource = interaction, indication = null, onClick = onClick)
                } else {
                    Modifier
                }
            )
            .padding(contentPadding),
        content = content
    )
}

/** Small neon pill, e.g. "96% match", "+12%", "Pro". */
@Composable
fun AccentBadge(
    text: String,
    modifier: Modifier = Modifier,
    leading: (@Composable () -> Unit)? = null,
    subtle: Boolean = false
) {
    val colors = CookedTheme.colors
    Row(
        modifier = modifier
            .clip(PillShape)
            .background(if (subtle) colors.accentContainer else colors.accent)
            .padding(horizontal = 10.dp, vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (leading != null) {
            leading()
            Spacer(Modifier.width(4.dp))
        }
        Text(
            text = text,
            style = CookedTheme.typography.labelMedium.copy(
                fontWeight = FontWeight.SemiBold,
                fontFeatureSettings = FontFeatureSettingsTabular
            ),
            color = if (subtle) colors.onAccentContainer else colors.onAccent
        )
    }
}

/**
 * Filter / category pill. Selected = ink (light) or neon (dark) fill; unselected = raised surface.
 */
@Composable
fun PillChip(
    text: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    trailing: (@Composable RowScope.() -> Unit)? = null
) {
    val colors = CookedTheme.colors
    val container by animateColorAsState(
        if (selected) colors.primary else if (colors.isDark) colors.surfaceContainer else colors.surface,
        label = "chipContainer"
    )
    val content by animateColorAsState(
        if (selected) colors.onPrimary else colors.onSurface,
        label = "chipContent"
    )
    val interaction = remember { MutableInteractionSource() }
    val scale = rememberPressScale(interaction)
    Row(
        modifier = modifier
            .graphicsLayer { scaleX = scale; scaleY = scale }
            .height(42.dp)
            .clip(PillShape)
            .background(container)
            .clickable(interactionSource = interaction, indication = null, role = Role.Tab, onClick = onClick)
            .padding(horizontal = 18.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center
    ) {
        Text(
            text = text,
            style = CookedTheme.typography.labelLarge.copy(fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Medium),
            color = content
        )
        trailing?.invoke(this)
    }
}

/** Muted small label above a value or headline ("Good morning", "Cooked score"). */
@Composable
fun Overline(
    text: String,
    modifier: Modifier = Modifier,
    color: Color = CookedTheme.colors.onSurfaceVariant
) {
    Text(
        text = text,
        style = CookedTheme.typography.labelMedium,
        color = color,
        modifier = modifier
    )
}

/** Thin neon/ink bar stack used as a mini chart inside hero cards. */
@Composable
fun MiniBarChart(
    values: List<Float>,
    modifier: Modifier = Modifier,
    highlightLast: Int = 2,
    barWidth: Dp = 10.dp,
    maxHeight: Dp = 56.dp
) {
    val colors = CookedTheme.colors
    val max = (values.maxOrNull() ?: 1f).coerceAtLeast(0.0001f)
    Row(
        modifier = modifier.height(maxHeight),
        horizontalArrangement = Arrangement.spacedBy(6.dp),
        verticalAlignment = Alignment.Bottom
    ) {
        values.forEachIndexed { index, v ->
            val isHighlight = index >= values.size - highlightLast
            Box(
                modifier = Modifier
                    .width(barWidth)
                    .height((maxHeight * (v / max).coerceIn(0.12f, 1f)))
                    .clip(PillShape)
                    .background(if (isHighlight) colors.accent else colors.heroRaised)
            )
        }
    }
}

data class FloatingNavItem(
    val key: String,
    val icon: ImageVector,
    val label: String
)

/**
 * Floating ink pill navigation bar (Nestora-style): icon-only items, the active one sits in a
 * neon circle, and an optional neon center action (quick add).
 */
@Composable
fun FloatingNavBar(
    items: List<FloatingNavItem>,
    selectedKey: String?,
    onItemSelected: (FloatingNavItem) -> Unit,
    modifier: Modifier = Modifier,
    centerAction: (@Composable BoxScope.() -> Unit)? = null,
    centerIndex: Int = items.size / 2
) {
    val colors = CookedTheme.colors
    val barColor = if (colors.isDark) colors.surfaceContainerHigh else colors.hero
    Row(
        modifier = modifier
            .softShadow(PillShape, 24.dp, 0.25f)
            .clip(PillShape)
            .background(barColor)
            .padding(horizontal = 8.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        items.forEachIndexed { index, item ->
            if (centerAction != null && index == centerIndex) {
                Box(modifier = Modifier.size(56.dp), contentAlignment = Alignment.Center, content = centerAction)
            }
            NavBarIcon(item = item, selected = item.key == selectedKey, onClick = { onItemSelected(item) })
        }
        if (centerAction != null && centerIndex >= items.size) {
            Box(modifier = Modifier.size(56.dp), contentAlignment = Alignment.Center, content = centerAction)
        }
    }
}

@Composable
private fun NavBarIcon(item: FloatingNavItem, selected: Boolean, onClick: () -> Unit) {
    val colors = CookedTheme.colors
    val container by animateColorAsState(if (selected) colors.accent else Color.Transparent, label = "navContainer")
    val tint by animateColorAsState(if (selected) colors.onAccent else colors.onHeroMuted, label = "navTint")
    val interaction = remember { MutableInteractionSource() }
    val scale = rememberPressScale(interaction, 0.9f)
    Box(
        modifier = Modifier
            .size(56.dp)
            .graphicsLayer { scaleX = scale; scaleY = scale }
            .clip(CircleShape)
            .background(container)
            .clickable(interactionSource = interaction, indication = null, role = Role.Tab, onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Icon(imageVector = item.icon, contentDescription = item.label, tint = tint, modifier = Modifier.size(24.dp))
    }
}

/** Neon center action used inside [FloatingNavBar]. */
@Composable
fun NavCenterAction(
    icon: ImageVector,
    contentDescription: String,
    onClick: () -> Unit
) {
    val colors = CookedTheme.colors
    val interaction = remember { MutableInteractionSource() }
    val scale = rememberPressScale(interaction, 0.9f)
    Box(
        modifier = Modifier
            .size(52.dp)
            .graphicsLayer { scaleX = scale; scaleY = scale }
            .clip(CircleShape)
            .background(if (colors.isDark) colors.onSurface else colors.surface)
            .clickable(interactionSource = interaction, indication = null, role = Role.Button, onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Icon(imageVector = icon, contentDescription = contentDescription, tint = colors.hero, modifier = Modifier.size(26.dp))
    }
}

/** Icon + label header row and a large value, like the "Walkability 92" tiles. */
@Composable
fun MetricTile(
    label: String,
    value: String,
    modifier: Modifier = Modifier,
    icon: ImageVector? = null,
    footer: (@Composable () -> Unit)? = null,
    onClick: (() -> Unit)? = null
) {
    CookedCard(modifier = modifier, onClick = onClick) {
        Column(modifier = Modifier.fillMaxWidth().padding(18.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                if (icon != null) {
                    Icon(icon, contentDescription = null, tint = CookedTheme.colors.onSurfaceVariant, modifier = Modifier.size(16.dp))
                    Spacer(Modifier.width(6.dp))
                }
                Text(label, style = CookedTheme.typography.labelMedium, color = CookedTheme.colors.onSurfaceVariant)
            }
            Spacer(Modifier.height(10.dp))
            Text(
                text = value,
                style = CookedTheme.typography.displaySmall,
                color = CookedTheme.colors.onSurface
            )
            if (footer != null) {
                Spacer(Modifier.height(6.dp))
                footer()
            }
        }
    }
}
