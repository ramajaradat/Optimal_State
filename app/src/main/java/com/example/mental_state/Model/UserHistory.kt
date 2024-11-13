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
}
