package com.theminimaldev.gymprint.di

import android.content.Context
import androidx.room.Room
import com.theminimaldev.gymprint.data.db.AppDatabase
import com.theminimaldev.gymprint.data.db.GymLocationDao
import com.theminimaldev.gymprint.data.db.GymVisitDao
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
    fun provideDatabase(@ApplicationContext context: Context): AppDatabase =
        Room.databaseBuilder(context, AppDatabase::class.java, "gymprint.db")
            .fallbackToDestructiveMigration()
            .build()

    @Provides
    fun provideGymLocationDao(db: AppDatabase): GymLocationDao = db.gymLocationDao()

    @Provides
    fun provideGymVisitDao(db: AppDatabase): GymVisitDao = db.gymVisitDao()
}
