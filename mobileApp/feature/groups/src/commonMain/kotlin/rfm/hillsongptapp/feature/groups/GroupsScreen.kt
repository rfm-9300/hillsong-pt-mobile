package rfm.hillsongptapp.feature.groups

import AppFonts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import hillsongptapp.feature.groups.generated.resources.Res
import hillsongptapp.feature.groups.generated.resources.groups_all
import hillsongptapp.feature.groups.generated.resources.groups_city_all
import hillsongptapp.feature.groups.generated.resources.groups_empty
import hillsongptapp.feature.groups.generated.resources.groups_error
import hillsongptapp.feature.groups.generated.resources.groups_ministry_casais
import hillsongptapp.feature.groups.generated.resources.groups_ministry_geral
import hillsongptapp.feature.groups.generated.resources.groups_ministry_jovens
import hillsongptapp.feature.groups.generated.resources.groups_ministry_mens
import hillsongptapp.feature.groups.generated.resources.groups_ministry_sisterhood
import hillsongptapp.feature.groups.generated.resources.groups_ministry_thirty_plus
import hillsongptapp.feature.groups.generated.resources.groups_search_placeholder
import hillsongptapp.feature.groups.generated.resources.groups_title
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel
import rfm.hillsongptapp.core.designsystem.theme.HillsongColors
import rfm.hillsongptapp.core.network.api.Ministry
import rfm.hillsongptapp.core.navigation.navigateToGroupDetail
import rfm.hillsongptapp.feature.groups.components.GroupCard
import rfm.hillsongptapp.feature.groups.components.MinistryFilterRow

private val cityOptions = listOf("Lisboa", "Porto", "Cascais", "Almada", "Braga", "Coimbra", "Faro", "Outros")

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GroupsScreen(
    navController: NavHostController,
    viewModel: GroupsViewModel = koinViewModel(),
) {
    val uiState by viewModel.uiState.collectAsState()
    val ministryOptions = listOf(
        null to stringResource(Res.string.groups_all),
        Ministry.SISTERHOOD to stringResource(Res.string.groups_ministry_sisterhood),
        Ministry.JOVENS_YXYA to stringResource(Res.string.groups_ministry_jovens),
        Ministry.MENS to stringResource(Res.string.groups_ministry_mens),
        Ministry.CASAIS to stringResource(Res.string.groups_ministry_casais),
        Ministry.THIRTY_PLUS to stringResource(Res.string.groups_ministry_thirty_plus),
        Ministry.GERAL to stringResource(Res.string.groups_ministry_geral),
    )

    Scaffold(containerColor = MaterialTheme.colorScheme.background) { paddingValues ->
        Column(modifier = Modifier.fillMaxSize().padding(paddingValues)) {
            GroupsTopBar(
                title = stringResource(Res.string.groups_title),
                onBackClick = { navController.popBackStack() },
            )

            OutlinedTextField(
                value = uiState.query,
                onValueChange = viewModel::updateQuery,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp, vertical = 12.dp),
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                placeholder = { Text(stringResource(Res.string.groups_search_placeholder)) },
                singleLine = true,
                shape = RoundedCornerShape(20.dp),
            )

            MinistryFilterRow(
                items = ministryOptions,
                selected = uiState.selectedMinistry,
                onSelect = viewModel::selectMinistry,
            )

            Spacer(modifier = Modifier.height(12.dp))

            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                contentPadding = PaddingValues(horizontal = 24.dp),
            ) {
                item {
                    CityChip(
                        label = stringResource(Res.string.groups_city_all),
                        isSelected = uiState.selectedCity == null,
                        onClick = { viewModel.selectCity(null) },
                    )
                }
                items(cityOptions) { city ->
                    CityChip(
                        label = city,
                        isSelected = uiState.selectedCity == city,
                        onClick = { viewModel.selectCity(city) },
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            when {
                uiState.isLoading -> {
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(color = HillsongColors.Gold)
                    }
                }
                uiState.error != null -> {
                    CenterMessage(text = stringResource(Res.string.groups_error))
                }
                uiState.groups.isEmpty() -> {
                    CenterMessage(text = stringResource(Res.string.groups_empty))
                }
                else -> {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(start = 24.dp, top = 8.dp, end = 24.dp, bottom = 40.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp),
                    ) {
                        items(uiState.groups) { group ->
                            GroupCard(
                                group = group,
                                imageUrl = group.imagePath?.let { "${viewModel.baseUrl}/api/files/$it" },
                                ministryLabel = labelForMinistry(group.ministry),
                                dayLabel = dayLabel(group),
                                frequencyLabel = frequencyLabel(group),
                                onClick = { navController.navigateToGroupDetail(group.id) },
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun CityChip(label: String, isSelected: Boolean, onClick: () -> Unit) {
    FilterChip(
        selected = isSelected,
        onClick = onClick,
        label = { Text(label) },
    )
}

@Composable
private fun CenterMessage(text: String) {
    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Text(
            text = text,
            style = TextStyle(
                fontFamily = AppFonts.andika(),
                fontSize = 14.sp,
                color = HillsongColors.Gray500,
            ),
        )
    }
}

@Composable
private fun GroupsTopBar(title: String, onBackClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp, vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        IconButton(onClick = onBackClick) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                contentDescription = "Back",
                tint = MaterialTheme.colorScheme.onBackground,
            )
        }
        Spacer(Modifier.width(4.dp))
        Text(
            text = title.uppercase(),
            style = TextStyle(
                fontFamily = AppFonts.anta(),
                fontSize = 14.sp,
                letterSpacing = 2.sp,
                color = MaterialTheme.colorScheme.onBackground,
            ),
        )
    }
}
