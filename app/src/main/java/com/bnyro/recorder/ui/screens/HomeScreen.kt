package com.bnyro.recorder.ui.screens

import android.view.SoundEffectConstants
import androidx.activity.ComponentActivity
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Sort
import androidx.compose.material.icons.filled.VideoLibrary
import androidx.compose.material.icons.filled.Videocam
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.viewmodel.compose.viewModel
import com.bnyro.recorder.R
import com.bnyro.recorder.enums.RecorderState
import com.bnyro.recorder.enums.RecorderType
import com.bnyro.recorder.enums.SortOrder
import com.bnyro.recorder.ui.Destination
import com.bnyro.recorder.ui.common.ClickableIcon
import com.bnyro.recorder.ui.components.PlayerView
import com.bnyro.recorder.ui.models.PlayerModel
import com.bnyro.recorder.ui.models.RecorderModel
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun HomeScreen(
    initialRecorder: RecorderType,
    onNavigate: (Destination) -> Unit,
    recorderModel: RecorderModel = viewModel(LocalContext.current as ComponentActivity)
) {
    val playerModel: PlayerModel = viewModel(factory = PlayerModel.Factory)
    val pagerState = rememberPagerState { 2 }
    val scope = rememberCoroutineScope()
    val view = LocalView.current
    val showVideoModeInitially = recorderModel.recordScreenMode
    val isRecorderView = pagerState.currentPage == 0
    val isRecordingsView = pagerState.currentPage == 1
    var selectedSortOrder by remember {
        mutableStateOf(SortOrder.ALPHABETIC)
    }
    Scaffold(modifier = Modifier.fillMaxSize(), topBar = {
        TopAppBar(title = { Text(stringResource(R.string.app_name)) }, actions = {
            if (isRecorderView) {
                ClickableIcon(
                    imageVector = Icons.Default.Settings,
                    contentDescription = stringResource(R.string.settings)
                ) {
                    onNavigate(Destination.Settings)
                }
                if (recorderModel.recorderState == RecorderState.IDLE) {
                    ClickableIcon(
                        imageVector = if (recorderModel.recordScreenMode) Icons.Default.Mic else Icons.Default.Videocam,
                        contentDescription = stringResource(R.string.record_screen)
                    ) {
                        recorderModel.recordScreenMode = !recorderModel.recordScreenMode
                    }
                }
            }
            if (isRecordingsView) {
                var showDropDown by remember {
                    mutableStateOf(false)
                }
                ClickableIcon(
                    imageVector = Icons.Default.Sort,
                    contentDescription = stringResource(R.string.sort)
                ) {
                    showDropDown = true
                }

                val sortOptions = listOf(
                    SortOrder.DEFAULT to R.string.default_sort,
                    SortOrder.ALPHABETIC to R.string.alphabetic,
                    SortOrder.ALPHABETIC_REV to R.string.alphabetic_rev,
                    SortOrder.SIZE to R.string.size,
                    SortOrder.SIZE_REV to R.string.size_rev
                )
                DropdownMenu(showDropDown, { showDropDown = false }) {
                    sortOptions.forEach { sortOrder ->
                        DropdownMenuItem(
                            text = {
                                Text(stringResource(sortOrder.second))
                            },
                            onClick = {
                                selectedSortOrder = sortOrder.first
                                playerModel.sortItems(sortOrder.first)
                                showDropDown = false
                            }
                        )
                    }
                }
            }
        })
    }, bottomBar = {
        Column {
            AnimatedVisibility(recorderModel.recorderState == RecorderState.IDLE) {
                NavigationBar {
                    NavigationBarItem(
                        icon = {
                            Icon(
                                imageVector = if (recorderModel.recordScreenMode) Icons.Default.Videocam else Icons.Default.Mic,
                                contentDescription = stringResource(
                                    id = if (recorderModel.recordScreenMode) R.string.record_screen else R.string.record_sound
                                )
                            )
                        },
                        label = { Text(stringResource(if (recorderModel.recordScreenMode) R.string.record_screen else R.string.record_sound)) },
                        selected = (isRecorderView),
                        onClick = {
                            view.playSoundEffect(SoundEffectConstants.CLICK)
                            scope.launch {
                                pagerState.animateScrollToPage(0)
                            }
                        }
                    )
                    NavigationBarItem(
                        icon = {
                            Icon(
                                imageVector = Icons.Default.VideoLibrary,
                                contentDescription = stringResource(
                                    id = R.string.recordings
                                )
                            )
                        },
                        label = { Text(stringResource(R.string.recordings)) },
                        selected = (isRecordingsView),
                        onClick = {
                            view.playSoundEffect(SoundEffectConstants.CLICK)
                            scope.launch {
                                pagerState.animateScrollToPage(1)
                            }
                        }
                    )
                }
            }
        }
    }) { paddingValues ->
        Column(
            Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            HorizontalPager(
                state = pagerState,
                modifier = Modifier.fillMaxSize()
            ) { index ->
                if (index == 0) {
                    RecorderView(initialRecorder = initialRecorder)
                } else if (index == 1) {
                    PlayerView(showVideoModeInitially, recorderModel.wasRecordingCreated)
                }
            }
        }
    }
}
