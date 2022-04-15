package com.almostreliable.lazierae2.data.client;

import com.almostreliable.lazierae2.core.Setup.Items;
import com.almostreliable.lazierae2.machine.MachineType;
import net.minecraft.data.DataGenerator;
import net.minecraft.item.Item;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.model.generators.ItemModelProvider;
import net.minecraftforge.client.model.generators.ModelFile;
import net.minecraftforge.common.data.ExistingFileHelper;

import java.util.Objects;
import java.util.function.Supplier;

import static com.almostreliable.lazierae2.core.Constants.MOD_ID;
import static com.almostreliable.lazierae2.util.TextUtil.f;

public class ItemModelData extends ItemModelProvider {

    public ItemModelData(
        DataGenerator gen, ExistingFileHelper fileHelper
    ) {
        super(gen, MOD_ID, fileHelper);
    }

    @Override
    protected void registerModels() {
        existingParent(MachineType.AGGREGATOR.getId());
        existingParent(MachineType.CENTRIFUGE.getId());
        existingParent(MachineType.ENERGIZER.getId());
        existingParent(MachineType.ETCHER.getId());

        builder(Items.CARB_FLUIX_DUST);
        builder(Items.COAL_DUST);
        builder(Items.FLUIX_IRON);
        builder(Items.FLUIX_STEEL);
        builder(Items.GROWTH_CHAMBER);
        builder(Items.LOGIC_UNIT);
        builder(Items.PARALLEL_PRINTED);
        builder(Items.PARALLEL_PROCESSOR);
        builder(Items.RESONATING_GEM);
        builder(Items.SPEC_CORE_1);
        builder(Items.SPEC_CORE_2);
        builder(Items.SPEC_CORE_4);
        builder(Items.SPEC_CORE_8);
        builder(Items.SPEC_CORE_16);
        builder(Items.SPEC_CORE_32);
        builder(Items.SPEC_CORE_64);
        builder(Items.SPEC_PRINTED);
        builder(Items.SPEC_PROCESSOR);
        builder(Items.UNIVERSAL_PRESS);
    }

    /**
     * Creates block item data from an existing block parent.
     *
     * @param id the id of the item and the parent block to get the data from
     */
    private void existingParent(String id) {
        ResourceLocation parentLocation = new ResourceLocation(MOD_ID, f("block/{}", id));
        withExistingParent(id, parentLocation);
    }

    /**
     * Creates basic item data from an item supplier.
     *
     * @param item the item to create the data for
     */
    private void builder(Supplier<? extends Item> item) {
        String name = Objects.requireNonNull(item.get().getRegistryName()).getPath();
        ModelFile itemGenerated = getExistingFile(mcLoc("item/generated"));
        getBuilder(name).parent(itemGenerated).texture("layer0", f("item/{}", name));
    }
}
