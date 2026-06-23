package com.example.playx.ui.screen

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.media3.common.Player
import com.example.playx.R
import com.example.playx.ui.viewmodel.ConfigViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ConfigScreen(
    viewModel: ConfigViewModel,
    onBack: () -> Unit
) {
    val imageUri by viewModel.imageUri.collectAsState()
    val videoUri by viewModel.videoUri.collectAsState()
    val noCoverMode by viewModel.noCoverMode.collectAsState()
    val repeatMode by viewModel.repeatMode.collectAsState()
    val adminPassword by viewModel.adminPassword.collectAsState()
    val context = LocalContext.current

    var showPasswordEdit by remember { mutableStateOf(false) }
    var newPassword by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }

    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri: Uri? ->
        uri?.let { viewModel.updateImageUri(it, context) }
    }

    val videoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri: Uri? ->
        uri?.let { viewModel.updateVideoUri(it, context) }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("后台配置", fontWeight = FontWeight.Medium) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(imageVector = Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = Color.White)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Black, titleContentColor = Color.White)
            )
        },
        containerColor = Color.Black
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp)
                .verticalScroll(rememberScrollState())
        ) {
            ConfigCard(
                title = stringResource(R.string.pick_image),
                currentValue = imageUri,
                placeholder = stringResource(R.string.no_image_selected),
                onPick = { imagePickerLauncher.launch(arrayOf("image/*")) },
                onClear = { viewModel.clearImageUri() }
            )

            Spacer(modifier = Modifier.height(16.dp))

            ConfigCard(
                title = stringResource(R.string.pick_video),
                currentValue = videoUri,
                placeholder = stringResource(R.string.no_video_selected),
                onPick = { videoPickerLauncher.launch(arrayOf("video/*")) },
                onClear = { viewModel.clearVideoUri() }
            )

            Spacer(modifier = Modifier.height(24.dp))

            // No Cover Mode
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF1A1A1A))
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(text = stringResource(R.string.no_cover_mode), style = MaterialTheme.typography.titleMedium, color = Color.White)
                        Text(text = "启用后将跳过封面图，4秒后自动播放", style = MaterialTheme.typography.bodySmall, color = Color.Gray)
                    }
                    Switch(
                        checked = noCoverMode,
                        onCheckedChange = { viewModel.setNoCoverMode(it) },
                        colors = SwitchDefaults.colors(checkedThumbColor = Color(0xFF00BCD4), checkedTrackColor = Color(0xFF00BCD4).copy(alpha = 0.5f))
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Repeat Mode Selection
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF1A1A1A))
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(text = stringResource(R.string.loop_mode), style = MaterialTheme.typography.titleMedium, color = Color.White)
                    Spacer(modifier = Modifier.height(8.dp))

                    RepeatModeOption(
                        label = stringResource(R.string.loop_none),
                        selected = repeatMode == Player.REPEAT_MODE_OFF,
                        enabled = !noCoverMode,
                        onClick = { viewModel.setRepeatMode(Player.REPEAT_MODE_OFF) }
                    )
                    RepeatModeOption(
                        label = stringResource(R.string.loop_single),
                        selected = repeatMode == Player.REPEAT_MODE_ONE,
                        enabled = true,
                        onClick = { viewModel.setRepeatMode(Player.REPEAT_MODE_ONE) }
                    )
                    RepeatModeOption(
                        label = stringResource(R.string.loop_all),
                        selected = repeatMode == Player.REPEAT_MODE_ALL,
                        enabled = true,
                        onClick = { viewModel.setRepeatMode(Player.REPEAT_MODE_ALL) }
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Admin Password Edit
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF1A1A1A))
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(text = "管理密码", style = MaterialTheme.typography.titleMedium, color = Color.White, modifier = Modifier.weight(1f))
                        TextButton(onClick = { showPasswordEdit = !showPasswordEdit }) {
                            Text(if (showPasswordEdit) "取消" else "修改", color = Color(0xFF00BCD4))
                        }
                    }
                    
                    if (!showPasswordEdit) {
                        Text(text = "当前密码: " + adminPassword.replace(Regex("."), "*"), color = Color.Gray, fontSize = 12.sp)
                    }

                    AnimatedVisibility(visible = showPasswordEdit) {
                        Column {
                            OutlinedTextField(
                                value = newPassword,
                                onValueChange = { if (it.length <= 8) newPassword = it },
                                label = { Text("新密码 (数字)") },
                                visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                trailingIcon = {
                                    IconButton(onClick = { passwordVisible = !passwordVisible }) {
                                        Icon(imageVector = if (passwordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff, contentDescription = null)
                                    }
                                },
                                modifier = Modifier.fillMaxWidth(),
                                colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = Color(0xFF00BCD4), focusedLabelColor = Color(0xFF00BCD4))
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Button(
                                onClick = {
                                    if (newPassword.isNotEmpty()) {
                                        viewModel.updateAdminPassword(newPassword)
                                        showPasswordEdit = false
                                        newPassword = ""
                                    }
                                },
                                modifier = Modifier.fillMaxWidth(),
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF00BCD4))
                            ) {
                                Text("保存新密码")
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun RepeatModeOption(
    label: String,
    selected: Boolean,
    enabled: Boolean,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 2.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        RadioButton(
            selected = selected,
            onClick = if (enabled) onClick else null,
            enabled = enabled,
            colors = RadioButtonDefaults.colors(selectedColor = Color(0xFF00BCD4), unselectedColor = Color.Gray, disabledSelectedColor = Color(0xFF00BCD4).copy(alpha = 0.5f))
        )
        Text(
            text = label + if (!enabled) " (无封面模式下禁用)" else "",
            style = MaterialTheme.typography.bodyMedium,
            color = if (enabled) Color.White else Color.Gray,
            modifier = Modifier.padding(start = 8.dp)
        )
    }
}

@Composable
private fun ConfigCard(
    title: String,
    currentValue: String?,
    placeholder: String,
    onPick: () -> Unit,
    onClear: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF1A1A1A))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(text = title, style = MaterialTheme.typography.titleMedium, color = Color.White)
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = currentValue ?: placeholder,
                style = MaterialTheme.typography.bodySmall,
                color = if (currentValue != null) Color.White.copy(alpha = 0.6f) else Color.White.copy(alpha = 0.3f),
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
            Spacer(modifier = Modifier.height(12.dp))
            Row {
                Button(onClick = onPick, colors = ButtonDefaults.buttonColors(containerColor = Color.White, contentColor = Color.Black), contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp)) {
                    Text("选择文件", fontSize = 13.sp)
                }
                Spacer(modifier = Modifier.width(12.dp))
                if (currentValue != null) {
                    OutlinedButton(onClick = onClear, border = androidx.compose.foundation.BorderStroke(1.dp, Color.Gray.copy(alpha = 0.5f)), colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.White.copy(alpha = 0.6f))) {
                        Text("重置", fontSize = 13.sp)
                    }
                }
            }
        }
    }
}
