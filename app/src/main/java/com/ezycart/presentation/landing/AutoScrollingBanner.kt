package com.ezycart.presentation.landing

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource

@Composable
fun AutoScrollingBanner(
    banners: List<Int>,
    currentIndex: Int,
    modifier: Modifier = Modifier
) {
    val pagerState = rememberPagerState(pageCount = { banners.size })

    // Sync Pager with ViewModel state
    LaunchedEffect(currentIndex) {
        pagerState.animateScrollToPage(currentIndex)
    }

    HorizontalPager(
        state = pagerState,
        modifier = modifier.fillMaxSize(),
        userScrollEnabled = true
    ) { page ->
        Image(
            painter = painterResource(id = banners[page]),
            contentDescription = null,
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.FillBounds // Fill the large 22-inch screen
        )
    }
}