package com.iti4.retailhub.userauthuntication

import android.content.IntentSender
import com.google.firebase.auth.FirebaseUser

/*sealed class AuthResultState {
        object Loading : AuthResultState()
        data class Success(val user: UserData) : AuthResultState()
        data class Failure(val message: String) : AuthResultState()
        data class SignInIntent(val intentSender: IntentSender) : AuthResultState()
        object SignedOut : AuthResultState()
    }
data class UserData(val userId: String, val userName: String?, val profilePicture: String?)
sealed class SignInResult {
    data class Success(val userData: UserData) : SignInResult()
    data class Failure(val errorMessage: String) : SignInResult()
}*/
sealed class AuthState {
    object Loading : AuthState()
    data class Success(val user: FirebaseUser?) : AuthState()
    data class Messages(val error: String) : AuthState()
}
