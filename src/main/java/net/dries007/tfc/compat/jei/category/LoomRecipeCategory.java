/*
 * Licensed under the EUPL, Version 1.2.
 * You may obtain a copy of the Licence at:
 * https://joinup.ec.europa.eu/collection/eupl/eupl-text-eupl-12
 */

package net.dries007.tfc.compat.jei.category;

import net.minecraft.world.item.ItemStack;

import com.mojang.blaze3d.vertex.PoseStack;
import mezz.jei.api.constants.VanillaTypes;
import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.gui.builder.IRecipeSlotBuilder;
import mezz.jei.api.gui.ingredient.IRecipeSlotsView;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.recipe.IFocusGroup;
import mezz.jei.api.recipe.RecipeIngredientRole;
import mezz.jei.api.recipe.RecipeType;
import net.dries007.tfc.common.items.TFCItems;
import net.dries007.tfc.common.recipes.LoomRecipe;

public class LoomRecipeCategory extends BaseRecipeCategory<LoomRecipe>
{
    public LoomRecipeCategory(RecipeType<LoomRecipe> type, IGuiHelper helper)
    {
        super(type, helper, helper.createBlankDrawable(78, 26), new ItemStack(TFCItems.BURLAP_CLOTH.get()));
    }

    @Override
    public void setRecipe(IRecipeLayoutBuilder builder, LoomRecipe recipe, IFocusGroup focuses)
    {
        IRecipeSlotBuilder inputItem = builder.addSlot(RecipeIngredientRole.INPUT, 6, 5);
        IRecipeSlotBuilder outputItem = builder.addSlot(RecipeIngredientRole.OUTPUT, 56, 5);

        // The ingredient doesn't come with an amount, but recipes take more than one
        inputItem.addIngredients(VanillaTypes.ITEM, collapseWithAmount(recipe.getIngredient(), recipe.getInputCount()));
        outputItem.addItemStack(recipe.getResultItem());
    }

    @Override
    public void draw(LoomRecipe recipe, IRecipeSlotsView recipeSlots, PoseStack stack, double mouseX, double mouseY)
    {
        slot.draw(stack, 5, 4);
        slot.draw(stack, 55, 4);
        arrow.draw(stack, 28, 5);
        arrowAnimated.draw(stack, 28, 5);
    }
}
