package com.composables

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp

val Report_icon: ImageVector
    get() {
        if (_Report != null) return _Report!!

        _Report = ImageVector.Builder(
            name = "Report",
            defaultWidth = 24.dp,
            defaultHeight = 24.dp,
            viewportWidth = 960f,
            viewportHeight = 960f
        ).apply {
            path(
                fill = SolidColor(Color(0xFF000000))
            ) {
                moveTo(480f, 680f)
                quadToRelative(17f, 0f, 28.5f, -11.5f)
                reflectiveQuadTo(520f, 640f)
                reflectiveQuadToRelative(-11.5f, -28.5f)
                reflectiveQuadTo(480f, 600f)
                reflectiveQuadToRelative(-28.5f, 11.5f)
                reflectiveQuadTo(440f, 640f)
                reflectiveQuadToRelative(11.5f, 28.5f)
                reflectiveQuadTo(480f, 680f)
                moveToRelative(-40f, -160f)
                horizontalLineToRelative(80f)
                verticalLineToRelative(-240f)
                horizontalLineToRelative(-80f)
                close()
                moveTo(330f, 840f)
                lineTo(120f, 630f)
                verticalLineToRelative(-300f)
                lineToRelative(210f, -210f)
                horizontalLineToRelative(300f)
                lineToRelative(210f, 210f)
                verticalLineToRelative(300f)
                lineTo(630f, 840f)
                close()
                moveToRelative(34f, -80f)
                horizontalLineToRelative(232f)
                lineToRelative(164f, -164f)
                verticalLineToRelative(-232f)
                lineTo(596f, 200f)
                horizontalLineTo(364f)
                lineTo(200f, 364f)
                verticalLineToRelative(232f)
                close()
                moveToRelative(116f, -280f)
            }
        }.build()

        return _Report!!
    }

private var _Report: ImageVector? = null

