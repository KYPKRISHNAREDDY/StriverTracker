package com.dsatracker.data.local.dao

import androidx.room.*
import com.dsatracker.data.local.entity.*
import kotlinx.coroutines.flow.Flow

@Dao
interface UserDao {
    @Query("SELECT * FROM users WHERE id = :userId")
    fun getUserFlow(userId: Long): Flow<UserEntity?>

    @Query("SELECT * FROM users WHERE id = :userId")
    suspend fun getUser(userId: Long): UserEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUser(user: UserEntity): Long

    @Update
    suspend fun updateUser(user: UserEntity)

    @Query("SELECT * FROM users LIMIT 1")
    suspend fun getCurrentUser(): UserEntity?
}

@Dao
interface SheetDao {
    @Query("SELECT * FROM sheets WHERE id = :sheetId")
    suspend fun getSheet(sheetId: String): SheetEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSheet(sheet: SheetEntity)

    @Query("SELECT * FROM sheets")
    fun getAllSheetsFlow(): Flow<List<SheetEntity>>
}

@Dao
interface TopicDao {
    @Query("SELECT * FROM topics WHERE sheetId = :sheetId ORDER BY orderIndex")
    fun getTopicsBySheet(sheetId: String): Flow<List<TopicEntity>>

    @Query("SELECT * FROM topics WHERE id = :topicId")
    suspend fun getTopic(topicId: Long): TopicEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTopics(topics: List<TopicEntity>)

    @Transaction
    @Query("""
        SELECT t.*,
        COUNT(p.id) as totalProblems,
        COUNT(CASE WHEN upp.status = 'SOLVED' THEN 1 END) as solvedProblems
        FROM topics t
        LEFT JOIN problems p ON p.topicId = t.id
        LEFT JOIN user_problem_progress upp ON upp.problemId = p.id AND upp.userId = :userId
        WHERE t.sheetId = :sheetId
        GROUP BY t.id
        ORDER BY t.orderIndex
    """)
    fun getTopicsWithProgress(sheetId: String, userId: Long): Flow<List<TopicWithProgress>>
}

data class TopicWithProgress(
    val id: Long,
    val name: String,
    val totalProblems: Int,
    val solvedProblems: Int
)

@Dao
interface ProblemDao {
    @Query("SELECT * FROM problems WHERE id = :problemId")
    suspend fun getProblem(problemId: Long): ProblemEntity?

    @Query("SELECT * FROM problems WHERE topicId = :topicId ORDER BY orderIndex")
    fun getProblemsByTopic(topicId: Long): Flow<List<ProblemEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertProblems(problems: List<ProblemEntity>)

    @Query("SELECT COUNT(*) FROM problems WHERE sheetId = :sheetId")
    suspend fun getTotalProblemsCount(sheetId: String): Int

    @Transaction
    @Query("""
        SELECT p.*, upp.status, upp.starred, upp.needsRevision
        FROM problems p
        LEFT JOIN user_problem_progress upp ON p.id = upp.problemId AND upp.userId = :userId
        WHERE p.topicId = :topicId
        ORDER BY p.orderIndex
    """)
    fun getProblemsWithStatus(topicId: Long, userId: Long): Flow<List<ProblemWithStatus>>
}

data class ProblemWithStatus(
    val id: Long,
    val title: String,
    val platform: Platform,
    val url: String,
    val difficulty: ProblemDifficulty,
    val status: ProblemStatus?,
    val starred: Boolean?,
    val needsRevision: Boolean?
)

@Dao
interface UserProblemProgressDao {
    @Query("SELECT * FROM user_problem_progress WHERE userId = :userId AND problemId = :problemId")
    suspend fun getProgress(userId: Long, problemId: Long): UserProblemProgressEntity?

    @Query("SELECT * FROM user_problem_progress WHERE userId = :userId AND problemId = :problemId")
    fun getProgressFlow(userId: Long, problemId: Long): Flow<UserProblemProgressEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertProgress(progress: UserProblemProgressEntity): Long

    @Update
    suspend fun updateProgress(progress: UserProblemProgressEntity)

    @Query("SELECT COUNT(*) FROM user_problem_progress WHERE userId = :userId AND status = 'SOLVED'")
    suspend fun getSolvedCount(userId: Long): Int

    @Query("SELECT COUNT(*) FROM user_problem_progress WHERE userId = :userId AND status = 'SOLVED'")
    fun getSolvedCountFlow(userId: Long): Flow<Int>

    @Query("""
        SELECT * FROM user_problem_progress
        WHERE userId = :userId AND status = 'SOLVED'
        ORDER BY lastSolvedAt DESC
        LIMIT :limit
    """)
    fun getRecentlySolved(userId: Long, limit: Int = 10): Flow<List<UserProblemProgressEntity>>
}

@Dao
interface UserProblemNoteDao {
    @Query("SELECT * FROM user_problem_notes WHERE userId = :userId AND problemId = :problemId")
    fun getNoteFlow(userId: Long, problemId: Long): Flow<UserProblemNoteEntity?>

    @Query("SELECT * FROM user_problem_notes WHERE userId = :userId AND problemId = :problemId")
    suspend fun getNote(userId: Long, problemId: Long): UserProblemNoteEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertNote(note: UserProblemNoteEntity): Long

    @Update
    suspend fun updateNote(note: UserProblemNoteEntity)

    @Delete
    suspend fun deleteNote(note: UserProblemNoteEntity)

    @Query("SELECT * FROM user_problem_notes WHERE userId = :userId AND content != ''")
    fun getAllNotesFlow(userId: Long): Flow<List<UserProblemNoteEntity>>
}

@Dao
interface RevisionCardDao {
    @Query("""
        SELECT * FROM revision_cards
        WHERE userId = :userId AND isActive = 1 AND nextRevisionDateUtc <= :endDate
        ORDER BY nextRevisionDateUtc
        LIMIT :limit
    """)
    fun getDueCards(userId: Long, endDate: Long, limit: Int = 15): Flow<List<RevisionCardEntity>>

    @Query("""
        SELECT * FROM revision_cards
        WHERE userId = :userId AND problemId = :problemId AND isActive = 1
        LIMIT 1
    """)
    suspend fun getActiveCard(userId: Long, problemId: Long): RevisionCardEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCard(card: RevisionCardEntity): Long

    @Update
    suspend fun updateCard(card: RevisionCardEntity)

    @Query("UPDATE revision_cards SET isActive = 0 WHERE userId = :userId AND problemId = :problemId")
    suspend fun deactivateCard(userId: Long, problemId: Long)

    @Query("SELECT COUNT(*) FROM revision_cards WHERE userId = :userId AND isActive = 1 AND nextRevisionDateUtc <= :date")
    fun getDueCountFlow(userId: Long, date: Long): Flow<Int>
}

@Dao
interface DailyStatsDao {
    @Query("SELECT * FROM daily_stats WHERE userId = :userId AND dateLocalEpochDay = :date")
    suspend fun getStats(userId: Long, date: Long): DailyStatsEntity?

    @Query("SELECT * FROM daily_stats WHERE userId = :userId AND dateLocalEpochDay = :date")
    fun getStatsFlow(userId: Long, date: Long): Flow<DailyStatsEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertStats(stats: DailyStatsEntity): Long

    @Update
    suspend fun updateStats(stats: DailyStatsEntity)

    @Query("""
        SELECT * FROM daily_stats
        WHERE userId = :userId AND dateLocalEpochDay >= :startDate AND dateLocalEpochDay <= :endDate
        ORDER BY dateLocalEpochDay DESC
    """)
    fun getStatsInRange(userId: Long, startDate: Long, endDate: Long): Flow<List<DailyStatsEntity>>

    @Query("""
        SELECT * FROM daily_stats
        WHERE userId = :userId
        ORDER BY dateLocalEpochDay DESC
        LIMIT :limit
    """)
    fun getRecentStats(userId: Long, limit: Int = 30): Flow<List<DailyStatsEntity>>
}

@Dao
interface GoalDao {
    @Query("SELECT * FROM goals WHERE userId = :userId AND isActive = 1 LIMIT 1")
    suspend fun getActiveGoal(userId: Long): GoalEntity?

    @Query("SELECT * FROM goals WHERE userId = :userId AND isActive = 1 LIMIT 1")
    fun getActiveGoalFlow(userId: Long): Flow<GoalEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertGoal(goal: GoalEntity): Long

    @Update
    suspend fun updateGoal(goal: GoalEntity)

    @Query("UPDATE goals SET isActive = 0 WHERE userId = :userId")
    suspend fun deactivateAllGoals(userId: Long)
}

@Dao
interface XpEventDao {
    @Insert
    suspend fun insertEvent(event: XpEventEntity): Long

    @Query("SELECT SUM(points) FROM xp_events WHERE userId = :userId")
    fun getTotalXpFlow(userId: Long): Flow<Int?>

    @Query("SELECT * FROM xp_events WHERE userId = :userId ORDER BY timestamp DESC LIMIT :limit")
    fun getRecentEvents(userId: Long, limit: Int = 20): Flow<List<XpEventEntity>>
}
