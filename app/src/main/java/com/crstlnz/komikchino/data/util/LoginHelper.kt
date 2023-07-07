package com.crstlnz.komikchino.data.util

import android.content.Context
import androidx.core.content.ContextCompat
import com.crstlnz.komikchino.R
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.firebase.auth.FirebaseAuth

fun getGoogleSignInClient(context: Context): GoogleSignInClient {
    return GoogleSignIn.getClient(
        context,
        GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(ContextCompat.getString(context, R.string.web_client_id))
            .requestEmail()
            .build()
    )
}

fun logout(context: Context) {
    getGoogleSignInClient(context).signOut()
    FirebaseAuth.getInstance().signOut()
}