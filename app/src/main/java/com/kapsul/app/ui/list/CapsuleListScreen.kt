@file:OptIn(
    androidx.compose.material3.ExperimentalMaterial3Api::class,
    androidx.compose.material3.ExperimentalMaterial3ExpressiveApi::class,
)

package com.kapsul.app.ui.list

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.LockOpen
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ButtonGroup
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.FloatingActionButtonMenu
import androidx.compose.material3.FloatingActionButtonMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.kapsul.app.R
import com.kapsul.app.data.Capsule
import com.kapsul.app.ui.components.capsuleContainer
import com.kapsul.app.ui.components.stampBackground
import com.kapsul.app.ui.newcapsule.NewCapsuleDialog
import com.kapsul.app.util.remainingText
import com.kapsul.app.viewmodel.CapsuleFilter
import com.kapsul.app.viewmodel.KapsulViewModel
import kotlinx.coroutines.delay

@Composable
fun CapsuleListScreen(
    vm: KapsulViewModel,
    onOpen: (String) -> Unit,
) {
    val capsules by vm.visible.collectAsState()
    val total by vm.capsules.collectAsState()
    val filter by vm.filter.collectAsState()
    val saving by vm.saving.collectAsState()
    var showNew by remember { mutableStateOf(false) }
    var showAbout by remember { mutableStateOf(false) }
    var fabExpanded by remember { mutableStateOf(false) }

    // Geri sayımın canlı olması için 30 saniyede bir tazele.
    var now by remember { mutableStateOf(System.currentTimeMillis()) }
    LaunchedEffect(Unit) {
        while (true) {
            now = System.currentTimeMillis()
            delay(30_000)
        }
    }

    Scaffold(
        floatingActionButton = {
            FloatingActionButtonMenu(
                expanded = fabExpanded,
                button = {
                    FloatingActionButton(onClick = { fabExpanded = !fabExpanded }) {
                        Icon(
                            imageVector = if (fabExpanded) Icons.Filled.Close else Icons.Filled.Add,
                            contentDescription = null,
                        )
                    }
                },
            ) {
                FloatingActionButtonMenuItem(
                    onClick = {
                        fabExpanded = false
                        showNew = true
                    },
                    text = { Text(stringResource(R.string.new_capsule)) },
                    icon = { Icon(Icons.Filled.Edit, contentDescription = null) },
                )
                FloatingActionButtonMenuItem(
                    onClick = {
                        fabExpanded = false
                        showAbout = true
                    },
                    text = { Text(stringResource(R.string.nav_about)) },
                    icon = { Icon(Icons.Filled.Info, contentDescription = null) },
                )
            }
        },
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize(),
        ) {
            Column(modifier = Modifier.padding(start = 20.dp, end = 20.dp, top = 12.dp)) {
                Text(
                    text = stringResource(R.string.app_name),
                    style = MaterialTheme.typography.displaySmall,
                )
                Text(
                    text = stringResource(R.string.tagline),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            // ButtonGroup içerik lambda'sı composable değildir; etiketler önceden çözülür.
            val labelAll = stringResource(R.string.filter_all)
            val labelSealed = stringResource(R.string.filter_sealed)
            val labelOpened = stringResource(R.string.filter_opened)

            ButtonGroup(
                overflowIndicator = {},
                modifier = Modifier.padding(horizontal = 20.dp),
            ) {
                toggleableItem(
                    checked = filter == CapsuleFilter.ALL,
                    label = labelAll,
                    onCheckedChange = { vm.setFilter(CapsuleFilter.ALL) },
                )
                toggleableItem(
                    checked = filter == CapsuleFilter.SEALED,
                    label = labelSealed,
                    onCheckedChange = { vm.setFilter(CapsuleFilter.SEALED) },
                )
                toggleableItem(
                    checked = filter == CapsuleFilter.OPENED,
                    label = labelOpened,
                    onCheckedChange = { vm.setFilter(CapsuleFilter.OPENED) },
                )
            }

            if (capsules.isEmpty()) {
                EmptyState(modifier = Modifier.weight(1f))
            } else {
                LazyVerticalGrid(
                    columns = GridCells.Adaptive(minSize = 156.dp),
                    contentPadding = PaddingValues(
                        start = 16.dp,
                        end = 16.dp,
                        top = 14.dp,
                        bottom = 96.dp,
                    ),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.fillMaxSize(),
                ) {
                    items(capsules, key = { it.id }) { capsule ->
                        CapsuleCard(
                            capsule = capsule,
                            now = now,
                            onClick = { onOpen(capsule.id) },
                            modifier = Modifier.animateItem(),
                        )
                    }
                }
            }
        }
    }

    if (showNew) {
        NewCapsuleDialog(
            saving = saving,
            onDismiss = { if (!saving) showNew = false },
            onCreate = { capsule ->
                vm.create(capsule)
                showNew = false
            },
        )
    }

    if (showAbout) {
        AboutDialog(
            count = total.size,
            onDismiss = { showAbout = false },
        )
    }
}

@Composable
private fun CapsuleCard(
    capsule: Capsule,
    now: Long,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Card(
        onClick = onClick,
        modifier = modifier.height(158.dp),
        shape = MaterialTheme.shapes.extraLarge,
        colors = CardDefaults.cardColors(
            containerColor = capsuleContainer(capsule.colorIndex),
        ),
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(14.dp),
            verticalArrangement = Arrangement.SpaceBetween,
        ) {
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(CircleShape)
                    .background(stampBackground),
                contentAlignment = Alignment.Center,
            ) {
                Text(text = capsule.emoji, fontSize = 22.sp)
            }

            Column {
                Text(
                    text = capsule.title,
                    style = MaterialTheme.typography.titleMedium,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                )
                Spacer(modifier = Modifier.height(4.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = if (capsule.isSealed) Icons.Filled.Lock else Icons.Filled.LockOpen,
                        contentDescription = null,
                        modifier = Modifier.size(14.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = if (capsule.isSealed) {
                            remainingText(capsule.unlockAt, now)
                        } else {
                            stringResource(R.string.filter_opened)
                        },
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
            }
        }
    }
}

@Composable
private fun EmptyState(modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Text(text = "📭", fontSize = 56.sp)
        Spacer(modifier = Modifier.height(12.dp))
        Text(
            text = stringResource(R.string.empty_title),
            style = MaterialTheme.typography.titleLarge,
            textAlign = TextAlign.Center,
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = stringResource(R.string.empty_body),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
        )
    }
}

@Composable
private fun AboutDialog(count: Int, onDismiss: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.app_name)) },
        text = {
            Column {
                Text(
                    text = stringResource(R.string.about_body),
                    style = MaterialTheme.typography.bodyMedium,
                )
                Spacer(modifier = Modifier.height(10.dp))
                Text(
                    text = stringResource(R.string.about_count, count),
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.ok))
            }
        },
    )
}
