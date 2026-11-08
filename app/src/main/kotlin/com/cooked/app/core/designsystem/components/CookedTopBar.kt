package com.cooked.app.core.designsystem.components

import androidx.compose.foundation.layout.RowScope
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LargeTopAppBar
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarColors
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.TopAppBarScrollBehavior
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewLightDark
import com.cooked.app.core.designsystem.CookedTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CookedTopBar(
    title: String,
    modifier: Modifier = Modifier,
    navigationIcon: (@Composable () -> Unit)? = null,
    onNavigationClick: (() -> Unit)? = null,
    actions: @Composable RowScope.() -> Unit = {},
    scrollBehavior: TopAppBarScrollBehavior? = null,
    isLarge: Boolean = false
) {
    val navIcon: (@Composable () -> Unit)? = navigationIcon ?: onNavigationClick?.let { onClick ->
        {
            CircleIconButton(
                icon = Icons.AutoMirrored.Rounded.ArrowBack,
                contentDescription = "Back",
                onClick = onClick,
                modifier = Modifier.padding(start = 12.dp, end = 4.dp),
                size = 42.dp
            )
        }
    }

    val colors = TopAppBarDefaults.topAppBarColors(
        containerColor = CookedTheme.colors.background,
        scrolledContainerColor = CookedTheme.colors.background,
        navigationIconContentColor = CookedTheme.colors.onBackground,
        titleContentColor = CookedTheme.colors.onBackground,
        actionIconContentColor = CookedTheme.colors.onSurfaceVariant
    )

    if (isLarge) {
        LargeTopAppBar(
            title = {
                Text(
                    text = title,
                    style = CookedTheme.typography.headlineLarge,
                    color = CookedTheme.colors.onBackground
                )
            },
            modifier = modifier,
            navigationIcon = { navIcon?.invoke() },
            actions = actions,
            colors = TopAppBarDefaults.largeTopAppBarColors(
                containerColor = CookedTheme.colors.background,
                scrolledContainerColor = CookedTheme.colors.background,
                navigationIconContentColor = CookedTheme.colors.onBackground,
                titleContentColor = CookedTheme.colors.onBackground,
                actionIconContentColor = CookedTheme.colors.onSurfaceVariant
            ),
            scrollBehavior = scrollBehavior
        )
    } else {
        TopAppBar(
            title = {
                Text(
                    text = title,
                    style = CookedTheme.typography.headlineSmall,
                    color = CookedTheme.colors.onBackground
                )
            },
            modifier = modifier,
            navigationIcon = { navIcon?.invoke() },
            actions = actions,
            colors = colors,
            scrollBehavior = scrollBehavior
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@PreviewLightDark
@Preview(fontScale = 1.5f)
@Composable
private fun CookedTopBarPreview() {
    CookedTheme {
        CookedTopBar(
            title = "Cooked",
            navigationIcon = {
                IconButton(onClick = {}) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Rounded.ArrowBack,
                        contentDescription = "Back"
                    )
                }
            },
            actions = {
                IconButton(onClick = {}) {
                    Icon(
                        imageVector = Icons.Rounded.Settings,
                        contentDescription = "Settings"
                    )
                }
            }
        )
    }
}

