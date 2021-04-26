package com.rishav.gidget.Realm

import io.realm.RealmObject

open class AddToWidget : RealmObject() {
    var username: String? = null
    var name: String? = null
    var message: String? = null
    var date: String? = null
    var avatarUrl: String? = null
}
