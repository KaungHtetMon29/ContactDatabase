package com.example.contactdatabase.data

data class Contact(
    val id: Int = 0,
    val name: String,
    val dob: String,
    val email: String,
    val profileImage: ByteArray? = null
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as Contact

        if (id != other.id) return false
        if (name != other.name) return false
        if (dob != other.dob) return false
        if (email != other.email) return false
        if (profileImage != null) {
            if (other.profileImage == null) return false
            if (!profileImage.contentEquals(other.profileImage)) return false
        } else if (other.profileImage != null) return false

        return true
    }

    override fun hashCode(): Int {
        var result = id
        result = 31 * result + name.hashCode()
        result = 31 * result + dob.hashCode()
        result = 31 * result + email.hashCode()
        result = 31 * result + (profileImage?.contentHashCode() ?: 0)
        return result
    }
}
