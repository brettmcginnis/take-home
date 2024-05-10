package com.brettmcgin.takehome

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import com.brettmcgin.takehome.client.client
import com.brettmcgin.takehome.github.GithubRepository
import com.brettmcgin.takehome.github.domain.Contributor
import com.brettmcgin.takehome.github.domain.Repository
import com.brettmcgin.takehome.github.network.GithubService
import com.brettmcgin.takehome.github.view.GithubViewModel
import com.brettmcgin.takehome.github.view.GithubViewModel.State.Loading
import com.brettmcgin.takehome.github.view.GithubViewModel.State.RepositoriesWithContributors
import com.brettmcgin.takehome.github.view.RepositoryWithContributors
import com.brettmcgin.takehome.mvi.ViewState.Data
import com.brettmcgin.takehome.mvi.ViewState.Empty
import com.brettmcgin.takehome.mvi.ViewState.Error
import com.brettmcgin.takehome.ui.theme.TakehomeTheme

class MainActivity : ComponentActivity() {
    private val service by lazy { GithubService(client) }
    private val repository by lazy { GithubRepository(service) }
    private val viewModel by lazy { GithubViewModel(repository) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            TakehomeTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    App(viewModel)
                }
            }
        }
    }
}

@Composable
private fun App(viewModel: GithubViewModel) = Surface(modifier = Modifier.fillMaxSize()) {
    val state by viewModel.state.collectAsState()

    when (state) {
        is Data -> when(val data = state.data) {
            Loading -> Loading()
            is RepositoriesWithContributors -> Repositories(data.repositoryWithContributor)
            null -> Error()
        }
        is Empty -> viewModel.load()
        is Error -> Error()
    }
}

@Composable
fun Loading() = Column {
    Text("Loading")
}

@Composable
fun Error() = Column {
    Text("Oh no something went wrong, please try again later")
}

@Composable
fun Repositories(data: List<RepositoryWithContributors>) = LazyColumn {
    data.forEach { repositoryWithContributors ->
        item(key = repositoryWithContributors.repository.fullName) {
            with(repositoryWithContributors) {
                RepositoryWithTopContributor(repository, contributors.firstOrNull())
            }
        }
    }
}

@Composable
fun RepositoryWithTopContributor(repository: Repository, topContributor: Contributor?) = Row {
    Column {
        Text("Repository: ${repository.name}")
        Text("Contributor: ${topContributor?.name ?: "Too Many Contributors"}")
    }
}

