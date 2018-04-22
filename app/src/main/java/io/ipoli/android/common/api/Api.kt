package io.ipoli.android.common.api

import com.fasterxml.jackson.core.type.TypeReference
import com.fasterxml.jackson.databind.ObjectMapper
import io.ipoli.android.BuildConfig
import io.ipoli.android.Constants
import io.ipoli.android.common.datetime.startOfDayUTC
import kotlinx.coroutines.experimental.suspendCancellableCoroutine
import okhttp3.*
import org.json.JSONObject
import org.threeten.bp.LocalDate
import java.io.IOException
import java.lang.Exception
import java.util.*
import java.util.concurrent.TimeUnit

/**
 * Created by Polina Zhelyazkova <polina@mypoli.fun>
 * on 3/23/18.
 */

object Api {

    private val objectMapper = ObjectMapper()
    private val urlProvider = if (BuildConfig.DEBUG) DevUrlProvider() else ProdUrlProvider()
    private val httpClient = OkHttpClient().newBuilder()
        .readTimeout(Constants.API_READ_TIMEOUT_SECONDS.toLong(), TimeUnit.SECONDS)
        .retryOnConnectionFailure(false).build()

    suspend fun migratePlayer(playerId: String, email: String) {

    }

    /**
     * @throws MembershipStatusException
     */
    suspend fun getMembershipStatus(
        subscriptionId: String,
        token: String
    ): MembershipStatus {
        val request = createRequest(subscriptionId, token)
        val response = callServer(request)
        if (!response.isSuccessful) {
            throw MembershipStatusException("Api membership status call failed with response ${response.message()}")
        }
        return toMembershipStatus(response)
    }

    private fun toMembershipStatus(response: Response): MembershipStatus {
        val mapTypeReference = object : TypeReference<Map<String, Any>>() {}
        val subs: Map<String, Any> =
            objectMapper.readValue(response.body()!!.charStream(), mapTypeReference)
        val startTimeMillis = subs["start_time"].toString().toLong()
        val expiryTimeMillis = subs["end_time"].toString().toLong()
        val autoRenewing = subs["autorenew"].toString().toBoolean()

        return MembershipStatus(
            startDate = startTimeMillis.startOfDayUTC,
            expirationDate = expiryTimeMillis.startOfDayUTC,
            isAutoRenewing = autoRenewing
        )
    }

    private fun createRequest(subscriptionId: String, token: String): Request {
        val params = HashMap<String, String>()
        params["subscription_id"] = subscriptionId
        params["token"] = token

        val jsonContent = JSONObject(params)

        val mediaType = MediaType.parse("application/json; charset=utf-8")
        val body = RequestBody.create(mediaType, jsonContent.toString())

        return Request.Builder()
            .url(urlProvider.getMembershipStatus())
            .post(body)
            .build()
    }

    private suspend fun callServer(request: Request) =
        try {
            httpClient.newCall(request).await()
        } catch (e: Exception) {
            throw MembershipStatusException("Api membership status call failed", e)
        }


    data class MembershipStatus(
        val startDate: LocalDate,
        val expirationDate: LocalDate,
        val isAutoRenewing: Boolean
    )

    class MembershipStatusException(message: String, cause: Throwable? = null) :
        Exception(message, cause)
}

suspend fun Call.await(): Response {
    return suspendCancellableCoroutine { continuation ->
        enqueue(object : Callback {
            override fun onResponse(call: Call, response: Response) {
                continuation.resume(response)
            }

            override fun onFailure(call: Call, e: IOException) {
                // Don't bother with resuming the continuation if it is already cancelled.
                if (continuation.isCancelled) return
                continuation.resumeWithException(e)
            }
        })

        continuation.invokeOnCompletion {
            if (continuation.isCancelled)
                try {
                    cancel()
                } catch (ex: Throwable) {
                    //Ignore cancel exception
                }
        }
    }
}