package com.example.mentalstate.Model

data class UserInformation(
    var firstName: String = "",
    var lastName: String = "",
    var dob: String = "",
    var isprovider: String = "",
    var email: String = "",
)
{
    constructor() : this("", "", "", "", "")

}


