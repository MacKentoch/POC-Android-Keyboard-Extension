package fr.eda.monclavierconfigurable

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import coil.compose.rememberAsyncImagePainter
import coil.decode.SvgDecoder
import coil.request.ImageRequest

@Composable
fun Touche(
    keyModel: KeyModel,
    modifier: Modifier = Modifier,
    onClick: (KeyModel) -> Unit
) {
    Surface(
        modifier = modifier
            .padding(2.dp)
            .clickable { onClick(keyModel) },
        shape = MaterialTheme.shapes.medium,
        color = MaterialTheme.colorScheme.secondaryContainer
    ) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier.fillMaxSize().padding(4.dp)
        ) {
            when (keyModel.display) {
                DisplayType.TEXT -> {
                    Text(text = if (keyModel.action == KeyAction.SPACE) "" else keyModel.value)
                }
                DisplayType.SVG -> {
                    val painter = rememberAsyncImagePainter(
                        model = ImageRequest.Builder(LocalContext.current)
                            .data("file:///android_asset/icons/${keyModel.value}") // Chemin vers notre SVG
                            .decoderFactory(SvgDecoder.Factory())
                            .build()
                    )
                    Image(
                        painter = painter,
                        contentDescription = keyModel.action.name
                    )
                }
            }
        }
    }
}

@Composable
fun ClavierView(
    layout: KeyboardLayout,
    onToucheClick: (KeyModel) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .height(240.dp)
            .background(MaterialTheme.colorScheme.background)
            .padding(4.dp)
    ) {
        layout.rows.forEach { row ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                row.forEach { keyModel ->
                    Touche(
                        keyModel = keyModel,
                        modifier = Modifier
                            .weight(keyModel.weight)
                            .fillMaxHeight(),
                        onClick = onToucheClick
                    )
                }
            }
        }
    }
}