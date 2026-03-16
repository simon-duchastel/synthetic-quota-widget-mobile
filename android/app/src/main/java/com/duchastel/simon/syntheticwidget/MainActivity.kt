package com.duchastel.simon.syntheticwidget

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.duchastel.simon.syntheticwidget.ui.theme.SyntheticWidgetTheme
import com.duchastel.simon.syntheticwidget.ui.widget.QuotaWidgetScreen
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    private val viewModel: MainViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            SyntheticWidgetTheme {
                SyntheticWidgetApp(viewModel = viewModel)
            }
        }
    }
}

sealed class Screen(val route: String) {
    data object WidgetList : Screen("widgets")
    data object Settings : Screen("widgets/{widgetId}/settings") {
        fun createRoute(widgetId: Int) = "widgets/$widgetId/settings"
    }
}

@Composable
fun SyntheticWidgetApp(
    viewModel: MainViewModel = hiltViewModel(),
    navController: NavHostController = rememberNavController()
) {
    NavHost(
        navController = navController,
        startDestination = Screen.WidgetList.route
    ) {
        composable(Screen.WidgetList.route) {
            WidgetListScreen(
                viewModel = viewModel,
                onNavigateToSettings = { widgetId ->
                    navController.navigate(Screen.Settings.createRoute(widgetId))
                }
            )
        }
        composable(
            route = Screen.Settings.route,
            arguments = listOf(
                navArgument("widgetId") { type = NavType.IntType }
            )
        ) { backStackEntry ->
            val widgetId = backStackEntry.arguments?.getInt("widgetId") ?: return@composable
            SettingsScreen(
                viewModel = viewModel,
                widgetId = widgetId,
                onNavigateBack = {
                    navController.popBackStack()
                }
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WidgetListScreen(
    viewModel: MainViewModel,
    onNavigateToSettings: (Int) -> Unit
) {
    val maskedApiKey by viewModel.maskedApiKey.collectAsState()
    val widgets by viewModel.widgets.collectAsState()
    var apiKeyInput by remember { mutableStateOf("") }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Synthetic Widget") }
            )
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp)
        ) {
            item {
                Text(
                    text = "API Key Configuration",
                    style = MaterialTheme.typography.headlineSmall,
                    modifier = Modifier.padding(bottom = 16.dp)
                )

                if (maskedApiKey.isNotEmpty()) {
                    Text(
                        text = "Current API Key: $maskedApiKey",
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.padding(bottom = 16.dp)
                    )
                }

                OutlinedTextField(
                    value = apiKeyInput,
                    onValueChange = { apiKeyInput = it },
                    label = { Text("Enter Synthetic API Key") },
                    placeholder = { Text("syn_...") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                Button(
                    onClick = {
                        viewModel.saveApiKey(apiKeyInput)
                        apiKeyInput = ""
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 16.dp),
                    enabled = apiKeyInput.isNotBlank()
                ) {
                    Text("Save API Key")
                }

                Spacer(modifier = Modifier.height(24.dp))

                Text(
                    text = "Your Widgets",
                    style = MaterialTheme.typography.headlineSmall,
                    modifier = Modifier.padding(bottom = 16.dp)
                )
            }

            items(widgets) { widgetInfo ->
                WidgetListItem(
                    maskedApiKey = maskedApiKey,
                    widgetInfo = widgetInfo,
                    onRefreshClick = { viewModel.refreshWidget(widgetInfo.glanceId) },
                    onSettingsClick = { onNavigateToSettings(widgetInfo.appWidgetId) }
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    viewModel: MainViewModel,
    widgetId: Int,
    onNavigateBack: () -> Unit,
) {
    val widgets by viewModel.widgets.collectAsState()
    val widgetInfo = widgets.find { widget ->
        widget.appWidgetId == widgetId
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Widget Settings") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .padding(paddingValues)
                .padding(16.dp)
        ) {
            widgetInfo?.let { _ ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth(),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp)
                    ) {
                        QuotaWidgetScreen(
                            quotaWidgetState = widgetInfo.state,
                            onRefreshClick = { },
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        Text(
                            text = "current api key: ???",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))
            }

            // Settings content
            Text(
                text = "Settings",
                style = MaterialTheme.typography.headlineSmall,
                modifier = Modifier.padding(bottom = 16.dp)
            )

            // Lorem ipsum placeholder text
            Text(
                text = "Lorem ipsum dolor sit amet, consectetur adipiscing elit. Sed do eiusmod tempor incididunt ut labore et dolore magna aliqua. Ut enim ad minim veniam, quis nostrud exercitation ullamco laboris nisi ut aliquip ex ea commodo consequat.",
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.padding(bottom = 16.dp)
            )

            Text(
                text = "Duis aute irure dolor in reprehenderit in voluptate velit esse cillum dolore eu fugiat nulla pariatur. Excepteur sint occaecat cupidatat non proident, sunt in culpa qui officia deserunt mollit anim id est laborum.",
                style = MaterialTheme.typography.bodyMedium
            )
        }
    }
}

@Composable
fun WidgetListItem(
    maskedApiKey: String,
    widgetInfo: WidgetInfo,
    onRefreshClick: () -> Unit,
    onSettingsClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
            .height(IntrinsicSize.Min),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Card(
            modifier = Modifier
                .weight(1f),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
        ) {
            Column(
                modifier = Modifier.padding(16.dp)
            ) {
                QuotaWidgetScreen(
                    quotaWidgetState = widgetInfo.state,
                    onRefreshClick = onRefreshClick
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = "current api key: $maskedApiKey",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        Spacer(modifier = Modifier.width(8.dp))

        Box(
            modifier = Modifier
                .fillMaxHeight()
                .clickable(onClick = onSettingsClick),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.Settings,
                contentDescription = "Settings",
                tint = MaterialTheme.colorScheme.onSurface
            )
        }
    }
}
