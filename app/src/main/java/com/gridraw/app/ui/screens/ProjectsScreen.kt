package com.gridraw.app.ui.screens

import android.net.Uri
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.SubcomposeAsyncImage
import com.gridraw.app.data.models.Project
import com.gridraw.app.ui.theme.*
import androidx.compose.ui.platform.LocalContext
import com.gridraw.app.viewmodel.EditorViewModel
import com.gridraw.app.viewmodel.ProjectViewModel
import java.text.SimpleDateFormat
import java.util.*

// ─────────────────────────────────────────────────────────────────────────────
// ProjectsScreen
// ─────────────────────────────────────────────────────────────────────────────

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProjectsScreen(
    projectViewModel: ProjectViewModel,
    editorViewModel: EditorViewModel,
    onOpenProject: () -> Unit,
    onNavigateBack: () -> Unit,
) {
    val projects by projectViewModel.projects.collectAsState()
    val context = LocalContext.current

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(BgRoot)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
        ) {
            // Top Bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .clip(CircleShape)
                        .background(BgCard)
                        .border(1.dp, BorderLight, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    IconButton(onClick = onNavigateBack, modifier = Modifier.fillMaxSize()) {
                        Icon(Icons.Rounded.ArrowBackIosNew, null, tint = TextMain, modifier = Modifier.size(18.dp))
                    }
                }

                Column(modifier = Modifier.weight(1f)) {
                    Text("Saved Projects", color = TextMain, fontSize = 20.sp, fontWeight = FontWeight.Bold)
                    Text(
                        "${projects.size} project${if (projects.size != 1) "s" else ""}",
                        color = TextMuted, fontSize = 12.sp
                    )
                }
            }

            HorizontalDivider(color = BorderLight, thickness = 0.5.dp)

            if (projects.isEmpty()) {
                EmptyProjectsState()
            } else {
                LazyVerticalGrid(
                    columns = GridCells.Fixed(2),
                    modifier = Modifier
                        .fillMaxSize()
                        .navigationBarsPadding(),
                    contentPadding = PaddingValues(16.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(projects, key = { it.id }) { project ->
                        ProjectCard(
                            project = project,
                            onOpen = {
                                editorViewModel.loadProject(context, project)
                                onOpenProject()
                            },
                            onDelete = { projectViewModel.deleteProject(project) },
                            onRename = { projectViewModel.renameProject(project, it) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun EmptyProjectsState() {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier.padding(40.dp)
        ) {
            Icon(Icons.Rounded.FolderOpen, null, tint = TextDim, modifier = Modifier.size(52.dp))
            Spacer(Modifier.height(4.dp))
            Text("No Saved Projects", color = TextMuted, fontSize = 16.sp, fontWeight = FontWeight.SemiBold)
            Text("Your saved work will appear here", color = TextDim, fontSize = 13.sp)
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun ProjectCard(
    project: Project,
    onOpen: () -> Unit,
    onDelete: () -> Unit,
    onRename: (String) -> Unit,
) {
    var showMenu by remember { mutableStateOf(false) }
    var showRenameDialog by remember { mutableStateOf(false) }

    val dateStr = remember(project.updatedAt) {
        SimpleDateFormat("MMM d, yyyy", Locale.getDefault()).format(Date(project.updatedAt))
    }

    // Build descriptive metadata subtitle
    val metaStr = remember(project.paperSize, project.orientation) {
        buildString {
            append(project.paperSize)
            append(" · ")
            append(project.orientation.lowercase().replaceFirstChar { it.uppercase() })
        }
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(180.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(BgCard)
            .border(1.dp, BorderLight, RoundedCornerShape(16.dp))
            .combinedClickable(
                onClick = onOpen,
                onLongClick = { showMenu = true }
            )
    ) {
        // Thumbnail area — show actual image if available
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight()
                .background(Color.White.copy(alpha = 0.03f)),
            contentAlignment = Alignment.Center
        ) {
            if (!project.imageUri.isNullOrBlank()) {
                // Load real image thumbnail via Coil
                SubcomposeAsyncImage(
                    model = Uri.parse(project.imageUri),
                    contentDescription = "Project thumbnail",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .fillMaxSize()
                        .clip(RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp)),
                    loading = {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            CircularProgressIndicator(
                                color = Color.White.copy(alpha = 0.3f),
                                strokeWidth = 2.dp,
                                modifier = Modifier.size(24.dp)
                            )
                        }
                    },
                    error = {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                Icons.Rounded.GridOn,
                                null,
                                tint = Color.White.copy(alpha = 0.12f),
                                modifier = Modifier.size(36.dp)
                            )
                        }
                    }
                )
            } else {
                // No image URI — show placeholder icon
                Icon(
                    Icons.Rounded.GridOn,
                    null,
                    tint = Color.White.copy(alpha = 0.12f),
                    modifier = Modifier.size(36.dp)
                )
            }
        }

        // Footer — fixed height with metadata info
        Column(
            modifier = Modifier
                .align(Alignment.BottomStart)
                .fillMaxWidth()
                .background(BgPanel.copy(alpha = 0.97f))
                .border(
                    width = 0.5.dp,
                    color = BorderLight,
                    shape = RoundedCornerShape(bottomStart = 16.dp, bottomEnd = 16.dp)
                )
                .padding(horizontal = 12.dp, vertical = 10.dp)
        ) {
            Text(
                project.name,
                color = TextMain,
                fontSize = 13.sp,
                fontWeight = FontWeight.SemiBold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Spacer(Modifier.height(2.dp))
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Text(metaStr, color = TextDim, fontSize = 10.sp)
                Box(
                    modifier = Modifier
                        .size(3.dp)
                        .clip(CircleShape)
                        .background(TextDim)
                )
                Text(dateStr, color = TextDim, fontSize = 10.sp)
            }
        }

        // Dropdown menu
        DropdownMenu(
            expanded = showMenu,
            onDismissRequest = { showMenu = false },
            modifier = Modifier.background(BgPanel)
        ) {
            DropdownMenuItem(
                text = { Text("Open", color = TextMain, fontSize = 14.sp) },
                leadingIcon = { Icon(Icons.Rounded.FolderOpen, null, tint = Color.White, modifier = Modifier.size(18.dp)) },
                onClick = { showMenu = false; onOpen() }
            )
            DropdownMenuItem(
                text = { Text("Rename", color = TextMain, fontSize = 14.sp) },
                leadingIcon = { Icon(Icons.Rounded.Edit, null, tint = Color.White, modifier = Modifier.size(18.dp)) },
                onClick = { showMenu = false; showRenameDialog = true }
            )
            HorizontalDivider(color = BorderLight, thickness = 0.5.dp)
            DropdownMenuItem(
                text = { Text("Delete", color = Danger, fontSize = 14.sp) },
                leadingIcon = { Icon(Icons.Rounded.Delete, null, tint = Danger, modifier = Modifier.size(18.dp)) },
                onClick = { showMenu = false; onDelete() }
            )
        }
    }

    // Rename Dialog
    if (showRenameDialog) {
        RenameDialog(
            currentName = project.name,
            onConfirm = { onRename(it); showRenameDialog = false },
            onDismiss = { showRenameDialog = false }
        )
    }
}

@Composable
private fun RenameDialog(
    currentName: String,
    onConfirm: (String) -> Unit,
    onDismiss: () -> Unit
) {
    var name by remember { mutableStateOf(currentName) }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = BgPanel,
        title = { Text("Rename Project", color = TextMain, fontWeight = FontWeight.SemiBold) },
        text = {
            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                label = { Text("Project name") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Color.White.copy(alpha = 0.6f),
                    unfocusedBorderColor = BorderLight,
                    focusedTextColor = TextMain,
                    unfocusedTextColor = TextMain,
                    cursorColor = Color.White,
                    focusedLabelColor = Color.White.copy(0.6f),
                    unfocusedLabelColor = TextMuted
                )
            )
        },
        confirmButton = {
            TextButton(
                onClick = { if (name.isNotBlank()) onConfirm(name.trim()) },
                enabled = name.isNotBlank()
            ) {
                Text("Rename", color = Color.White, fontWeight = FontWeight.SemiBold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel", color = TextMuted)
            }
        }
    )
}