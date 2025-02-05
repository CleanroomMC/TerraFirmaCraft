/*
 * Licensed under the EUPL, Version 1.2.
 * You may obtain a copy of the Licence at:
 * https://joinup.ec.europa.eu/collection/eupl/eupl-text-eupl-12
 */

package net.dries007.tfc.common.commands;

import java.util.function.Supplier;

import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.commands.synchronization.SuggestionProviders;

import net.minecraftforge.common.util.Lazy;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import net.dries007.tfc.util.Helpers;
import net.dries007.tfc.world.biome.TFCBiomes;

public final class TFCCommands
{
    public static final Supplier<SuggestionProvider<CommandSourceStack>> TFC_BIOMES = register("available_biomes", (context, builder) -> SharedSuggestionProvider.suggestResource(TFCBiomes.getVariantsKeys(), builder));

    public static void registerSuggestionProviders()
    {
        TFC_BIOMES.get();
    }

    public static void registerCommands(CommandDispatcher<CommandSourceStack> dispatcher)
    {
        // Register all new commands as sub commands of the `tfc` root
        dispatcher.register(Commands.literal("tfc")
            .then(ClearWorldCommand.create())
            .then(ClimateUpdateCommand.create())
            .then(HeatCommand.create())
            .then(PlayerCommand.create())
            .then(TreeCommand.create())
            .then(LocateVeinCommand.create())
            .then(CountBlockCommand.create())
            .then(TFCLocateCommand.create())
        );

        // For command modifications / replacements, we register directly
        // First, remove the vanilla command by the same name
        // This seems to work. It does leave the command still lying around, but it shouldn't matter as we replace it anyway
        dispatcher.getRoot().getChildren().removeIf(node -> node.getName().equals("time"));
        dispatcher.register(TimeCommand.create());
    }

    public static <S extends SharedSuggestionProvider> Supplier<SuggestionProvider<S>> register(String id, SuggestionProvider<SharedSuggestionProvider> provider)
    {
        return Lazy.of(() -> SuggestionProviders.register(Helpers.identifier(id), provider));
    }
}