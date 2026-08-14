package com.example.cyloop.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import qrcode.raw.QRCodeProcessor

@Composable
fun QRCodeView(
    data: String,
    modifier: Modifier = Modifier,
    size: Dp = 200.dp,
    color: Color = Color.Black
) {
    val qrCodeProcessor = remember(data) { QRCodeProcessor(data) }
    val rawData = remember(qrCodeProcessor) { qrCodeProcessor.encode() }
    
    Canvas(modifier = modifier.size(size)) {
        val numModules = rawData.size
        val cellSize = this.size.width / numModules
        
        rawData.forEachIndexed { rowIndex, row ->
            row.forEachIndexed { colIndex, cell ->
                if (cell.dark) {
                    drawRect(
                        color = color,
                        topLeft = Offset(colIndex * cellSize, rowIndex * cellSize),
                        size = Size(cellSize, cellSize)
                    )
                }
            }
        }
    }
}
