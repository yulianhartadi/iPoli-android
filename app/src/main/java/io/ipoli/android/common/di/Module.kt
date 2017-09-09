package io.ipoli.android.common.di

import android.content.Context
import android.content.SharedPreferences
import android.preference.PreferenceManager
import android.view.LayoutInflater
import io.ipoli.android.quest.persistence.QuestRepository
import io.ipoli.android.quest.persistence.RealmQuestRepository
import space.traversal.kapsule.HasModules

/**
 * Created by Venelin Valkov <venelin@curiousily.com>
 * on 9/10/17.
 */
interface RepositoryModule {
    val questRepository: QuestRepository
}

class RealmRepositoryModule : RepositoryModule {
    override val questRepository = RealmQuestRepository()
}

interface AndroidModule {
    val layoutInflater: LayoutInflater

    val sharedPreferences: SharedPreferences
}

class MainAndroidModule(private val context: Context) : AndroidModule {

    override val layoutInflater: LayoutInflater get() = LayoutInflater.from(context)

    override val sharedPreferences: SharedPreferences get() = PreferenceManager.getDefaultSharedPreferences(context)
}

class Module(androidModule: AndroidModule, repositoryModule: RepositoryModule) :
    AndroidModule by androidModule,
    RepositoryModule by repositoryModule,
    HasModules {

    override val modules = setOf(androidModule, repositoryModule)
}