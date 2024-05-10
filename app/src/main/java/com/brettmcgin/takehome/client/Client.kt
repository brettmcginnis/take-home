package com.brettmcgin.takehome.client

import android.util.Log
import com.brettmcgin.takehome.BuildConfig
import io.ktor.client.HttpClient
import io.ktor.client.engine.android.Android
import io.ktor.client.plugins.auth.Auth
import io.ktor.client.plugins.auth.providers.BearerTokens
import io.ktor.client.plugins.auth.providers.bearer
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.defaultRequest
import io.ktor.client.plugins.logging.LogLevel
import io.ktor.client.plugins.logging.Logger
import io.ktor.client.plugins.logging.Logging
import io.ktor.client.request.header
import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders
import io.ktor.http.URLProtocol
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json

private const val TIME_OUT = 60_000
private const val GITHUB_HOST = "api.github.com"

val client = HttpClient(Android) {
    install(Logging) {
        level = LogLevel.ALL
        logger = AndroidLogCat
    }

    install(Auth) {
        bearer {
            loadTokens {
                BearerTokens(BuildConfig.GITHUB_TOKEN, "")
            }
        }
    }

    install(ContentNegotiation) {
        json(Json {
            ignoreUnknownKeys = true
        })
    }

    engine {
        connectTimeout = TIME_OUT
        socketTimeout = TIME_OUT
    }

    defaultRequest {
        host = GITHUB_HOST
        url.protocol = URLProtocol.HTTPS
        header(HttpHeaders.ContentType, ContentType.Application.Json)
    }
}

private object AndroidLogCat : Logger {
    override fun log(message: String) {
        Log.d("HttpClientLogger", message)
    }
}
