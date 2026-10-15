package com.jnasser.pokedex.feature.pokemonlist.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.paging.LoadState
import androidx.paging.compose.LazyPagingItems
import androidx.paging.compose.collectAsLazyPagingItems
import androidx.paging.compose.itemContentType
import androidx.paging.compose.itemKey
import coil3.compose.SubcomposeAsyncImage
import coil3.request.ImageRequest
import coil3.request.crossfade
import com.jnasser.pokedex.core.designsystem.component.FavoriteButton
import com.jnasser.pokedex.core.designsystem.component.PokedexEmptyState
import com.jnasser.pokedex.core.designsystem.component.PokedexErrorState
import com.jnasser.pokedex.core.designsystem.component.PokedexLoadingState
import com.jnasser.pokedex.core.designsystem.component.PokemonTypeBadge
import com.jnasser.pokedex.core.designsystem.theme.PokedexDesignSystem
import com.jnasser.pokedex.core.designsystem.theme.PokemonTypeTokens
import com.jnasser.pokedex.core.model.PokemonSummary
import com.jnasser.pokedex.core.model.SyncFailure
import com.jnasser.pokedex.feature.pokemonlist.PokemonListAction
import com.jnasser.pokedex.feature.pokemonlist.PokemonListEffect
import com.jnasser.pokedex.feature.pokemonlist.PokemonListUiState
import com.jnasser.pokedex.feature.pokemonlist.PokemonListViewModel
import com.jnasser.pokedex.feature.pokemonlist.R
import java.util.Locale
import kotlinx.coroutines.flow.collectLatest
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun PokemonListRoute(
    modifier: Modifier = Modifier,
    viewModel: PokemonListViewModel = koinViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val pokemon = viewModel.pokemon.collectAsLazyPagingItems()
    val snackbarHostState = remember { SnackbarHostState() }
    val favoriteErrorMessage = stringResource(R.string.favorite_update_error)

    LaunchedEffect(viewModel, snackbarHostState) {
        viewModel.effects.collectLatest { effect ->
            when (effect) {
                PokemonListEffect.FavoriteUpdateFailed -> {
                    snackbarHostState.showSnackbar(favoriteErrorMessage)
                }
            }
        }
    }

    PokemonListScreen(
        uiState = uiState,
        pokemon = pokemon,
        snackbarHostState = snackbarHostState,
        onAction = viewModel::onAction,
        modifier = modifier,
    )
}

@Composable
internal fun PokemonListScreen(
    uiState: PokemonListUiState,
    pokemon: LazyPagingItems<PokemonSummary>,
    snackbarHostState: SnackbarHostState,
    onAction: (PokemonListAction) -> Unit,
    modifier: Modifier = Modifier,
) {
    Scaffold(
        modifier = modifier.fillMaxSize(),
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            PokemonListHeader(
                pokemonCount = uiState.storedPokemonCount,
                isRefreshing = uiState.isRefreshing,
            )
        },
    ) { contentPadding ->
        PokemonListContent(
            uiState = uiState,
            pokemon = pokemon,
            onAction = onAction,
            modifier = Modifier
                .fillMaxSize()
                .padding(contentPadding),
        )
    }
}

@Composable
private fun PokemonListHeader(
    pokemonCount: Int,
    isRefreshing: Boolean,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.surface)
            .padding(
                horizontal = PokedexDesignSystem.spacing.compactScreenMargin,
                vertical = PokedexDesignSystem.spacing.medium,
            ),
        verticalArrangement = Arrangement.spacedBy(PokedexDesignSystem.spacing.small),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primary),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = stringResource(R.string.pokedex_mark),
                    color = MaterialTheme.colorScheme.onPrimary,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                )
            }
            Spacer(Modifier.width(PokedexDesignSystem.spacing.medium))
            Column {
                Text(
                    text = stringResource(R.string.pokemon_list_title),
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.SemiBold,
                )
                Text(
                    text = stringResource(R.string.pokemon_list_subtitle),
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    style = MaterialTheme.typography.bodyMedium,
                )
            }
        }

        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = stringResource(R.string.kanto_index),
                style = MaterialTheme.typography.titleMedium,
            )
            if (pokemonCount > 0) {
                Text(
                    text = pluralStringResource(
                        R.plurals.pokemon_count,
                        pokemonCount,
                        pokemonCount,
                    ),
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    style = MaterialTheme.typography.labelLarge,
                )
            }
        }

        AnimatedVisibility(visible = isRefreshing) {
            Column(verticalArrangement = Arrangement.spacedBy(PokedexDesignSystem.spacing.extraSmall)) {
                LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
                Text(
                    text = stringResource(R.string.catalog_refreshing),
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    style = MaterialTheme.typography.bodySmall,
                )
            }
        }
    }
}

@Composable
private fun PokemonListContent(
    uiState: PokemonListUiState,
    pokemon: LazyPagingItems<PokemonSummary>,
    onAction: (PokemonListAction) -> Unit,
    modifier: Modifier = Modifier,
) {
    val refreshState = pokemon.loadState.refresh

    when {
        uiState.isInitialLoading -> {
            PokedexLoadingState(
                label = stringResource(R.string.catalog_initial_loading),
                modifier = modifier,
            )
        }

        uiState.initialError != null -> {
            PokedexErrorState(
                title = stringResource(R.string.catalog_initial_error_title),
                message = syncFailureMessage(uiState.initialError),
                retryLabel = stringResource(R.string.retry),
                onRetry = { onAction(PokemonListAction.RetrySynchronization) },
                modifier = modifier,
            )
        }

        refreshState is LoadState.Loading && pokemon.itemCount == 0 -> {
            PokedexLoadingState(
                label = stringResource(R.string.local_catalog_loading),
                modifier = modifier,
            )
        }

        refreshState is LoadState.Error && pokemon.itemCount == 0 -> {
            PokedexErrorState(
                title = stringResource(R.string.local_catalog_error_title),
                message = stringResource(R.string.local_catalog_error_message),
                retryLabel = stringResource(R.string.retry),
                onRetry = pokemon::retry,
                modifier = modifier,
            )
        }

        refreshState is LoadState.NotLoading && pokemon.itemCount == 0 -> {
            PokedexEmptyState(
                title = stringResource(R.string.empty_catalog_title),
                message = stringResource(R.string.empty_catalog_message),
                actionLabel = stringResource(R.string.retry),
                onAction = { onAction(PokemonListAction.RetrySynchronization) },
                modifier = modifier,
            )
        }

        else -> {
            PokemonGrid(
                pokemon = pokemon,
                refreshError = uiState.refreshError,
                onRetrySynchronization = {
                    onAction(PokemonListAction.RetrySynchronization)
                },
                onFavoriteChange = { id, isFavorite ->
                    onAction(PokemonListAction.FavoriteChanged(id, isFavorite))
                },
                modifier = modifier,
            )
        }
    }
}

@Composable
private fun PokemonGrid(
    pokemon: LazyPagingItems<PokemonSummary>,
    refreshError: SyncFailure?,
    onRetrySynchronization: () -> Unit,
    onFavoriteChange: (Int, Boolean) -> Unit,
    modifier: Modifier = Modifier,
) {
    LazyVerticalGrid(
        columns = GridCells.Adaptive(minSize = 156.dp),
        modifier = modifier,
        contentPadding = PaddingValues(PokedexDesignSystem.spacing.compactScreenMargin),
        horizontalArrangement = Arrangement.spacedBy(PokedexDesignSystem.spacing.medium),
        verticalArrangement = Arrangement.spacedBy(PokedexDesignSystem.spacing.medium),
    ) {
        if (refreshError != null) {
            item(span = { GridItemSpan(maxLineSpan) }) {
                RefreshWarning(
                    message = syncFailureMessage(refreshError),
                    onRetry = onRetrySynchronization,
                )
            }
        }

        items(
            count = pokemon.itemCount,
            key = pokemon.itemKey(PokemonSummary::id),
            contentType = pokemon.itemContentType { "pokemon" },
        ) { index ->
            pokemon[index]?.let { item ->
                PokemonCard(
                    pokemon = item,
                    onFavoriteChange = { isFavorite ->
                        onFavoriteChange(item.id, isFavorite)
                    },
                )
            }
        }

        when (val appendState = pokemon.loadState.append) {
            is LoadState.Loading -> {
                item(span = { GridItemSpan(maxLineSpan) }) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(PokedexDesignSystem.spacing.medium),
                        contentAlignment = Alignment.Center,
                    ) {
                        CircularProgressIndicator()
                    }
                }
            }

            is LoadState.Error -> {
                item(span = { GridItemSpan(maxLineSpan) }) {
                    PokedexErrorState(
                        title = stringResource(R.string.next_page_error_title),
                        retryLabel = stringResource(R.string.retry),
                        onRetry = pokemon::retry,
                    )
                }
            }

            is LoadState.NotLoading -> Unit
        }
    }
}

@Composable
private fun PokemonCard(
    pokemon: PokemonSummary,
    onFavoriteChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
) {
    val primaryType = pokemon.types.minByOrNull { it.slot }?.name.orEmpty()
    val typeContainer = PokemonTypeTokens.colorsFor(primaryType)?.container
    val containerColor = typeContainer?.copy(alpha = if (isSystemInDarkTheme()) 0.2f else 0.55f)
        ?: MaterialTheme.colorScheme.surfaceContainer

    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = containerColor),
        elevation = CardDefaults.cardElevation(
            defaultElevation = PokedexDesignSystem.elevations.level1,
        ),
    ) {
        Column(
            modifier = Modifier.padding(PokedexDesignSystem.spacing.medium),
            verticalArrangement = Arrangement.spacedBy(PokedexDesignSystem.spacing.small),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top,
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = stringResource(R.string.pokemon_number, pokemon.id),
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        style = MaterialTheme.typography.labelLarge,
                    )
                    Text(
                        text = pokemon.name.toDisplayName(),
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.SemiBold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
                FavoriteButton(
                    isFavorite = pokemon.isFavorite,
                    contentDescription = stringResource(
                        if (pokemon.isFavorite) {
                            R.string.remove_favorite
                        } else {
                            R.string.add_favorite
                        },
                        pokemon.name.toDisplayName(),
                    ),
                    onFavoriteChange = onFavoriteChange,
                )
            }

            PokemonImage(
                imageUrl = pokemon.imageUrl,
                pokemonName = pokemon.name.toDisplayName(),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(132.dp),
            )

            pokemon.types.sortedBy { it.slot }.forEach { type ->
                PokemonTypeBadge(
                    typeName = type.name,
                    label = type.name.toDisplayName(),
                )
            }
        }
    }
}

@Composable
private fun PokemonImage(
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
        modifier = modifier.clip(MaterialTheme.shapes.medium),
        contentScale = ContentScale.Fit,
        loading = {
            PokemonImagePlaceholder(showProgress = true)
        },
        error = {
            PokemonImagePlaceholder(showProgress = false)
        },
    )
}

@Composable
private fun PokemonImagePlaceholder(
    showProgress: Boolean,
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.surfaceContainerLow),
        contentAlignment = Alignment.Center,
    ) {
        if (showProgress) {
            CircularProgressIndicator(modifier = Modifier.size(28.dp))
        } else {
            Text(
                text = stringResource(R.string.image_unavailable),
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                style = MaterialTheme.typography.bodySmall,
            )
        }
    }
}

@Composable
private fun RefreshWarning(
    message: String,
    onRetry: () -> Unit,
) {
    Card(
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.errorContainer,
            contentColor = MaterialTheme.colorScheme.onErrorContainer,
        ),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(
                    start = PokedexDesignSystem.spacing.medium,
                    top = PokedexDesignSystem.spacing.small,
                    bottom = PokedexDesignSystem.spacing.small,
                ),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = message,
                modifier = Modifier.weight(1f),
                style = MaterialTheme.typography.bodyMedium,
            )
            TextButton(onClick = onRetry) {
                Text(stringResource(R.string.retry))
            }
        }
    }
}

@Composable
private fun syncFailureMessage(failure: SyncFailure): String = stringResource(
    when (failure) {
        SyncFailure.NETWORK -> R.string.sync_error_network
        SyncFailure.SERVER -> R.string.sync_error_server
        SyncFailure.INCOMPLETE_RESPONSE -> R.string.sync_error_incomplete
        SyncFailure.STORAGE -> R.string.sync_error_storage
        SyncFailure.UNKNOWN -> R.string.sync_error_unknown
    }
)

private fun String.toDisplayName(): String = replaceFirstChar { character ->
    if (character.isLowerCase()) character.titlecase(Locale.ROOT) else character.toString()
}
