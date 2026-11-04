package com.example.data.api

import com.example.data.api.models.HealthResponse
import com.example.data.api.models.LanguagesResponse
import com.example.data.api.models.ProcessAudioResponseDto
import com.example.data.api.models.TranscriptionResponseDto
import com.example.data.api.models.TranslationRequestDto
import com.example.data.api.models.TranslationResponseDto
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.Part

interface TextFlowBackendApi {

    @GET("api/v1/health")
    suspend fun checkHealth(): HealthResponse

    @GET("api/v1/languages")
    suspend fun getLanguages(): LanguagesResponse

    @POST("api/v1/translate")
    suspend fun translateText(
        @Body request: TranslationRequestDto
    ): TranslationResponseDto

    @Multipart
    @POST("api/v1/audio/transcribe")
    suspend fun transcribeAudio(
        @Part file: MultipartBody.Part,
        @Part("source_lang") sourceLang: RequestBody
    ): TranscriptionResponseDto

    @Multipart
    @POST("api/v1/process")
    suspend fun processAudio(
        @Part file: MultipartBody.Part,
        @Part("source_lang") sourceLang: RequestBody,
        @Part("target_lang") targetLang: RequestBody,
        @Part("auto_punctuate") autoPunctuate: RequestBody
    ): ProcessAudioResponseDto
}
