package com.jnasser.pokedex.navigation

import androidx.compose.ui.unit.dp
import org.junit.Assert.assertEquals
import org.junit.Test

class MainNavigationLayoutTest {
    @Test
    fun `compact width uses bottom navigation`() {
        assertEquals(
            MainNavigationLayout.BottomBar,
            mainNavigationLayout(width = 599.dp),
        )
    }

    @Test
    fun `medium and expanded widths use navigation rail`() {
        assertEquals(
            MainNavigationLayout.Rail,
            mainNavigationLayout(width = 600.dp),
        )
        assertEquals(
            MainNavigationLayout.Rail,
            mainNavigationLayout(width = 1_200.dp),
        )
    }
}
