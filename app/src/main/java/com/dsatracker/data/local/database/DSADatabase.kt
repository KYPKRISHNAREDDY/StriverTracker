package com.dsatracker.data.local.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.dsatracker.data.local.converters.Converters
import com.dsatracker.data.local.dao.*
import com.dsatracker.data.local.entity.*

@Database(
    entities = [
        UserEntity::class,
        SheetEntity::class,
        TopicEntity::class,
        ProblemEntity::class,
        UserProblemProgressEntity::class,
        UserProblemNoteEntity::class,
        RevisionCardEntity::class,
        DailyStatsEntity::class,
        GoalEntity::class,
        XpEventEntity::class
    ],
    version = 1,
    exportSchema = true
)
@TypeConverters(Converters::class)
abstract class DSADatabase : RoomDatabase() {

    abstract fun userDao(): UserDao
    abstract fun sheetDao(): SheetDao
    abstract fun topicDao(): TopicDao
    abstract fun problemDao(): ProblemDao
    abstract fun userProblemProgressDao(): UserProblemProgressDao
    abstract fun userProblemNoteDao(): UserProblemNoteDao
    abstract fun revisionCardDao(): RevisionCardDao
    abstract fun dailyStatsDao(): DailyStatsDao
    abstract fun goalDao(): GoalDao
    abstract fun xpEventDao(): XpEventDao

    companion object {
        private const val DATABASE_NAME = "dsa_tracker.db"

        @Volatile
        private var INSTANCE: DSADatabase? = null

        fun getInstance(context: Context): DSADatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    DSADatabase::class.java,
                    DATABASE_NAME
                )
                    .fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
