package com.jnasser.pokedex.feature.pokemonlist.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
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
import androidx.compose.ui.text.input.ImeAction
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
import com.jnasser.pokedex.core.designsystem.component.PokedexBrandMark
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
    onPokemonClick: (Int) -> Unit,
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
        onPokemonClick = onPokemonClick,
        modifier = modifier,
    )
}

@Composable
internal fun PokemonListScreen(
    uiState: PokemonListUiState,
    pokemon: LazyPagingItems<PokemonSummary>,
    snackbarHostState: SnackbarHostState,
    onAction: (PokemonListAction) -> Unit,
    onPokemonClick: (Int) -> Unit,
    modifier: Modifier = Modifier,
) {
    Scaffold(
        modifier = modifier.fillMaxSize(),
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            PokemonListHeader(
                searchQuery = uiState.searchQuery,
                onSearchQueryChange = { query ->
                    onAction(PokemonListAction.SearchQueryChanged(query))
                },
                onClearSearch = { onAction(PokemonListAction.ClearSearch) },
            )
        },
    ) { contentPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(contentPadding),
            contentAlignment = Alignment.TopCenter,
        ) {
            PokemonListContent(
                uiState = uiState,
                pokemon = pokemon,
                onAction = onAction,
                onPokemonClick = onPokemonClick,
                modifier = Modifier
                    .widthIn(max = 1200.dp)
                    .fillMaxHeight()
                    .fillMaxWidth(),
            )
        }
    }
}

@Composable
private fun PokemonListHeader(
    searchQuery: String,
    onSearchQueryChange: (String) -> Unit,
    onClearSearch: () -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.surface)
            .statusBarsPadding()
            .padding(
                horizontal = PokedexDesignSystem.spacing.compactScreenMargin,
                vertical = PokedexDesignSystem.spacing.small,
            ),
        verticalArrangement = Arrangement.spacedBy(PokedexDesignSystem.spacing.small),
    ) {
        Row(
            modifier = Modifier.heightIn(min = 64.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            PokedexBrandMark()
            Spacer(Modifier.width(12.dp))
            Column {
                Text(
                    text = stringResource(R.string.pokemon_list_title),
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Medium,
                )
                Text(
                    text = stringResource(R.string.pokemon_list_subtitle),
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    style = MaterialTheme.typography.labelMedium,
                )
            }
        }

        OutlinedTextField(
            value = searchQuery,
            onValueChange = onSearchQueryChange,
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = 56.dp),
            placeholder = { Text(stringResource(R.string.search_supporting_text)) },
            leadingIcon = {
                Icon(
                    imageVector = Icons.Filled.Search,
                    contentDescription = null,
                )
            },
            trailingIcon = if (searchQuery.isNotEmpty()) {
                {
                    IconButton(onClick = onClearSearch) {
                        Icon(
                            imageVector = Icons.Filled.Close,
                            contentDescription = stringResource(R.string.clear_search),
                        )
                    }
                }
            } else {
                null
            },
            singleLine = true,
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
            shape = MaterialTheme.shapes.extraLarge,
            colors = OutlinedTextFieldDefaults.colors(
                focusedContainerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
                unfocusedContainerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
                focusedBorderColor = MaterialTheme.colorScheme.primary,
                unfocusedBorderColor = Color.Transparent,
            ),
        )
    }
}

@Composable
private fun PokemonListContent(
    uiState: PokemonListUiState,
    pokemon: LazyPagingItems<PokemonSummary>,
    onAction: (PokemonListAction) -> Unit,
    onPokemonClick: (Int) -> Unit,
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
            if (uiState.searchQuery.isNotBlank()) {
                PokedexEmptyState(
                    title = stringResource(R.string.empty_search_title),
                    message = stringResource(
                        R.string.empty_search_message,
                        uiState.searchQuery.trim(),
                    ),
                    actionLabel = stringResource(R.string.clear_search),
                    onAction = { onAction(PokemonListAction.ClearSearch) },
                    modifier = modifier,
                )
            } else {
                PokedexEmptyState(
                    title = stringResource(R.string.empty_catalog_title),
                    message = stringResource(R.string.empty_catalog_message),
                    actionLabel = stringResource(R.string.retry),
                    onAction = { onAction(PokemonListAction.RetrySynchronization) },
                    modifier = modifier,
                )
            }
        }

        else -> {
            PokemonGrid(
                pokemon = pokemon,
                onFavoriteChange = { id, isFavorite ->
                    onAction(PokemonListAction.FavoriteChanged(id, isFavorite))
                },
                onPokemonClick = onPokemonClick,
                modifier = modifier,
            )
        }
    }
}

@Composable
private fun PokemonGrid(
    pokemon: LazyPagingItems<PokemonSummary>,
    onFavoriteChange: (Int, Boolean) -> Unit,
    onPokemonClick: (Int) -> Unit,
    modifier: Modifier = Modifier,
) {
    val minimumCardWidth = if (LocalDensity.current.fontScale >= 1.3f) 220.dp else 160.dp

    LazyVerticalGrid(
        columns = GridCells.Adaptive(minSize = minimumCardWidth),
        modifier = modifier,
        contentPadding = PaddingValues(
            start = PokedexDesignSystem.spacing.compactScreenMargin,
            top = PokedexDesignSystem.spacing.small,
            end = PokedexDesignSystem.spacing.compactScreenMargin,
            bottom = PokedexDesignSystem.spacing.medium,
        ),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        items(
            count = pokemon.itemCount,
            key = pokemon.itemKey(PokemonSummary::id),
            contentType = pokemon.itemContentType { "pokemon" },
        ) { index ->
            pokemon[index]?.let { item ->
                PokemonCard(
                    pokemon = item,
                    onClick = { onPokemonClick(item.id) },
                    onFavoriteChange = { isFavorite ->
                        onFavoriteChange(item.id, isFavorite)
                    },
                    modifier = Modifier.animateItem(),
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
    onClick: () -> Unit,
    onFavoriteChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
) {
    val primaryType = pokemon.types.minByOrNull { it.slot }?.name.orEmpty()
    val typeContainer = PokemonTypeTokens.colorsFor(primaryType)?.container
    val gradientEnd = MaterialTheme.colorScheme.surfaceContainerHigh
    val gradientStart = typeContainer.cardBackgroundColor(
        fallback = MaterialTheme.colorScheme.surfaceContainer,
        darkTheme = isSystemInDarkTheme(),
        darkSurface = gradientEnd,
    )

    Card(
        onClick = onClick,
        modifier = modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.extraLarge,
        colors = CardDefaults.cardColors(
            containerColor = Color.Transparent,
            contentColor = MaterialTheme.colorScheme.onSurface,
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = PokedexDesignSystem.elevations.level1,
        ),
    ) {
        Column(
            modifier = Modifier
                .background(
                    Brush.linearGradient(
                        colors = listOf(gradientStart, gradientEnd),
                    )
                )
                .padding(14.dp),
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
                        maxLines = 2,
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
                    .aspectRatio(1f),
            )

            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(PokedexDesignSystem.spacing.small),
                verticalArrangement = Arrangement.spacedBy(PokedexDesignSystem.spacing.extraSmall),
            ) {
                pokemon.types.sortedBy { it.slot }.forEach { type ->
                    PokemonTypeBadge(
                        typeName = type.name,
                        label = type.name.toDisplayName(),
                    )
                }
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

private fun Color?.cardBackgroundColor(
    fallback: Color,
    darkTheme: Boolean,
    darkSurface: Color,
): Color {
    val typeColor = this ?: fallback
    return if (darkTheme) lerp(darkSurface, typeColor, 0.2f) else typeColor
}
