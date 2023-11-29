package com.ammar.wallflow.data.db.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import com.ammar.wallflow.model.Source
import com.ammar.wallflow.model.Viewed
import kotlinx.datetime.Instant
import kotlinx.serialization.Serializable

@Entity(
    tableName = "viewed",
    indices = [
        Index(
            value = ["source_id", "source"],
            unique = true,
        ),
    ],
)
@Serializable
data class ViewedEntity(
    @PrimaryKey(autoGenerate = true) val id: Long,
    @ColumnInfo(name = "source_id") val sourceId: String,
    val source: Source,
    @ColumnInfo(name = "last_viewed_on") val lastViewedOn: Instant,
)

fun ViewedEntity.toViewed() = Viewed(
    sourceId = sourceId,
    source = source,
)
