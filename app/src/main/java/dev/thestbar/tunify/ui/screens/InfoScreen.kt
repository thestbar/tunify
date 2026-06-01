package dev.thestbar.tunify.ui.screens

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp

@Composable
fun InfoScreen(modifier: Modifier = Modifier) {
    val primary = MaterialTheme.colorScheme.primary
    val onSurface = MaterialTheme.colorScheme.onSurface
    val onSurfaceVariant = MaterialTheme.colorScheme.onSurfaceVariant
    val link = SpanStyle(color = primary, textDecoration = TextDecoration.Underline)
    val bold = SpanStyle(fontWeight = FontWeight.Bold)
    val paraStyle = MaterialTheme.typography.bodyLarge
    val paraModifier = Modifier.padding(bottom = 20.dp)

    Column(modifier = modifier.fillMaxSize()) {
        TunifyTopBar(title = "Info")
        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp, vertical = 8.dp)
                .padding(bottom = 32.dp)
        ) {
            Text(
                text = buildAnnotatedString {
                    withStyle(bold) { append("Tunify") }
                    append(" is a guitar tuner application implementing the Yin algorithm for pitch detection. You can read more in the ")
                    withStyle(link) { append("paper") }
                    append(" published by Alain de Cheveigné and Hideki Kawahara in 2001.")
                },
                style = paraStyle, color = onSurface, modifier = paraModifier
            )
            Text(
                text = buildAnnotatedString {
                    append("The code of the project is entirely open sourced and you can find it ")
                    withStyle(link) { append("here") }
                    append(".")
                },
                style = paraStyle, color = onSurface, modifier = paraModifier
            )
            Text(
                text = buildAnnotatedString {
                    append("More information about the way the project was structured and built can be found on my personal website ")
                    withStyle(link) { append("thestbar.dev") }
                    append(".")
                },
                style = paraStyle, color = onSurface, modifier = paraModifier
            )
            Text(
                text = buildAnnotatedString {
                    append("The Android code is written in Kotlin. Feel free to contact me at ")
                    withStyle(link) { append("stavrosbarousis@gmail.com") }
                    append(" or through my personal website.")
                },
                style = paraStyle, color = onSurface, modifier = paraModifier
            )
            Text(
                text = buildAnnotatedString {
                    append("This application does not collect any user data. For more information read our ")
                    withStyle(link) { append("Privacy Policy") }
                    append(".")
                },
                style = paraStyle, color = onSurface, modifier = paraModifier
            )
            Text(
                text = "MIT License",
                style = MaterialTheme.typography.titleMedium,
                color = onSurface,
                modifier = Modifier.padding(bottom = 12.dp)
            )
            Text(
                text = "Copyright © 2023 Stavros Barousis. Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files (the \"Software\"), to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software.",
                style = MaterialTheme.typography.bodyMedium,
                color = onSurfaceVariant
            )
        }
    }
}
