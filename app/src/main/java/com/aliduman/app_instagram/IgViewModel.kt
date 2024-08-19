package com.aliduman.app_instagram

import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import com.aliduman.app_instagram.data.Event
import com.aliduman.app_instagram.data.UserData
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.auth.User
import com.google.firebase.firestore.ktx.toObject
import com.google.firebase.storage.FirebaseStorage
import dagger.hilt.android.lifecycle.HiltViewModel
import java.lang.Exception
import javax.inject.Inject

const val USERS = "users"

@HiltViewModel
class IgViewModel @Inject constructor(
    private val auth: FirebaseAuth,
    private val firestore: FirebaseFirestore,
    private val storage: FirebaseStorage
) : ViewModel() {

    val signedIn = mutableStateOf(false)
    val inProgress = mutableStateOf(false)
    val userData = mutableStateOf<UserData?>(null)
    val popupNotification = mutableStateOf<Event<String>?>(null)

    init {
        //auth.signOut()
        val currentUser = auth.currentUser
        signedIn.value = currentUser != null
        currentUser?.uid?.let { uid ->
            getUserData(uid)
        }
    }

    fun onSignup(username: String, email: String, password: String){
        if (username.isEmpty() || email.isEmpty() || password.isEmpty()){
            handleException(customMessage = "Some fields are empty")
            return
        }

        inProgress.value = true
        firestore.collection(USERS).whereEqualTo("username", username).get()
            .addOnSuccessListener { documents ->
                if (documents.size() > 0){
                    handleException(customMessage = "Username already exists")
                    inProgress.value = false
                } else{
                    auth.createUserWithEmailAndPassword(email, password)
                        .addOnCompleteListener { task ->
                            if (task.isSuccessful){
                                signedIn.value = true
                                createOrUpdateProfile(username)

                            } else{
                                handleException(task.exception, "Signup failed")
                            }
                            inProgress.value = false
                        }
                }
            }
            .addOnFailureListener {

            }
    }

    fun onLogin(email: String, password: String){
        if (email.isEmpty() || password.isEmpty()){
            handleException(customMessage = "Please fill in all fields")
            return
        }

        inProgress.value = true
        auth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                if (task.isSuccessful){
                    signedIn.value = true
                    inProgress.value = false
                    auth.currentUser?.uid?.let { uid ->
                        handleException(customMessage = "Login successful")
                        getUserData(uid)
                    }
                } else{
                    handleException(task.exception, "Login failed")
                    inProgress.value = false
                }
            }
            .addOnFailureListener { exc ->
                handleException(exc, "Login failed")
                inProgress.value = false
            }
    }

    private fun createOrUpdateProfile(
        name: String? = null,
        username: String? = null,
        bio: String? = null,
        imageUrl: String? = null,
    ){
        val uid = auth.currentUser?.uid
        val userData = UserData(
            userId = uid,
            name = name ?: userData.value?.name,
            username = username ?: userData.value?.username,
            imageUrl = imageUrl ?: userData.value?.imageUrl,
            bio = bio ?: userData.value?.bio,
            following = userData.value?.following
        )

        uid?.let { uid ->
            inProgress.value = true
            firestore.collection(USERS).document(uid).get()
                .addOnSuccessListener {
                    if (it.exists()){
                        it.reference.update(userData.toMap())
                            .addOnSuccessListener {
                                this.userData.value = userData
                                inProgress.value = false
                            }
                            .addOnFailureListener {
                                handleException(it, "Can't update profile")
                                inProgress.value = false
                            }
                    }else{
                        firestore.collection(USERS).document().set(userData)
                        getUserData(uid)
                        inProgress.value = false
                    }
                }
                .addOnFailureListener { exc ->
                    handleException(exc, "Can't get user")
                    inProgress.value = false
                }

        }


    }

    private fun getUserData(uid: String){
        inProgress.value = true
        firestore.collection(USERS).document(uid).get()
            .addOnSuccessListener {
                val user = it.toObject<UserData>()
                userData.value = user
                inProgress.value = false
            }
            .addOnFailureListener {
                handleException(it, "Can't get user")
            }

    }

    fun handleException(exception: Exception? = null, customMessage: String? = "") {
        exception?.printStackTrace()
        val errorMsg = exception?.localizedMessage ?: ""
        val message = if (customMessage.isNullOrEmpty()) errorMsg else "$customMessage $errorMsg"
        popupNotification.value = Event(message)

    }

}