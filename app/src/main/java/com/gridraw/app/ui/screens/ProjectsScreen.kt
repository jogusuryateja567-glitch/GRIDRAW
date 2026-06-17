package com.gridraw.app.ui.screens

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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.gridraw.app.data.models.Project
import com.gridraw.app.ui.theme.*
import com.gridraw.app.viewmodel.ProjectViewModel
import java.text.SimpleDateFormat
import java.util.*

// ──────────────────────────────────────────────────────────────────────────────
// ProjectsScreen — Saved Projects Gallery
// ──────────────────────────────────────────────────────────────────────────────

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProjectsScreen(
    projectViewModel: ProjectViewModel,
    onOpenProject: () -> Unit,
    onNavigateBack: () -> Unit,
) {
    val projects by projectViewModel.projects.collectAsState()

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
            // ── Top Bar ───────────────────────────────────────────────────────
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                IconButton(
                    onClick = onNavigateBack,
                    modifier = Modifier
                        .size(44.dp)
                        .clip(CircleShape)
                        .background(BgCard)
                ) {
                    Icon(Icons.Rounded.ArrowBackIosNew, null, tint = TextMain)
                }

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        "Saved Projects",
                        color = TextMain,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        "${projects.size} project${if (projects.size != 1) "s" else ""}",
                        color = TextMuted,
                        fontSize = 12.sp
                    )
                }
            }

            HorizontalDivider(color = BorderLight, thickness = 1.dp)

            // ── Content ───────────────────────────────────────────────────────
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
                            onOpen = { onOpenProject() },
                            onDelete = { projectViewModel.deleteProject(project) }
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
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Icon(
                Icons.Rounded.FolderOpen,
                null,
                tint = TextDim,
                modifier = Modifier.size(56.dp)
            )
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
) {
    var showMenu by remember { mutableStateOf(false) }
    val dateStr = remember(project.updatedAt) {
        SimpleDateFormat("MMM d, yyyy", Locale.getDefault()).format(Date(project.updatedAt))
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .aspectRatio(0.75f)
            .clip(RoundedCornerShape(18.dp))
            .background(BgCard)
            .border(1.dp, BorderLight, RoundedCornerShape(18.dp))
            .combinedClickable(
                onClick = onOpen,
                onLongClick = { showMenu = true }
            )
    ) {
        // Thumbnail area
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f, fill = true)
                .background(
                    Brush.linearGradient(
                        listOf(AccentBlue.copy(alpha = 0.1f), AccentPurple.copy(alpha = 0.05f))
                    )
                ),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                Icons.Rounded.GridOn,
                null,
                tint = AccentBlue.copy(alpha = 0.3f),
                modifier = Modifier.size(40.dp)
            )
        }

        // Info footer
        Column(
            modifier = Modifier
                .align(Alignment.BottomStart)
                .fillMaxWidth()
                .background(BgPanel.copy(alpha = 0.95f))
                .padding(12.dp)
        ) {
            Text(
                project.name,
                color = TextMain,
                fontSize = 13.sp,
                fontWeight = FontWeight.SemiBold,
                maxLines = 1
            )
            Text(
                dateStr,
                color = TextDim,
                fontSize = 11.sp
            )
        }

        // Dropdown menu
        DropdownMenu(
            expanded = showMenu,
            onDismissRequest = { showMenu = false },
            modifier = Modifier.background(BgCard)
        ) {
            DropdownMenuItem(
                text = { Text("Open", color = TextMain) },
                leadingIcon = { Icon(Icons.Rounded.FolderOpen, null, tint = AccentBlue) },
                onClick = { showMenu = false; onOpen() }
            )
            DropdownMenuItem(
                text = { Text("Delete", color = Danger) },
                leadingIcon = { Icon(Icons.Rounded.Delete, null, tint = Danger) },
                onClick = { showMenu = false; onDelete() }
            )
        }
    }
}
