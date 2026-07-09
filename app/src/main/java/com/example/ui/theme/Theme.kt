package com.example.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext

private val DarkColorScheme =
  darkColorScheme(
    primary = NaturalGreenPrimaryDark,
    onPrimary = NaturalGreenOnPrimaryDark,
    primaryContainer = NaturalGreenContainerDark,
    onPrimaryContainer = NaturalGreenOnContainerDark,
    
    secondary = NaturalRustSecondaryDark,
    onSecondary = NaturalRustOnSecondaryDark,
    secondaryContainer = NaturalRustContainerDark,
    onSecondaryContainer = NaturalRustOnContainerDark,
    
    tertiary = NaturalWarmTertiary,
    onTertiary = NaturalWarmOnTertiary,
    tertiaryContainer = NaturalWarmContainer,
    onTertiaryContainer = NaturalWarmOnContainer,
    
    background = NaturalBackgroundDark,
    onBackground = Color(0xFFFDF8F6),
    surface = NaturalSurfaceDark,
    onSurface = Color(0xFFFDF8F6),
    surfaceVariant = NaturalSurfaceVariantDark,
    onSurfaceVariant = Color(0xFFC9C5C1)
  )

private val LightColorScheme =
  lightColorScheme(
    primary = NaturalGreenPrimary,
    onPrimary = NaturalGreenOnPrimary,
    primaryContainer = NaturalGreenContainer,
    onPrimaryContainer = NaturalGreenOnContainer,

    secondary = NaturalRustSecondary,
    onSecondary = NaturalRustOnSecondary,
    secondaryContainer = NaturalRustContainer,
    onSecondaryContainer = NaturalRustOnContainer,

    tertiary = NaturalWarmTertiary,
    onTertiary = NaturalWarmOnTertiary,
    tertiaryContainer = NaturalWarmContainer,
    onTertiaryContainer = NaturalWarmOnContainer,

    background = NaturalBackgroundLight,
    onBackground = NaturalTextPrimaryLight,
    surface = Color.White,
    onSurface = NaturalTextPrimaryLight,
    surfaceVariant = Color(0xFFF5EFE7),
    onSurfaceVariant = Color(0xFF7A7572)
  )

@Composable
fun MyApplicationTheme(
  darkTheme: Boolean = isSystemInDarkTheme(),
  // Set dynamic color default to false to always honor the gorgeous theme
  dynamicColor: Boolean = false,
  selectedTheme: AppThemeOption = AppThemeOption.SAGE,
  content: @Composable () -> Unit,
) {
  val colorScheme =
    when {
      dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
        val context = LocalContext.current
        if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
      }

      else -> getColorSchemeForTheme(selectedTheme, darkTheme)
    }

  MaterialTheme(colorScheme = colorScheme, typography = Typography, content = content)
}

enum class AppThemeOption(val displayName: String, val primaryColorLight: Color) {
    SAGE("Sage Green", Color(0xFF4F6630)),
    OCEAN("Cosmic Ocean", Color(0xFF1E3D59)),
    TERRACOTTA("Sunset Terracotta", Color(0xFF9E4B33)),
    AMETHYST("Lavender Amethyst", Color(0xFF673AB7)),
    ROSE("Rose Quartz", Color(0xFFB04A67)),
    WHITE("Minimalist White", Color(0xFFE0E0E0))
}

fun getColorSchemeForTheme(themeOption: AppThemeOption, isDark: Boolean): androidx.compose.material3.ColorScheme {
    return if (isDark) {
        when (themeOption) {
            AppThemeOption.SAGE -> DarkColorScheme
            AppThemeOption.OCEAN -> darkColorScheme(
                primary = Color(0xFF90CAF9),
                onPrimary = Color(0xFF0D47A1),
                primaryContainer = Color(0xFF1565C0).copy(alpha = 0.5f),
                onPrimaryContainer = Color(0xFFE8F1F5),
                secondary = Color(0xFF81D4FA),
                onSecondary = Color(0xFF006064),
                secondaryContainer = Color(0xFF00838F).copy(alpha = 0.5f),
                onSecondaryContainer = Color(0xFFE0F7FA),
                tertiary = Color(0xFFFFB74D),
                onTertiary = Color(0xFFE65100),
                tertiaryContainer = Color(0xFFEF6C00).copy(alpha = 0.5f),
                onTertiaryContainer = Color(0xFFFFF3E0),
                background = Color(0xFF0B131A),
                onBackground = Color(0xFFF0F4F8),
                surface = Color(0xFF111E2E),
                onSurface = Color(0xFFF0F4F8),
                surfaceVariant = Color(0xFF1A2F45),
                onSurfaceVariant = Color(0xFFB0C4DE)
            )
            AppThemeOption.TERRACOTTA -> darkColorScheme(
                primary = Color(0xFFE6846A),
                onPrimary = Color(0xFF5C2619),
                primaryContainer = Color(0xFF7A3929).copy(alpha = 0.5f),
                onPrimaryContainer = Color(0xFFFCEAE6),
                secondary = Color(0xFFF7C392),
                onSecondary = Color(0xFF5E2B06),
                secondaryContainer = Color(0xFF7A3E11).copy(alpha = 0.5f),
                onSecondaryContainer = Color(0xFFFFF3E0),
                tertiary = Color(0xFFE8C5A5),
                onTertiary = Color(0xFF3E2715),
                tertiaryContainer = Color(0xFF523B28).copy(alpha = 0.5f),
                onTertiaryContainer = Color(0xFFE8C5A5),
                background = Color(0xFF1D1513),
                onBackground = Color(0xFFFCF3F0),
                surface = Color(0xFF281E1B),
                onSurface = Color(0xFFFCF3F0),
                surfaceVariant = Color(0xFF382925),
                onSurfaceVariant = Color(0xFFE0D0CC)
            )
            AppThemeOption.AMETHYST -> darkColorScheme(
                primary = Color(0xFFD1C4E9),
                onPrimary = Color(0xFF311B92),
                primaryContainer = Color(0xFF4A148C).copy(alpha = 0.5f),
                onPrimaryContainer = Color(0xFFF3E5F5),
                secondary = Color(0xFFF06292),
                onSecondary = Color(0xFF4A0033),
                secondaryContainer = Color(0xFF880E4F).copy(alpha = 0.5f),
                onSecondaryContainer = Color(0xFFFCE4EC),
                tertiary = Color(0xFFB39DDB),
                onTertiary = Color(0xFF12005E),
                tertiaryContainer = Color(0xFF311B92).copy(alpha = 0.5f),
                onTertiaryContainer = Color(0xFFEDE7F6),
                background = Color(0xFF120F1D),
                onBackground = Color(0xFFF5F2F9),
                surface = Color(0xFF1A162B),
                onSurface = Color(0xFFF5F2F9),
                surfaceVariant = Color(0xFF25213B),
                onSurfaceVariant = Color(0xFFD6CEE3)
            )
            AppThemeOption.ROSE -> darkColorScheme(
                primary = Color(0xFFF48FB1),
                onPrimary = Color(0xFF5A102E),
                primaryContainer = Color(0xFF782143).copy(alpha = 0.5f),
                onPrimaryContainer = Color(0xFFFDE4EC),
                secondary = Color(0xFFFFAB91),
                onSecondary = Color(0xFF5D1000),
                secondaryContainer = Color(0xFF7E2A12).copy(alpha = 0.5f),
                onSecondaryContainer = Color(0xFFFBE9E7),
                tertiary = Color(0xFFD81B60),
                onTertiary = Color(0xFFFFFFFF),
                tertiaryContainer = Color(0xFF880E4F).copy(alpha = 0.5f),
                onTertiaryContainer = Color(0xFFFCE4EC),
                background = Color(0xFF1F1115),
                onBackground = Color(0xFFFCEEF1),
                surface = Color(0xFF2C1B20),
                onSurface = Color(0xFFFCEEF1),
                surfaceVariant = Color(0xFF3A282D),
                onSurfaceVariant = Color(0xFFE4D2D6)
            )
            AppThemeOption.WHITE -> darkColorScheme(
                primary = Color(0xFFFAFAFA),
                onPrimary = Color(0xFF212121),
                primaryContainer = Color(0xFF424242).copy(alpha = 0.5f),
                onPrimaryContainer = Color(0xFFFAFAFA),
                secondary = Color(0xFFEEEEEE),
                onSecondary = Color(0xFF212121),
                secondaryContainer = Color(0xFF424242).copy(alpha = 0.5f),
                onSecondaryContainer = Color(0xFFEEEEEE),
                tertiary = Color(0xFFBDBDBD),
                onTertiary = Color(0xFF212121),
                tertiaryContainer = Color(0xFF616161).copy(alpha = 0.5f),
                onTertiaryContainer = Color(0xFFFAFAFA),
                background = Color(0xFF121212),
                onBackground = Color(0xFFFAFAFA),
                surface = Color(0xFF1E1E1E),
                onSurface = Color(0xFFFAFAFA),
                surfaceVariant = Color(0xFF333333),
                onSurfaceVariant = Color(0xFFBDBDBD)
            )
        }
    } else {
        when (themeOption) {
            AppThemeOption.SAGE -> LightColorScheme
            AppThemeOption.OCEAN -> lightColorScheme(
                primary = Color(0xFF1E3D59),
                onPrimary = Color(0xFFFFFFFF),
                primaryContainer = Color(0xFFE8F1F5),
                onPrimaryContainer = Color(0xFF0F2338),
                secondary = Color(0xFF17B890),
                onSecondary = Color(0xFFFFFFFF),
                secondaryContainer = Color(0xFFD3F2EA),
                onSecondaryContainer = Color(0xFF003D2F),
                tertiary = Color(0xFFFFC107),
                onTertiary = Color(0xFF423200),
                tertiaryContainer = Color(0xFFFFF2CC),
                onTertiaryContainer = Color(0xFF423200),
                background = Color(0xFFF5F8FA),
                onBackground = Color(0xFF111E2E),
                surface = Color.White,
                onSurface = Color(0xFF111E2E),
                surfaceVariant = Color(0xFFE6ECEF),
                onSurfaceVariant = Color(0xFF455A64)
            )
            AppThemeOption.TERRACOTTA -> lightColorScheme(
                primary = Color(0xFF9E4B33),
                onPrimary = Color(0xFFFFFFFF),
                primaryContainer = Color(0xFFFCEAE6),
                onPrimaryContainer = Color(0xFF3D150C),
                secondary = Color(0xFFD67D4B),
                onSecondary = Color(0xFFFFFFFF),
                secondaryContainer = Color(0xFFFAEDE5),
                onSecondaryContainer = Color(0xFF4A2000),
                tertiary = Color(0xFF825D42),
                onTertiary = Color(0xFFFFFFFF),
                tertiaryContainer = Color(0xFFF5EFE7),
                onTertiaryContainer = Color(0xFF2D2926),
                background = Color(0xFFFDF9F7),
                onBackground = Color(0xFF2D1E1B),
                surface = Color.White,
                onSurface = Color(0xFF2D1E1B),
                surfaceVariant = Color(0xFFF5ECE8),
                onSurfaceVariant = Color(0xFF6E5650)
            )
            AppThemeOption.AMETHYST -> lightColorScheme(
                primary = Color(0xFF673AB7),
                onPrimary = Color(0xFFFFFFFF),
                primaryContainer = Color(0xFFEDE7F6),
                onPrimaryContainer = Color(0xFF21005D),
                secondary = Color(0xFFE91E63),
                onSecondary = Color(0xFFFFFFFF),
                secondaryContainer = Color(0xFFFCE4EC),
                onSecondaryContainer = Color(0xFF40001D),
                tertiary = Color(0xFF9575CD),
                onTertiary = Color(0xFFFFFFFF),
                tertiaryContainer = Color(0xFFF3E5F5),
                onTertiaryContainer = Color(0xFF2D1457),
                background = Color(0xFFFAF8FC),
                onBackground = Color(0xFF1C1A22),
                surface = Color.White,
                onSurface = Color(0xFF1C1A22),
                surfaceVariant = Color(0xFFF1EEF5),
                onSurfaceVariant = Color(0xFF5D596B)
            )
            AppThemeOption.ROSE -> lightColorScheme(
                primary = Color(0xFFB04A67),
                onPrimary = Color(0xFFFFFFFF),
                primaryContainer = Color(0xFFFCEEF2),
                onPrimaryContainer = Color(0xFF3F0B1E),
                secondary = Color(0xFFD3524A),
                onSecondary = Color(0xFFFFFFFF),
                secondaryContainer = Color(0xFFFDE9E7),
                onSecondaryContainer = Color(0xFF490F09),
                tertiary = Color(0xFF880E4F),
                onTertiary = Color(0xFFFFFFFF),
                tertiaryContainer = Color(0xFFFFF1F4),
                onTertiaryContainer = Color(0xFF4F002B),
                background = Color(0xFFFFF9FA),
                onBackground = Color(0xFF2C1B20),
                surface = Color.White,
                onSurface = Color(0xFF2C1B20),
                surfaceVariant = Color(0xFFF7EAED),
                onSurfaceVariant = Color(0xFF755E63)
            )
            AppThemeOption.WHITE -> lightColorScheme(
                primary = Color(0xFF424242),
                onPrimary = Color(0xFFFFFFFF),
                primaryContainer = Color(0xFFEEEEEE),
                onPrimaryContainer = Color(0xFF212121),
                secondary = Color(0xFF616161),
                onSecondary = Color(0xFFFFFFFF),
                secondaryContainer = Color(0xFFF5F5F5),
                onSecondaryContainer = Color(0xFF212121),
                tertiary = Color(0xFF757575),
                onTertiary = Color(0xFFFFFFFF),
                tertiaryContainer = Color(0xFFFAFAFA),
                onTertiaryContainer = Color(0xFF212121),
                background = Color(0xFFFAFAFA),
                onBackground = Color(0xFF212121),
                surface = Color.White,
                onSurface = Color(0xFF212121),
                surfaceVariant = Color(0xFFEEEEEE),
                onSurfaceVariant = Color(0xFF616161)
            )
        }
    }
}
