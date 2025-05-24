package com.example.healthcalculator

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.widget.Button
import android.widget.LinearLayout
import android.widget.Spinner
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import androidx.core.content.ContextCompat
import java.util.Locale
import kotlin.math.max
import kotlin.math.min
import kotlin.math.pow

class IntermediateActivity : AppCompatActivity() {

    private var isCreatineExpanded = false
    private var isProteinExpanded = false
    private var isMultivitaminExpanded = false
    private var isOmega3Expanded = false
    private var isHydrationExpanded = false
    private var isEnergyExpanded = false
    private var isImcExpanded = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        try {
            setContentView(R.layout.activity_intermediate_new)
        } catch (e: Exception) {
            Log.e("IntermediateActivity", "Erro ao inflar o layout: ${e.message}")
            finish()
            return
        }

        val ageSpinner = findViewById<Spinner>(R.id.ageSpinner)
        val sexSpinner = findViewById<Spinner>(R.id.sexSpinner)
        val weightSpinner = findViewById<Spinner>(R.id.weightSpinner)
        val heightSpinner = findViewById<Spinner>(R.id.heightSpinner)
        val sleepSpinner = findViewById<Spinner>(R.id.sleepSpinner)
        val mealsSpinner = findViewById<Spinner>(R.id.mealsSpinner)
        val trainingSpinner = findViewById<Spinner>(R.id.trainingSpinner)
        val goalsSpinner = findViewById<Spinner>(R.id.goalsSpinner)
        val calculateButton = findViewById<Button>(R.id.calculateButton)
        val clearButton = findViewById<Button>(R.id.clearButton)
        val backButton = findViewById<Button>(R.id.backButton)
        val recommendationsTitle = findViewById<TextView>(R.id.recommendationsTitle)
        val supplementsResult = findViewById<LinearLayout>(R.id.supplementsResult)

        calculateButton.setOnClickListener {
            updateResults(
                ageSpinner.selectedItem.toString(),
                sexSpinner.selectedItem.toString(),
                weightSpinner.selectedItem.toString(),
                heightSpinner.selectedItem.toString(),
                sleepSpinner.selectedItem.toString(),
                mealsSpinner.selectedItem.toString(),
                trainingSpinner.selectedItem.toString(),
                goalsSpinner.selectedItem.toString(),
                supplementsResult
            )
        }

        clearButton.setOnClickListener {
            ageSpinner.setSelection(0)
            sexSpinner.setSelection(0)
            weightSpinner.setSelection(0)
            heightSpinner.setSelection(0)
            sleepSpinner.setSelection(0)
            mealsSpinner.setSelection(0)
            trainingSpinner.setSelection(0)
            goalsSpinner.setSelection(0)
            recommendationsTitle.visibility = View.GONE
            supplementsResult.removeAllViews()
            isCreatineExpanded = false
            isProteinExpanded = false
            isMultivitaminExpanded = false
            isOmega3Expanded = false
            isHydrationExpanded = false
            isEnergyExpanded = false
            isImcExpanded = false
        }

        backButton.setOnClickListener {
            finish() // Volta para a tela anterior (MainActivity)
        }
    }

    // Função para atualizar os resultados
    private fun updateResults(
        age: String,
        sex: String,
        weight: String,
        height: String,
        sleep: String,
        meals: String,
        trainingFrequency: String,
        goal: String,
        supplementsResult: LinearLayout
    ) {
        val recommendationsTitle = findViewById<TextView>(R.id.recommendationsTitle)
        if (age == "Selecione sua idade" || sex == "Selecione seu sexo" ||
            weight == "Selecione seu peso" || height == "Selecione sua altura" ||
            sleep == "Selecione suas horas de sono" || meals == "Selecione suas refeições diárias" ||
            trainingFrequency == "Selecione sua frequência de treino" ||
            goal == "Selecione seu objetivo") {
            recommendationsTitle.visibility = View.GONE
            supplementsResult.removeAllViews()
            supplementsResult.addView(createErrorView("Preencha todos os campos"))
            return
        }

        // Mostrar o título ao calcular com sucesso
        recommendationsTitle.visibility = View.VISIBLE
        supplementsResult.removeAllViews()

        // Conversão de valores
        val weightValue = weight.toInt()
        val heightValue = height.toDouble()
        val ageValue = age.toInt()
        val mealsValue = meals.toInt()
        val sleepValue = sleep.toDouble()
        val trainingIndex = when (trainingFrequency) {
            "1-2x/semana" -> 1
            "3-5x/semana" -> 2
            "6-7x/semana" -> 3
            "2x por dia" -> 4
            else -> 0
        }
        val goalIndex = when (goal) {
            "Ganho de Massa" -> 0
            "Perda de Gordura" -> 1
            "Manutenção" -> 2
            else -> 2
        }

        // Cálculo de Hidratação
        val hydrationMl = weightValue * 35
        val hydrationL = hydrationMl / 1000.0

        // Cálculo de Creatina
        val creatineMin = weightValue * 0.03
        val creatineMax = weightValue * 0.07
        val creatineMinFormatted = String.format(Locale.US, "%.1f", creatineMin)
        val creatineMaxFormatted = String.format(Locale.US, "%.1f", creatineMax)

        // Cálculo de Whey Protein
        val proteinPerKg = 1.6 // Para nível Intermediário, assumimos 1.6 g/kg
        val totalProtein = (weightValue * proteinPerKg).toInt()
        val mainMeals = min(mealsValue, 2)
        val lightMeals = max(0, mealsValue - 2)
        val proteinFromMainMeals = mainMeals * 11.15
        val proteinFromLightMeals = lightMeals * 2.0
        val proteinFromFood = proteinFromMainMeals + proteinFromLightMeals
        val proteinDeficit = (totalProtein - proteinFromFood).coerceAtLeast(0.0).toInt()

        // Cálculo do Gasto Energético
        val heightM = heightValue / 100.0
        val geb = when (sex) {
            "Masculino" -> 66.47 + (13.75 * weightValue) + (5.0 * heightValue) - (6.76 * ageValue)
            "Feminino" -> 655.1 + (9.56 * weightValue) + (1.85 * heightValue) - (4.68 * ageValue)
            else -> 0.0
        }
        val tmb = if (sex == "Masculino") {
            (10 * weightValue) + (6.25 * heightValue) - (5 * ageValue) + 5
        } else {
            (10 * weightValue) + (6.25 * heightValue) - (5 * ageValue) - 161
        }
        val activityFactor = when (trainingFrequency) {
            "1-2x/semana" -> 1.375 // Leve
            "3-5x/semana" -> 1.55  // Moderado
            "6-7x/semana" -> 1.725 // Intenso
            "2x por dia" -> 1.9    // Atleta
            else -> 1.2            // Sedentário (padrão)
        }
        val totalCaloricExpenditure = tmb * activityFactor
        val targetCalories = when (goalIndex) {
            0 -> totalCaloricExpenditure * 1.15 // Ganho de Massa: +15%
            1 -> totalCaloricExpenditure - 500 // Perda de Gordura: -500 kcal
            2 -> totalCaloricExpenditure // Manutenção: sem ajuste
            else -> totalCaloricExpenditure
        }
        val caloriesToLoseWeightLightMin = totalCaloricExpenditure - 300
        val caloriesToLoseWeightLightMax = totalCaloricExpenditure - 500
        val caloriesToLoseWeightModerateMin = totalCaloricExpenditure - 500
        val caloriesToLoseWeightModerateMax = totalCaloricExpenditure - 700
        val energyDetails = """
            - GEB (Harris-Benedict): ${String.format("%.0f", geb)} kcal/dia
            - TMB (Mifflin-St Jeor): ${String.format("%.0f", tmb)} kcal/dia
            - Gasto Calórico Total (com treino, fator ${String.format("%.3f", activityFactor)}): ${String.format("%.0f", totalCaloricExpenditure)} kcal/dia
            - Meta Calórica (Objetivo): ${String.format("%.0f", targetCalories)} kcal/dia
            - Calorias para Perda de Peso (Déficit Leve): ${String.format("%.0f", caloriesToLoseWeightLightMin)} a ${String.format("%.0f", caloriesToLoseWeightLightMax)} kcal/dia
            - Calorias para Perda de Peso (Déficit Moderado): ${String.format("%.0f", caloriesToLoseWeightModerateMin)} a ${String.format("%.0f", caloriesToLoseWeightModerateMax)} kcal/dia
        """.trimIndent()

        // Cálculo do IMC
        val weightInKg = weightValue.toDouble()
        val heightInMeters = heightValue / 100.0
        val heightSquared = heightInMeters * heightInMeters
        val imc = weightInKg / heightSquared
        val imcFormatted = String.format(Locale.US, "%.1f", imc)
        val imcClassification = when {
            imc < 18.5 -> "Abaixo do peso"
            imc < 25 -> "Peso normal"
            imc < 30 -> "Sobrepeso"
            imc < 35 -> "Obesidade Grau I"
            imc < 40 -> "Obesidade Grau II"
            else -> "Obesidade Grau III (mórbida)"
        }
        val imcDetails = """
            - Valor: $imcFormatted
            - Classificação: $imcClassification
        """.trimIndent()

        // Adicionar itens ao supplementsResult
        supplementsResult.addView(createResultItem("📏 IMC", imcDetails, isImcExpanded) { isImcExpanded = !isImcExpanded })
        supplementsResult.addView(createResultItem("💧 Hidratação", "Ingestão diária sugerida: ${String.format("%.2f", hydrationL)} litros", isHydrationExpanded) { isHydrationExpanded = !isHydrationExpanded })
        supplementsResult.addView(createResultItem("⚡ Creatina", "Dose mínima: $creatineMinFormatted g/dia\nDose máxima: $creatineMaxFormatted g/dia", isCreatineExpanded) { isCreatineExpanded = !isCreatineExpanded })
        supplementsResult.addView(createResultItem("🥛 Whey Protein (sugestão diária)", "Dose recomendada: $proteinDeficit g/dia", isProteinExpanded) { isProteinExpanded = !isProteinExpanded })
        supplementsResult.addView(createResultItem("🔋 Gasto Energético", energyDetails, isEnergyExpanded) { isEnergyExpanded = !isEnergyExpanded })
        supplementsResult.addView(createResultItem("💊 Multivitamínico", "Dose recomendada: 1 cápsula por dia", isMultivitaminExpanded) { isMultivitaminExpanded = !isMultivitaminExpanded })
        supplementsResult.addView(createResultItem("🌿 Ômega-3", "Dose recomendada: 1000 mg por dia", isOmega3Expanded) { isOmega3Expanded = !isOmega3Expanded })
    }

    // Função para criar um item de resultado
    private fun createResultItem(label: String, details: String, isExpanded: Boolean, onClick: () -> Unit): CardView {
        val cardView = LayoutInflater.from(this).inflate(R.layout.result_item, null) as CardView
        cardView.radius = 8f
        cardView.cardElevation = 4f
        cardView.setCardBackgroundColor(ContextCompat.getColor(this, R.color.white))
        cardView.layoutParams = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            LinearLayout.LayoutParams.WRAP_CONTENT
        ).apply { setMargins(0, 0, 0, 16) }

        val cardContent = cardView.findViewById<LinearLayout>(R.id.cardContent)
        val itemLabel = cardView.findViewById<TextView>(R.id.itemLabel)
        val itemDetails = cardView.findViewById<TextView>(R.id.itemDetails)
        val expandButton = cardView.findViewById<TextView>(R.id.expandButton)

        itemLabel.text = label
        itemDetails.text = details
        itemDetails.visibility = if (isExpanded) View.VISIBLE else View.GONE
        expandButton.text = if (isExpanded) "▼" else "▶"
        expandButton.setTextColor(ContextCompat.getColor(this, if (isExpanded) android.R.color.holo_orange_light else R.color.blue))

        // Clique na seta
        expandButton.setOnClickListener {
            onClick() // Atualiza o estado de expansão (ex.: isImcExpanded = !isImcExpanded)
            itemDetails.visibility = if (itemDetails.visibility == View.GONE) View.VISIBLE else View.GONE
            expandButton.text = if (itemDetails.visibility == View.VISIBLE) "▼" else "▶"
            expandButton.setTextColor(ContextCompat.getColor(this, if (itemDetails.visibility == View.VISIBLE) android.R.color.holo_orange_light else R.color.blue))
        }

        // Clique no cartão inteiro
        cardView.setOnClickListener {
            onClick()
            itemDetails.visibility = if (itemDetails.visibility == View.GONE) View.VISIBLE else View.GONE
            expandButton.text = if (itemDetails.visibility == View.VISIBLE) "▼" else "▶"
            expandButton.setTextColor(ContextCompat.getColor(this, if (itemDetails.visibility == View.VISIBLE) android.R.color.holo_orange_light else R.color.blue))
        }

        return cardView
    }

    // Função para criar uma view de erro
    private fun createErrorView(message: String): TextView {
        return TextView(this).apply {
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
            text = message
            setTextColor(ContextCompat.getColor(this@IntermediateActivity, R.color.red))
            textSize = 16f
        }
    }

    // Função auxiliar para encontrar o índice de um item no Spinner
    private fun getSpinnerIndex(spinner: Spinner, value: String): Int {
        for (i in 0 until spinner.count) {
            if (spinner.getItemAtPosition(i).toString() == value) {
                return i
            }
        }
        return 0
    }
}