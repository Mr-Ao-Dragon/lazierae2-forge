package com.almostreliable.lazierae2.content.maintainer;

import appeng.api.config.FuzzyMode;
import appeng.api.networking.IStackWatcher;
import appeng.api.networking.storage.IStorageWatcherNode;
import appeng.api.stacks.AEItemKey;
import appeng.api.stacks.AEKey;
import appeng.api.stacks.GenericStack;
import net.minecraft.nbt.CompoundTag;
import net.minecraftforge.common.util.INBTSerializable;

import javax.annotation.Nullable;

import static com.almostreliable.lazierae2.core.Constants.Nbt.*;

public class StorageManager implements IStorageWatcherNode, INBTSerializable<CompoundTag> {

    private final Storage[] storages;
    private final MaintainerEntity owner;
    @Nullable
    private IStackWatcher stackWatcher;

    StorageManager(MaintainerEntity owner, int slots) {
        this.owner = owner;
        storages = new Storage[slots];
    }

    public Storage get(int slot) {
        if (storages[slot] == null) {
            storages[slot] = new Storage();
        }
        return storages[slot];
    }

    @Override
    public void updateWatcher(IStackWatcher newWatcher) {
        stackWatcher = newWatcher;
        resetWatcher();
    }

    @Override
    public void onStackChange(AEKey what, long amount) {
        for (var slot = 0; slot < storages.length; slot++) {
            if (owner.getCraftRequests().matches(slot, what)) {
                get(slot).knownAmount = amount;
                get(slot).pendingAmount = 0;
            }
        }
    }

    public long computeDelta(int slot) {
        var request = owner.getCraftRequests().get(slot);
        if (request.stack().isEmpty()) {
            return 0;
        }

        var storedAmount = get(slot).knownAmount + get(slot).pendingAmount;
        if (storedAmount < request.count()) {
            return request.batch();
        }
        return 0;
    }

    @Override
    public CompoundTag serializeNBT() {
        var tag = new CompoundTag();
        for (var slot = 0; slot < storages.length; slot++) {
            tag.put(String.valueOf(slot), get(slot).serializeNBT());
        }
        return tag;
    }

    @Override
    public void deserializeNBT(CompoundTag tag) {
        for (var slot = 0; slot < storages.length; slot++) {
            get(slot).deserializeNBT(tag.getCompound(String.valueOf(slot)));
        }
    }

    void clear(int slot) {
        get(slot).knownAmount = -1;
        calcSlotAmount(slot);
        resetWatcher();
    }

    private void populateWatcher(IStackWatcher watcher) {
        for (var slot = 0; slot < storages.length; slot++) {
            if (!owner.getCraftRequests().get(slot).stack().isEmpty()) {
                watcher.add(AEItemKey.of(owner.getCraftRequests().get(slot).stack()));
            }
        }
    }

    private void resetWatcher() {
        if (stackWatcher != null) {
            stackWatcher.reset();
            populateWatcher(stackWatcher);
        }
    }

    private void calcSlotAmount(int slot) {
        var request = owner.getCraftRequests().get(slot);
        if (request.stack().isEmpty()) {
            return;
        }
        var genericStack = GenericStack.fromItemStack(request.stack());
        if (genericStack == null) {
            return;
        }
        get(slot).knownAmount = owner
            .getMainNodeGrid()
            .getStorageService()
            .getInventory()
            .getAvailableStacks()
            .get(genericStack.what());
    }

    public static class Storage implements INBTSerializable<CompoundTag> {

        @Nullable
        private AEKey itemType;
        private long bufferAmount;
        private long pendingAmount;
        private long knownAmount = -1;

        @Override
        public CompoundTag serializeNBT() {
            var tag = new CompoundTag();
            if (itemType != null) tag.put(ITEM_TYPE_ID, itemType.toTagGeneric());
            tag.putLong(BUFFER_AMOUNT_ID, bufferAmount);
            tag.putLong(PENDING_AMOUNT_ID, pendingAmount);
            tag.putLong(KNOWN_AMOUNT_ID, knownAmount);
            return tag;
        }

        @Override
        public void deserializeNBT(CompoundTag tag) {
            if (tag.contains(ITEM_TYPE_ID)) itemType = AEKey.fromTagGeneric(tag.getCompound(ITEM_TYPE_ID));
            bufferAmount = tag.getLong(BUFFER_AMOUNT_ID);
            pendingAmount = tag.getLong(PENDING_AMOUNT_ID);
            knownAmount = tag.getLong(KNOWN_AMOUNT_ID);
        }

        /**
         * @param inserted amount of items inserted into the system
         * @return true if the buffer is not empty
         */
        public boolean compute(long inserted) {
            pendingAmount = inserted;
            bufferAmount = getBufferAmount() - inserted;
            if (bufferAmount == 0) {
                itemType = null;
            }
            return bufferAmount > 0;
        }

        void update(AEKey itemType, long bufferAmount) {
            if (this.itemType != null && !itemType.fuzzyEquals(this.itemType, FuzzyMode.IGNORE_ALL)) {
                throw new IllegalArgumentException("itemType mismatch");
            }
            this.itemType = itemType;
            this.bufferAmount += bufferAmount;
        }

        @Nullable
        public AEKey getItemType() {
            return itemType;
        }

        public long getBufferAmount() {
            return itemType == null ? 0 : bufferAmount;
        }

        public long getKnownAmount() {
            return knownAmount;
        }
    }
}