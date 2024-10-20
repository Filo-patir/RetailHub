package com.iti4.retailhub.userauthuntication

import android.content.Intent
import android.content.IntentSender
import com.google.firebase.auth.AuthResult
import com.google.firebase.auth.FirebaseUser

interface UserAuthunticationInterface {
    suspend fun createUserWithEmailAndPassword(email: String, password: String): AuthResult?
     fun loginOut(): Boolean
    suspend fun signInWithEmailAndPassword(email: String, password: String): AuthResult?
    /*suspend fun signInWithIntent(intent: Intent): AuthResult?
    suspend fun signIn(): IntentSender?*/
    suspend fun sendEmailVerification(user: FirebaseUser): Boolean
    suspend fun signWithGoogle(idToken: String): FirebaseUser?
}
