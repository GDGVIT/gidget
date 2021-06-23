package com.dscvit.gidget.realm

import io.realm.RealmObject
import io.realm.annotations.PrimaryKey

open class SignUp : RealmObject() {
    @PrimaryKey
    var email: String? = null

    var name: String = ""
    var photoUrl: String = ""
    var username: String = ""
}
