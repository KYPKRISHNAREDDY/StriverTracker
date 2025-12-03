package com.dsatracker.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.Index
import androidx.room.ForeignKey

@Entity(tableName = "users")
data class UserEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String? = null,
    val email: String? = null,
    val currentGoalId: Long? = null,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "sheets")
data class SheetEntity(
    @PrimaryKey
    val id: String,
    val name: String,
    val description: String? = null,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)

@Entity(
    tableName = "topics",
    indices = [Index(value = ["sheetId"])],
    foreignKeys = [
        ForeignKey(
            entity = SheetEntity::class,
            parentColumns = ["id"],
            childColumns = ["sheetId"],
            onDelete = ForeignKey.CASCADE
        )
    ]
)
data class TopicEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val sheetId: String,
    val name: String,
    val orderIndex: Int,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)

@Entity(
    tableName = "problems",
    indices = [
        Index(value = ["sheetId"]),
        Index(value = ["topicId"])
    ],
    foreignKeys = [
        ForeignKey(
            entity = SheetEntity::class,
            parentColumns = ["id"],
            childColumns = ["sheetId"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = TopicEntity::class,
            parentColumns = ["id"],
            childColumns = ["topicId"],
            onDelete = ForeignKey.CASCADE
        )
    ]
)
data class ProblemEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val sheetId: String,
    val topicId: Long,
    val title: String,
    val platform: Platform,
    val url: String,
    val difficulty: ProblemDifficulty,
    val orderIndex: Int,
    val tags: String? = null, // Comma-separated
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)

@Entity(
    tableName = "user_problem_progress",
    indices = [
        Index(value = ["userId"]),
        Index(value = ["problemId"]),
        Index(value = ["userId", "problemId"], unique = true)
    ],
    foreignKeys = [
        ForeignKey(
            entity = UserEntity::class,
            parentColumns = ["id"],
            childColumns = ["userId"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = ProblemEntity::class,
            parentColumns = ["id"],
            childColumns = ["problemId"],
            onDelete = ForeignKey.CASCADE
        )
    ]
)
data class UserProblemProgressEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val userId: Long,
    val problemId: Long,
    val status: ProblemStatus = ProblemStatus.NOT_STARTED,
    val timesSolved: Int = 0,
    val lastSolvedAt: Long? = null,
    val timeTakenSeconds: Int? = null,
    val neededHelp: Boolean = false,
    val needsRevision: Boolean = false,
    val starred: Boolean = false,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)

@Entity(
    tableName = "user_problem_notes",
    indices = [
        Index(value = ["userId"]),
        Index(value = ["problemId"]),
        Index(value = ["userId", "problemId"], unique = true)
    ],
    foreignKeys = [
        ForeignKey(
            entity = UserEntity::class,
            parentColumns = ["id"],
            childColumns = ["userId"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = ProblemEntity::class,
            parentColumns = ["id"],
            childColumns = ["problemId"],
            onDelete = ForeignKey.CASCADE
        )
    ]
)
data class UserProblemNoteEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val userId: Long,
    val problemId: Long,
    val content: String,
    val lastUpdatedAt: Long = System.currentTimeMillis()
)

@Entity(
    tableName = "revision_cards",
    indices = [
        Index(value = ["userId"]),
        Index(value = ["problemId"]),
        Index(value = ["userId", "problemId", "isActive"])
    ],
    foreignKeys = [
        ForeignKey(
            entity = UserEntity::class,
            parentColumns = ["id"],
            childColumns = ["userId"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = ProblemEntity::class,
            parentColumns = ["id"],
            childColumns = ["problemId"],
            onDelete = ForeignKey.CASCADE
        )
    ]
)
data class RevisionCardEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val userId: Long,
    val problemId: Long,
    val nextRevisionDateUtc: Long,
    val difficultyRating: Int, // 1-3 (Hard/Okay/Easy)
    val reviewCount: Int = 0,
    val lastReviewedAtUtc: Long? = null,
    val isActive: Boolean = true,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)

@Entity(
    tableName = "daily_stats",
    indices = [
        Index(value = ["userId"]),
        Index(value = ["userId", "dateLocalEpochDay"], unique = true)
    ],
    foreignKeys = [
        ForeignKey(
            entity = UserEntity::class,
            parentColumns = ["id"],
            childColumns = ["userId"],
            onDelete = ForeignKey.CASCADE
        )
    ]
)
data class DailyStatsEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val userId: Long,
    val dateLocalEpochDay: Long,
    val problemsSolvedCount: Int = 0,
    val revisionsCount: Int = 0,
    val minutesStudied: Int = 0,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)

@Entity(
    tableName = "goals",
    indices = [Index(value = ["userId"])],
    foreignKeys = [
        ForeignKey(
            entity = UserEntity::class,
            parentColumns = ["id"],
            childColumns = ["userId"],
            onDelete = ForeignKey.CASCADE
        )
    ]
)
data class GoalEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val userId: Long,
    val mode: GoalMode,
    val targetProblemsPerDay: Int? = null,
    val targetMinutesPerDay: Int? = null,
    val daysPerWeek: Int = 7,
    val startDateLocalEpochDay: Long,
    val targetEndDateLocalEpochDay: Long? = null,
    val autoRecalculateOnMiss: Boolean = true,
    val isActive: Boolean = true,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)

@Entity(
    tableName = "xp_events",
    indices = [Index(value = ["userId"])],
    foreignKeys = [
        ForeignKey(
            entity = UserEntity::class,
            parentColumns = ["id"],
            childColumns = ["userId"],
            onDelete = ForeignKey.CASCADE
        )
    ]
)
data class XpEventEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val userId: Long,
    val type: String, // "PROBLEM_SOLVED", "REVISION_DONE"
    val points: Int,
    val timestamp: Long = System.currentTimeMillis()
)
