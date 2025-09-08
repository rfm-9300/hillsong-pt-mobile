package rfm.hillsongptapp.feature.kids.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Warning
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import rfm.hillsongptapp.core.navigation.KidsNav
import rfm.hillsongptapp.feature.kids.navigation.KidsBreadcrumb
import rfm.hillsongptapp.feature.kids.navigation.generateKidsBreadcrumbs

/**
 * Breadcrumb navigation component for kids management workflows
 */
@Composable
fun BreadcrumbNavigation(
    currentRoute: KidsNav,
    navController: NavHostController,
    modifier: Modifier = Modifier
) {
    val breadcrumbs = generateKidsBreadcrumbs(currentRoute)
    
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.Start,
        verticalAlignment = Alignment.CenterVertically
    ) {
        breadcrumbs.forEachIndexed { index, breadcrumb ->
            // Breadcrumb text
            Text(
                text = breadcrumb.title,
                style = MaterialTheme.typography.bodyMedium,
                color = if (breadcrumb.isClickable) {
                    MaterialTheme.colorScheme.primary
                } else {
                    MaterialTheme.colorScheme.onSurface
                },
                fontWeight = if (index == breadcrumbs.lastIndex) {
                    FontWeight.SemiBold
                } else {
                    FontWeight.Normal
                },
                modifier = if (breadcrumb.isClickable) {
                    Modifier.clickable {
                        when (breadcrumb.route) {
                            is KidsNav.Management -> navController.navigate(KidsNav.Management) {
                                popUpTo(KidsNav.Management) { inclusive = false }
                            }
                            is KidsNav.Services -> navController.navigate(KidsNav.Services)
                            else -> { /* Handle other routes if needed */ }
                        }
                    }
                } else {
                    Modifier
                }
            )
            
            // Chevron separator (except for last item)
            if (index < breadcrumbs.lastIndex) {
                Icon(
                    imageVector = Icons.Default.Warning,
                    contentDescription = "Navigate to",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(horizontal = 4.dp)
                )
            }
        }
    }
}

/**
 * Simple breadcrumb component for basic navigation
 */
@Composable
fun SimpleBreadcrumb(
    title: String,
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.Start,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = "Kids Management",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.clickable { onBackClick() }
        )
        
        Icon(
            imageVector = Icons.Default.Warning,
            contentDescription = "Navigate to",
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(horizontal = 4.dp)
        )
        
        Text(
            text = title,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurface,
            fontWeight = FontWeight.SemiBold
        )
    }
}