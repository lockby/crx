package com.crstlnz.komikchino.ui.screens.home.fragments.settings

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.crstlnz.komikchino.R
import com.crstlnz.komikchino.data.api.KomikServer
import com.crstlnz.komikchino.ui.navigations.HomeSections
import com.crstlnz.komikchino.ui.navigations.MainNavigation
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingScreen(navController: NavController) {
    val v = hiltViewModel<SettingViewModel>()
    val context = LocalContext.current

    val komikServer by v.settings.komikServer.collectAsState(initial = KomikServer.KIRYUU)
    val homePage by v.settings.homepage.collectAsState(initial = HomeSections.HOME)

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
                        Toast
                            .makeText(context, "Belum diimplementasi bodo!", Toast.LENGTH_SHORT)
                            .show()
                    }) {
                Row(
                    Modifier
                        .padding(20.dp)
                        .fillMaxWidth(), verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        Modifier.background(
                            color = MaterialTheme.colorScheme.surfaceVariant,
                            CircleShape
                        )
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Person,
                            contentDescription = "Account",
                            modifier = Modifier
                                .padding(15.dp)
                                .width(40.dp)
                                .height(40.dp)
                        )
                    }
                    Spacer(Modifier.width(20.dp))
                    Column() {
                        Text(
                            "Login",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                        )
                        Spacer(Modifier.width(5.dp))
                        Text(
                            "Enable cloud save",
                            style = MaterialTheme.typography.bodyMedium
                        )
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
                    Text(komikServer.value.capitalize(Locale.ROOT))
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