package com.example.cyloop

import kotlin.math.pow
import kotlin.math.roundToInt

fun Double.format(decimals: Int): String {
    val factor = 10.0.pow(decimals)
    val rounded = (this * factor).roundToInt() / factor
    return rounded.toString()
}

fun formatPrice(price: Double): String {
    return price.format(2)
}
