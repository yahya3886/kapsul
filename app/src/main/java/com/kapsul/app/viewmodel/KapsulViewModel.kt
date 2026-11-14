package com.kapsul.app.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.kapsul.app.data.Capsule
import com.kapsul.app.data.CapsuleRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn

enum class CapsuleFilter { ALL, SEALED, OPENED }

class KapsulViewModel(private val repo: CapsuleRepository) : ViewModel() {

    val capsules: StateFlow<List<Capsule>> = repo.capsules

    private val _filter = MutableStateFlow(CapsuleFilter.ALL)
    val filter: StateFlow<CapsuleFilter> = _filter

    private val _saving = MutableStateFlow(false)
    val saving: StateFlow<Boolean> = _saving

    /** Filtrelenmiş ve sıralanmış kapsüller: mühürlüler önce (yaklaşan açılış sırasıyla). */
    val visible: StateFlow<List<Capsule>> =
        combine(capsules, filter) { list, current ->
            when (current) {
                CapsuleFilter.ALL -> list
                CapsuleFilter.SEALED -> list.filter { it.isSealed }
                CapsuleFilter.OPENED -> list.filterNot { it.isSealed }
            }.sortedWith(
                compareBy(
                    { !it.isSealed },
                    { if (it.isSealed) it.unlockAt else -(it.openedAt ?: 0L) },
                )
            )
        }.stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())

    fun setFilter(f: CapsuleFilter) {
        _filter.value = f
    }

    fun create(capsule: Capsule) {
        viewModelScope.run {
            _saving.value = true
            repo.upsert(capsule)
            _saving.value = false
        }
    }

    fun open(id: String) = repo.open(id)

    fun delete(id: String) = repo.remove(id)

    companion object {
        /** Repository örneği tekeldir; ViewModel onu paylaşır. */
        fun factory(repo: CapsuleRepository) = viewModelFactory {
            initializer { KapsulViewModel(repo) }
        }
    }
}
