package com.brettmcgin.takehome.github.network

import com.brettmcgin.takehome.github.domain.Contributor as DomainContributor

fun List<Contributor>.toDomain() = map { it.toDomain()}

private fun Contributor.toDomain() = DomainContributor(name = login.orEmpty())
