package com.rishav.gidget.Realm

import io.realm.RealmObject
import io.realm.annotations.PrimaryKey

open class AddToWidget : RealmObject() {
    @PrimaryKey
    var username: String? = null

    var type: String = ""
}
