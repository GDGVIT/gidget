package com.rishav.gidget.Realm

// import io.realm.DynamicRealm
// import io.realm.FieldAttribute
// import io.realm.RealmMigration
//
// class MyMigration : RealmMigration {
//    override fun migrate(realm: DynamicRealm, oldVersion: Long, newVersion: Long) {
//        var oldVersion = oldVersion
//        val schema = realm.schema
//        if (oldVersion == 0L) {
//            schema.create(AddToWidget::class.java.simpleName)
//                .addField("username", String::class.java, FieldAttribute.PRIMARY_KEY)
//                .addField("type", String::class.java)
//            oldVersion++
//        }
//    }
// }
