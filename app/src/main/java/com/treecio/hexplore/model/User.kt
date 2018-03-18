package com.treecio.hexplore.model

import android.database.Cursor
import com.raizlabs.android.dbflow.annotation.Column
import com.raizlabs.android.dbflow.annotation.PrimaryKey
import com.raizlabs.android.dbflow.annotation.Table
import com.raizlabs.android.dbflow.structure.BaseModel
import com.treecio.hexplore.db.AppDatabase
import java.util.*

@Table(database = AppDatabase::class)
class User (

        @PrimaryKey var shortId: String? = null,

        @Column var handshakeCount: Int = 0,
        @Column var lastHandshake: Date? = null,
        @Column var name: String? = null,
        @Column var profilePhoto: String? = null,
        @Column var occupation: String? = null,
        @Column var bio: String? = null,
        @Column var profileUrl: String? = null

) : BaseModel() {

    fun fromCursor(cursor: Cursor) {

    }

}
