package com.jnasser.pokedex.core.designsystem.component

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.jnasser.pokedex.core.designsystem.theme.PokedexDesignSystem
import com.jnasser.pokedex.core.designsystem.theme.PokedexTheme
import com.jnasser.pokedex.core.designsystem.theme.PokedexThemePreviews

@PokedexThemePreviews
@Composable
private fun DesignComponentsPreview() {
    PokedexTheme {
        Surface(color = MaterialTheme.colorScheme.surface) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(PokedexDesignSystem.spacing.medium),
                verticalArrangement = Arrangement.spacedBy(PokedexDesignSystem.spacing.large),
            ) {
                Row(horizontalArrangement = Arrangement.spacedBy(PokedexDesignSystem.spacing.small)) {
                    PokemonTypeBadge(typeName = "fire", label = "Fuego")
                    PokemonTypeBadge(typeName = "water", label = "Agua")
                    PokemonTypeBadge(typeName = "electric", label = "Eléctrico")
                }
                Row(horizontalArrangement = Arrangement.spacedBy(PokedexDesignSystem.spacing.small)) {
                    FavoriteButton(
                        isFavorite = false,
                        contentDescription = "Agregar a favoritos",
                        onFavoriteChange = {},
                    )
                    FavoriteButton(
                        isFavorite = true,
                        contentDescription = "Quitar de favoritos",
                        onFavoriteChange = {},
                    )
                }
                PokedexEmptyState(
                    title = "Todavía no hay favoritos",
                    message = "Explora la Pokédex y guarda los que más te gusten.",
                    actionLabel = "Explorar",
                    onAction = {},
                )
            }
        }
    }
}
