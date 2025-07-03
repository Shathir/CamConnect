package com.outdu.camconnect.network

import io.ktor.client.*
import io.ktor.client.engine.cio.* // Or Android engine if preferred
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.serialization.json.Json

object HttpClientProvider {
    val client: HttpClient by lazy {
        HttpClient(CIO) {
            install(ContentNegotiation) {
                json(Json {
                    ignoreUnknownKeys = true
                    prettyPrint = true
                    isLenient = true
                })
            }

            // Optional: Set timeouts, logging, etc.
        }
    }
}