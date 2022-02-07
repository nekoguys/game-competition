package ru.nekoguys.game.web.util

import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.extension.AfterEachCallback
import org.junit.jupiter.api.extension.BeforeEachCallback
import org.junit.jupiter.api.extension.ExtensionContext
import org.springframework.test.context.junit.jupiter.SpringExtension

class CleanDatabaseExtension : BeforeEachCallback, AfterEachCallback {
    override fun beforeEach(context: ExtensionContext) = runBlocking {
        SpringExtension
            .getApplicationContext(context)
            .getBean(DatabaseCleaner::class.java)
            .clearDatabase()
    }

    override fun afterEach(context: ExtensionContext) = beforeEach(context)
}
