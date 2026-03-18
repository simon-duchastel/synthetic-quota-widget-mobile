package com.duchastel.simon.syntheticwidget

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.displayCutout
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.ime
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.Checkbox
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import androidx.compose.ui.window.Dialog
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.duchastel.simon.syntheticwidget.data.ApiKeyEntry
import com.duchastel.simon.syntheticwidget.ui.theme.SyntheticWidgetTheme
import com.duchastel.simon.syntheticwidget.ui.widget.QuotaWidgetScreen
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    private val viewModel: MainViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        enableEdgeToEdge()

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
        modifier = Modifier.windowInsetsPadding(WindowInsets.displayCutout),
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
    val widgets by viewModel.widgets.collectAsState()
    val apiKeys by viewModel.apiKeys.collectAsState()

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
                    text = "Your Widgets",
                    style = MaterialTheme.typography.headlineSmall,
                    modifier = Modifier.padding(bottom = 16.dp)
                )
            }

            items(widgets) { widgetInfo ->
                val apiKey = viewModel.getApiKeyForWidget(widgetInfo)
                WidgetListItem(
                    apiKey = apiKey,
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
    val apiKeys by viewModel.apiKeys.collectAsState()
    val widgetInfo = widgets.find { widget ->
        widget.appWidgetId == widgetId
    }
    val currentApiKey = widgetInfo?.let { viewModel.getApiKeyForWidget(it) }

    var showAddKeyDialog by remember { mutableStateOf(false) }

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
            widgetInfo?.let { info ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth(),
                    shape = RoundedCornerShape(24.dp),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp)
                    ) {
                        QuotaWidgetScreen(
                            quotaWidgetState = info.state,
                            onRefreshClick = { },
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        val apiKeyDisplay = if (currentApiKey != null) {
                            "${currentApiKey.name}: ${currentApiKey.getMaskedKey()}"
                        } else {
                            "No API key assigned"
                        }
                        Text(
                            text = "current api key: $apiKeyDisplay",
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

            // API Key dropdown
            Text(
                text = "API Key",
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.padding(bottom = 8.dp)
            )

            ApiKeyDropdown(
                apiKeys = apiKeys,
                selectedApiKey = currentApiKey,
                onApiKeySelected = { apiKey ->
                    widgetInfo?.let { info ->
                        viewModel.setWidgetApiKey(info.glanceId, apiKey?.id)
                    }
                },
                onAddNewKey = {
                    showAddKeyDialog = true
                }
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Clear background checkbox
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp)
                    .clickable {
                        widgetInfo?.let { info ->
                            viewModel.setClearBackground(info.glanceId, !info.state.isClearBackground)
                        }
                    },
                verticalAlignment = Alignment.CenterVertically
            ) {
                Checkbox(
                    checked = widgetInfo?.state?.isClearBackground ?: false,
                    onCheckedChange = null
                )
                Text(
                    text = "Transparent background",
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.padding(start = 8.dp)
                )
            }
        }
    }

    if (showAddKeyDialog) {
        AddApiKeyDialog(
            onDismiss = { showAddKeyDialog = false },
            onConfirm = { name, apiKey ->
                viewModel.addApiKey(name, apiKey)
                showAddKeyDialog = false
            }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ApiKeyDropdown(
    apiKeys: List<ApiKeyEntry>,
    selectedApiKey: ApiKeyEntry?,
    onApiKeySelected: (ApiKeyEntry?) -> Unit,
    onAddNewKey: () -> Unit
) {
    var expanded by remember { mutableStateOf(false) }

    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = !expanded }
    ) {
        OutlinedTextField(
            value = selectedApiKey?.let { "${it.name} (${it.getMaskedKey()})" } ?: "Select an API key",
            onValueChange = {},
            readOnly = true,
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
            modifier = Modifier
                .fillMaxWidth()
                .menuAnchor()
        )

        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            // Option to clear selection
            DropdownMenuItem(
                text = { Text("None") },
                onClick = {
                    onApiKeySelected(null)
                    expanded = false
                }
            )

            // List existing keys
            apiKeys.forEach { apiKey ->
                DropdownMenuItem(
                    text = { Text("${apiKey.name} (${apiKey.getMaskedKey()})") },
                    onClick = {
                        onApiKeySelected(apiKey)
                        expanded = false
                    }
                )
            }

            // Add new key option
            DropdownMenuItem(
                text = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = null,
                            modifier = Modifier.padding(end = 8.dp)
                        )
                        Text("Add new key")
                    }
                },
                onClick = {
                    onAddNewKey()
                    expanded = false
                }
            )
        }
    }
}

@Composable
fun AddApiKeyDialog(
    onDismiss: () -> Unit,
    onConfirm: (name: String, apiKey: String) -> Unit
) {
    var name by remember { mutableStateOf("") }
    var apiKey by remember { mutableStateOf("") }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                Text(
                    text = "Add New API Key",
                    style = MaterialTheme.typography.headlineSmall,
                    modifier = Modifier.padding(bottom = 16.dp)
                )

                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Key Name") },
                    placeholder = { Text("e.g., Work Account") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(16.dp))

                OutlinedTextField(
                    value = apiKey,
                    onValueChange = { apiKey = it },
                    label = { Text("API Key") },
                    placeholder = { Text("syn_...") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(24.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(onClick = onDismiss) {
                        Text("Cancel")
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = {
                            if (name.isNotBlank() && apiKey.isNotBlank()) {
                                onConfirm(name, apiKey)
                            }
                        },
                        enabled = name.isNotBlank() && apiKey.isNotBlank()
                    ) {
                        Text("Add")
                    }
                }
            }
        }
    }
}

@Composable
fun WidgetListItem(
    apiKey: ApiKeyEntry?,
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
            shape = RoundedCornerShape(24.dp),
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

                val apiKeyDisplay = if (apiKey != null) {
                    "${apiKey.name}: ${apiKey.getMaskedKey()}"
                } else {
                    "No API key assigned"
                }
                Text(
                    text = "current api key: $apiKeyDisplay",
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
