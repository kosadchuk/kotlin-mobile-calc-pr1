package com.example.firstlabkotlin

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.rememberScrollState


import com.example.firstlabkotlin.ui.theme.FirstLabKotlinTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            FirstLabKotlinTheme {
                FuelCalculatorApp()
            }
        }
    }
}

@Composable
fun FuelCalculatorApp() {
    var H_value by remember { mutableStateOf("") }
    var C_value by remember { mutableStateOf("") }
    var S_value by remember { mutableStateOf("") }
    var N_value by remember { mutableStateOf("") }
    var O_value by remember { mutableStateOf("") }
    var A_value by remember { mutableStateOf("") }
    var W_value by remember { mutableStateOf("") }

    var results by remember { mutableStateOf<Map<String, Any>?>(null) }

    val scrollState = rememberScrollState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(scrollState)
    ) {
        Text("Калькулятор")

        Spacer(modifier = Modifier.height(16.dp))

        TextField(
            value = H_value,
            onValueChange = { H_value = it },
            label = { Text("Водень (H%)") }
        )

        Spacer(modifier = Modifier.height(8.dp))
        TextField(
            value = C_value,
            onValueChange = { C_value = it },
            label = { Text("Вуглець (C%)") }
        )

        Spacer(modifier = Modifier.height(8.dp))
        TextField(
            value = S_value,
            onValueChange = { S_value = it },
            label = { Text("Сірка (S%)") }
        )

        Spacer(modifier = Modifier.height(8.dp))
        TextField(
            value = N_value,
            onValueChange = { N_value = it },
            label = { Text("Азот (N%)") }
        )

        Spacer(modifier = Modifier.height(8.dp))
        TextField(
            value = O_value,
            onValueChange = { O_value = it },
            label = { Text("Кисень (O%)") }
        )

        Spacer(modifier = Modifier.height(8.dp))
        TextField(
            value = A_value,
            onValueChange = { A_value = it },
            label = { Text("Зола (A%)") }
        )

        Spacer(modifier = Modifier.height(8.dp))
        TextField(
            value = W_value,
            onValueChange = { W_value = it },
            label = { Text("Волога (W%)") }
        )

        Button(
            onClick = {
                val h = H_value.toDoubleOrNull() ?: 0.0
                val c = C_value.toDoubleOrNull() ?: 0.0
                val s = S_value.toDoubleOrNull() ?: 0.0
                val n = N_value.toDoubleOrNull() ?: 0.0
                val o = O_value.toDoubleOrNull() ?: 0.0
                val a = A_value.toDoubleOrNull() ?: 0.0
                val w = W_value.toDoubleOrNull() ?: 0.0

                results = calculateFuelProperties(h, c, s, n, o, a, w)
            },
            modifier = Modifier.padding(top = 16.dp)
        ) {
            Text("Розрахувати")
        }

        results?.let {
            Spacer(modifier = Modifier.height(16.dp))
            CalculationResults(it)
        }
    }
}

@Composable
fun CalculationResults(results: Map<String, Any>) {
    Column(modifier = Modifier.padding(16.dp)) {
        Text("Коефіцієнт переходу від робочої до сухої маси: ${String.format("%.2f", results["Conversion Coefficient to Dry Mass"] as Double)}")
        Text("Коефіцієнт переходу від робочої до горючої маси: ${String.format("%.2f", results["Conversion Coefficient to Combustible Mass"] as Double)}")

        Text("Склад сухої маси палива")
        (results["Dry Mass"] as Map<String, Double>).forEach { (component, value) ->
            Text("$component: ${String.format("%.2f", value)}%")
        }

        Spacer(modifier = Modifier.height(16.dp))

        Text("Склад горючої маси палива")
        (results["Combustible Mass"] as Map<String, Double>).forEach { (component, value) ->
            Text("$component: ${String.format("%.2f", value)}%")
        }

        Spacer(modifier = Modifier.height(16.dp))

        Text("Нижча теплота згоряння для робочої маси: ${String.format("%.2f", results["Lower Heating Value Raw"] as Double)} МДж/кг")
        Text("Нижча теплота згоряння для сухої маси: ${String.format("%.2f", results["Lower Heating Value Dry"] as Double)} МДж/кг")
        Text("Нижча теплота згоряння для горючої маси: ${String.format("%.2f", results["Lower Heating Value Combustible"] as Double)} МДж/кг")
    }
}


fun calculateFuelProperties(
    h: Double, c: Double, s: Double, n: Double, o: Double, a: Double, w: Double
): Map<String, Any> {
    val krS = 100 / (100 - w)
    val krG = 100 / (100 - w - a)

    val dryMass = mapOf(
        "HC" to h * krS,
        "CC" to c * krS,
        "SC" to s * krS,
        "NC" to n * krS,
        "OC" to o * krS,
        "AC" to a * krS
    )

    val combustibleMass = mapOf(
        "HГ" to h * krG,
        "CГ" to c * krG,
        "SГ" to s * krG,
        "NГ" to n * krG,
        "OГ" to o * krG
    )

    // Нижча теплота згоряння для робочої маси
    val lowerHeatingValueRaw = (339 * c + 1030 * h - 108.8 * (o - s) - 25 * w) / 1000

    // Нижча теплота згоряння для сухої маси
    val lowerHeatingValueDry = (lowerHeatingValueRaw + 0.025 * w) * (100 / (100 - w))

    // Нижча теплота згоряння для горючої маси
    val lowerHeatingValueCombustible = (lowerHeatingValueRaw + 0.025 * w) * (100 / (100 - w - a))

    return mapOf(
        "Dry Mass" to dryMass,
        "Combustible Mass" to combustibleMass,
        "Lower Heating Value Raw" to lowerHeatingValueRaw,
        "Lower Heating Value Dry" to lowerHeatingValueDry,
        "Lower Heating Value Combustible" to lowerHeatingValueCombustible,
        "Conversion Coefficient to Dry Mass" to krS,
        "Conversion Coefficient to Combustible Mass" to krG
    )
}