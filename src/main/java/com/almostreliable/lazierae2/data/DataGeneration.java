package com.almostreliable.lazierae2.data;

import com.almostreliable.lazierae2.data.client.BlockStateData;
import com.almostreliable.lazierae2.data.client.ItemModelData;
import com.almostreliable.lazierae2.data.server.RecipeData;
import com.almostreliable.lazierae2.data.server.TagData.BlockTags;
import com.almostreliable.lazierae2.data.server.TagData.ItemTags;
import net.minecraft.data.DataGenerator;
import net.minecraftforge.common.data.ExistingFileHelper;
import net.minecraftforge.fml.event.lifecycle.GatherDataEvent;

public final class DataGeneration {

    private DataGeneration() {}

    public static void init(GatherDataEvent event) {
        DataGenerator gen = event.getGenerator();
        ExistingFileHelper fileHelper = event.getExistingFileHelper();

        if (event.includeServer()) {
            // generate tags
            BlockTags blockTags = new BlockTags(gen, fileHelper);
            gen.addProvider(blockTags);
            gen.addProvider(new ItemTags(gen, blockTags, fileHelper));
            // generate recipes
            gen.addProvider(new RecipeData(gen));
        }
        if (event.includeClient()) {
            // generate block states and block models
            gen.addProvider(new BlockStateData(gen, fileHelper));
            // generate item models
            gen.addProvider(new ItemModelData(gen, fileHelper));
        }
    }
}
