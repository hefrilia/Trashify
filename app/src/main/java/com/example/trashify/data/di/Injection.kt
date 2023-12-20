package com.example.trashify.data.di

import android.content.Context
import com.example.trashify.data.database.PostDatabase
import com.example.trashify.data.database.PostRepository
import com.example.trashify.data.retrofit.ApiConfigPost

object Injection {
    fun provideRepository(context: Context, token: String? = null): PostRepository{
        val database = PostDatabase.getDatabase(context)
        val apiService = ApiConfigPost.getApiServicePost(token)
        return PostRepository(database, apiService)
    }
}