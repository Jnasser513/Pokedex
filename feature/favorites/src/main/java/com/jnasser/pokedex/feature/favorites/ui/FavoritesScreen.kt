package com.jnasser.pokedex.feature.favorites.ui

import androidx.compose.animation.Crossfade
import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil3.compose.SubcomposeAsyncImage
import coil3.request.ImageRequest
import coil3.request.crossfade
import com.jnasser.pokedex.core.designsystem.component.PokedexBrandMark
import com.jnasser.pokedex.core.designsystem.component.PokedexEmptyState
import com.jnasser.pokedex.core.designsystem.component.PokedexErrorState
import com.jnasser.pokedex.core.designsystem.component.PokedexLoadingState
import com.jnasser.pokedex.core.designsystem.component.PokemonTypeBadge
import com.jnasser.pokedex.core.designsystem.theme.PokedexDesignSystem
import com.jnasser.pokedex.core.designsystem.theme.PokemonTypeTokens
import com.jnasser.pokedex.core.model.PokemonSummary
import com.jnasser.pokedex.feature.favorites.FavoritesAction
import com.jnasser.pokedex.feature.favorites.FavoritesEffect
import com.jnasser.pokedex.feature.favorites.FavoritesUiState
import com.jnasser.pokedex.feature.favorites.FavoritesViewModel
import com.jnasser.pokedex.feature.favorites.R
import java.util.Locale
import kotlinx.coroutines.flow.collectLatest
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun FavoritesRoute(
    onPokemonClick: (Int) -> Unit,
    onBrowsePokedex: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: FavoritesViewModel = koinViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    val favoriteErrorMessage = stringResource(R.string.favorite_update_error)

    LaunchedEffect(viewModel, snackbarHostState) {
        viewModel.effects.collectLatest { effect ->
            when (effect) {
                FavoritesEffect.FavoriteUpdateFailed -> {
                    snackbarHostState.showSnackbar(favoriteErrorMessage)
                }
            }
        }
    }

    FavoritesScreen(
        uiState = uiState,
        snackbarHostState = snackbarHostState,
        onAction = viewModel::onAction,
        onPokemonClick = onPokemonClick,
        onBrowsePokedex = onBrowsePokedex,
        modifier = modifier,
    )
}

@Composable
internal fun FavoritesScreen(
    uiState: FavoritesUiState,
    snackbarHostState: SnackbarHostState,
    onAction: (FavoritesAction) -> Unit,
    onPokemonClick: (Int) -> Unit,
    onBrowsePokedex: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Scaffold(
        modifier = modifier.fillMaxSize(),
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = { FavoritesHeader() },
    ) { contentPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(contentPadding),
            contentAlignment = Alignment.TopCenter,
        ) {
            Crossfade(
                targetState = uiState,
                modifier = Modifier
                    .widthIn(max = 840.dp)
                    .fillMaxHeight()
                    .fillMaxWidth(),
                label = "favoritesState",
            ) { state ->
                when (state) {
                    FavoritesUiState.Loading -> {
                        PokedexLoadingState(
                            label = stringResource(R.string.favorites_loading),
                            modifier = Modifier.fillMaxSize(),
                        )
                    }

                    FavoritesUiState.Empty -> {
                        PokedexEmptyState(
                            title = stringResource(R.string.favorites_empty_title),
                            message = stringResource(R.string.favorites_empty_message),
                            actionLabel = stringResource(R.string.browse_pokedex),
                            onAction = onBrowsePokedex,
                            modifier = Modifier.fillMaxSize(),
                        )
                    }

                    FavoritesUiState.Error -> {
                        PokedexErrorState(
                            title = stringResource(R.string.favorites_error_title),
                            message = stringResource(R.string.favorites_error_message),
                            retryLabel = stringResource(R.string.retry),
                            onRetry = { onAction(FavoritesAction.Retry) },
                            modifier = Modifier.fillMaxSize(),
                        )
                    }

                    is FavoritesUiState.Content -> {
                        FavoritePokemonList(
                            pokemon = state.pokemon,
                            onPokemonClick = onPokemonClick,
                            onRemoveFavorite = { pokemonId ->
                                onAction(FavoritesAction.RemoveFavorite(pokemonId))
                            },
                            modifier = Modifier.fillMaxSize(),
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun FavoritesHeader() {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.surface)
            .statusBarsPadding()
            .heightIn(min = 80.dp)
            .padding(horizontal = PokedexDesignSystem.spacing.compactScreenMargin),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        PokedexBrandMark()
        Spacer(Modifier.width(12.dp))
        Column {
            Text(
                text = stringResource(R.string.favorites_title),
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Medium,
            )
            Text(
                text = stringResource(R.string.favorites_subtitle),
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                style = MaterialTheme.typography.labelMedium,
            )
        }
    }
}

@Composable
private fun FavoritePokemonList(
    pokemon: List<PokemonSummary>,
    onPokemonClick: (Int) -> Unit,
    onRemoveFavorite: (Int) -> Unit,
    modifier: Modifier = Modifier,
) {
    LazyColumn(
        modifier = modifier,
        contentPadding = PaddingValues(
            start = PokedexDesignSystem.spacing.compactScreenMargin,
            top = PokedexDesignSystem.spacing.small,
            end = PokedexDesignSystem.spacing.compactScreenMargin,
            bottom = PokedexDesignSystem.spacing.medium,
        ),
        verticalArrangement = Arrangement.spacedBy(PokedexDesignSystem.spacing.small),
    ) {
        items(
            items = pokemon,
            key = PokemonSummary::id,
            contentType = { "favoritePokemon" },
        ) { item ->
            FavoritePokemonCard(
                pokemon = item,
                onClick = { onPokemonClick(item.id) },
                onRemoveFavorite = { onRemoveFavorite(item.id) },
                modifier = Modifier.animateItem(),
            )
        }
    }
}

@Composable
private fun FavoritePokemonCard(
    pokemon: PokemonSummary,
    onClick: () -> Unit,
    onRemoveFavorite: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val displayName = pokemon.name.toDisplayName()
    val primaryType = pokemon.types.minByOrNull { it.slot }?.name.orEmpty()
    val typeColor = PokemonTypeTokens.colorsFor(primaryType)?.container
    val surfaceColor = MaterialTheme.colorScheme.surfaceContainerLow
    val containerColor = lerp(
        start = surfaceColor,
        stop = typeColor ?: MaterialTheme.colorScheme.primaryContainer,
        fraction = if (isSystemInDarkTheme()) 0.18f else 0.28f,
    )
    val imageSize = if (LocalDensity.current.fontScale >= 1.3f) 80.dp else 112.dp

    Card(
        onClick = onClick,
        modifier = modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.extraLarge,
        colors = CardDefaults.cardColors(containerColor = Color.Transparent),
        elevation = CardDefaults.cardElevation(PokedexDesignSystem.elevations.level1),
    ) {
        Row(
            modifier = Modifier
                .background(
                    Brush.horizontalGradient(
                        colors = listOf(containerColor, surfaceColor),
                    )
                )
                .padding(PokedexDesignSystem.spacing.small),
            horizontalArrangement = Arrangement.spacedBy(PokedexDesignSystem.spacing.small),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            FavoritePokemonArtwork(
                imageUrl = pokemon.imageUrl,
                pokemonName = displayName,
                modifier = Modifier.size(imageSize),
            )
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(6.dp),
            ) {
                Text(
                    text = stringResource(R.string.pokemon_number, pokemon.id),
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    style = MaterialTheme.typography.labelMedium,
                )
                Text(
                    text = displayName,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                )
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp),
                ) {
                    pokemon.types.sortedBy { it.slot }.forEach { type ->
                        PokemonTypeBadge(
                            typeName = type.name,
                            label = type.name.toDisplayName(),
                        )
                    }
                }
                FilledTonalButton(
                    onClick = onRemoveFavorite,
                    modifier = Modifier
                        .align(Alignment.End)
                        .heightIn(min = PokedexDesignSystem.spacing.minimumTouchTarget),
                    colors = ButtonDefaults.filledTonalButtonColors(
                        containerColor = MaterialTheme.colorScheme.errorContainer,
                        contentColor = MaterialTheme.colorScheme.onErrorContainer,
                    ),
                ) {
                    Icon(
                        imageVector = Icons.Filled.Delete,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp),
                    )
                    Spacer(Modifier.width(8.dp))
                    Text(stringResource(R.string.remove_favorite))
                }
            }
        }
    }
}

@Composable
private fun FavoritePokemonArtwork(
    imageUrl: String?,
    pokemonName: String,
    modifier: Modifier = Modifier,
) {
    SubcomposeAsyncImage(
        model = ImageRequest.Builder(androidx.compose.ui.platform.LocalContext.current)
            .data(imageUrl)
            .crossfade(true)
            .build(),
        contentDescription = stringResource(R.string.pokemon_image, pokemonName),
        modifier = modifier.clip(MaterialTheme.shapes.large),
        contentScale = ContentScale.Fit,
        loading = { ArtworkPlaceholder(showProgress = true) },
        error = { ArtworkPlaceholder(showProgress = false) },
    )
}

@Composable
private fun ArtworkPlaceholder(showProgress: Boolean) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.surfaceContainer),
        contentAlignment = Alignment.Center,
    ) {
        if (showProgress) {
            CircularProgressIndicator(modifier = Modifier.size(28.dp))
        } else {
            Text(
                text = stringResource(R.string.image_unavailable),
                modifier = Modifier.padding(PokedexDesignSystem.spacing.small),
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                style = MaterialTheme.typography.labelSmall,
            )
        }
    }
}

private fun String.toDisplayName(): String =
    replace('-', ' ').replaceFirstChar { character ->
        if (character.isLowerCase()) character.titlecase(Locale.getDefault()) else character.toString()
    }
