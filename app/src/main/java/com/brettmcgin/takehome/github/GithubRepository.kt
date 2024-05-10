package com.brettmcgin.takehome.github

import com.brettmcgin.takehome.github.domain.Contributor
import com.brettmcgin.takehome.github.domain.Repository
import com.brettmcgin.takehome.github.network.GithubService
import com.brettmcgin.takehome.github.network.toDomain

private const val MORE_THAN_ZERO_STARS = "stars:>0"

class GithubRepository(private val service: GithubService) {
    suspend fun getRepositories(query: String = MORE_THAN_ZERO_STARS, perPage: Int = 100) =
        service.getRepositories(query, perPage).toDomain()

    suspend fun getContributors(repository: Repository): List<Contributor> =
        with(repository) { service.getContributors(fullName) }.toDomain()
}
