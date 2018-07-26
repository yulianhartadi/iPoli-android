package io.ipoli.android.common.persistence

import com.google.android.gms.tasks.Tasks
import com.google.firebase.firestore.*

fun Query.execute(): QuerySnapshot =
    Tasks.await(get(Source.SERVER))

val Query.documents: List<DocumentSnapshot> get() = execute().documents

fun DocumentReference.getSync(): DocumentSnapshot =
    Tasks.await(get(Source.SERVER))