package ru.nekoguys.game.persistence.util

import org.springframework.data.domain.Persistable

abstract class MyPersistable<ID>(
    private val idProperty: () -> ID?,
    private val isNew: Boolean,
) : Persistable<ID> {
    final override fun getId(): ID? = idProperty()
    final override fun isNew(): Boolean = isNew
}
