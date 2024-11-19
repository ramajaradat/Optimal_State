package com.example.mental_state.Model

class UserHistory(
    val email: String = "",
    val status: String = "",
    val day: Int = 0,
    val month: Int = 0,
    val year: Int = 0,
    val time: String = ""
) {

    constructor() : this("", "", 0, 0, 0, "")
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is UserHistory) return false
        return email == other.email && time == other.time && status == other.status
    }

    override fun hashCode(): Int {
        return listOf(email, time, status).hashCode()
    }
}
