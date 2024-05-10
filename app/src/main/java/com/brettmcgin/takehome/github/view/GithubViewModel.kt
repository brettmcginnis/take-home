package com.brettmcgin.takehome.github.view

import com.brettmcgin.takehome.github.GithubRepository
import com.brettmcgin.takehome.github.domain.Contributor
import com.brettmcgin.takehome.github.domain.Repository
import com.brettmcgin.takehome.github.view.GithubViewModel.Intent
import com.brettmcgin.takehome.github.view.GithubViewModel.Intent.Load
import com.brettmcgin.takehome.github.view.GithubViewModel.State
import com.brettmcgin.takehome.github.view.GithubViewModel.State.Loading
import com.brettmcgin.takehome.github.view.GithubViewModel.State.RepositoriesWithContributors
import com.brettmcgin.takehome.mvi.StateViewModel
import com.brettmcgin.takehome.mvi.ViewState.Data
import com.brettmcgin.takehome.mvi.ViewState.Error
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.transform
import kotlinx.coroutines.sync.Semaphore

private const val MAX_CONCURRENT_REQUESTS = 10

data class RepositoryWithContributors(
    val repository: Repository,
    val contributors: List<Contributor>
)

class GithubViewModel(private val githubRepository: GithubRepository) :
    StateViewModel<Intent, State>() {

    sealed class Intent {
        data object Load : Intent()
    }

    sealed class State {
        data class RepositoriesWithContributors(
            val repositoryWithContributor: List<RepositoryWithContributors> = listOf(),
        ) : State()

        data object Loading : State()
    }

    override fun transformIntents(intentFlow: Flow<Intent>) = intentFlow.transform { intent ->
        when (intent) {
            is Load -> emitAll(fetch())
        }
    }

    fun load() = emitIntent(Load)

    private suspend fun fetch() = flow {
        emit(Data(Loading))

        try {
            emit(Data(githubRepository.getRepositoriesWithContributors()))
        } catch (e: Exception) {
            emit(Error(e))
        }
    }

    private suspend fun GithubRepository.getRepositoriesWithContributors(): RepositoriesWithContributors {
        val semaphore = Semaphore(MAX_CONCURRENT_REQUESTS)

        val repositoryWithContributors = coroutineScope {
            getRepositories().map { repository ->
                async {
                    semaphore.acquire()
                    try {
                        RepositoryWithContributors(repository, getContributors(repository))
                    } finally {
                        semaphore.release()
                    }
                }
            }.awaitAll()
        }

        return RepositoriesWithContributors(repositoryWithContributors)
    }
}
