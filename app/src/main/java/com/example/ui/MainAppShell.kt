package com.example.ui

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.TouchApp
import androidx.compose.material.icons.outlined.AutoAwesome
import androidx.compose.material.icons.outlined.BarChart
import androidx.compose.material.icons.outlined.EmojiEvents
import androidx.compose.material.icons.outlined.TouchApp
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.NavigationRail
import androidx.compose.material3.NavigationRailItem
import androidx.compose.material3.NavigationRailItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Snackbar
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.credentials.CredentialManager
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.R
import com.example.data.signOutUser
import com.example.ui.components.AboutDedicationDialog
import com.example.ui.components.AddCustomDhikrDialog
import com.example.ui.components.DedicationBanner
import com.example.ui.components.TasbihEmblemIcon
import com.example.ui.components.UserProfileDialog
import com.example.ui.components.islamicSurface
import com.example.ui.screens.CompetitionScreen
import com.example.ui.screens.DhikrCardStudioScreen
import com.example.ui.screens.StatsScreen
import com.example.ui.screens.TasbihHomeScreen
import com.example.ui.theme.AmberWarm
import com.example.ui.theme.CreamText
import com.example.ui.theme.EmeraldAccent
import com.example.ui.theme.GoldDeep
import com.example.ui.theme.GoldGlow
import com.example.ui.theme.GoldMain
import com.example.ui.theme.MutedGold
import com.example.ui.theme.OliveBg
import com.example.ui.theme.OliveCard
import com.example.ui.theme.OliveLift
import com.google.firebase.auth.FirebaseUser
import kotlinx.coroutines.launch

enum class TasbihTab(
    val titleRes: Int,
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector,
    val tag: String
) {
    TASBIH(
        titleRes = R.string.tab_tasbih,
        selectedIcon = Icons.Filled.TouchApp,
        unselectedIcon = Icons.Outlined.TouchApp,
        tag = "nav_tab_tasbih"
    ),
    STATS(
        titleRes = R.string.tab_stats,
        selectedIcon = Icons.Filled.BarChart,
        unselectedIcon = Icons.Outlined.BarChart,
        tag = "nav_tab_stats"
    ),
    COMPETITION(
        titleRes = R.string.tab_competition,
        selectedIcon = Icons.Filled.EmojiEvents,
        unselectedIcon = Icons.Outlined.EmojiEvents,
        tag = "nav_tab_competition"
    ),
    CARDS(
        titleRes = R.string.tab_cards,
        selectedIcon = Icons.Filled.AutoAwesome,
        unselectedIcon = Icons.Outlined.AutoAwesome,
        tag = "nav_tab_cards"
    )
}

@Composable
fun MainAppShell(
    user: FirebaseUser
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val credentialManager = remember(context) { CredentialManager.create(context) }
    val databaseId = remember(context) { context.getString(R.string.firestore_database_id) }

    val tasbihViewModel: TasbihViewModel = viewModel(
        key = user.uid,
        factory = TasbihViewModel.provideFactory(
            databaseId = databaseId,
            currentUserId = user.uid,
            initialDisplayName = user.displayName
        )
    )
    val cardViewModel: DhikrCardViewModel = viewModel(key = "cards_${user.uid}")

    val uiState by tasbihViewModel.uiState.collectAsStateWithLifecycle()
    var currentTab by rememberSaveable { mutableStateOf(TasbihTab.TASBIH) }

    var showAboutDialog by remember { mutableStateOf(false) }
    var showAddDhikrDialog by remember { mutableStateOf(false) }
    var showProfileDialog by remember { mutableStateOf(false) }

    val snackbarHostState = remember { SnackbarHostState() }
    val targetCompletedMsg = stringResource(R.string.target_completed_toast)

    // Handle system Back button cleanly from secondary tabs back to Tasbih Home
    BackHandler(enabled = currentTab != TasbihTab.TASBIH) {
        currentTab = TasbihTab.TASBIH
    }

    LaunchedEffect(uiState.local.statusMessage, uiState.local.errorMessage, uiState.cloudError) {
        val msg = uiState.local.statusMessage
            ?: uiState.local.errorMessage
            ?: uiState.cloudError
        if (!msg.isNullOrBlank()) {
            snackbarHostState.showSnackbar(msg)
            tasbihViewModel.clearTransientMessages()
        }
    }

    BoxWithConstraints(
        modifier = Modifier
            .fillMaxSize()
            .islamicSurface()
    ) {
        val useNavigationRail = maxWidth >= 600.dp

        Scaffold(
            containerColor = Color.Transparent,
            contentWindowInsets = WindowInsets(0, 0, 0, 0),
            snackbarHost = {
                SnackbarHost(hostState = snackbarHostState) { data ->
                    Snackbar(
                        snackbarData = data,
                        containerColor = OliveLift,
                        contentColor = GoldGlow,
                        shape = RoundedCornerShape(14.dp)
                    )
                }
            },
            topBar = {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(OliveBg.copy(alpha = 0.96f))
                        .windowInsetsPadding(WindowInsets.statusBars)
                ) {
                    DedicationBanner(onClick = { showAboutDialog = true })
                    TasbihTopAppBar(
                        title = stringResource(currentTab.titleRes),
                        displayName = uiState.local.displayName,
                        syncing = uiState.local.syncing || uiState.loadingCloud,
                        onAboutClick = { showAboutDialog = true },
                        onProfileClick = { showProfileDialog = true }
                    )
                }
            },
            bottomBar = {
                if (!useNavigationRail) {
                    TasbihBottomNavigationBar(
                        currentTab = currentTab,
                        onSelectTab = { currentTab = it }
                    )
                }
            }
        ) { innerPadding ->
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
            ) {
                if (useNavigationRail) {
                    TasbihNavigationRail(
                        currentTab = currentTab,
                        onSelectTab = { currentTab = it }
                    )
                }

                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight()
                ) {
                    when (currentTab) {
                        TasbihTab.TASBIH -> TasbihHomeScreen(
                            uiState = uiState,
                            onTapTasbih = tasbihViewModel::onTapTasbih,
                            onSelectDhikr = tasbihViewModel::selectDhikr,
                            onCycleTarget = tasbihViewModel::cycleTarget,
                            onToggleSound = tasbihViewModel::toggleSound,
                            onToggleVibration = tasbihViewModel::toggleVibration,
                            onResetSession = tasbihViewModel::resetSession,
                            onOpenAddDhikr = { showAddDhikrDialog = true },
                            onDeleteCustomDhikr = tasbihViewModel::deleteCustomDhikr,
                            onTargetCompletedHandled = {
                                tasbihViewModel.clearTargetCompletedFlag()
                                scope.launch {
                                    snackbarHostState.showSnackbar(targetCompletedMsg)
                                }
                            }
                        )

                        TasbihTab.STATS -> StatsScreen(
                            uiState = uiState,
                            onSelectScope = tasbihViewModel::setStatsScope,
                            onSelectPeriod = tasbihViewModel::setStatsPeriod
                        )

                        TasbihTab.COMPETITION -> CompetitionScreen(
                            uiState = uiState,
                            currentUserId = user.uid,
                            onSelectFilter = tasbihViewModel::setCompetitionFilter
                        )

                        TasbihTab.CARDS -> DhikrCardStudioScreen(
                            activeDhikrTitle = uiState.activeDhikr.title,
                            viewModel = cardViewModel,
                            onShowMessage = { msg ->
                                scope.launch { snackbarHostState.showSnackbar(msg) }
                            }
                        )
                    }
                }
            }
        }
    }

    if (showAboutDialog) {
        AboutDedicationDialog(onDismiss = { showAboutDialog = false })
    }

    if (showAddDhikrDialog) {
        AddCustomDhikrDialog(
            onDismiss = { showAddDhikrDialog = false },
            onAdd = { title, target, colorHex, countsAsIstighfar ->
                tasbihViewModel.addCustomDhikr(title, target, colorHex, countsAsIstighfar)
                showAddDhikrDialog = false
            }
        )
    }

    if (showProfileDialog) {
        UserProfileDialog(
            currentDisplayName = uiState.local.displayName,
            allTimeTotal = uiState.local.allTimeTotal,
            todayTotal = uiState.local.todayTotal,
            onDismiss = { showProfileDialog = false },
            onSaveName = { newName -> tasbihViewModel.updateDisplayName(newName) },
            onSignOut = {
                showProfileDialog = false
                signOutUser(
                    credentialManager = credentialManager,
                    scope = scope
                )
            }
        )
    }
}

@Composable
private fun TasbihTopAppBar(
    title: String,
    displayName: String,
    syncing: Boolean,
    onAboutClick: () -> Unit,
    onProfileClick: () -> Unit
) {
    val initial = displayName.trim().firstOrNull()?.toString() ?: "م"
    Column(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            // RTL Start: Emblem + Screen Title
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.weight(1f)
            ) {
                TasbihEmblemIcon(size = 38.dp)
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = title,
                        style = MaterialTheme.typography.titleMedium.copy(
                            color = CreamText,
                            fontWeight = FontWeight.ExtraBold
                        ),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = stringResource(R.string.subtitle_free_forever),
                        style = MaterialTheme.typography.labelSmall.copy(color = MutedGold),
                        maxLines = 1
                    )
                }
            }

            // RTL End: Cloud Status + Heart About Button + User Pill
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Surface(
                    shape = RoundedCornerShape(50),
                    color = OliveCard,
                    border = BorderStroke(
                        1.dp,
                        if (syncing) AmberWarm.copy(alpha = 0.4f) else EmeraldAccent.copy(alpha = 0.4f)
                    )
                ) {
                    Text(
                        text = stringResource(
                            if (syncing) R.string.status_cloud_syncing else R.string.status_cloud_connected
                        ),
                        style = MaterialTheme.typography.labelSmall.copy(
                            color = if (syncing) AmberWarm else EmeraldAccent,
                            fontWeight = FontWeight.Bold
                        ),
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }

                IconButton(
                    onClick = onAboutClick,
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(OliveCard)
                        .border(1.dp, GoldMain.copy(alpha = 0.35f), CircleShape)
                        .testTag("about_heart_button")
                ) {
                    Icon(
                        imageVector = Icons.Filled.Favorite,
                        contentDescription = stringResource(R.string.cd_about_dedication),
                        tint = Color(0xFFEF4444),
                        modifier = Modifier.size(18.dp)
                    )
                }

                Surface(
                    onClick = onProfileClick,
                    shape = RoundedCornerShape(50),
                    color = OliveCard,
                    border = BorderStroke(1.dp, GoldMain.copy(alpha = 0.45f)),
                    modifier = Modifier.testTag("user_profile_pill")
                ) {
                    Row(
                        modifier = Modifier.padding(start = 10.dp, end = 4.dp, top = 4.dp, bottom = 4.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Text(
                            text = displayName,
                            style = MaterialTheme.typography.labelMedium.copy(
                                color = CreamText,
                                fontWeight = FontWeight.Bold
                            ),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.widthIn(max = 68.dp)
                        )
                        Box(
                            modifier = Modifier
                                .size(26.dp)
                                .clip(CircleShape)
                                .background(
                                    Brush.linearGradient(colors = listOf(GoldGlow, GoldDeep))
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = initial,
                                style = MaterialTheme.typography.labelMedium.copy(
                                    color = OliveBg,
                                    fontWeight = FontWeight.Black
                                )
                            )
                        }
                    }
                }
            }
        }
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(1.dp)
                .background(GoldMain.copy(alpha = 0.20f))
        )
    }
}

@Composable
private fun TasbihBottomNavigationBar(
    currentTab: TasbihTab,
    onSelectTab: (TasbihTab) -> Unit
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(1.dp)
                .background(GoldMain.copy(alpha = 0.22f))
        )
        NavigationBar(
            containerColor = OliveCard.copy(alpha = 0.98f),
            contentColor = GoldGlow,
            windowInsets = WindowInsets.navigationBars
        ) {
            TasbihTab.entries.forEach { tab ->
                val selected = currentTab == tab
                NavigationBarItem(
                    selected = selected,
                    onClick = { onSelectTab(tab) },
                    icon = {
                        Icon(
                            imageVector = if (selected) tab.selectedIcon else tab.unselectedIcon,
                            contentDescription = stringResource(tab.titleRes)
                        )
                    },
                    label = {
                        Text(
                            text = stringResource(tab.titleRes),
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontWeight = if (selected) FontWeight.ExtraBold else FontWeight.SemiBold
                            ),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = OliveBg,
                        selectedTextColor = GoldGlow,
                        indicatorColor = GoldMain,
                        unselectedIconColor = MutedGold,
                        unselectedTextColor = MutedGold
                    ),
                    modifier = Modifier.testTag(tab.tag)
                )
            }
        }
    }
}

@Composable
private fun TasbihNavigationRail(
    currentTab: TasbihTab,
    onSelectTab: (TasbihTab) -> Unit
) {
    Row(modifier = Modifier.fillMaxHeight()) {
        NavigationRail(
            containerColor = OliveCard.copy(alpha = 0.98f),
            contentColor = GoldGlow,
            modifier = Modifier.windowInsetsPadding(WindowInsets.navigationBars)
        ) {
            Spacer(modifier = Modifier.height(12.dp))
            TasbihTab.entries.forEach { tab ->
                val selected = currentTab == tab
                NavigationRailItem(
                    selected = selected,
                    onClick = { onSelectTab(tab) },
                    icon = {
                        Icon(
                            imageVector = if (selected) tab.selectedIcon else tab.unselectedIcon,
                            contentDescription = stringResource(tab.titleRes)
                        )
                    },
                    label = {
                        Text(
                            text = stringResource(tab.titleRes),
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontWeight = if (selected) FontWeight.ExtraBold else FontWeight.SemiBold
                            )
                        )
                    },
                    colors = NavigationRailItemDefaults.colors(
                        selectedIconColor = OliveBg,
                        selectedTextColor = GoldGlow,
                        indicatorColor = GoldMain,
                        unselectedIconColor = MutedGold,
                        unselectedTextColor = MutedGold
                    ),
                    modifier = Modifier.testTag(tab.tag)
                )
            }
        }
        Box(
            modifier = Modifier
                .width(1.dp)
                .fillMaxHeight()
                .background(GoldMain.copy(alpha = 0.22f))
        )
    }
}
