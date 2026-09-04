package com.example.data.repository

import com.example.data.local.AppDatabase
import com.example.data.local.entities.ProjectEntity
import kotlinx.coroutines.flow.Flow
import java.util.UUID

class ProjectRepository(private val database: AppDatabase) {
    private val projectDao = database.projectDao()

    fun getUserProjects(userId: String): Flow<List<ProjectEntity>> {
        return projectDao.getProjectsByUserId(userId)
    }

    fun getDraftProjects(userId: String): Flow<List<ProjectEntity>> {
        return projectDao.getDraftProjectsByUserId(userId)
    }

    fun getProjectById(projectId: String): Flow<ProjectEntity?> {
        return projectDao.getProjectById(projectId)
    }

    suspend fun getProjectByIdSync(projectId: String): ProjectEntity? {
        return projectDao.getProjectByIdSync(projectId)
    }

    suspend fun createProject(
        userId: String,
        name: String,
        genre: String = "Afrobeats",
        mood: String = "Inspiring",
        lyrics: String = "",
        masterAudioUrl: String = "",
        vocalTrackUrl: String = "",
        instrumentalTrackUrl: String = "",
        videoUrl: String = "",
        status: String = "draft"
    ): ProjectEntity {
        val project = ProjectEntity(
            id = "proj_" + UUID.randomUUID().toString().take(8),
            userId = userId,
            name = name.ifBlank { "Untitled Project" },
            coverImageUrl = "",
            genre = genre,
            mood = mood,
            lyrics = lyrics,
            vocalTrackUrl = vocalTrackUrl,
            instrumentalTrackUrl = instrumentalTrackUrl,
            masterAudioUrl = masterAudioUrl,
            videoUrl = videoUrl,
            status = status,
            createdAt = System.currentTimeMillis(),
            updatedAt = System.currentTimeMillis()
        )
        projectDao.insertOrUpdate(project)
        return project
    }

    suspend fun saveProject(project: ProjectEntity) {
        projectDao.insertOrUpdate(project.copy(updatedAt = System.currentTimeMillis()))
    }

    suspend fun renameProject(projectId: String, newName: String) {
        projectDao.renameProject(projectId, newName.trim())
    }

    suspend fun duplicateProject(project: ProjectEntity): ProjectEntity {
        val copy = project.copy(
            id = "proj_" + UUID.randomUUID().toString().take(8),
            name = "${project.name} (Copy)",
            createdAt = System.currentTimeMillis(),
            updatedAt = System.currentTimeMillis()
        )
        projectDao.insertOrUpdate(copy)
        return copy
    }

    suspend fun deleteProject(projectId: String) {
        projectDao.deleteProject(projectId)
    }

    suspend fun updateStatus(projectId: String, status: String) {
        projectDao.updateStatus(projectId, status)
    }
}
