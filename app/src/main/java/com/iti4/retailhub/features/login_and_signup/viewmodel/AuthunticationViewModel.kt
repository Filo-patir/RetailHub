package com.iti4.retailhub.features.login_and_signup.viewmodel

import android.content.Intent
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.apollographql.apollo.api.Optional
import com.google.android.gms.common.api.ApiException
import com.google.firebase.auth.FirebaseAuthException
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.FirebaseAuthInvalidUserException
import com.google.firebase.auth.FirebaseAuthUserCollisionException
import com.google.firebase.auth.FirebaseAuthWeakPasswordException
import com.iti4.retailhub.datastorage.Repository
import com.iti4.retailhub.type.CustomerInput
import com.iti4.retailhub.userauthuntication.AuthState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.launch
import java.io.IOException
import javax.inject.Inject


@HiltViewModel
class UserAuthunticationViewModelViewModel @Inject constructor(private val reposatory: Repository) :
    ViewModel() {


    private val _authState = MutableStateFlow<AuthState>(AuthState.Messages("Idle"))
    val authState: StateFlow<AuthState> = _authState



    fun signIn(email: String, password: String) {
        viewModelScope.launch {
            _authState.value = AuthState.Loading
            try {
                val user = reposatory.signInWithEmailAndPassword(email, password)
                if (user?.user?.isEmailVerified == true) {

                    reposatory.addUserData(user.user!!.uid)

                    reposatory.getCustomerIdByEmail(email)
                        .catch { e ->
                              _authState.emit(AuthState.Messages(e.message!!))
                        }.collect {
                              reposatory.addUserShopLocalId(it.edges[0].node.id)
                              _authState.value = AuthState.Success(user.user)
                        }


                } else {
                    _authState.value =AuthState.Messages("Email is not verified")
                }
            }


            catch (e: FirebaseAuthInvalidCredentialsException) {
                _authState.value = AuthState.Messages("Email account does not exist")
            } catch (e: FirebaseAuthInvalidUserException) {
                _authState.value = AuthState.Messages("email or password is incorrect.")

            }
            catch (e: Exception) {
                _authState.value = AuthState.Messages("Error: ${e.message}")
            }




        }
    }

    fun createUser(userName: String, email: String, password: String) {
        val name = userName.split(" ")
        val firstName = name[0]
        val lastName = if (name.size > 1) name[1] else ""
        viewModelScope.launch {
            _authState.value = AuthState.Loading
            try {
                val user = reposatory.createUserWithEmailAndPassword(email, password)
                if (user?.user != null) {
                    val emailSent = reposatory.sendEmailVerification(user.user!!)
                    if (emailSent) {
                        reposatory.createUser(
                            CustomerInput(
                                firstName = Optional.present(firstName),
                                lastName = Optional.present(lastName),
                                email = Optional.Present(user.user?.email)
                            )
                        ).catch { e ->
                            _authState.emit(AuthState.Messages(e.message!!))
                        }.collect {
                            reposatory.addUserShopLocalId(it.customer?.id)
                            _authState.emit(AuthState.Messages("Verification email is send"))
                        }


                    } else {
                        _authState.value = AuthState.Messages("Failed to send verification email")
                    }
                } else {
                    _authState.value = AuthState.Messages("User creation failed")
                }
            } catch (e: FirebaseAuthWeakPasswordException) {
                _authState.value = AuthState.Messages("Password is too weak")
            } catch (e: FirebaseAuthInvalidCredentialsException) {
                _authState.value = AuthState.Messages("Invalid email format")
            } catch (e: FirebaseAuthUserCollisionException) {
                _authState.value = AuthState.Messages("Email is already in use")
            }
            catch (e: Exception) {
                _authState.value = AuthState.Messages("Error: ${e.message}")
            }
        }
    }



        fun setLoginStatus(loginStatus: String) {
            reposatory.setLoginStatus(loginStatus)
        }
    fun isguestMode(): Boolean {
        if( reposatory.getLoginStatus()=="guest"){
            return true
        }else{
            return false
        }
    }
    fun isUserLoggedIn(): Boolean {
       if( reposatory.getLoginStatus()!=null){
           return true
       }else{
           return false
       }
    }

    fun signWithGoogle(idToken: String) {
        viewModelScope.launch {
            _authState.value = AuthState.Loading
            try {
                val user = reposatory.signWithGoogle(idToken)
                if (user != null) {
                    reposatory.addUserData(user.uid)


                    reposatory.getCustomerIdByEmail(user.email.toString()).catch { e ->
//                        _authState.emit(AuthState.Messages(e.message!!))
                    }.collect {
                        if (it.edges.isEmpty()) {
                            reposatory.createUser(
                                CustomerInput(
                                    firstName = Optional.present(user.displayName),
                                    email = Optional.Present(user.email)
                                )
                            ).catch { e ->
                                _authState.emit(AuthState.Messages(e.message!!))
                            }.collect {
                                reposatory.setLoginStatus("login")
                                reposatory.addUserShopLocalId(it.customer?.id)
                                _authState.value = AuthState.Success(user)
                            }
                        } else {
                            reposatory.addUserShopLocalId(it.edges[0].node.id)
                            _authState.value = AuthState.Success(user)
                        }
                    }
                } else {
                    _authState.value = AuthState.Messages("Google sign-in failed")
                }
            } catch (e: Exception) {
                _authState.value = AuthState.Messages("Error: ${e.message}")
            }
        }
    }

}