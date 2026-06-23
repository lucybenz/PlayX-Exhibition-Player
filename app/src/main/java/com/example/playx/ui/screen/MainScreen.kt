package com.example.playx.ui.screen

import android.content.res.Configuration
import android.net.Uri
import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.media3.common.C
import androidx.media3.ui.PlayerView
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.playx.R
import com.example.playx.ui.viewmodel.MainViewModel
import com.example.playx.ui.viewmodel.ScreenMode
import kotlinx.coroutines.delay

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(
    viewModel: MainViewModel,
    onNavigateToConfig: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current
    var showSettingsButton by remember { mutableStateOf(false) }
    var showPasswordDialog by remember { mutableStateOf(false) }
    var password by remember { mutableStateOf("") }
    var passwordError by remember { mutableStateOf(false) }

    // Auto-start timer logic: re-triggers when back to COVER mode
    LaunchedEffect(uiState.noCoverMode, uiState.isConfigured, viewModel.screenMode) {
        if (uiState.noCoverMode && uiState.isConfigured && viewModel.screenMode == ScreenMode.COVER) {
            delay(4000)
            viewModel.onAutoStartTriggered()
        }
    }

    LaunchedEffect(showSettingsButton) {
        if (showSettingsButton) {
            delay(4000)
            showSettingsButton = false
        }
    }

    BackHandler(enabled = viewModel.screenMode == ScreenMode.VIDEO) {
        viewModel.onBackPressed()
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
            .pointerInput(Unit) {
                detectTapGestures(
                    onTap = {
                        if (viewModel.screenMode == ScreenMode.COVER) {
                            viewModel.onCoverTapped()
                        }
                    },
                    onDoubleTap = {
                        showPasswordDialog = true
                    }
                )
            }
    ) {
        when (viewModel.screenMode) {
            ScreenMode.COVER -> {
                if (!uiState.noCoverMode && uiState.isConfigured) {
                    AsyncImage(
                        model = ImageRequest.Builder(context)
                            .data(Uri.parse(uiState.imageUri ?: ""))
                            .crossfade(true)
                            .build(),
                        contentDescription = "Cover image",
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )

                    val alpha by rememberInfiniteTransition(label = "breathing").animateFloat(
                        initialValue = 0.2f,
                        targetValue = 0.8f,
                        animationSpec = infiniteRepeatable(
                            animation = tween(durationMillis = 2000, easing = LinearEasing),
                            repeatMode = RepeatMode.Reverse
                        ),
                        label = "alpha"
                    )
                    Text(
                        text = "点击播放 • 双击设置",
                        color = Color.White.copy(alpha = alpha),
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Light,
                        modifier = Modifier
                            .align(Alignment.BottomCenter)
                            .padding(bottom = 48.dp)
                    )
                } else if (!uiState.noCoverMode) {
                    Text(
                        text = "未配置资源\n双击屏幕进入配置",
                        color = Color.White.copy(alpha = 0.4f),
                        fontSize = 14.sp,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.align(Alignment.Center)
                    )
                }
            }

            ScreenMode.VIDEO -> {
                val player = remember { viewModel.getOrCreatePlayer() }
                if (player != null) {
                    AndroidView(
                        factory = { ctx ->
                            PlayerView(ctx).apply {
                                useController = false
                                try {
                                    val method = javaClass.getMethod("setSurfaceType", Int::class.javaPrimitiveType)
                                    method.invoke(this, 2)
                                } catch (e: Exception) {}
                                this.player = player
                            }
                        },
                        modifier = Modifier.fillMaxSize()
                    )

                    VideoControls(
                        viewModel = viewModel,
                        player = player,
                        onOpenSettings = { showPasswordDialog = true }
                    )
                }
            }
        }

        // Hidden settings trigger area
        Box(
            modifier = Modifier
                .size(80.dp)
                .align(Alignment.TopStart)
                .clickable(
                    indication = null,
                    interactionSource = remember { MutableInteractionSource() }
                ) {
                    showSettingsButton = true
                }
        )

        if (viewModel.screenMode == ScreenMode.COVER) {
            AnimatedVisibility(
                visible = showSettingsButton || !uiState.isConfigured,
                enter = fadeIn(),
                exit = fadeOut(),
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .padding(16.dp)
            ) {
                IconButton(onClick = { showPasswordDialog = true }) {
                    Icon(
                        imageVector = Icons.Default.Settings,
                        contentDescription = "Settings",
                        tint = Color.White.copy(alpha = 0.4f),
                        modifier = Modifier.size(24.dp)
                    )
                }
            }
        }

        if (showPasswordDialog) {
            AlertDialog(
                onDismissRequest = { 
                    showPasswordDialog = false
                    password = ""
                    passwordError = false
                },
                title = { Text("配置校验", fontSize = 18.sp, fontWeight = FontWeight.Medium) },
                text = {
                    Column {
                        OutlinedTextField(
                            value = password,
                            onValueChange = { 
                                password = it
                                passwordError = false
                            },
                            label = { Text("请输入授权密码") },
                            isError = passwordError,
                            singleLine = true,
                            visualTransformation = PasswordVisualTransformation(),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = Color(0xFF00BCD4),
                                focusedLabelColor = Color(0xFF00BCD4)
                            ),
                            modifier = Modifier.fillMaxWidth()
                        )
                        if (passwordError) {
                            Text(
                                text = "授权码不正确",
                                color = Color.Red.copy(alpha = 0.8f),
                                fontSize = 12.sp,
                                modifier = Modifier.padding(top = 4.dp)
                            )
                        }
                    }
                },
    confirmButton = {
        val adminPassword by viewModel.adminPassword.collectAsState("2026")
        TextButton(
            onClick = {
                if (password == adminPassword) {
                    showPasswordDialog = false
                    password = ""
                    onNavigateToConfig()
                } else {
                    passwordError = true
                }
            }
        ) {
            Text("进入", color = Color(0xFF00BCD4))
        }
    },
                dismissButton = {
                    TextButton(onClick = { showPasswordDialog = false; password = ""; passwordError = false }) {
                        Text("取消", color = Color.Gray)
                    }
                }
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun VideoControls(
    viewModel: MainViewModel,
    player: androidx.media3.exoplayer.ExoPlayer,
    onOpenSettings: () -> Unit
) {
    var isPlaying by remember { mutableStateOf(player.isPlaying) }
    var currentPosition by remember { mutableLongStateOf(player.currentPosition) }
    var duration by remember { mutableLongStateOf(player.duration) }
    var volume by remember { mutableFloatStateOf(player.volume) }
    var controlsVisible by remember { mutableStateOf(true) }
    var isUserSeeking by remember { mutableStateOf(false) }
    val isLandscape = LocalConfiguration.current.orientation == Configuration.ORIENTATION_LANDSCAPE

    LaunchedEffect(Unit) {
        while (true) {
            isPlaying = player.isPlaying
            if (!isUserSeeking) {
                currentPosition = player.currentPosition
            }
            duration = player.duration
            volume = player.volume
            delay(200)
        }
    }

    LaunchedEffect(controlsVisible, isPlaying) {
        if (controlsVisible && isPlaying) {
            delay(4000)
            controlsVisible = false
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .pointerInput(Unit) {
                detectTapGestures(
                    onTap = { controlsVisible = !controlsVisible },
                    onDoubleTap = { onOpenSettings() }
                )
            }
    ) {
        AnimatedVisibility(
            visible = controlsVisible,
            enter = fadeIn(),
            exit = fadeOut(),
            modifier = Modifier.align(Alignment.BottomCenter)
        ) {
            if (isLandscape) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color.Black.copy(alpha = 0.4f))
                        .padding(horizontal = 16.dp, vertical = 4.dp)
                ) {
                    IconButton(onClick = { viewModel.onBackPressed() }, modifier = Modifier.size(32.dp)) {
                        Image(painter = painterResource(R.drawable.back), contentDescription = null, modifier = Modifier.size(18.dp))
                    }

                    IconButton(onClick = { viewModel.togglePlayPause() }, modifier = Modifier.size(32.dp)) {
                        Icon(imageVector = if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow, contentDescription = null, tint = Color.White, modifier = Modifier.size(20.dp))
                    }

                    Spacer(modifier = Modifier.width(8.dp))
                    
                    Icon(imageVector = if (volume > 0f) Icons.Default.VolumeUp else Icons.Default.VolumeOff, contentDescription = null, tint = Color.White.copy(alpha = 0.7f), modifier = Modifier.size(14.dp))
                    
                    ElegantSlider(
                        value = volume,
                        onValueChange = { player.volume = it; volume = it },
                        modifier = Modifier.width(70.dp)
                    )

                    Spacer(modifier = Modifier.width(12.dp))

                    ElegantSlider(
                        value = if (duration > 0) currentPosition.toFloat() / duration else 0f,
                        onValueChange = { fraction ->
                            isUserSeeking = true
                            currentPosition = (fraction * duration).toLong()
                        },
                        onValueChangeFinished = {
                            viewModel.seekTo(currentPosition)
                            isUserSeeking = false
                        },
                        modifier = Modifier.weight(1f)
                    )

                    Text(
                        text = "${formatTime(currentPosition)} / ${formatTime(duration)}",
                        color = Color.White.copy(alpha = 0.8f),
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Light,
                        modifier = Modifier.padding(start = 12.dp)
                    )
                }
            } else {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color.Black.copy(alpha = 0.4f))
                        .padding(horizontal = 16.dp, vertical = 8.dp)
                ) {
                    ElegantSlider(
                        value = if (duration > 0) currentPosition.toFloat() / duration else 0f,
                        onValueChange = { fraction ->
                            isUserSeeking = true
                            currentPosition = (fraction * duration).toLong()
                        },
                        onValueChangeFinished = {
                            viewModel.seekTo(currentPosition)
                            isUserSeeking = false
                        },
                        modifier = Modifier.fillMaxWidth()
                    )

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth().padding(top = 4.dp)
                    ) {
                        IconButton(onClick = { viewModel.onBackPressed() }, modifier = Modifier.size(32.dp)) {
                            Image(painter = painterResource(R.drawable.back), contentDescription = null, modifier = Modifier.size(18.dp))
                        }

                        IconButton(onClick = { viewModel.togglePlayPause() }, modifier = Modifier.size(32.dp)) {
                            Icon(imageVector = if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow, contentDescription = null, tint = Color.White, modifier = Modifier.size(22.dp))
                        }

                        Spacer(modifier = Modifier.width(12.dp))

                        Icon(imageVector = if (volume > 0f) Icons.Default.VolumeUp else Icons.Default.VolumeOff, contentDescription = null, tint = Color.White.copy(alpha = 0.7f), modifier = Modifier.size(16.dp))

                        ElegantSlider(
                            value = volume,
                            onValueChange = { player.volume = it; volume = it },
                            modifier = Modifier.width(80.dp)
                        )

                        Spacer(modifier = Modifier.weight(1f))

                        Text(
                            text = "${formatTime(currentPosition)} / ${formatTime(duration)}",
                            color = Color.White.copy(alpha = 0.8f),
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Light
                        )
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ElegantSlider(
    value: Float,
    onValueChange: (Float) -> Unit,
    modifier: Modifier = Modifier,
    onValueChangeFinished: (() -> Unit)? = null
) {
    Slider(
        value = value,
        onValueChange = onValueChange,
        onValueChangeFinished = onValueChangeFinished,
        modifier = modifier.height(20.dp),
        colors = SliderDefaults.colors(
            thumbColor = Color(0xFF00BCD4),
            activeTrackColor = Color(0xFF00BCD4),
            inactiveTrackColor = Color.White.copy(alpha = 0.12f),
        ),
        thumb = {
            Box(
                Modifier
                    .size(6.dp)
                    .background(Color(0xFF00BCD4), shape = CircleShape)
            )
        },
        track = { sliderState ->
            SliderDefaults.Track(
                sliderState = sliderState,
                modifier = Modifier.height(1.5.dp),
                thumbTrackGapSize = 0.dp,
                colors = SliderDefaults.colors(
                    activeTrackColor = Color(0xFF00BCD4),
                    inactiveTrackColor = Color.White.copy(alpha = 0.12f),
                )
            )
        }
    )
}

private fun formatTime(ms: Long): String {
    if (ms <= 0 || ms == C.TIME_UNSET) return "00:00"
    val totalSeconds = ms / 1000
    val minutes = totalSeconds / 60
    val seconds = totalSeconds % 60
    return "%02d:%02d".format(minutes, seconds)
}
