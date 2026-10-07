package com.kongko.app.core.di

import android.content.Context
import androidx.room.Room
import com.kongko.app.core.database.KongkoDatabase
import com.kongko.app.core.database.dao.ChatRoomDao
import com.kongko.app.core.database.dao.MessageDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): KongkoDatabase {
        return Room.databaseBuilder(
            context,
            KongkoDatabase::class.java,
            "kongko.db"
        )
            .fallbackToDestructiveMigration()
            .build()
    }

    @Provides
    fun provideChatRoomDao(db: KongkoDatabase): ChatRoomDao = db.chatRoomDao()

    @Provides
    fun provideMessageDao(db: KongkoDatabase): MessageDao = db.messageDao()
}
