package com.dsatracker.domain.usecase

import android.content.Context
import com.dsatracker.R
import com.dsatracker.data.local.entity.*
import com.dsatracker.data.preferences.PreferencesManager
import com.dsatracker.data.repository.DSARepository
import com.google.gson.Gson
import com.google.gson.annotations.SerializedName
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.InputStreamReader
import javax.inject.Inject

/**
 * Use case to seed the database with initial data from JSON file
 * This should only run once on first app launch
 */
class SeedDatabaseUseCase @Inject constructor(
    @ApplicationContext private val context: Context,
    private val repository: DSARepository,
    private val preferencesManager: PreferencesManager,
    private val gson: Gson
) {
    suspend operator fun invoke(): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            // Check if already seeded
            if (preferencesManager.getIsDataSeeded()) {
                return@withContext Result.success(Unit)
            }

            // Read and parse JSON file
            val jsonData = readJsonFile()
            val sheetData = parseSheetData(jsonData)

            // Insert sheet
            val sheet = SheetEntity(
                id = sheetData.sheet.id,
                name = sheetData.sheet.name,
                description = sheetData.sheet.description
            )
            repository.insertSheet(sheet)

            // Insert topics
            val topics = sheetData.topics.map { topicDto ->
                TopicEntity(
                    id = topicDto.id,
                    sheetId = topicDto.sheetId,
                    name = topicDto.name,
                    orderIndex = topicDto.orderIndex
                )
            }
            repository.insertTopics(topics)

            // Insert problems
            val problems = sheetData.problems.map { problemDto ->
                ProblemEntity(
                    id = problemDto.id,
                    sheetId = problemDto.sheetId,
                    topicId = problemDto.topicId,
                    title = problemDto.title,
                    platform = parsePlatform(problemDto.platform),
                    url = problemDto.url,
                    difficulty = parseDifficulty(problemDto.difficulty),
                    orderIndex = problemDto.orderIndex,
                    tags = problemDto.tags
                )
            }
            repository.insertProblems(problems)

            // Mark as seeded
            preferencesManager.setDataSeeded(true)

            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    private fun readJsonFile(): String {
        return context.resources.openRawResource(R.raw.striver_a2z).use { inputStream ->
            InputStreamReader(inputStream).use { reader ->
                reader.readText()
            }
        }
    }

    private fun parseSheetData(jsonString: String): SheetDataDto {
        return gson.fromJson(jsonString, SheetDataDto::class.java)
    }

    private fun parsePlatform(platformString: String): Platform {
        return when (platformString.uppercase()) {
            "LEETCODE" -> Platform.LEETCODE
            "GFG" -> Platform.GFG
            "TUF" -> Platform.TUF
            else -> Platform.OTHER
        }
    }

    private fun parseDifficulty(difficultyString: String): ProblemDifficulty {
        return when (difficultyString.uppercase()) {
            "EASY" -> ProblemDifficulty.EASY
            "MEDIUM" -> ProblemDifficulty.MEDIUM
            "HARD" -> ProblemDifficulty.HARD
            else -> ProblemDifficulty.MEDIUM // Default
        }
    }
}

// DTOs for JSON parsing
data class SheetDataDto(
    @SerializedName("sheet")
    val sheet: SheetDto,
    @SerializedName("topics")
    val topics: List<TopicDto>,
    @SerializedName("problems")
    val problems: List<ProblemDto>
)

data class SheetDto(
    @SerializedName("id")
    val id: String,
    @SerializedName("name")
    val name: String,
    @SerializedName("description")
    val description: String,
    @SerializedName("totalQuestions")
    val totalQuestions: Int
)

data class TopicDto(
    @SerializedName("id")
    val id: Long,
    @SerializedName("sheetId")
    val sheetId: String,
    @SerializedName("name")
    val name: String,
    @SerializedName("orderIndex")
    val orderIndex: Int
)

data class ProblemDto(
    @SerializedName("id")
    val id: Long,
    @SerializedName("sheetId")
    val sheetId: String,
    @SerializedName("topicId")
    val topicId: Long,
    @SerializedName("title")
    val title: String,
    @SerializedName("platform")
    val platform: String,
    @SerializedName("url")
    val url: String,
    @SerializedName("difficulty")
    val difficulty: String,
    @SerializedName("orderIndex")
    val orderIndex: Int,
    @SerializedName("tags")
    val tags: String? = null
)
