package com.crstlnz.komikchino.ui.screens.home.fragments.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.Card
import androidx.compose.material.Divider
import androidx.compose.material.Icon
import androidx.compose.material.MaterialTheme
import androidx.compose.material.SnackbarHostState
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Person
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.crstlnz.komikchino.ui.screens.home.LocalSnackbarHostState
import com.crstlnz.komikchino.ui.theme.Black2
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@Composable
fun SettingScreen(navController: NavController) {
    LazyColumn(
        Modifier
            .fillMaxSize()
    ) {
        item {
            Box(Modifier.clickable { }) {
                Row(
                    Modifier
                        .padding(20.dp)
                        .fillMaxWidth(), verticalAlignment = Alignment.CenterVertically
                ) {
                    Card(backgroundColor = Black2, shape = CircleShape) {
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
                            style = MaterialTheme.typography.h6.copy(fontWeight = FontWeight.Bold)
                        )
                        Spacer(Modifier.width(5.dp))
                        Text(
                            "Aktivasi cloud save",
                            style = MaterialTheme.typography.body2
                        )
                    }
                }
            }
        }
        item {
            Divider()
        }
    }
}