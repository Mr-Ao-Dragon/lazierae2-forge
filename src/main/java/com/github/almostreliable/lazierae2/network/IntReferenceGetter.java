package com.github.almostreliable.lazierae2.network;

import net.minecraft.util.IntReferenceHolder;

import java.util.function.IntSupplier;

public class IntReferenceGetter extends IntReferenceHolder {

    private final IntSupplier supplier;

    public IntReferenceGetter(IntSupplier supplier) {
        this.supplier = supplier;
    }

    @Override
    public int get() {
        return supplier.getAsInt();
    }

    @Override
    public void set(int pValue) {
        // do nothing
    }
}