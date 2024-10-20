package com.iti4.retailhub.userauthuntication

import android.content.Context
import android.content.Intent
import android.content.IntentSender
import com.google.android.gms.auth.api.identity.BeginSignInRequest
import com.google.android.gms.auth.api.identity.BeginSignInRequest.GoogleIdTokenRequestOptions
import com.google.android.gms.auth.api.identity.Identity
import com.google.firebase.Firebase
import com.google.firebase.auth.AuthResult
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.auth.auth
import com.iti4.retailhub.R
import kotlinx.coroutines.tasks.await

class UserAuthuntication (
    private val context: Context
) : UserAuthunticationInterface {
val auth =Firebase.auth

    override suspend fun createUserWithEmailAndPassword(email: String, password: String): AuthResult? {
        return auth.createUserWithEmailAndPassword(email, password).await()
    }

    override suspend fun signInWithEmailAndPassword(email: String, password: String): AuthResult? {
        return auth.signInWithEmailAndPassword(email, password).await()
    }
    override  fun loginOut():Boolean {
        try {
            auth.signOut()
            return true
        } catch (e: Exception) {
            return false
        }
    }
    override suspend fun sendEmailVerification(user: FirebaseUser): Boolean {
        return try {
            user.sendEmailVerification().await()
            true // Success
        } catch (e: Exception) {
            false // Failure
        }
    }
  /*  override  suspend fun signIn(): IntentSender? = try {
        val result = Identity.getSignInClient(context).beginSignIn(buildSignInRequest()).await()
        result.pendingIntent.intentSender
    } catch (e: Exception) {
        null
    }
    private fun buildSignInRequest(): BeginSignInRequest {
            return BeginSignInRequest.Builder()
                .setGoogleIdTokenRequestOptions(
                    GoogleIdTokenRequestOptions.builder()
                        .setSupported(true)
                        .setFilterByAuthorizedAccounts(false)
                        .setServerClientId(context.getString(R.string.web_client_id))
                        .build()
                )
                .setAutoSelectEnabled(true)
                .build()
    }
    override suspend fun signInWithIntent(intent: Intent): AuthResult? {
        val credential = Identity.getSignInClient(context).getSignInCredentialFromIntent(intent)
        val googleIdToken = credential.googleIdToken
        if (googleIdToken != null) {
            val firebaseCredential = GoogleAuthProvider.getCredential(googleIdToken, null)
            return auth.signInWithCredential(firebaseCredential).await()
        }
        return null
    }


*/





    override suspend fun signWithGoogle(idToken: String): FirebaseUser? {
        return try {
            val credential = GoogleAuthProvider.getCredential(idToken, null)
            val authResult = auth.signInWithCredential(credential).await()
            authResult.user
        } catch (e: Exception) {
            null
        }
    }
}

