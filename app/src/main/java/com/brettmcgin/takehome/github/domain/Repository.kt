package com.brettmcgin.takehome.github.domain

data class Repository(
    val name: String,
    val url: String,
    val fullName: String,
)

data class Contributor(val name: String)
