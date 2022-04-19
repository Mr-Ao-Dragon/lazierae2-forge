package com.almostreliable.lazierae2.content.machine;

import com.almostreliable.lazierae2.component.EnergyHandler;
import com.almostreliable.lazierae2.component.InventoryHandler.MachineInventory;
import com.almostreliable.lazierae2.content.GenericMenu;
import com.almostreliable.lazierae2.core.Setup.Menus;
import com.almostreliable.lazierae2.inventory.OutputSlot;
import com.almostreliable.lazierae2.inventory.UpgradeSlot;
import com.almostreliable.lazierae2.util.DataSlotUtil;
import com.almostreliable.lazierae2.util.GameUtil;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.DataSlot;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.energy.CapabilityEnergy;
import net.minecraftforge.energy.IEnergyStorage;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.SlotItemHandler;

import static com.almostreliable.lazierae2.util.TextUtil.f;

public class MachineMenu extends GenericMenu<MachineEntity> {

    private MachineInventory machineInventory;

    public MachineMenu(int id, MachineEntity entity, Inventory menuInventory) {
        super(Menus.MACHINE.get(), id, entity, menuInventory);
        entity.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY).ifPresent(inv -> {
            machineInventory = (MachineInventory) inv;
            setupContainerInventory();
        });
        setupPlayerInventory();
        syncData();
    }

    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        var stack = ItemStack.EMPTY;
        var slot = slots.get(index);

        // check if the slot has an item inside
        if (!slot.hasItem()) return stack;

        var slotStack = slot.getItem();
        stack = slotStack.copy();

        if (index < machineInventory.getSlots()) {
            // from machine to inventory
            if (!moveItemStackTo(slotStack,
                machineInventory.getSlots(),
                machineInventory.getSlots() + PLAYER_INV_SIZE,
                false
            )) {
                return ItemStack.EMPTY;
            }
        } else if (GameUtil.isValidUpgrade(slotStack)) {
            // from inventory to upgrade slot
            if (!moveItemStackTo(slotStack, MachineInventory.UPGRADE_SLOT, MachineInventory.UPGRADE_SLOT + 1, false)) {
                return ItemStack.EMPTY;
            }
        } else {
            // from inventory to machine inputs
            if (!moveItemStackTo(slotStack,
                MachineInventory.NON_INPUT_SLOTS,
                MachineInventory.NON_INPUT_SLOTS + machineInventory.getSlots(),
                false
            )) {
                return ItemStack.EMPTY;
            }
        }

        // check if something changed
        if (slotStack.isEmpty()) {
            slot.set(ItemStack.EMPTY);
        } else {
            slot.setChanged();
        }

        if (slotStack.getCount() == stack.getCount()) return ItemStack.EMPTY;
        slot.onTake(player, slotStack);

        return stack;
    }

    public boolean hasUpgrades() {
        return getUpgradeCount() > 0;
    }

    @Override
    protected void setupContainerInventory() {
        var inputSlots = machineInventory.getInputSlots();
        addSlot(new UpgradeSlot(this, machineInventory, MachineInventory.UPGRADE_SLOT, 8, 50));
        addSlot(new OutputSlot(machineInventory, MachineInventory.OUTPUT_SLOT, 116, 29));
        if (inputSlots == 1) {
            addSlot(new SlotItemHandler(machineInventory, 2, 44, 29));
        } else if (inputSlots == 3) {
            addSlot(new SlotItemHandler(machineInventory, 2, 44, 8));
            addSlot(new SlotItemHandler(machineInventory, 3, 44, 29));
            addSlot(new SlotItemHandler(machineInventory, 4, 44, 50));
        } else {
            throw new IllegalArgumentException(f("Invalid input slot count: {}", inputSlots));
        }
    }

    @Override
    protected int getSlotY() {
        return 72;
    }

    private void syncData() {
        addDataSlot(DataSlotUtil.forBoolean(entity, entity::isAutoExtracting, entity::setAutoExtract));
        addDataSlot(DataSlotUtil.forInteger(entity, entity::getProgress, entity::setProgress));
        addDataSlot(DataSlotUtil.forInteger(entity, entity::getProcessTime, entity::setProcessTime));
        addDataSlot(DataSlotUtil.forInteger(entity, entity::getRecipeTime, entity::setRecipeTime));
        addDataSlot(DataSlotUtil.forInteger(entity, entity::getEnergyCost, entity::setEnergyCost));
        addDataSlot(DataSlotUtil.forInteger(entity, entity::getRecipeEnergy, entity::setRecipeEnergy));
        addMultipleDataSlots(DataSlotUtil.forIntegerSplit(entity, this::getEnergyStored, this::setEnergyStored));
        addMultipleDataSlots(DataSlotUtil.forIntegerSplit(entity, this::getEnergyCapacity, this::setEnergyCapacity));
        addDataSlots(entity.sideConfig.toContainerData());
    }

    private void addMultipleDataSlots(DataSlot... holders) {
        for (var holder : holders) {
            addDataSlot(holder);
        }
    }

    public int getUpgradeCount() {
        return machineInventory.getUpgradeCount();
    }

    public int getEnergyStored() {
        return getEnergyCap().map(IEnergyStorage::getEnergyStored).orElse(0);
    }

    public void setEnergyStored(int energy) {
        getEnergyCap().ifPresent(energyCap -> ((EnergyHandler) energyCap).setEnergy(energy));
    }

    public int getEnergyCapacity() {
        return getEnergyCap().map(IEnergyStorage::getMaxEnergyStored).orElse(1);
    }

    private void setEnergyCapacity(int capacity) {
        getEnergyCap().ifPresent(energyCap -> ((EnergyHandler) energyCap).setCapacity(capacity));
    }

    private LazyOptional<IEnergyStorage> getEnergyCap() {
        return entity.getCapability(CapabilityEnergy.ENERGY);
    }
}