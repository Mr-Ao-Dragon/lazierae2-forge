package com.almostreliable.lazierae2.recipe.type;

import com.almostreliable.lazierae2.content.processor.ProcessorType;
import com.almostreliable.lazierae2.recipe.IRecipeItemProvider;
import com.almostreliable.lazierae2.recipe.IngredientWithCount;
import net.minecraft.core.NonNullList;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.common.crafting.conditions.ICondition;

import java.util.List;

@FunctionalInterface
public interface ProcessorRecipeFactory {
    ProcessorRecipe create(
        ResourceLocation recipeId, ProcessorType processorType, List<ICondition> conditions, IRecipeItemProvider output,
        NonNullList<IngredientWithCount> inputs, int processTime, int energyCost
    );
}
