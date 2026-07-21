package com.ranielschneider.tvdetracker

import android.content.Context
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import com.ranielschneider.tvdetracker.ui.AppDrawer
import com.ranielschneider.tvdetracker.ui.HistoricoScreen
import com.ranielschneider.tvdetracker.ui.MapaScreen
import com.ranielschneider.tvdetracker.ui.PerfilScreen
import com.ranielschneider.tvdetracker.ui.ResumoScreen
import com.ranielschneider.tvdetracker.ui.RotasScreen
import com.ranielschneider.tvdetracker.ui.TrackerScreen
import com.ranielschneider.tvdetracker.ui.components.HomeBottomNavigation
import com.ranielschneider.tvdetracker.ui.components.HomeNavigationDestination
import com.ranielschneider.tvdetracker.ui.theme.TvdeTrackerTheme
import kotlinx.coroutines.launch



class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {

            TvdeTrackerTheme {

                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {

                    AppScreen()
                }
            }
        }
    }
}


private enum class AppDestination {
    TRACKER,
    HISTORICO,
    ROTAS,
    RESUMO,
    PERFIL,
    MAPA
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppScreen() {

    var destinoAtual by remember {
        mutableStateOf(AppDestination.TRACKER)
    }

    var sessaoSelecionadaId by remember {
        mutableLongStateOf(-1L)
    }


    val drawerState = rememberDrawerState(
        initialValue = DrawerValue.Closed
    )

    val scope = rememberCoroutineScope()

    val context = LocalContext.current


    val prefs = remember(context) {

        context.getSharedPreferences(
            "tvde_prefs",
            Context.MODE_PRIVATE
        )
    }


    var nome by remember {

        mutableStateOf(
            prefs.getString("nome", "")
                .orEmpty()
        )
    }


    var matricula by remember {

        mutableStateOf(
            prefs.getString("matricula", "")
                .orEmpty()
        )
    }


    LaunchedEffect(destinoAtual) {

        nome =
            prefs.getString("nome", "")
                .orEmpty()

        matricula =
            prefs.getString("matricula", "")
                .orEmpty()
    }


    ModalNavigationDrawer(

        drawerState = drawerState,

        gesturesEnabled =
            destinoAtual != AppDestination.MAPA,

        drawerContent = {

            AppDrawer(

                nomeUtilizador = nome,

                matricula = matricula,

                telaAtual =
                    destinoAtual.toDrawerRoute(),


                onPerfil = {
                    destinoAtual =
                        AppDestination.PERFIL
                },


                onHistorico = {
                    destinoAtual =
                        AppDestination.HISTORICO
                },


                onRotas = {
                    destinoAtual =
                        AppDestination.ROTAS
                },


                onResumo = {
                    destinoAtual =
                        AppDestination.RESUMO
                },


                onFechar = {

                    scope.launch {
                        drawerState.close()
                    }
                }
            )
        }

    ) {


        Scaffold(

            modifier = Modifier.fillMaxSize(),


            bottomBar = {

                HomeBottomNavigation(

                    selectedDestination =
                        destinoAtual
                            .toBottomNavigationDestination(),


                    onHomeClick = {

                        destinoAtual =
                            AppDestination.TRACKER
                    },


                    onRoutesClick = {

                        destinoAtual =
                            AppDestination.ROTAS
                    },


                    onStatisticsClick = {

                        destinoAtual =
                            AppDestination.RESUMO
                    },


                    onSettingsClick = {

                        destinoAtual =
                            AppDestination.PERFIL
                    }
                )
            }

        ) { paddingValues ->


            Box(

                modifier = Modifier
                    .fillMaxSize()
                    .padding(
                        bottom = paddingValues.calculateBottomPadding()
                    )

            ) {


                when (destinoAtual) {


                    AppDestination.TRACKER -> {

                        TrackerScreen(

                            onVerMapa = { sessaoId ->

                                sessaoSelecionadaId =
                                    sessaoId

                                destinoAtual =
                                    AppDestination.MAPA
                            },


                            onVerHistorico = {

                                destinoAtual =
                                    AppDestination.HISTORICO
                            },


                            onAbrirMenu = {

                                scope.launch {

                                    drawerState.open()
                                }
                            }
                        )
                    }


                    AppDestination.HISTORICO -> {

                        HistoricoScreen(

                            onVerMapa = { sessaoId ->

                                sessaoSelecionadaId =
                                    sessaoId

                                destinoAtual =
                                    AppDestination.MAPA
                            },


                            onVoltar = {

                                destinoAtual =
                                    AppDestination.TRACKER
                            }
                        )
                    }


                    AppDestination.ROTAS -> {

                        RotasScreen(

                            onVerMapa = { sessaoId ->

                                sessaoSelecionadaId =
                                    sessaoId

                                destinoAtual =
                                    AppDestination.MAPA
                            },


                            onVoltar = {

                                destinoAtual =
                                    AppDestination.TRACKER
                            }
                        )
                    }


                    AppDestination.RESUMO -> {

                        ResumoScreen(

                            onVoltar = {

                                destinoAtual =
                                    AppDestination.TRACKER
                            }
                        )
                    }


                    AppDestination.PERFIL -> {

                        PerfilScreen(

                            onVoltar = {

                                destinoAtual =
                                    AppDestination.TRACKER
                            }
                        )
                    }


                    AppDestination.MAPA -> {

                        MapaScreen(

                            sessaoId =
                                sessaoSelecionadaId,


                            onVoltar = {

                                destinoAtual =
                                    AppDestination.ROTAS
                            }
                        )
                    }
                }
            }
        }
    }
}



private fun AppDestination.toBottomNavigationDestination():

        HomeNavigationDestination {


    return when (this) {

        AppDestination.TRACKER ->
            HomeNavigationDestination.HOME


        AppDestination.ROTAS,
        AppDestination.HISTORICO,
        AppDestination.MAPA ->
            HomeNavigationDestination.ROUTES


        AppDestination.RESUMO ->
            HomeNavigationDestination.STATISTICS


        AppDestination.PERFIL ->
            HomeNavigationDestination.SETTINGS
    }
}



private fun AppDestination.toDrawerRoute(): String {

    return when (this) {

        AppDestination.TRACKER ->
            "tracker"

        AppDestination.HISTORICO ->
            "historico"

        AppDestination.ROTAS ->
            "rotas"

        AppDestination.RESUMO ->
            "resumo"

        AppDestination.PERFIL ->
            "perfil"

        AppDestination.MAPA ->
            "mapa"
    }
}