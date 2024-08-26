package com.aliduman.app_instagram

import android.net.Uri
import android.util.Log
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import com.aliduman.app_instagram.data.CommentData
import com.aliduman.app_instagram.data.Event
import com.aliduman.app_instagram.data.PostData
import com.aliduman.app_instagram.data.UserData
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.QuerySnapshot
import com.google.firebase.firestore.ktx.toObject
import com.google.firebase.storage.FirebaseStorage
import dagger.hilt.android.lifecycle.HiltViewModel
import java.util.Locale
import java.util.UUID
import javax.inject.Inject

const val USERS = "users"
const val POSTS = "posts"
const val COMMENTS = "comments"

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

    val refreshPostsProgress = mutableStateOf(false)//like in progress
    val posts = mutableStateOf<List<PostData>>(listOf())

    val searchedPosts = mutableStateOf<List<PostData>>(listOf())
    val searchInProgress = mutableStateOf(false)

    val postsFeed = mutableStateOf<List<PostData>>(listOf())
    val postsFeedLoading = mutableStateOf(false)

    val comments = mutableStateOf<List<CommentData>>(listOf())
    val commentsLoading = mutableStateOf(false)

    val followers = mutableStateOf<List<UserData>>(listOf())

    init {
        //auth.signOut()
        val currentUser = auth.currentUser

        signedIn.value = currentUser != null
        currentUser?.uid?.let { uid ->
            getUserData(uid)
            Log.d("myuserdatainit", uid)
        }
    }

    fun onSignup(username: String, email: String, password: String) {
        if (username.isEmpty() or email.isEmpty() or password.isEmpty()) {
            handleException(customMessage = "Please fill in all fields")
            return
        }

        inProgress.value = true

        firestore.collection(USERS).whereEqualTo("username", username).get()
            .addOnSuccessListener { documents ->
                if (documents.size() > 0) {
                    handleException(customMessage = "Username already exists")
                    inProgress.value = false
                } else {
                    auth.createUserWithEmailAndPassword(email, password)
                        .addOnCompleteListener { task ->
                            if (task.isSuccessful) {
                                signedIn.value = true
                                createOrUpdateProfile(username)
                            } else {
                                handleException(task.exception, "Signup failed")
                            }
                            inProgress.value = false
                        }
                }
            }
            .addOnFailureListener {

            }
    }

    fun onLogin(email: String, password: String) {
        if (email.isEmpty() or password.isEmpty()) {
            handleException(customMessage = "Please fill in all fields")
            return
        }

        inProgress.value = true
        auth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    signedIn.value = true
                    inProgress.value = false
                    auth.currentUser?.uid?.let { uid ->
                        //handleException(customMessage = "Login successful")
                        getUserData(uid)
                    }
                } else {
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
    ) {
        val uid = auth.currentUser?.uid
        val userData = UserData(
            userId = uid,
            name = name ?: userData.value?.name,
            username = username ?: userData.value?.username,
            bio = bio ?: userData.value?.bio,
            imageUrl = imageUrl ?: userData.value?.imageUrl,
            following = userData.value?.following
        )

        uid?.let { uid ->
            inProgress.value = true
            firestore.collection(USERS).document(uid).get()
                .addOnSuccessListener {
                    if (it.exists()) {
                        it.reference.update(userData.toMap())
                            .addOnSuccessListener {
                                this.userData.value = userData
                                inProgress.value = false
                            }
                            .addOnFailureListener { exc ->
                                handleException(exc, "Can't update profile")
                                inProgress.value = false
                            }
                    } else {
                        firestore.collection(USERS).document(uid).set(userData)
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

    private fun getUserData(uid: String) {
        Log.d("myuserdata", "start getUserData")
        inProgress.value = true
        firestore.collection(USERS).document(uid).get()
            .addOnSuccessListener {
                Log.d("myuserdata", "success getUserData ${userData.value?.name}")

                val user = it.toObject<UserData>()
                userData.value = user
                inProgress.value = false
                refreshPosts()
                getPersonalizedFeed()
                getFollowers(user?.userId)
                println("succesfully get userdata")
            }.addOnFailureListener {
                Log.d("myuserdata", "failed getUserData")
                handleException(it, "Can't get user")
                inProgress.value = false
            }
    }

    fun handleException(exception: Exception? = null, customMessage: String? = "") {
        exception?.printStackTrace()
        val errorMsg = exception?.localizedMessage ?: ""
        val message = if (customMessage.isNullOrEmpty()) errorMsg else "$customMessage $errorMsg"
        popupNotification.value = Event(message)

    }

    fun updateProfileData(name: String, username: String, bio: String) {
        createOrUpdateProfile(name, username, bio)
    }

    private fun uploadImage(uri: Uri, onSuccess: (Uri) -> Unit) {
        inProgress.value = true
        val storageRef = storage.reference
        val uuid = UUID.randomUUID().toString()
        val imageRef = storageRef.child("images/$uuid")
        val uploadTask = imageRef.putFile(uri)

        uploadTask.addOnSuccessListener {
            val result = it.metadata?.reference?.downloadUrl
            result?.addOnSuccessListener(onSuccess)

        }
            .addOnFailureListener {
                handleException(it, "Can't upload image")
                inProgress.value = false
            }
    }

    fun uploadProfileImage(uri: Uri) {
        uploadImage(uri) { url ->
            createOrUpdateProfile(imageUrl = url.toString())
            updatePostUserImageData(url.toString())
        }
    }

    private fun updatePostUserImageData(imageUrl: String) {
        val currentUserId = auth.currentUser?.uid
        firestore.collection(POSTS).whereEqualTo("userId", currentUserId).get()
            .addOnSuccessListener {
                val posts = mutableStateOf<List<PostData>>(arrayListOf())
                convertPosts(it, posts)
                val refs = arrayListOf<DocumentReference>()
                for (post in posts.value) {
                    post.postId?.let { id ->
                        refs.add(firestore.collection(POSTS).document(id))
                    }
                }
                if (refs.isNotEmpty()) {
                    firestore.runBatch { batch ->
                        for (ref in refs) {
                            batch.update(ref, "userImage", imageUrl)
                        }
                    }
                        .addOnSuccessListener {
                            refreshPosts()
                        }
                }
            }
    }

    fun onLogout() {
        auth.signOut()
        signedIn.value = false
        userData.value = null
        popupNotification.value = Event("Logged out successfully")
        searchedPosts.value = listOf()
        postsFeed.value = listOf()
        comments.value = listOf()
        //delte below if any problems
        posts.value = listOf()
        refreshPostsProgress.value = false
        searchInProgress.value = false
        inProgress.value = false
        Log.d("myuserdata", "logout")
    }

    fun onNewPost(uri: Uri, description: String, onPostSuccess: () -> Unit) {
        uploadImage(uri) {
            onCreatePost(it, description, onPostSuccess)
        }
    }

    private fun onCreatePost(
        imageUrl: Uri? = null,
        description: String? = null,
        onPostSuccess: () -> Unit
    ) {
        inProgress.value = true
        val currentUid = auth.currentUser?.uid
        val currentUsername = userData.value?.username
        val currentUserImage = userData.value?.imageUrl

        if (currentUid != null) {
            val postId = UUID.randomUUID().toString()

            val fillerWords = listOf(
                "the",
                "is",
                "be",
                "to",
                "of",
                "and",
                "a",
                "in",
                "that",
                "have",
                "I",
                "it",
                "for",
                "not",
                "on",
                "with",
                "he",
                "as",
                "you",
                "do",
                "at",
                "this",
                "but",
                "his",
                "by",
                "from",
                "they"
            )
            val searchTerms =
                description
                    .orEmpty()
                    .split(" ", ".", "!", "?", ",", ";", ":", "(", ")")
                    .map { it.lowercase(Locale.getDefault()) }
                    .filter { it.isNotEmpty() && !fillerWords.contains(it) }

            val post = PostData(
                postId = postId,
                userId = currentUid,
                username = currentUsername,
                userImage = currentUserImage,
                postImage = imageUrl.toString(),
                postDescription = description,
                time = System.currentTimeMillis(),
                likes = listOf<String>(),
                searchTerms = searchTerms
            )
            firestore.collection(POSTS).document(postId).set(post)
                .addOnSuccessListener {
                    popupNotification.value = Event("Post successfully created")
                    inProgress.value = false
                    refreshPosts()
                    onPostSuccess.invoke()
                }
                .addOnFailureListener {
                    handleException(it, "Can't create post")
                    inProgress.value = false
                }

        } else {
            handleException(customMessage = "User name unavailable. Unable to create post.")
            onLogout()
            inProgress.value = false
        }

    }

    private fun refreshPosts() {
        val currentUserId = auth.currentUser?.uid
        if (currentUserId != null) {
            refreshPostsProgress.value = true
            firestore.collection(POSTS).whereEqualTo("userId", currentUserId).get()
                .addOnSuccessListener { documents ->
                    convertPosts(documents, posts)
                    refreshPostsProgress.value = false
                }
                .addOnFailureListener {
                    handleException(it, "Can't fetch posts")
                    refreshPostsProgress.value = false
                }

        } else {
            handleException(customMessage = "User name unavailable. Unable to refresh post.")
            onLogout()
        }
    }

    private fun convertPosts(documents: QuerySnapshot, outState: MutableState<List<PostData>>) {
        val posts = mutableListOf<PostData>()
        for (doc in documents) {
            val post = doc.toObject<PostData>()
            posts.add(post)
        }
        val sortedPosts = posts.sortedByDescending { it.time }
        outState.value = sortedPosts
    }

    fun searchPosts(searchTerm: String) {
        if (searchTerm.isEmpty()) {
            searchedPosts.value = listOf()
            return
        } else {
            searchInProgress.value = true
            firestore.collection(POSTS)
                .whereArrayContains("searchTerms", searchTerm.trim().lowercase())
                .get()
                .addOnSuccessListener {
                    convertPosts(it, searchedPosts)
                    searchInProgress.value = false
                }
                .addOnFailureListener {
                    handleException(it, "Can't search posts")
                    searchInProgress.value = false
                }

        }
    }

    fun onFollowClick(userId: String) {
        auth.currentUser?.uid?.let { uid ->
            val following = arrayListOf<String>()
            userData.value?.following?.let {
                following.addAll(it)
            }
            if (following.contains(userId)) {
                following.remove(userId)
            } else {
                following.add(userId)
            }
            firestore.collection(USERS).document(uid).update("following", following)
                .addOnSuccessListener {
                    getUserData(uid)
                }
        }
    }

    private fun getPersonalizedFeed() {
        val following = userData.value?.following
        if (!following.isNullOrEmpty()) {
            postsFeedLoading.value = true
            firestore.collection(POSTS).whereIn("userId", following).get()
                .addOnSuccessListener {
                    convertPosts(it, postsFeed)
                    if (postsFeed.value.isEmpty()) {
                        getGeneralFeed()
                    } else {
                        postsFeedLoading.value = false
                    }
                }
                .addOnFailureListener {
                    handleException(it, "Can't get personalized feeds")
                    postsFeedLoading.value = false
                }
        } else {
            getGeneralFeed()
        }

    }

    private fun getGeneralFeed() {
        val currentTime = System.currentTimeMillis()
        val difference = 24 * 60 * 60 * 1000 // 1 day in millis
        firestore.collection(POSTS).whereGreaterThan("time", currentTime - difference).get()
            .addOnSuccessListener {
                convertPosts(it, postsFeed)
                postsFeedLoading.value = false
            }
            .addOnFailureListener {
                handleException(it, "Can't get general feed")
                postsFeedLoading.value = false
            }
    }

    fun onLikePost(postData: PostData) {
        auth.currentUser?.uid?.let { userId ->
            postData.likes?.let { likes ->
                val newLikes = arrayListOf<String>()
                if (likes.contains(userId)) {
                    newLikes.addAll(likes.filter { userId != it })
                } else {
                    newLikes.addAll(likes)
                    newLikes.add(userId)
                }
                postData.postId?.let { postId ->
                    firestore.collection(POSTS).document(postId).update("likes", newLikes)
                        .addOnSuccessListener {
                            postData.likes = newLikes
                        }
                        .addOnFailureListener {
                            handleException(it, "Unable to like post")
                        }
                }
            }
        }
    }

    fun createComment(postId: String, text: String) {
        userData.value?.username?.let { username ->
            val commentId = UUID.randomUUID().toString()
            val comment = CommentData(
                commentId = commentId,
                postId = postId,
                username = username,
                text = text,
                timestamp = System.currentTimeMillis()
            )
            firestore.collection(COMMENTS).document(commentId).set(comment)
                .addOnSuccessListener {
                    getComments(postId = postId)
                }
                .addOnFailureListener {
                    handleException(it, "can not create comment.")
                }
        }
    }

    fun getComments(postId: String?) {
        commentsLoading.value = true
        firestore.collection(COMMENTS).whereEqualTo("postId", postId).get()
            .addOnSuccessListener { documents ->
                val newComments = mutableListOf<CommentData>()
                for (doc in documents) {
                    val comment = doc.toObject<CommentData>()
                    newComments.add(comment)
                }
                val sortedComments = newComments.sortedByDescending { it.timestamp }
                comments.value = sortedComments
                commentsLoading.value = false
            }
            .addOnFailureListener {
                handleException(it, "can not get comments")
                commentsLoading.value = false
            }
    }

    private fun getFollowers(uid: String?) {
        firestore.collection(USERS).whereArrayContains("following", uid ?: "").get()
            .addOnSuccessListener { documents ->
                val newFollowers = mutableListOf<UserData>()
                for (doc in documents) {
                    val user = doc.toObject<UserData>()
                    newFollowers.add(user)
                }
                followers.value = newFollowers
            }
            .addOnFailureListener {
                handleException(it, "can not get followers")
            }
    }



}