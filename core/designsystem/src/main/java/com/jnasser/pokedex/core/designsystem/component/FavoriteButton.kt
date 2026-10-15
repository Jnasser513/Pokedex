package com.jnasser.pokedex.core.designsystem.component

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.sizeIn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.IconToggleButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.jnasser.pokedex.core.designsystem.theme.PokedexDesignSystem

@Composable
fun FavoriteButton(
    isFavorite: Boolean,
    contentDescription: String,
    onFavoriteChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
) {
    IconToggleButton(
        checked = isFavorite,
        onCheckedChange = onFavoriteChange,
        modifier = modifier.sizeIn(
            minWidth = PokedexDesignSystem.spacing.minimumTouchTarget,
            minHeight = PokedexDesignSystem.spacing.minimumTouchTarget,
        ),
        colors = IconButtonDefaults.iconToggleButtonColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerLowest,
            contentColor = MaterialTheme.colorScheme.onSurfaceVariant,
            checkedContainerColor = MaterialTheme.colorScheme.primaryContainer,
            checkedContentColor = MaterialTheme.colorScheme.onPrimaryContainer,
        ),
    ) {
        AnimatedContent(
            targetState = isFavorite,
            transitionSpec = {
                (fadeIn() + scaleIn(initialScale = 0.75f)) togetherWith
                    (fadeOut() + scaleOut(targetScale = 0.75f))
            },
            label = "favoriteIcon",
        ) { selected ->
            Icon(
                imageVector = if (selected) Icons.Filled.Favorite else Icons.Outlined.FavoriteBorder,
                contentDescription = contentDescription,
            )
        }
    }
}
