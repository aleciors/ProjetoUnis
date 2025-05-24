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

class BeginnerActivity : AppCompatActivity() {

    private var isCreatineExpanded = false
    private var isProteinExpanded = false
    private var isMultivitaminExpanded = false
    private var isOmega3Expanded = false
    private var isHydrationExpanded = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        try {
            setContentView(R.layout.activity_beginner_new)
        } catch (e: Exception) {
            Log.e("BeginnerActivity", "Erro ao inflar o layout: ${e.message}")
            finish()
            return
        }

        val ageSpinner = findViewById<Spinner>(R.id.ageSpinner)
        val sexSpinner = findViewById<Spinner>(R.id.sexSpinner)
        val weightSpinner = findViewById<Spinner>(R.id.weightSpinner)
        val heightSpinner = findViewById<Spinner>(R.id.heightSpinner)
        val mealsSpinner = findViewById<Spinner>(R.id.mealsSpinner)
        val calculateButton = findViewById<Button>(R.id.calculateButton)
        val clearButton = findViewById<Button>(R.id.clearButton)
        val backButton = findViewById<Button>(R.id.backButton)
        val recommendationsTitle = findViewById<TextView>(R.id.recommendationsTitle)
        val supplementsResultContainer = findViewById<LinearLayout>(R.id.supplementsResultContainer)

        calculateButton.setOnClickListener {
            updateResults(
                ageSpinner.selectedItem.toString(),
                sexSpinner.selectedItem.toString(),
                weightSpinner.selectedItem.toString(),
                heightSpinner.selectedItem.toString(),
                mealsSpinner.selectedItem.toString(),
                supplementsResultContainer
            )
        }

        clearButton.setOnClickListener {
            ageSpinner.setSelection(0)
            sexSpinner.setSelection(0)
            weightSpinner.setSelection(0)
            heightSpinner.setSelection(0)
            mealsSpinner.setSelection(0)
            recommendationsTitle.visibility = View.GONE
            supplementsResultContainer.removeAllViews()
            isCreatineExpanded = false
            isProteinExpanded = false
            isMultivitaminExpanded = false
            isOmega3Expanded = false
            isHydrationExpanded = false
        }

        backButton.setOnClickListener {
            finish() // Volta para a tela anterior (MainActivity)
        }
    }

    // Função para atualizar os resultados
    private fun updateResults(age: String, sex: String, weight: String, height: String, meals: String, supplementsResultContainer: LinearLayout) {
        val recommendationsTitle = findViewById<TextView>(R.id.recommendationsTitle)
        if (age == "Selecione sua idade" || sex == "Selecione seu sexo" ||
            weight == "Selecione seu peso" || height == "Selecione sua altura" ||
            meals == "Selecione suas refeições diárias") {
            recommendationsTitle.visibility = View.GONE
            supplementsResultContainer.removeAllViews()
            supplementsResultContainer.addView(createErrorView("Preencha todos os campos"))
            return
        }

        // Mostrar o título ao calcular com sucesso
        recommendationsTitle.visibility = View.VISIBLE
        supplementsResultContainer.removeAllViews()

        // Conversão de valores
        val weightValue = weight.toInt()
        val heightValue = height.toDouble()
        val mealsValue = meals.toInt()
        val ageValue = age.toInt()

        // Cálculo de Hidratação
        val hydrationMl = weightValue * 35
        val hydrationL = hydrationMl / 1000.0

        // Cálculo de Creatina
        val creatineMin = weightValue * 0.03
        val creatineMax = weightValue * 0.07
        val creatineMinFormatted = String.format(Locale.US, "%.1f", creatineMin)
        val creatineMaxFormatted = String.format(Locale.US, "%.1f", creatineMax)

        // Cálculo de Whey Protein
        val proteinPerKg = 1.0
        val totalProtein = (weightValue * proteinPerKg).toInt()
        val mainMeals = min(mealsValue, 2)
        val lightMeals = max(0, mealsValue - 2)
        val proteinFromMainMeals = mainMeals * 11.15
        val proteinFromLightMeals = lightMeals * 2.0
        val proteinFromFood = proteinFromMainMeals + proteinFromLightMeals
        val proteinDeficit = (totalProtein - proteinFromFood).coerceAtLeast(0.0).toInt()

        // Criar cartões expansíveis na ordem especificada
        val items = mutableListOf<Triple<String, String, String>>()

        // 1. Creatina (se aplicável)
        if (sex == "Masculino" && ageValue >= 18 || sex == "Feminino" && ageValue >= 18) {
            items.add(Triple("⚡", "Creatina", "Dose mínima: $creatineMinFormatted g/dia\nDose máxima: $creatineMaxFormatted g/dia"))
        }

        // 2. Whey Protein (se aplicável)
        if (sex == "Masculino" && ageValue >= 18 || sex == "Feminino" && ageValue >= 18) {
            items.add(Triple("🥛", "Whey Protein (sugestão diária)", "Dose recomendada: $proteinDeficit g/dia"))
        }

        // 3. Hidratação (sempre presente)
        items.add(Triple("💧", "Hidratação", "Ingestão diária sugerida: ${String.format(Locale.US, "%.2f", hydrationL)} litros"))

        // 4. Ômega-3
        val omega3Dose = if (sex == "Masculino" && ageValue >= 18 || sex == "Feminino" && ageValue >= 18) "1000 mg por dia" else "500 mg por dia"
        items.add(Triple("🌿", "Ômega-3", "Dose recomendada: $omega3Dose"))

        // 5. Multivitamínico
        val multivitaminLabel = if (ageValue < 18) "Multivitamínico Infantil" else "Multivitamínico"
        items.add(Triple("💊", multivitaminLabel, "Dose recomendada: 1 cápsula por dia"))

        items.forEach { (icon, label, details) ->
            val isExpanded = when (label) {
                "Creatina" -> isCreatineExpanded
                "Whey Protein (sugestão diária)" -> isProteinExpanded
                "Multivitamínico", "Multivitamínico Infantil" -> isMultivitaminExpanded
                "Ômega-3" -> isOmega3Expanded
                "Hidratação" -> isHydrationExpanded
                else -> false
            }
            supplementsResultContainer.addView(createExpandableCard(icon, label, details, isExpanded) {
                when (label) {
                    "Creatina" -> isCreatineExpanded = !isCreatineExpanded
                    "Whey Protein (sugestão diária)" -> isProteinExpanded = !isProteinExpanded
                    "Multivitamínico", "Multivitamínico Infantil" -> isMultivitaminExpanded = !isMultivitaminExpanded
                    "Ômega-3" -> isOmega3Expanded = !isOmega3Expanded
                    "Hidratação" -> isHydrationExpanded = !isHydrationExpanded
                }
                updateResults(age, sex, weight, height, meals, supplementsResultContainer)
            })
        }
    }

    // Função para criar um cartão expansível
    private fun createExpandableCard(icon: String, label: String, details: String, isExpanded: Boolean, onClick: () -> Unit): CardView {
        val cardView = LayoutInflater.from(this).inflate(R.layout.result_card, null) as CardView
        cardView.radius = 8f
        cardView.cardElevation = 4f
        cardView.setCardBackgroundColor(ContextCompat.getColor(this, R.color.white))
        cardView.layoutParams = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            LinearLayout.LayoutParams.WRAP_CONTENT
        ).apply { setMargins(0, 0, 0, 16) }

        val cardContent = cardView.findViewById<LinearLayout>(R.id.cardContent)
        val titleText = cardView.findViewById<TextView>(R.id.cardTitle)
        val detailsText = cardView.findViewById<TextView>(R.id.cardDetails)
        val expandButton = cardView.findViewById<TextView>(R.id.expandButton)

        titleText.text = "$icon  $label"
        detailsText.text = details
        detailsText.visibility = if (isExpanded) View.VISIBLE else View.GONE
        expandButton.text = if (isExpanded) "▼" else "▶"
        // Alterar a cor com base no estado de expansão
        expandButton.setTextColor(ContextCompat.getColor(this, if (isExpanded) android.R.color.holo_orange_light else R.color.blue))
        expandButton.setOnClickListener { onClick() }

        // Tornar o cartão inteiro clicável
        cardView.setOnClickListener {
            onClick()
            detailsText.visibility = if (detailsText.visibility == View.GONE) View.VISIBLE else View.GONE
            expandButton.text = if (detailsText.visibility == View.VISIBLE) "▼" else "▶"
            expandButton.setTextColor(ContextCompat.getColor(this, if (detailsText.visibility == View.VISIBLE) android.R.color.holo_orange_light else R.color.blue))
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
            setTextColor(ContextCompat.getColor(this@BeginnerActivity, R.color.red))
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
        return 0 // Retorna 0 se o valor não for encontrado
    }
}