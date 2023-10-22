package com.ammar.wallflow.data.db

import androidx.room.AutoMigration
import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.ammar.wallflow.data.db.converters.Converters
import com.ammar.wallflow.data.db.dao.AutoWallpaperHistoryDao
import com.ammar.wallflow.data.db.dao.FavoriteDao
import com.ammar.wallflow.data.db.dao.LastUpdatedDao
import com.ammar.wallflow.data.db.dao.ObjectDetectionModelDao
import com.ammar.wallflow.data.db.dao.RateLimitDao
import com.ammar.wallflow.data.db.dao.reddit.RedditSearchQueryWallpapersDao
import com.ammar.wallflow.data.db.dao.reddit.RedditWallpapersDao
import com.ammar.wallflow.data.db.dao.search.SavedSearchDao
import com.ammar.wallflow.data.db.dao.search.SearchHistoryDao
import com.ammar.wallflow.data.db.dao.search.SearchQueryDao
import com.ammar.wallflow.data.db.dao.search.SearchQueryRemoteKeysDao
import com.ammar.wallflow.data.db.dao.wallhaven.WallhavenPopularTagsDao
import com.ammar.wallflow.data.db.dao.wallhaven.WallhavenSearchQueryWallpapersDao
import com.ammar.wallflow.data.db.dao.wallhaven.WallhavenTagsDao
import com.ammar.wallflow.data.db.dao.wallhaven.WallhavenUploadersDao
import com.ammar.wallflow.data.db.dao.wallhaven.WallhavenWallpapersDao
import com.ammar.wallflow.data.db.entity.AutoWallpaperHistoryEntity
import com.ammar.wallflow.data.db.entity.FavoriteEntity
import com.ammar.wallflow.data.db.entity.LastUpdatedEntity
import com.ammar.wallflow.data.db.entity.ObjectDetectionModelEntity
import com.ammar.wallflow.data.db.entity.RateLimitEntity
import com.ammar.wallflow.data.db.entity.reddit.RedditSearchQueryWallpaperEntity
import com.ammar.wallflow.data.db.entity.search.SavedSearchEntity
import com.ammar.wallflow.data.db.entity.search.SearchHistoryEntity
import com.ammar.wallflow.data.db.entity.search.SearchQueryEntity
import com.ammar.wallflow.data.db.entity.search.SearchQueryRemoteKeyEntity
import com.ammar.wallflow.data.db.entity.wallhaven.WallhavenPopularTagEntity
import com.ammar.wallflow.data.db.entity.wallhaven.WallhavenSearchQueryWallpaperEntity
import com.ammar.wallflow.data.db.entity.wallhaven.WallhavenTagEntity
import com.ammar.wallflow.data.db.entity.wallhaven.WallhavenUploaderEntity
import com.ammar.wallflow.data.db.entity.wallhaven.WallhavenWallpaperTagsEntity
import com.ammar.wallflow.data.db.entity.wallpaper.RedditWallpaperEntity
import com.ammar.wallflow.data.db.entity.wallpaper.WallhavenWallpaperEntity

@Database(
    entities = [
        LastUpdatedEntity::class,
        WallhavenPopularTagEntity::class,
        SearchQueryEntity::class,
        SearchQueryRemoteKeyEntity::class,
        WallhavenSearchQueryWallpaperEntity::class,
        WallhavenWallpaperEntity::class,
        WallhavenUploaderEntity::class,
        WallhavenTagEntity::class,
        WallhavenWallpaperTagsEntity::class,
        SearchHistoryEntity::class,
        ObjectDetectionModelEntity::class,
        SavedSearchEntity::class,
        AutoWallpaperHistoryEntity::class,
        FavoriteEntity::class,
        RateLimitEntity::class,
        RedditWallpaperEntity::class,
        RedditSearchQueryWallpaperEntity::class,
    ],
    version = 4,
    autoMigrations = [
        AutoMigration(from = 2, to = 3),
    ],
)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun lastUpdatedDao(): LastUpdatedDao
    abstract fun wallhavenPopularTagsDao(): WallhavenPopularTagsDao
    abstract fun searchQueryDao(): SearchQueryDao
    abstract fun searchQueryRemoteKeysDao(): SearchQueryRemoteKeysDao
    abstract fun wallhavenSearchQueryWallpapersDao(): WallhavenSearchQueryWallpapersDao
    abstract fun wallhavenWallpapersDao(): WallhavenWallpapersDao
    abstract fun wallhavenTagsDao(): WallhavenTagsDao
    abstract fun wallhavenUploadersDao(): WallhavenUploadersDao
    abstract fun searchHistoryDao(): SearchHistoryDao
    abstract fun objectDetectionModelDao(): ObjectDetectionModelDao
    abstract fun savedSearchDao(): SavedSearchDao
    abstract fun autoWallpaperHistoryDao(): AutoWallpaperHistoryDao
    abstract fun favoriteDao(): FavoriteDao
    abstract fun rateLimitDao(): RateLimitDao
    abstract fun redditWallpapersDao(): RedditWallpapersDao
    abstract fun redditSearchQueryWallpapersDao(): RedditSearchQueryWallpapersDao
}
