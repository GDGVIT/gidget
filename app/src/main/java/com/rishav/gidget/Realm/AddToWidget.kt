package com.rishav.gidget.Realm

import android.os.Parcel
import android.os.Parcelable

class AddToWidget(
    var username: String? = null,
    var name: String? = null,
    var message: String? = null,
    var date: String? = null,
    var avatarUrl: String? = null,
    var icon: Int? = null
) : Parcelable {
    constructor(parcel: Parcel) : this(
        parcel.readString(),
        parcel.readString(),
        parcel.readString(),
        parcel.readString(),
        parcel.readString(),
        parcel.readValue(Int::class.java.classLoader) as? Int
    ) {
    }

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(username)
        parcel.writeString(name)
        parcel.writeString(message)
        parcel.writeString(date)
        parcel.writeString(avatarUrl)
        parcel.writeValue(icon)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<AddToWidget> {
        override fun createFromParcel(parcel: Parcel): AddToWidget {
            return AddToWidget(parcel)
        }

        override fun newArray(size: Int): Array<AddToWidget?> {
            return arrayOfNulls(size)
        }
    }
}
