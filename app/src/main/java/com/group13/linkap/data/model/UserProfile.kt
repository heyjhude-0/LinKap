
package com.group13.linkap.data.model

data class UserProfile(
    val uid: String,
    val fullName: String,
    val email: String,
    val role: String,
    val barangayId: String,
    val isActive: Boolean
)
