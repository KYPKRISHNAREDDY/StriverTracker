package com.dsatracker.domain.usecase

import android.content.Context
import android.content.Intent
import androidx.core.content.FileProvider
import com.dsatracker.data.preferences.PreferencesManager
import com.dsatracker.data.repository.DSARepository
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext
import java.io.File
import java.text.SimpleDateFormat
import java.util.*
import javax.inject.Inject

/**
 * Use case to export all problem notes to a text file
 * Shares the file using Android's share intent
 */
class ExportNotesUseCase @Inject constructor(
    @ApplicationContext private val context: Context,
    private val repository: DSARepository,
    private val preferencesManager: PreferencesManager
) {
    suspend operator fun invoke(): Result<Intent> = withContext(Dispatchers.IO) {
        try {
            // Get current user ID
            val userId = preferencesManager.currentUserId.first()
                ?: return@withContext Result.failure(Exception("User not found"))

            // Get all notes
            val notes = repository.getAllNotes(userId).first()

            if (notes.isEmpty()) {
                return@withContext Result.failure(Exception("No notes to export"))
            }

            // Build notes text
            val notesText = buildString {
                appendLine("=" .repeat(50))
                appendLine("DSA Tracker - My Problem Notes")
                appendLine("Exported on: ${getCurrentTimestamp()}")
                appendLine("=" .repeat(50))
                appendLine()

                notes.forEach { note ->
                    // Get problem details
                    val problem = repository.getProblem(note.problemId)

                    if (problem != null) {
                        appendLine("Problem: ${problem.title}")
                        appendLine("Platform: ${problem.platform.name}")
                        appendLine("Difficulty: ${problem.difficulty.name}")
                        appendLine("URL: ${problem.url}")
                        appendLine()
                        appendLine("Notes:")
                        appendLine(note.content)
                        appendLine()
                        appendLine("-" .repeat(50))
                        appendLine()
                    }
                }

                appendLine()
                appendLine("=" .repeat(50))
                appendLine("End of Notes")
                appendLine("Total: ${notes.size} problem(s)")
                appendLine("=" .repeat(50))
            }

            // Create temporary file
            val cacheDir = context.cacheDir
            val file = File(cacheDir, "dsa_notes_${System.currentTimeMillis()}.txt")

            // Write to file
            file.writeText(notesText)

            // Create share intent
            val fileUri = FileProvider.getUriForFile(
                context,
                "${context.packageName}.fileprovider",
                file
            )

            val shareIntent = Intent(Intent.ACTION_SEND).apply {
                type = "text/plain"
                putExtra(Intent.EXTRA_STREAM, fileUri)
                putExtra(Intent.EXTRA_SUBJECT, "DSA Tracker Notes")
                putExtra(Intent.EXTRA_TEXT, "My DSA problem solving notes")
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }

            Result.success(Intent.createChooser(shareIntent, "Share Notes"))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    private fun getCurrentTimestamp(): String {
        val sdf = SimpleDateFormat("MMM dd, yyyy HH:mm", Locale.getDefault())
        return sdf.format(Date())
    }
}
