package com.jnasser.pokedex.core.designsystem.component

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.jnasser.pokedex.core.designsystem.theme.PokemonTypeTokens

@Composable
fun PokemonTypeBadge(
    typeName: String,
    modifier: Modifier = Modifier,
    label: String = typeName,
    leadingIcon: (@Composable () -> Unit)? = null,
) {
    val definedColors = PokemonTypeTokens.colorsFor(typeName)
    val containerColor = definedColors?.container
        ?: MaterialTheme.colorScheme.surfaceContainerHighest
    val contentColor = definedColors?.content
        ?: MaterialTheme.colorScheme.onSurfaceVariant

    Surface(
        modifier = modifier.heightIn(min = 32.dp),
        shape = MaterialTheme.shapes.extraLarge,
        color = containerColor,
        contentColor = contentColor,
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            leadingIcon?.invoke()
            Text(
                text = label,
                style = MaterialTheme.typography.labelMedium,
            )
        }
    }
}
