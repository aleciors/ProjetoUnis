package com.example.healthcalculator

import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.Spinner
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import kotlin.math.pow

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val editAge = findViewById<EditText>(R.id.editAge)
        val spinnerSex = findViewById<Spinner>(R.id.spinnerSex)
        val editHeight = findViewById<EditText>(R.id.editHeight)
        val editWeight = findViewById<EditText>(R.id.editWeight)
        val editSleep = findViewById<EditText>(R.id.editSleep)
        val editMeals = findViewById<EditText>(R.id.editMeals)
        val spinnerActivityLevel = findViewById<Spinner>(R.id.spinnerActivityLevel)
        val calculateButton = findViewById<Button>(R.id.calculateButton)
        val clearButton = findViewById<Button>(R.id.clearButton)
        val resultLayout = findViewById<LinearLayout>(R.id.resultLayout)
        val bmiText = findViewById<TextView>(R.id.bmiText)
        val energyText = findViewById<TextView>(R.id.energyText)
        val cortisolText = findViewById<TextView>(R.id.cortisolText)
        val supplementsText = findViewById<TextView>(R.id.supplementsText)
        val noteText = findViewById<TextView>(R.id.noteText)

        calculateButton.setOnClickListener {
            val age = editAge.text.toString().toIntOrNull()
            val sex = when (spinnerSex.selectedItemPosition) {
                0 -> "M"
                1 -> "F"
                else -> ""
            }
            val heightCm = editHeight.text.toString().toDoubleOrNull()
            val weight = editWeight.text.toString().toDoubleOrNull()
            val sleepHours = editSleep.text.toString().toDoubleOrNull()
            val meals = editMeals.text.toString().toIntOrNull()
            val activityLevel = spinnerActivityLevel.selectedItemPosition

            if (age == null || sex.isEmpty() || heightCm == null || weight == null || sleepHours == null || meals == null) {
                bmiText.text = "Por favor, preencha todos os campos corretamente."
                energyText.text = ""
                cortisolText.text = ""
                supplementsText.text = ""
                noteText.text = ""
                resultLayout.visibility = View.VISIBLE
                return@setOnClickListener
            }

            val heightM = heightCm / 100.0

            val bmi = weight / heightM.pow(2)
            val bmiClassification = when {
                bmi < 18.5 -> "Abaixo do Peso"
                bmi <= 24.9 -> "Peso Ideal"
                bmi <= 29.9 -> "Sobrepeso"
                bmi <= 34.9 -> "Obesidade Grau I"
                bmi <= 39.9 -> "Obesidade Grau II"
                else -> "Obesidade Grau III"
            }

            val geb = when (sex) {
                "M" -> 66.47 + (13.75 * weight) + (5.0 * heightCm) - (6.76 * age)
                "F" -> 655.1 + (9.56 * weight) + (1.85 * heightCm) - (4.68 * age)
                else -> 0.0
            }

            val activityFactor = when (activityLevel) {
                0 -> 1.2 // Sedentário
                1 -> 1.375 // Levemente Ativo
                2 -> 1.55 // Moderadamente Ativo
                3 -> 1.725 // Muito Ativo
                4 -> 1.9 // Extremamente Ativo
                else -> 1.55
            }
            val get = geb * activityFactor

            val caloriesToLoseWeightLightMin = get - 300
            val caloriesToLoseWeightLightMax = get - 500
            val caloriesToLoseWeightModerateMin = get - 500
            val caloriesToLoseWeightModerateMax = get - 700

            val cortisolEstimate = when {
                sleepHours < 6 -> "Alto (sono insuficiente pode elevar cortisol)"
                sleepHours <= 8 -> "Normal (sono adequado)"
                else -> "Baixo (sono excessivo pode indicar desequilíbrios)"
            }

            val creatineMin = weight * 0.03
            val creatineMax = weight * 0.07
            val totalProteinNeed = when {
                sleepHours < 6 || bmi > 30 -> weight * 0.8
                else -> weight * 1.3
            }
            val proteinPerMeal = totalProteinNeed / meals
            val wheyPortions = (totalProteinNeed - (proteinPerMeal * meals * 0.5)) / 25
            val wheyRecommendation = if (wheyPortions > 0) "${String.format("%.0f", wheyPortions)} porções/dia (25g cada)" else "0 porções/dia"
            val b12 = when (age) {
                in 0..6 -> 0.4
                in 7..12 -> 0.5
                in 13..36 -> 0.9
                in 37..96 -> 1.2
                in 97..156 -> 1.8
                in 157..Int.MAX_VALUE -> 2.4
                else -> 2.4
            }
            val water = when {
                age <= 17 -> weight * 40
                age <= 64 -> weight * 35
                else -> weight * 27.5
            } / 1000.0

            var vitaminD = when {
                age in 1..18 -> 700.0
                age in 19..70 -> 600.0
                age > 70 -> 1400.0
                else -> 600.0
            }
            if (sex == "F" && age in 15..45) {
                vitaminD = if (bmi > 30) 1000.0 else 600.0
            } else if (age in 19..70 && bmi > 30) {
                vitaminD = 800.0
            }

            bmiText.text = "IMC: ${String.format("%.2f", bmi)} ($bmiClassification)"
            energyText.text = """
                Gasto Energético:
                - GEB (Harris-Benedict): ${String.format("%.0f", geb)} kcal/dia
                - GET (fator de atividade ${String.format("%.3f", activityFactor)}): ${String.format("%.0f", get)} kcal/dia
                - Calorias para Perder Peso (Déficit Leve): ${String.format("%.0f", caloriesToLoseWeightLightMin)} a ${String.format("%.0f", caloriesToLoseWeightLightMax)} kcal/dia
                - Calorias para Perder Peso (Déficit Moderado): ${String.format("%.0f", caloriesToLoseWeightModerateMin)} a ${String.format("%.0f", caloriesToLoseWeightModerateMax)} kcal/dia
            """.trimIndent()
            cortisolText.text = "Estimativa de Cortisol: $cortisolEstimate"
            supplementsText.text = """
                Recomendações de Suplementos e Hidratação:
                - Creatina diária:
                  - Mínima: ${String.format("%.2f", creatineMin)} g
                  - Máxima: ${String.format("%.2f", creatineMax)} g
                - Whey Protein: $wheyRecommendation
                - Vitamina B12: ${String.format("%.1f", b12)} mcg/dia
                - Vitamina D: ${String.format("%.0f", vitaminD)} UI/dia
                - Água: ${String.format("%.2f", water)} L/dia
                - Ômega-3: 1-2 g/dia (dose geral para saúde)
            """.trimIndent()
            noteText.text = "*Nota: Consulte um nutricionista para ajustes personalizados."
            resultLayout.visibility = View.VISIBLE
        }

        clearButton.setOnClickListener {
            editAge.text.clear()
            spinnerSex.setSelection(0)
            editHeight.text.clear()
            editWeight.text.clear()
            editSleep.text.clear()
            editMeals.text.clear()
            spinnerActivityLevel.setSelection(0)
            bmiText.text = "IMC: Aguardando cálculo..."
            energyText.text = "Gasto Energético: Aguardando cálculo..."
            cortisolText.text = "Estimativa de Cortisol: Aguardando cálculo..."
            supplementsText.text = "Recomendações de Suplementos e Hidratação: Aguardando cálculo..."
            noteText.text = "*Nota: Consulte um nutricionista para ajustes personalizados."
            resultLayout.visibility = View.GONE
        }
    }
}