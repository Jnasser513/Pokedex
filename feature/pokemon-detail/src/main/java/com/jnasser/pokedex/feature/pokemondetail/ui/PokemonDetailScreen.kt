package com.jnasser.pokedex.feature.pokemondetail.ui

import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
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
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
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
import com.jnasser.pokedex.core.model.PokemonAbility
import com.jnasser.pokedex.core.model.PokemonDetail
import com.jnasser.pokedex.core.model.PokemonStat
import com.jnasser.pokedex.feature.pokemondetail.PokemonDetailAction
import com.jnasser.pokedex.feature.pokemondetail.PokemonDetailEffect
import com.jnasser.pokedex.feature.pokemondetail.PokemonDetailUiState
import com.jnasser.pokedex.feature.pokemondetail.PokemonDetailViewModel
import com.jnasser.pokedex.feature.pokemondetail.R
import java.util.Locale
import kotlinx.coroutines.flow.collectLatest
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.parameter.parametersOf

@Composable
fun PokemonDetailRoute(
    pokemonId: Int,
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: PokemonDetailViewModel = koinViewModel(
        parameters = { parametersOf(pokemonId) },
    ),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    val favoriteErrorMessage = stringResource(R.string.favorite_update_error)

    LaunchedEffect(viewModel, snackbarHostState) {
        viewModel.effects.collectLatest { effect ->
            when (effect) {
                PokemonDetailEffect.FavoriteUpdateFailed -> {
                    snackbarHostState.showSnackbar(favoriteErrorMessage)
                }
            }
        }
    }

    PokemonDetailScreen(
        uiState = uiState,
        snackbarHostState = snackbarHostState,
        onBackClick = onBackClick,
        onAction = viewModel::onAction,
        modifier = modifier,
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun PokemonDetailScreen(
    uiState: PokemonDetailUiState,
    snackbarHostState: SnackbarHostState,
    onBackClick: () -> Unit,
    onAction: (PokemonDetailAction) -> Unit,
    modifier: Modifier = Modifier,
) {
    val pokemon = (uiState as? PokemonDetailUiState.Content)?.pokemon

    Scaffold(
        modifier = modifier.fillMaxSize(),
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        PokedexBrandMark(modifier = Modifier.size(28.dp))
                        Spacer(Modifier.width(PokedexDesignSystem.spacing.small))
                        Text(
                            text = stringResource(R.string.pokemon_detail_title),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(R.string.navigate_back),
                        )
                    }
                },
                actions = {
                    if (pokemon != null) {
                        val displayName = pokemon.name.toDisplayName()
                        FavoriteButton(
                            isFavorite = pokemon.isFavorite,
                            contentDescription = stringResource(
                                if (pokemon.isFavorite) {
                                    R.string.remove_favorite
                                } else {
                                    R.string.add_favorite
                                },
                                displayName,
                            ),
                            onFavoriteChange = { isFavorite ->
                                onAction(PokemonDetailAction.FavoriteChanged(isFavorite))
                            },
                        )
                    }
                },
            )
        },
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
                label = "pokemonDetailState",
            ) { state ->
                when (state) {
                    PokemonDetailUiState.Loading -> {
                        PokedexLoadingState(
                            label = stringResource(R.string.detail_loading),
                            modifier = Modifier.fillMaxSize(),
                        )
                    }

                    PokemonDetailUiState.NotFound -> {
                        PokedexEmptyState(
                            title = stringResource(R.string.detail_not_found_title),
                            message = stringResource(R.string.detail_not_found_message),
                            modifier = Modifier.fillMaxSize(),
                        )
                    }

                    PokemonDetailUiState.InvalidPokemonId -> {
                        PokedexEmptyState(
                            title = stringResource(R.string.invalid_pokemon_title),
                            message = stringResource(R.string.invalid_pokemon_message),
                            modifier = Modifier.fillMaxSize(),
                        )
                    }

                    PokemonDetailUiState.Error -> {
                        PokedexErrorState(
                            title = stringResource(R.string.detail_error_title),
                            message = stringResource(R.string.detail_error_message),
                            retryLabel = stringResource(R.string.retry),
                            onRetry = { onAction(PokemonDetailAction.Retry) },
                            modifier = Modifier.fillMaxSize(),
                        )
                    }

                    is PokemonDetailUiState.Content -> {
                        PokemonDetailContent(
                            pokemon = state.pokemon,
                            modifier = Modifier.fillMaxSize(),
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun PokemonDetailContent(
    pokemon: PokemonDetail,
    modifier: Modifier = Modifier,
) {
    LazyColumn(
        modifier = modifier,
        contentPadding = androidx.compose.foundation.layout.PaddingValues(
            start = PokedexDesignSystem.spacing.compactScreenMargin,
            top = PokedexDesignSystem.spacing.small,
            end = PokedexDesignSystem.spacing.compactScreenMargin,
            bottom = PokedexDesignSystem.spacing.large,
        ),
        verticalArrangement = Arrangement.spacedBy(PokedexDesignSystem.spacing.medium),
    ) {
        item(key = "hero") {
            PokemonHero(pokemon)
        }
        item(key = "measurements") {
            Measurements(pokemon)
        }
        item(key = "abilities") {
            AbilitiesCard(pokemon.abilities)
        }
        item(key = "stats") {
            StatsCard(pokemon.stats)
        }
        item(key = "navigation-bar-inset") {
            Spacer(Modifier.navigationBarsPadding())
        }
    }
}

@Composable
private fun PokemonHero(pokemon: PokemonDetail) {
    val primaryType = pokemon.types.minByOrNull { it.slot }?.name.orEmpty()
    val typeColor = PokemonTypeTokens.colorsFor(primaryType)?.container
    val surfaceColor = MaterialTheme.colorScheme.surfaceContainerHigh
    val heroColor = typeColor.heroBackgroundColor(
        fallback = MaterialTheme.colorScheme.primaryContainer,
        darkTheme = isSystemInDarkTheme(),
        darkSurface = surfaceColor,
    )
    val displayName = pokemon.name.toDisplayName()

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.extraLarge,
        colors = CardDefaults.cardColors(containerColor = Color.Transparent),
        elevation = CardDefaults.cardElevation(PokedexDesignSystem.elevations.level1),
    ) {
        Column(
            modifier = Modifier
                .background(
                    Brush.verticalGradient(
                        listOf(heroColor, MaterialTheme.colorScheme.surfaceContainerLow)
                    )
                )
                .padding(PokedexDesignSystem.spacing.medium),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(PokedexDesignSystem.spacing.small),
        ) {
            Text(
                text = stringResource(R.string.pokemon_number, pokemon.id),
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                style = MaterialTheme.typography.labelLarge,
            )
            PokemonArtwork(
                imageUrl = pokemon.imageUrl,
                pokemonName = displayName,
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(4f / 3f),
            )
            Text(
                text = displayName,
                style = MaterialTheme.typography.headlineLarge,
                fontWeight = FontWeight.SemiBold,
            )
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(PokedexDesignSystem.spacing.small),
                verticalArrangement = Arrangement.spacedBy(PokedexDesignSystem.spacing.small),
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
private fun PokemonArtwork(
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
            CircularProgressIndicator(modifier = Modifier.size(32.dp))
        } else {
            Text(
                text = stringResource(R.string.image_unavailable),
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                style = MaterialTheme.typography.bodyMedium,
            )
        }
    }
}

@Composable
private fun Measurements(pokemon: PokemonDetail) {
    val height = stringResource(R.string.height_value, pokemon.heightDecimetres / 10f)
    val weight = stringResource(R.string.weight_value, pokemon.weightHectograms / 10f)

    if (LocalDensity.current.fontScale >= 1.3f) {
        Column(verticalArrangement = Arrangement.spacedBy(PokedexDesignSystem.spacing.small)) {
            MetricCard(
                label = stringResource(R.string.height),
                value = height,
                modifier = Modifier.fillMaxWidth(),
            )
            MetricCard(
                label = stringResource(R.string.weight),
                value = weight,
                modifier = Modifier.fillMaxWidth(),
            )
        }
    } else {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(PokedexDesignSystem.spacing.small),
        ) {
            MetricCard(
                label = stringResource(R.string.height),
                value = height,
                modifier = Modifier.weight(1f),
            )
            MetricCard(
                label = stringResource(R.string.weight),
                value = weight,
                modifier = Modifier.weight(1f),
            )
        }
    }
}

@Composable
private fun MetricCard(
    label: String,
    value: String,
    modifier: Modifier = Modifier,
) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainer,
        ),
        shape = MaterialTheme.shapes.large,
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(PokedexDesignSystem.spacing.medium),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(PokedexDesignSystem.spacing.extraSmall),
        ) {
            Text(
                text = label,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                style = MaterialTheme.typography.labelLarge,
            )
            Text(
                text = value,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.SemiBold,
            )
        }
    }
}

@Composable
private fun AbilitiesCard(abilities: List<PokemonAbility>) {
    DetailSection(title = stringResource(R.string.abilities)) {
        abilities.sortedBy { it.slot }.forEach { ability ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = PokedexDesignSystem.spacing.small),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = ability.name.toDisplayName(),
                    style = MaterialTheme.typography.bodyLarge,
                )
                if (ability.isHidden) {
                    Text(
                        text = stringResource(R.string.hidden_ability),
                        color = MaterialTheme.colorScheme.primary,
                        style = MaterialTheme.typography.labelMedium,
                    )
                }
            }
        }
    }
}

@Composable
private fun StatsCard(stats: List<PokemonStat>) {
    DetailSection(title = stringResource(R.string.base_stats)) {
        stats.forEach { stat ->
            StatRow(stat)
        }
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = PokedexDesignSystem.spacing.small),
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Text(
                text = stringResource(R.string.stat_total),
                style = MaterialTheme.typography.titleMedium,
            )
            Text(
                text = stats.sumOf(PokemonStat::baseValue).toString(),
                color = MaterialTheme.colorScheme.primary,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
            )
        }
    }
}

@Composable
private fun DetailSection(
    title: String,
    content: @Composable () -> Unit,
) {
    Card(
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerLow,
        ),
        shape = MaterialTheme.shapes.large,
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(PokedexDesignSystem.spacing.medium),
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.SemiBold,
            )
            Spacer(Modifier.height(PokedexDesignSystem.spacing.small))
            content()
        }
    }
}

@Composable
private fun StatRow(stat: PokemonStat) {
    val targetProgress = (stat.baseValue / MAX_STAT_VALUE).coerceIn(0f, 1f)
    val animatedProgress by animateFloatAsState(
        targetValue = targetProgress,
        label = "${stat.name}Progress",
    )
    val indicatorColor = when {
        stat.baseValue < 60 -> MaterialTheme.colorScheme.primary
        stat.baseValue < 100 -> MaterialTheme.colorScheme.secondary
        else -> MaterialTheme.colorScheme.tertiary
    }

    val statLabel = stat.localizedName()
    val usesStackedLayout = LocalDensity.current.fontScale >= 1.3f

    if (usesStackedLayout) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = PokedexDesignSystem.spacing.small),
            verticalArrangement = Arrangement.spacedBy(PokedexDesignSystem.spacing.extraSmall),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                StatLabel(statLabel)
                StatValue(stat.baseValue)
            }
            StatProgress(progress = animatedProgress, color = indicatorColor)
        }
    } else {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = PokedexDesignSystem.spacing.extraSmall),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            StatLabel(statLabel, modifier = Modifier.width(96.dp))
            StatValue(stat.baseValue, modifier = Modifier.width(40.dp))
            StatProgress(
                progress = animatedProgress,
                color = indicatorColor,
                modifier = Modifier.weight(1f),
            )
        }
    }
}

@Composable
private fun StatLabel(
    label: String,
    modifier: Modifier = Modifier,
) {
    Text(
        text = label,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        style = MaterialTheme.typography.bodyMedium,
        modifier = modifier,
    )
}

@Composable
private fun StatValue(
    value: Int,
    modifier: Modifier = Modifier,
) {
    Text(
        text = value.toString(),
        style = MaterialTheme.typography.titleSmall,
        fontWeight = FontWeight.SemiBold,
        modifier = modifier,
    )
}

@Composable
private fun StatProgress(
    progress: Float,
    color: Color,
    modifier: Modifier = Modifier,
) {
    LinearProgressIndicator(
        progress = { progress },
        modifier = modifier
            .fillMaxWidth()
            .height(8.dp)
            .clip(MaterialTheme.shapes.extraLarge)
            .clearAndSetSemantics { },
        color = color,
        trackColor = MaterialTheme.colorScheme.surfaceContainerHighest,
    )
}

@Composable
private fun PokemonStat.localizedName(): String = when (name) {
    "hp" -> stringResource(R.string.stat_hp)
    "attack" -> stringResource(R.string.stat_attack)
    "defense" -> stringResource(R.string.stat_defense)
    "special-attack" -> stringResource(R.string.stat_special_attack)
    "special-defense" -> stringResource(R.string.stat_special_defense)
    "speed" -> stringResource(R.string.stat_speed)
    else -> name.toDisplayName()
}

private fun String.toDisplayName(): String =
    split('-').joinToString(" ") { part ->
        part.replaceFirstChar { character ->
            if (character.isLowerCase()) character.titlecase(Locale.ROOT) else character.toString()
        }
    }

private fun Color?.heroBackgroundColor(
    fallback: Color,
    darkTheme: Boolean,
    darkSurface: Color,
): Color {
    val color = this ?: fallback
    return if (darkTheme) lerp(darkSurface, color, 0.2f) else color
}

private const val MAX_STAT_VALUE = 255f
