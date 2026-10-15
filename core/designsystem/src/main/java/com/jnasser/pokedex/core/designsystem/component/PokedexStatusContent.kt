package com.jnasser.pokedex.core.designsystem.component

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.sizeIn
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import com.jnasser.pokedex.core.designsystem.theme.PokedexDesignSystem

@Composable
fun PokedexLoadingState(
    label: String,
    modifier: Modifier = Modifier,
) {
    StatusContent(modifier = modifier) {
        CircularProgressIndicator()
        Text(
            text = label,
            style = MaterialTheme.typography.bodyLarge,
            textAlign = TextAlign.Center,
        )
    }
}

@Composable
fun PokedexEmptyState(
    title: String,
    modifier: Modifier = Modifier,
    message: String? = null,
    actionLabel: String? = null,
    onAction: (() -> Unit)? = null,
) {
    StatusContent(modifier = modifier) {
        StatusMessage(title = title, message = message)
        if (actionLabel != null && onAction != null) {
            Button(onClick = onAction) {
                Text(actionLabel)
            }
        }
    }
}

@Composable
fun PokedexErrorState(
    title: String,
    retryLabel: String,
    onRetry: () -> Unit,
    modifier: Modifier = Modifier,
    message: String? = null,
) {
    StatusContent(modifier = modifier) {
        StatusMessage(title = title, message = message)
        Button(onClick = onRetry) {
            Text(retryLabel)
        }
    }
}

@Composable
private fun StatusContent(
    modifier: Modifier,
    content: @Composable () -> Unit,
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(PokedexDesignSystem.spacing.medium)
            .sizeIn(minHeight = PokedexDesignSystem.spacing.minimumTouchTarget),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(PokedexDesignSystem.spacing.medium),
    ) {
        content()
    }
}

@Composable
private fun StatusMessage(
    title: String,
    message: String?,
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(PokedexDesignSystem.spacing.small),
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleLarge,
            textAlign = TextAlign.Center,
        )
        if (message != null) {
            Text(
                text = message,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                style = MaterialTheme.typography.bodyMedium,
                textAlign = TextAlign.Center,
            )
        }
    }
}
