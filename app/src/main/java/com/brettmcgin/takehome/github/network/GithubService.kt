package com.brettmcgin.takehome.github.network

import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get

class GithubService(private val client: HttpClient) {
    suspend fun getRepositories(query: String, perPage: Int) =
        client.get("/search/repositories") {
            with(url.parameters) {
                append("q", query)
                append("per_page", perPage.toString())
                append("o", "desc")
            }
        }.body<SearchRepositoriesResponse>()

    /**
     * Github doesn't provide any contributors if the total is too high
     * https://github.com/orgs/community/discussions/45433
     */
    suspend fun getContributors(fullName: String) =
        client.get("repos/$fullName/contributors").run {
            emptyList<Contributor>().takeIf { status.value >= 400 } ?: body<List<Contributor>>()
        }
}

