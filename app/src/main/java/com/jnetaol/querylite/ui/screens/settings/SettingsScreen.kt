package com.jnetaol.querylite.ui.screens.settings

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.jnetaol.querylite.ui.components.*
import com.jnetaol.querylite.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    onNavigateBack: () -> Unit
) {
    val context = LocalContext.current
    val versionName = try {
        context.packageManager.getPackageInfo(context.packageName, 0).versionName ?: "1.0.0"
    } catch (_: Exception) { "1.0.0" }

    Scaffold(
        snackbarHost = {
            SnackbarHost(remember { SnackbarHostState() })
        },
        topBar = {
            TopAppBar(
                title = { Text("Settings", color = TextPrimary, fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, "Back", tint = AccentPrimary)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = DarkSurface)
            )
        },
        containerColor = DarkBackground
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            item { SectionHeader(title = "About") }

            item {
                NeonCard {
                    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
                        Icon(
                            Icons.Default.Storage,
                            null,
                            tint = AccentPrimary,
                            modifier = Modifier.size(48.dp)
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Text("QueryLite", color = AccentPrimary, fontWeight = FontWeight.Bold, fontSize = 22.sp)
                        Text("SQLite Database Browser", color = TextSecondary, fontSize = 14.sp)
                        Spacer(modifier = Modifier.height(4.dp))
                        Text("Version $versionName", color = TextDisabled, fontSize = 12.sp)

                        Spacer(modifier = Modifier.height(12.dp))

                        Text(
                            "Made By jnetai.com",
                            color = AccentPrimary,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Medium,
                            modifier = Modifier.clickable {
                                val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://jnetai.com"))
                                context.startActivity(intent)
                            }
                        )
                    }
                }
            }

            item { SectionHeader(title = "Actions") }

            item {
                NeonCard {
                    SettingRow(
                        icon = Icons.Default.Update,
                        title = "Check For Updates",
                        subtitle = "Version $versionName",
                        onClick = {
                            val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://jnetai.com/querylite"))
                            context.startActivity(intent)
                        }
                    )
                }
            }

            item {
                NeonCard {
                    SettingRow(
                        icon = Icons.Default.Share,
                        title = "Share QueryLite",
                        subtitle = "Share this app with others",
                        onClick = {
                            val shareIntent = Intent(Intent.ACTION_SEND).apply {
                                type = "text/plain"
                                putExtra(Intent.EXTRA_TEXT, "Check out QueryLite - a powerful SQLite Database Browser for Android!\nhttps://jnetai.com/querylite")
                            }
                            context.startActivity(Intent.createChooser(shareIntent, "Share QueryLite"))
                        }
                    )
                }
            }

            item {
                NeonCard {
                    SettingRow(
                        icon = Icons.Default.Feedback,
                        title = "Feedback",
                        subtitle = "Send your suggestions and bug reports",
                        onClick = {
                            val intent = Intent(Intent.ACTION_SENDTO).apply {
                                data = Uri.parse("mailto:hello@jnetai.com")
                                putExtra(Intent.EXTRA_SUBJECT, "QueryLite Feedback")
                            }
                            context.startActivity(intent)
                        }
                    )
                }
            }

            item {
                NeonCard {
                    SettingRow(
                        icon = Icons.Default.Info,
                        title = "Open Source Licenses",
                        subtitle = "View third-party library licenses",
                        onClick = {
                            val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://jnetai.com/querylite/licenses"))
                            context.startActivity(intent)
                        }
                    )
                }
            }

            item {
                NeonCard {
                    SettingRow(
                        icon = Icons.Default.Description,
                        title = "Privacy Policy",
                        subtitle = "How we handle your data",
                        onClick = {
                            val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://jnetai.com/privacy"))
                            context.startActivity(intent)
                        }
                    )
                }
            }

            // Log info
            item { SectionHeader(title = "Debug Info") }
            item {
                NeonCard {
                    Text("Debug logger initialized", color = TextSecondary, fontSize = 12.sp)
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        "Version: $versionName",
                        color = TextDisabled,
                        fontSize = 11.sp
                    )
                }
            }

            item {
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    "QueryLite © ${java.util.Calendar.getInstance().get(java.util.Calendar.YEAR)} jnetai.com",
                    color = TextDisabled,
                    fontSize = 11.sp,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )
            }

            item { Spacer(modifier = Modifier.height(80.dp)) }
        }
    }
}

@Composable
private fun SettingRow(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    subtitle: String,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(icon, null, tint = AccentPrimary, modifier = Modifier.size(24.dp))
        Spacer(modifier = Modifier.width(14.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(title, color = TextPrimary, fontWeight = FontWeight.Medium, fontSize = 14.sp)
            Text(subtitle, color = TextSecondary, fontSize = 12.sp)
        }
        Icon(Icons.Default.ChevronRight, null, tint = TextDisabled, modifier = Modifier.size(20.dp))
    }
}
