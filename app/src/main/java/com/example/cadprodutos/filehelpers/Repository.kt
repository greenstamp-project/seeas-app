package com.example.cadprodutos.filehelpers;

import android.util.Log
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.future.future
import retrofit2.Retrofit
import retrofit2.converter.scalars.ScalarsConverterFactory
import java.util.concurrent.CompletableFuture

class Repository {

    // IP DO COMPUTADOR (HOST)
    private val ip = "10.16.0.49"
    private val baseUrl = "http://$ip:3000/"

    fun doWhatNowAsync(): CompletableFuture<String> =
        GlobalScope.future { started() }

    fun doLogDataAsync(): CompletableFuture<String> =
        GlobalScope.future { logData() }

    fun doneAsync(): CompletableFuture<String> =
        GlobalScope.future { done() }

    private val service = Retrofit.Builder()
        .baseUrl(baseUrl)
        .addConverterFactory(ScalarsConverterFactory.create())
        .build()
        .create(Api::class.java)

    suspend fun started(): String {
        //perguntamos ao server o que vamos rodar
        Log.i("REQUEST", "WHATNOW")
        val call = service.started(
            device = android.os.Build.MODEL
        )
        return call.body() ?: throw ApiError(call.errorBody()!!.toString())
    }

    suspend fun logData(): String {
        Log.i("REQUEST", "LOGDATA")
        val call = service.logData(
            device = android.os.Build.MODEL
        )
        return call.body() ?: throw ApiError(call.errorBody()!!.toString())
    }

    suspend fun done(): String {
        Log.i("REQUEST", "DONE")
        val call = service.done(
            device = android.os.Build.MODEL
        )
        return call.body() ?: throw ApiError(call.errorBody()!!.toString())
    }

    suspend fun test(): String {
        val call = service.test()
        return call.body() ?: throw ApiError(call.errorBody()!!.toString())
    }
}

class ApiError(body: String): Exception(body)