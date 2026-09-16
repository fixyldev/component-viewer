package dev.fixyl.componentviewer.control.component;

import java.util.Comparator;
import java.util.List;
import java.util.function.Supplier;

import net.minecraft.core.component.DataComponentPatch;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.component.Removed;
import net.minecraft.world.item.ItemStack;

import dev.fixyl.componentviewer.annotation.NullPermitted;

final class PatchedItemStackComponents extends ItemStackComponents {

    private final Supplier<DataComponentPatch> dataComponentPatch;

    PatchedItemStackComponents(ItemStack itemStack, Supplier<DataComponentPatch> dataComponentPatch, ComponentContext componentContext) {
        super(itemStack, componentContext);

        this.dataComponentPatch = dataComponentPatch;
    }

    @Override
    public int size() {
        return this.dataComponentPatch.get().size();
    }

    @Override
    public boolean isEmpty() {
        return this.dataComponentPatch.get().isEmpty();
    }

    @Override
    public List<DataComponentType<?>> getComponentTypes() {
        DataComponentPatch currentPatch = this.dataComponentPatch.get();

        return currentPatch.map.keySet().stream()
            .sorted(REGISTRY_ID_COMPARATOR)
            .sorted(Comparator.<DataComponentType<?>, Boolean>comparing(dataComponentType ->
                PatchedItemStackComponents.wasRemovedWithPatch(currentPatch, dataComponentType)
            ))
            .toList();
    }

    @Override
    public <T> @NullPermitted T getValue(DataComponentType<T> dataComponentType) {
        DataComponentPatch patch = this.dataComponentPatch.get();

        if (PatchedItemStackComponents.wasRemovedWithPatch(patch, dataComponentType)) {
            return this.itemStack.getPrototype().get(dataComponentType);
        }

        return PatchedItemStackComponents.getValueFromPatch(patch, dataComponentType);
    }

    @Override
    public <T> boolean wasRemoved(DataComponentType<T> dataComponentType) {
        return PatchedItemStackComponents.wasRemovedWithPatch(
            this.dataComponentPatch.get(),
            dataComponentType
        );
    }

    // Suppress warning for unchecked type cast.
    // We know that the map only contains matching types.
    // It just doesn't expose this because it's heterogeneous,
    // meaning each key-value-pair has a different type.
    @SuppressWarnings("unchecked")
    private static <T> @NullPermitted T getValueFromPatch(DataComponentPatch dataComponentPatch, DataComponentType<T> dataComponentType) {
        Object value = dataComponentPatch.map.get(dataComponentType);

        return (T) Removed.removedToNull(value);
    }

    private static <T> boolean wasRemovedWithPatch(DataComponentPatch dataComponentPatch, DataComponentType<T> dataComponentType) {
        Object value = dataComponentPatch.map.get(dataComponentType);

        return Removed.isRemoved(value);
    }

}
