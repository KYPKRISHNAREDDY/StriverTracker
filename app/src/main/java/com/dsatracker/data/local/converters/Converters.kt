package com.dsatracker.data.local.converters

import androidx.room.TypeConverter
import com.dsatracker.data.local.entity.*

class Converters {

    @TypeConverter
    fun fromProblemStatus(value: ProblemStatus): String = value.name

    @TypeConverter
    fun toProblemStatus(value: String): ProblemStatus =
        enumValueOf<ProblemStatus>(value)

    @TypeConverter
    fun fromProblemDifficulty(value: ProblemDifficulty): String = value.name

    @TypeConverter
    fun toProblemDifficulty(value: String): ProblemDifficulty =
        enumValueOf<ProblemDifficulty>(value)

    @TypeConverter
    fun fromPlatform(value: Platform): String = value.name

    @TypeConverter
    fun toPlatform(value: String): Platform =
        enumValueOf<Platform>(value)

    @TypeConverter
    fun fromGoalMode(value: GoalMode): String = value.name

    @TypeConverter
    fun toGoalMode(value: String): GoalMode =
        enumValueOf<GoalMode>(value)
}
