package com.android.shelfLife.viewmodel.recipes

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import com.android.shelfLife.model.recipe.Ingredient
import com.android.shelfLife.model.recipe.Recipe
import com.android.shelfLife.model.recipe.RecipeRepository
import com.android.shelfLife.model.user.UserRepository
import kotlinx.coroutines.selects.select

class IndividualRecipeViewModel (
    private val recipeRepository: RecipeRepository,
):ViewModel(){

    var selectedRecipe by mutableStateOf<Recipe?>(null)
    var selectedRecipeIsNonEmpty by mutableStateOf<Boolean>(true)

    init{
        selectedRecipe = recipeRepository.selectedRecipe.value
        if(selectedRecipe == null){
            selectedRecipeIsNonEmpty = false
        }
    }

    fun getRecipeName():String{
        return selectedRecipe!!.name
    }

    fun getRecipeServing() : Float {
        return selectedRecipe!!.servings
    }

    fun getRecipeTime() : Long {
        return selectedRecipe!!.time.inWholeMinutes
    }

    fun getRecipeIngredients(): List<Ingredient>{
        return selectedRecipe!!.ingredients
    }

    fun getRecipeInstruction(): List<String>{
        return selectedRecipe!!.instructions
    }
}