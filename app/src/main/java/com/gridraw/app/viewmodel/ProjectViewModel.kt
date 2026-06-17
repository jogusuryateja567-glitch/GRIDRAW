package com.gridraw.app.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.gridraw.app.GridRawApplication
import com.gridraw.app.data.models.Project
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class ProjectViewModel(application: Application) : AndroidViewModel(application) {

    private val db  = (application as GridRawApplication).database
    private val dao = db.projectDao()

    val projects: StateFlow<List<Project>> = dao.getAllProjects()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    fun deleteProject(project: Project) {
        viewModelScope.launch { dao.deleteProject(project) }
    }

    fun renameProject(project: Project, newName: String) {
        viewModelScope.launch {
            dao.updateProject(project.copy(name = newName, updatedAt = System.currentTimeMillis()))
        }
    }
}
