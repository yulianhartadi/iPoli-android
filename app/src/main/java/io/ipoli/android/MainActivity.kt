package io.ipoli.android

import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import com.bluelinelabs.conductor.Conductor
import com.bluelinelabs.conductor.Router
import com.bluelinelabs.conductor.RouterTransaction
import com.couchbase.lite.DataSource
import com.couchbase.lite.Expression
import com.couchbase.lite.Query
import com.couchbase.lite.SelectResult
import io.ipoli.android.common.datetime.startOfDayUTC
import io.ipoli.android.common.di.Module
import io.ipoli.android.home.HomeController
import io.ipoli.android.player.persistence.ProviderType
import io.ipoli.android.quest.AuthProvider
import io.ipoli.android.quest.Player
import org.threeten.bp.LocalDate
import space.traversal.kapsule.Injects
import space.traversal.kapsule.inject
import space.traversal.kapsule.required
import timber.log.Timber
import java.util.Spliterators.iterator

/**
 * Created by Venelin Valkov <venelin@ipoli.io>
 * on 7/6/17.
 */
class MainActivity : AppCompatActivity(), Injects<Module> {

    lateinit var router: Router

    private val playerRepository by required { playerRepository }

    private val database by required { database }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

//        ActivityCompat.requestPermissions(this,
//            arrayOf(Manifest.permission.READ_PHONE_STATE),
//            1231)

        router = Conductor.attachRouter(this, findViewById(R.id.controllerContainer), savedInstanceState)
        inject(iPoliApp.module(this, router))
        val hasNoRootController = !router.hasRootController()

        if (playerRepository.find() == null) {
            val player = Player(authProvider = AuthProvider(provider = ProviderType.ANONYMOUS.name))
            playerRepository.save(player)
        }

        if (hasNoRootController) {
            router.setRoot(RouterTransaction.with(HomeController()))
        }

//        val query = Query.select(SelectResult.all(), SelectResult.expression(Expression.meta().id))
//            .from(DataSource.database(database)).
//            where(Expression.property("scheduledDate").
//                equalTo(LocalDate.now().startOfDayUTC())
//                .and(Expression.property("type").equalTo("Quest")))
//
//        val live = query.toLive()
//        live.addChangeListener { change ->
////            change.rows.
//            for (r in change.rows) {
//                @Suppress("UNCHECKED_CAST")
//                val map = r.toMap().get("iPoli") as MutableMap<String, Any?>
//                map.put("id", r.toMap().get("_id"))
//                Timber.d("AAAA ${map}")
//            }
//        }
//        live.run()

    }

    override fun onBackPressed() {
        if (!router.handleBack()) {
            super.onBackPressed()
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        router.onActivityResult(requestCode, resultCode, data)
    }
}