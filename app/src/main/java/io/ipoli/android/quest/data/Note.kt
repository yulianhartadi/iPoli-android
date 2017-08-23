package io.ipoli.android.quest.data

import io.realm.RealmObject

/**
 * Created by Venelin Valkov <venelin@curiousily.com>
 * on 8/19/17.
 */
open class Note : RealmObject {
    enum class NoteType {

        TEXT, INTENT, URL
    }

    var noteType: String? = null
    var text: String? = null
    var data: String? = null

    constructor()

    constructor(noteType: NoteType, text: String, data: String) {
        this.noteType = noteType.name
        this.text = text
        this.data = data
    }

    constructor(text: String) {
        this.noteType = NoteType.TEXT.name
        this.text = text
        this.data = ""
    }

    val noteTypeValue: NoteType
        get() = NoteType.valueOf(noteType!!)
}
