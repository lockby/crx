package com.crstlnz.komikchino.ui.screens

import android.util.Log
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Alignment.Companion.Center
import androidx.compose.ui.Alignment.Companion.CenterVertically
import androidx.compose.ui.Alignment.Companion.TopCenter
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.crstlnz.komikchino.R
import com.crstlnz.komikchino.config.nunito
import com.crstlnz.komikchino.data.database.model.User
import com.crstlnz.komikchino.data.util.getGoogleSignInClient
import com.crstlnz.komikchino.ui.components.ImageView
import com.crstlnz.komikchino.ui.navigations.MainNavigation
import com.crstlnz.komikchino.ui.theme.GoogleBlue
import com.crstlnz.komikchino.ui.util.noRippleClickable
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.common.api.ApiException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.firestore.FirebaseFirestore

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun LoginScreen(navController: NavController) {
    val context = LocalContext.current

    val googleSignInClient = remember { getGoogleSignInClient(context) }
    var loginProgress by remember { mutableStateOf(false) }
    fun signAnonymously() {
        if (FirebaseAuth.getInstance().currentUser?.isAnonymous == true) return MainNavigation.toHome(
            navController
        )
        FirebaseAuth.getInstance().signInAnonymously()
            .addOnSuccessListener {
                val user = FirebaseAuth.getInstance().currentUser
                if (user != null) {
                    MainNavigation.toHome(navController)
                    FirebaseFirestore.getInstance()
                        .collection("user_data")
                        .document(user.uid)
                        .set(
                            User(
                                id = user.uid,
                                name = user.displayName ?: "",
                                email = user.email ?: "",
                                img = user.photoUrl.toString(),
                            )
                        ).addOnSuccessListener {
                            Log.d("FIRESTORE SUCCESS", "INSERT USER DATA")
                        }.addOnFailureListener {
                            Log.d("FIRESTORE FAILED", it.stackTraceToString())
                        }
                } else {
                    loginProgress = false
                    Toast.makeText(context, "Gagal Login, user kosong!", Toast.LENGTH_LONG).show()
                }
            }.addOnFailureListener {
                Log.d("LOGIN ERROR", it.stackTraceToString().toString())
                loginProgress = false
                Toast.makeText(context, "Gagal Login!", Toast.LENGTH_LONG).show()
            }
    }

    val launcher =
        rememberLauncherForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            val task = GoogleSignIn.getSignedInAccountFromIntent(result.data)
            try {
                val account = task.getResult(ApiException::class.java)
                val idToken = account?.idToken ?: throw Error("Token missing!")
                val credential = GoogleAuthProvider.getCredential(idToken, null)
                FirebaseAuth.getInstance().signInWithCredential(credential).addOnSuccessListener {
                    if (!task.isSuccessful) {
                        Log.d("LOGIN ERROR", task.exception?.stackTraceToString().toString())
                        loginProgress = false
                        Toast.makeText(context, "Gagal Login!", Toast.LENGTH_LONG).show()
                    }
                }.addOnFailureListener {
                    Log.d("LOGIN ERROR", it.stackTraceToString())
                    loginProgress = false
                    Toast.makeText(context, "Gagal Login!", Toast.LENGTH_LONG).show()
                }
            } catch (e: ApiException) {
                Log.d("LOGIN ERROR", e.stackTraceToString())
                loginProgress = false
                Toast.makeText(context, "Gagal Login!", Toast.LENGTH_LONG).show()
            }
        }



    Scaffold(contentWindowInsets = WindowInsets.navigationBars) {
        Surface(
            Modifier
                .padding(it)
                .fillMaxSize()
        ) {
            Box(Modifier.fillMaxSize(), contentAlignment = TopCenter) {
                Column(
                    Modifier
                        .fillMaxSize()
                        .verticalScroll(state = rememberScrollState()),
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    ImageView(
                        url = "https://lockby.github.io/assets/img/login_bg.jpg",
                        contentDescription = "Login Banner",
                        modifier = Modifier
                            .fillMaxSize()
                            .weight(0.7f),
                        contentScale = ContentScale.Crop,
                    )
                    Box(Modifier.weight(0.3f), contentAlignment = Center) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier.padding(vertical = 40.dp)
                        ) {
                            Text(
                                stringResource(id = R.string.app_name),
                                style = MaterialTheme.typography.headlineLarge.copy(fontWeight = FontWeight.ExtraBold),
                                fontFamily = nunito
                            )
                            Spacer(
                                Modifier
                                    .heightIn(min = 30.dp)
                            )
                            Button(
                                onClick = {
                                    loginProgress = true
                                    launcher.launch(googleSignInClient.signInIntent)
                                },
                                enabled = !loginProgress,
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier
                                    .height(40.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = GoogleBlue),
                                contentPadding = PaddingValues(start = 2.5.dp, end = 8.dp)
                            ) {
                                Box(
                                    contentAlignment = Center,
                                    modifier = Modifier
                                        .align(CenterVertically)
                                        .background(
                                            color = Color.White,
                                            shape = RoundedCornerShape(8.dp)
                                        )
                                        .size(35.dp)
                                ) {
                                    Image(
                                        painterResource(id = R.drawable.google),
                                        contentDescription = "Google Icon",
                                        modifier = Modifier
                                            .size(18.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.width(10.dp))
                                Text(
                                    "Sign in with Google",
                                    modifier = Modifier.align(CenterVertically),
                                    style = MaterialTheme.typography.titleSmall.copy(
                                        color = Color.White,
                                        fontWeight = FontWeight.Medium
                                    ),
                                )
                            }
                        }
                    }
                    FlowRow(
                        horizontalArrangement = Arrangement.Center,
                        modifier = Modifier.padding(15.dp)
                    ) {
                        Text(
                            "By sign in, you agree to this app ",
                            style = MaterialTheme.typography.labelSmall,
                            textAlign = TextAlign.Center
                        )
                        Text(
                            "Terms of Service",
                            style = MaterialTheme.typography.labelSmall.copy(color = MaterialTheme.colorScheme.primary),
                            textAlign = TextAlign.Center,
                            modifier = Modifier.noRippleClickable {
                                MainNavigation.toWebView(
                                    navController,
                                    "file:///android_asset/term.html"
                                )
                            }
                        )
                        Text(
                            " and ",
                            style = MaterialTheme.typography.labelSmall,
                            textAlign = TextAlign.Center
                        )
                        Text(
                            "Privacy Policy.",
                            style = MaterialTheme.typography.labelSmall.copy(color = MaterialTheme.colorScheme.primary),
                            textAlign = TextAlign.Center,
                            modifier = Modifier.noRippleClickable {
                                MainNavigation.toWebView(
                                    navController,
                                    "file:///android_asset/policy.html"
                                )
                            }
                        )
                    }
                }

//                FilledTonalButton(
//                    onClick = { signAnonymously() },
//                    modifier = Modifier
//                        .align(TopEnd)
//                        .safeDrawingPadding()
//                        .padding(vertical = 10.dp, horizontal = 15.dp),
//                    enabled = !loginProgress
//                ) {
//                    Text(text = "Skip")
//                }
            }
        }
    }
}