package com.composables

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp

val Groups_icon: ImageVector
    get() {
        if (_Groups != null) return _Groups!!

        _Groups = ImageVector.Builder(
            name = "Groups",
            defaultWidth = 24.dp,
            defaultHeight = 24.dp,
            viewportWidth = 960f,
            viewportHeight = 960f
        ).apply {
            path(
                fill = SolidColor(Color(0xFF000000))
            ) {
                moveTo(0f, 720f)
                verticalLineToRelative(-63f)
                quadToRelative(0f, -43f, 44f, -70f)
                reflectiveQuadToRelative(116f, -27f)
                quadToRelative(13f, 0f, 25f, 0.5f)
                reflectiveQuadToRelative(23f, 2.5f)
                quadToRelative(-14f, 21f, -21f, 44f)
                reflectiveQuadToRelative(-7f, 48f)
                verticalLineToRelative(65f)
                close()
                moveToRelative(240f, 0f)
                verticalLineToRelative(-65f)
                quadToRelative(0f, -32f, 17.5f, -58.5f)
                reflectiveQuadTo(307f, 550f)
                reflectiveQuadToRelative(76.5f, -30f)
                reflectiveQuadToRelative(96.5f, -10f)
                quadToRelative(53f, 0f, 97.5f, 10f)
                reflectiveQuadToRelative(76.5f, 30f)
                reflectiveQuadToRelative(49f, 46.5f)
                reflectiveQuadToRelative(17f, 58.5f)
                verticalLineToRelative(65f)
                close()
                moveToRelative(540f, 0f)
                verticalLineToRelative(-65f)
                quadToRelative(0f, -26f, -6.5f, -49f)
                reflectiveQuadTo(754f, 563f)
                quadToRelative(11f, -2f, 22.5f, -2.5f)
                reflectiveQuadToRelative(23.5f, -0.5f)
                quadToRelative(72f, 0f, 116f, 26.5f)
                reflectiveQuadToRelative(44f, 70.5f)
                verticalLineToRelative(63f)
                close()
                moveToRelative(-455f, -80f)
                horizontalLineToRelative(311f)
                quadToRelative(-10f, -20f, -55.5f, -35f)
                reflectiveQuadTo(480f, 590f)
                reflectiveQuadToRelative(-100.5f, 15f)
                reflectiveQuadToRelative(-54.5f, 35f)
                moveTo(160f, 520f)
                quadToRelative(-33f, 0f, -56.5f, -23.5f)
                reflectiveQuadTo(80f, 440f)
                quadToRelative(0f, -34f, 23.5f, -57f)
                reflectiveQuadToRelative(56.5f, -23f)
                quadToRelative(34f, 0f, 57f, 23f)
                reflectiveQuadToRelative(23f, 57f)
                quadToRelative(0f, 33f, -23f, 56.5f)
                reflectiveQuadTo(160f, 520f)
                moveToRelative(640f, 0f)
                quadToRelative(-33f, 0f, -56.5f, -23.5f)
                reflectiveQuadTo(720f, 440f)
                quadToRelative(0f, -34f, 23.5f, -57f)
                reflectiveQuadToRelative(56.5f, -23f)
                quadToRelative(34f, 0f, 57f, 23f)
                reflectiveQuadToRelative(23f, 57f)
                quadToRelative(0f, 33f, -23f, 56.5f)
                reflectiveQuadTo(800f, 520f)
                moveToRelative(-320f, -40f)
                quadToRelative(-50f, 0f, -85f, -35f)
                reflectiveQuadToRelative(-35f, -85f)
                quadToRelative(0f, -51f, 35f, -85.5f)
                reflectiveQuadToRelative(85f, -34.5f)
                quadToRelative(51f, 0f, 85.5f, 34.5f)
                reflectiveQuadTo(600f, 360f)
                quadToRelative(0f, 50f, -34.5f, 85f)
                reflectiveQuadTo(480f, 480f)
                moveToRelative(0f, -80f)
                quadToRelative(17f, 0f, 28.5f, -11.5f)
                reflectiveQuadTo(520f, 360f)
                reflectiveQuadToRelative(-11.5f, -28.5f)
                reflectiveQuadTo(480f, 320f)
                reflectiveQuadToRelative(-28.5f, 11.5f)
                reflectiveQuadTo(440f, 360f)
                reflectiveQuadToRelative(11.5f, 28.5f)
                reflectiveQuadTo(480f, 400f)
                moveToRelative(0f, -40f)
            }
        }.build()

        return _Groups!!
    }

private var _Groups: ImageVector? = null

