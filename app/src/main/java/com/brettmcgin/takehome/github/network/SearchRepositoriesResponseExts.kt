package com.brettmcgin.takehome.github.network

import com.brettmcgin.takehome.github.domain.Repository

fun SearchRepositoriesResponse.toDomain() = items?.map { it.toRepository() }.orEmpty()

private fun Item.toRepository(): Repository = Repository(
    name = name.orEmpty(),
    url = htmlUrl.orEmpty(),
    fullName = fullName.orEmpty()
)
