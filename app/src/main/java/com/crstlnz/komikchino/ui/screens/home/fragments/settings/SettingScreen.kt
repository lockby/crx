package com.crstlnz.komikchino.ui.screens.home.fragments.settings

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.ime
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment.Companion.Center
import androidx.compose.ui.Alignment.Companion.CenterVertically
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.crstlnz.komikchino.LocalStatusBarPadding
import com.crstlnz.komikchino.R
import com.crstlnz.komikchino.data.api.KomikServer
import com.crstlnz.komikchino.data.util.logout
import com.crstlnz.komikchino.ui.components.ImageView
import com.crstlnz.komikchino.ui.navigations.HomeSections
import com.crstlnz.komikchino.ui.navigations.MainNavigation
import com.google.firebase.auth.FirebaseAuth
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingScreen(navController: NavController) {
    val v = hiltViewModel<SettingViewModel>()
    val context = LocalContext.current

    val komikServer by v.settings.komikServer.collectAsState(initial = KomikServer.KIRYUU)
    val homePage by v.settings.homepage.collectAsState(initial = HomeSections.HOME)

    Column {
        val user = FirebaseAuth.getInstance().currentUser
        val image = user?.photoUrl
        TopAppBar(
            modifier = Modifier.padding(top = LocalStatusBarPadding.current),
            windowInsets = WindowInsets.ime,
            title = {
                Row(verticalAlignment = CenterVertically) {
                    Image(
                        painter = painterResource(id = R.mipmap.app_icon),
                        contentDescription = "App Icon",
                        modifier = Modifier.height(38.dp)
                    )
                    Spacer(Modifier.width(12.dp))
                    Text(
                        stringResource(HomeSections.SETTINGS.title),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            },
            actions = {
                IconButton(
                    onClick = {
                        navController.navigate(MainNavigation.SEARCH)
                    },
                ) {
                    Icon(
                        painter = painterResource(id = R.drawable.baseline_search_24),
                        contentDescription = "Search",
                    )
                }
                Spacer(modifier = Modifier.width(5.dp))
            },
        )
        LazyColumn(
            Modifier
                .fillMaxSize()
                .background(color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.025f))
        ) {
            item {
                Box(
                    Modifier
                        .background(color = MaterialTheme.colorScheme.surface)
                        .clickable {
                            if (FirebaseAuth.getInstance().currentUser?.isAnonymous == true)
                                MainNavigation.toLogin(navController)
                        }) {

                    if (FirebaseAuth.getInstance().currentUser?.isAnonymous == false) {
                        Row(
                            Modifier
                                .padding(20.dp)
                                .fillMaxWidth(), verticalAlignment = CenterVertically
                        ) {
                            ImageView(
                                url = image?.toString() ?: "",
                                contentDescription = "Profile Image",
                                modifier = Modifier
                                    .size(40.dp)
                                    .clip(CircleShape)
                            )
                            Spacer(Modifier.width(20.dp))
                            Column {
                                Text(
                                    user?.displayName ?: "",
                                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                                )
                                Spacer(Modifier.width(5.dp))
                                Text(
                                    user?.email ?: "",
                                    style = MaterialTheme.typography.bodyMedium
                                )
                            }
                        }
                    } else {
                        Row(
                            Modifier
                                .padding(20.dp)
                                .fillMaxWidth(), verticalAlignment = CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(40.dp)
                                    .clip(CircleShape),
                                contentAlignment = Center
                            ) {
                                Icon(
                                    painter = painterResource(id = R.drawable.user),
                                    contentDescription = "User Icon"
                                )
                            }
                            Spacer(Modifier.width(20.dp))
                            Column {
                                Text(
                                    "Login",
                                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                                )
                                Spacer(Modifier.width(5.dp))
                                Text(
                                    "Get cloud save now!",
                                    style = MaterialTheme.typography.bodyMedium
                                )
                            }
                        }
                    }
                }
            }

            item {
                Divider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.1f))
                Text("Preferences", modifier = Modifier.padding(15.dp))
            }

            item {
                ListItem(
                    modifier = Modifier.clickable {
                        navController.navigate(MainNavigation.SERVER_SELECTION)
                    },
                    headlineContent = {
                        Text("Server")
                    },
                    trailingContent = {
                        Text(komikServer.value.replaceFirstChar {
                            if (it.isLowerCase()) it.titlecase(
                                Locale.ROOT
                            ) else it.toString()
                        })
                    },
                    leadingContent = {
                        Icon(
                            painter = painterResource(id = R.drawable.web),
                            contentDescription = "Server Icon"
                        )
                    }
                )
            }

            item {
                ListItem(
                    modifier = Modifier.clickable {
                        navController.navigate(MainNavigation.HOME_SELECTION)
                    },
                    headlineContent = {
                        Text("Homepage")
                    },
                    trailingContent = {
                        Text(stringResource(id = homePage.title))
                    },
                    leadingContent = {
                        Icon(
                            painter = painterResource(id = R.drawable.home_wifi),
                            contentDescription = "Home change icon"
                        )
                    }
                )
            }

            item {
                Text("Applications", modifier = Modifier.padding(15.dp))
            }

            item {
                ListItem(
                    modifier = Modifier.clickable {
                        navController.navigate(MainNavigation.CACHE_SCREEN)
                    },
                    headlineContent = {
                        Text("Cache")
                    },
                    leadingContent = {
                        Icon(
                            painter = painterResource(id = R.drawable.cache),
                            contentDescription = "Cache icon"
                        )
                    }
                )
            }

            item {
                ListItem(
                    modifier = Modifier.clickable {
                        navController.navigate(MainNavigation.CHECK_UPDATE)
                    },
                    headlineContent = {
                        Text("Check for updates")
                    },
                    leadingContent = {
                        Icon(
                            painter = painterResource(id = R.drawable.sync),
                            contentDescription = "Update icon"
                        )
                    }
                )
            }

            item {
                if (FirebaseAuth.getInstance().currentUser?.isAnonymous != true) {
                    ListItem(
                        modifier = Modifier.clickable {
                            logout(context)
                        },
                        headlineContent = {
                            Text("Logout")
                        },
                        leadingContent = {
                            Icon(
                                painter = painterResource(id = R.drawable.logout),
                                contentDescription = "Logout icon"
                            )
                        }
                    )
                } else {
                    ListItem(
                        modifier = Modifier.clickable {
                            MainNavigation.toLogin(navController)
                        },
                        headlineContent = {
                            Text("Login")
                        },
                        leadingContent = {
                            Icon(
                                painter = painterResource(id = R.drawable.logout),
                                contentDescription = "Logout icon"
                            )
                        }
                    )
                }
            }

//        item {
//            ListItem(
//                modifier = Modifier.clickable {
//                    navController.navigate(MainNavigation.APP_INFO)
//                },
//                headlineContent = {
//                    Text("App info")
//                },
//                leadingContent = {
//                    Icon(
//                        painter = painterResource(id = R.drawable.info),
//                        contentDescription = "Update icon"
//                    )
//                }
//            )
//        }
        }
    }
}