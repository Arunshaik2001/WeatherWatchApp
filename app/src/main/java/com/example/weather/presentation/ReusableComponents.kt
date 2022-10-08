/*
 * Copyright 2021 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.example.weather.presentation

import android.Manifest
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.LocationOn
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.wear.compose.material.*
import com.example.weather.R
import com.example.weather.presentation.theme.Wear2Theme
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.rememberMultiplePermissionsState

/* Contains individual Wear OS demo composables for the code lab. */


@Composable
fun ButtonWidget(
    modifier: Modifier = Modifier,
    iconModifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.Center
    ) {
        // Button
        Button(
            modifier = Modifier.size(ButtonDefaults.LargeButtonSize),
            onClick = { onClick() },
        ) {
            Icon(
                imageVector = Icons.Rounded.LocationOn,
                contentDescription = "triggers phone action",
                modifier = iconModifier
            )
        }
    }
}

@Composable
fun TextWidget(modifier: Modifier = Modifier, text: String) {
    Text(
        modifier = modifier,
        textAlign = TextAlign.Center,
        color = MaterialTheme.colors.primary,
        text = text
    )
}

@Composable
fun CardWidget(
    modifier: Modifier = Modifier,
    title: String,
    weatherDescription: String,
    time: String,
    temperature: Double
) {
    AppCard(
        modifier = modifier,
        appName = { Text("Weather Details", color = Color.White) },
        time = { Text(time, color = if (temperature < 20) Color.White else Color.Red) },
        title = { Text(title, color = Color.Yellow) },
        onClick = {}
    ) {

        val icon = if(temperature < 20.0) R.mipmap.cold else R.mipmap.hot

        Row(horizontalArrangement = Arrangement.Center) {
            Image(
                modifier = Modifier.height(20.dp),
                painter = painterResource(id = icon),
                contentDescription = "",
            )
            Spacer(modifier = Modifier.size(5.dp))
            Text(weatherDescription)
        }

    }
}

@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun WearApp(locationUtil: LocationUtil) {

    val listState = rememberScalingLazyListState()

    Wear2Theme {
        val locationPermissionsState = rememberMultiplePermissionsState(
            listOf(
                Manifest.permission.ACCESS_COARSE_LOCATION,
                Manifest.permission.ACCESS_FINE_LOCATION,
            )
        )

        val contentModifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 8.dp)
        val iconModifier = Modifier
            .size(24.dp)
            .wrapContentSize(align = Alignment.Center)

        ScalingLazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(
                top = 32.dp,
                start = 8.dp,
                end = 8.dp,
                bottom = 32.dp
            ),
            verticalArrangement = Arrangement.Bottom,
            state = listState,
            autoCentering = true
        ) {
            item { Spacer(modifier = Modifier.size(20.dp)) }

            if (locationPermissionsState.allPermissionsGranted){
                if (!locationUtil.dataLoaded.value)
                    item { TextWidget(contentModifier,"Thanks! I can access your exact location :D") }
                else{
                    item { CardWidget(
                        modifier = contentModifier,
                        title = locationUtil.data.value.name ,
                        weatherDescription =  locationUtil.data.value.weatherDescription,
                        time = locationUtil.data.value.time , temperature = 12.0)
                    }
                }
            }
            else{
                val allPermissionsRevoked =
                    locationPermissionsState.permissions.size ==
                            locationPermissionsState.revokedPermissions.size


                val textToShow = if (!allPermissionsRevoked) {
                    "Yay! Thanks for letting me access your approximate location. " +
                            "But you know what would be great? If you allow me to know where you " +
                            "exactly are. Thank you!"
                } else if (locationPermissionsState.shouldShowRationale) {
                    "Getting your exact location is important for this app. " +
                            "Please grant us fine location. Thank you :D"
                } else {
                    "This feature requires location permission"
                }




                item { TextWidget(contentModifier,textToShow) }
                item { ButtonWidget(contentModifier, iconModifier){
                    locationPermissionsState.launchMultiplePermissionRequest()
                } }
            }

        }
    }
}

