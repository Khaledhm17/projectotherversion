package com.example.projectotherversion.presentation.theme

import androidx.compose.material3.*
import androidx.compose.runtime.Composable

private val LightColorScheme = lightColorScheme(
    primary = MainBlue,
    onPrimary = PureWhite,
    background = PureWhite,
    onBackground = PureBlack,
    surface = PureWhite,
    onSurface = PureBlack,

    // هذا هو اللون الذي سيجعل الرسائل بنفس لون الشكاوى في الصورة
    surfaceVariant = ComplaintBoxGray,
    onSurfaceVariant = PureBlack,

    outline = FrameSubtle
)

@Composable
fun ProjectOtherVersionTheme(
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = LightColorScheme,
        typography = Typography,
        content = content
    )
}