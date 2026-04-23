package rfm.hillsongptapp.feature.groups

import hillsongptapp.feature.groups.generated.resources.Res
import hillsongptapp.feature.groups.generated.resources.groups_ministry_casais
import hillsongptapp.feature.groups.generated.resources.groups_ministry_geral
import hillsongptapp.feature.groups.generated.resources.groups_ministry_jovens
import hillsongptapp.feature.groups.generated.resources.groups_ministry_mens
import hillsongptapp.feature.groups.generated.resources.groups_ministry_sisterhood
import hillsongptapp.feature.groups.generated.resources.groups_ministry_thirty_plus
import org.jetbrains.compose.resources.stringResource
import androidx.compose.runtime.Composable
import rfm.hillsongptapp.core.network.api.GroupSummary
import rfm.hillsongptapp.core.network.api.MeetingFrequency
import rfm.hillsongptapp.core.network.api.Ministry

@Composable
internal fun labelForMinistry(ministry: Ministry): String = when (ministry) {
    Ministry.SISTERHOOD -> stringResource(Res.string.groups_ministry_sisterhood)
    Ministry.JOVENS_YXYA -> stringResource(Res.string.groups_ministry_jovens)
    Ministry.MENS -> stringResource(Res.string.groups_ministry_mens)
    Ministry.CASAIS -> stringResource(Res.string.groups_ministry_casais)
    Ministry.THIRTY_PLUS -> stringResource(Res.string.groups_ministry_thirty_plus)
    Ministry.GERAL -> stringResource(Res.string.groups_ministry_geral)
}

internal fun dayLabel(group: GroupSummary): String = group.meetingDay.name.lowercase()
    .replaceFirstChar { if (it.isLowerCase()) it.titlecase() else it.toString() }

internal fun frequencyLabel(group: GroupSummary): String = group.frequency.name.lowercase()
    .replaceFirstChar { if (it.isLowerCase()) it.titlecase() else it.toString() }

internal fun frequencyLabel(frequency: MeetingFrequency): String = frequency.name.lowercase()
    .replaceFirstChar { if (it.isLowerCase()) it.titlecase() else it.toString() }
