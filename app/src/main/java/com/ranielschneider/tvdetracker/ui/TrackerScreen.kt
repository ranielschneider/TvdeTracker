package com.ranielschneider.tvdetracker.ui

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.provider.Settings
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.ranielschneider.tvdetracker.ui.components.DashboardStatsRow
import com.ranielschneider.tvdetracker.ui.components.FrequentZoneCard
import com.ranielschneider.tvdetracker.ui.components.HomeHeader
import com.ranielschneider.tvdetracker.ui.components.LastJourneyCard
import com.ranielschneider.tvdetracker.ui.components.LiveMapCard
import com.ranielschneider.tvdetracker.ui.components.LocationPermissionDialog
import com.ranielschneider.tvdetracker.ui.components.MainJourneyButton
import com.ranielschneider.tvdetracker.ui.viewmodel.TrackerViewModel


@Composable
fun TrackerScreen(
    onVerMapa: (Long) -> Unit,
    onVerHistorico: () -> Unit,
    onAbrirMenu: () -> Unit,
    viewModel: TrackerViewModel = viewModel()
) {

    val context = LocalContext.current


    val uiState by viewModel.uiState
        .collectAsStateWithLifecycle()


    val snackbarHostState = remember {
        SnackbarHostState()
    }


    var showLocationDialog by remember {
        mutableStateOf(false)
    }


    val preferences = remember(context) {

        context.getSharedPreferences(
            "tvde_prefs",
            Context.MODE_PRIVATE
        )
    }


    val nome = preferences
        .getString("nome", "")
        .orEmpty()



    var hasLocationPermission by remember {

        mutableStateOf(

            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED

        )
    }



    fun abrirConfiguracoes() {

        val intent = Intent(
            Settings.ACTION_APPLICATION_DETAILS_SETTINGS
        ).apply {

            data = Uri.parse(
                "package:${context.packageName}"
            )
        }


        context.startActivity(intent)
    }



    val permissionLauncher =

        rememberLauncherForActivityResult(

            contract =
                ActivityResultContracts.RequestMultiplePermissions()

        ) { permissions ->


            hasLocationPermission =

                permissions[
                    Manifest.permission.ACCESS_FINE_LOCATION
                ] == true ||

                        permissions[
                            Manifest.permission.ACCESS_COARSE_LOCATION
                        ] == true



            if (hasLocationPermission) {

                viewModel.startTracking()

            } else {

                showLocationDialog = true

            }
        }




    LaunchedEffect(uiState.errorMessage) {

        uiState.errorMessage?.let { message ->

            snackbarHostState.showSnackbar(message)

            viewModel.clearError()
        }
    }



    Scaffold(

        modifier = Modifier.fillMaxSize(),

        containerColor =
            MaterialTheme.colorScheme.background,

        snackbarHost = {

            SnackbarHost(
                hostState = snackbarHostState
            )
        }

    ) { scaffoldPadding ->



        if (showLocationDialog) {

            LocationPermissionDialog(

                onDismiss = {

                    showLocationDialog = false

                },

                onOpenSettings = {

                    showLocationDialog = false

                    abrirConfiguracoes()

                }
            )
        }



        LazyColumn(

            modifier = Modifier
                .fillMaxSize()
                .padding(scaffoldPadding)
                .background(
                    MaterialTheme.colorScheme.background
                ),


            contentPadding = PaddingValues(

                start = 20.dp,
                top = 20.dp,
                end = 20.dp,
                bottom = 24.dp

            ),


            verticalArrangement =
                Arrangement.spacedBy(18.dp)

        ) {



            item {

                HomeHeader(

                    nome = nome,

                    estado =
                        uiState.trackingState,

                    onAbrirMenu =
                        onAbrirMenu

                )
            }




            item {

                Column(

                    verticalArrangement =
                        Arrangement.spacedBy(12.dp)

                ) {


                    Text(

                        text =
                            "Resumo de hoje",

                        fontSize = 20.sp,

                        fontWeight =
                            FontWeight.Bold,

                        color =
                            MaterialTheme
                                .colorScheme
                                .onBackground

                    )



                    DashboardStatsRow(

                        tempoTotalHojeMs =
                            uiState.totalTimeTodayMs,

                        distanciaHojeKm =
                            uiState.distanceTodayKm,

                        tempoConduzidoHojeMs =
                            uiState.drivingTimeTodayMs,

                        quantidadeSessoes =
                            uiState.sessionsTodayCount,

                        carregando =
                            uiState.isLoading

                    )
                }
            }




            item {

                MainJourneyButton(

                    estado =
                        uiState.trackingState,


                    temPermissao =
                        hasLocationPermission,


                    onPedirPermissao = {

                        permissionLauncher.launch(

                            arrayOf(

                                Manifest.permission
                                    .ACCESS_FINE_LOCATION,

                                Manifest.permission
                                    .ACCESS_COARSE_LOCATION

                            )
                        )
                    },


                    onIniciar =
                        viewModel::startTracking,


                    onPausar =
                        viewModel::pauseTracking,


                    onRetomar =
                        viewModel::resumeTracking,


                    onEncerrar =
                        viewModel::stopTracking

                )
            }




            item {

                LiveMapCard(

                    pontos =
                        uiState.lastSessionPoints,


                    onOpenMap = {

                        uiState.lastSession?.let { session ->

                            onVerMapa(session.id)

                        }
                    }
                )
            }




            item {

                LastJourneyCard(

                    sessao =
                        uiState.lastSession,


                    onVerMapa =
                        onVerMapa,


                    onVerHistorico =
                        onVerHistorico

                )
            }




            item {

                FrequentZoneCard(

                    dailyDrivingTimes =
                        uiState.drivingTimeLast7Days,


                    isLoading =
                        uiState.isLoading

                )
            }
        }
    }
}