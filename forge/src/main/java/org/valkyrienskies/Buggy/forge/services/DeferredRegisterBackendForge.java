package org.valkyrienskies.Buggy.forge.services;

import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import org.jetbrains.annotations.NotNull;
import org.valkyrienskies.Buggy.forge.DeferredRegisterImpl;
import org.valkyrienskies.buggy.registry.DeferredRegister;
import org.valkyrienskies.buggy.services.DeferredRegisterBackend;

public class DeferredRegisterBackendForge implements DeferredRegisterBackend {

    @NotNull
    @Override
    public <T> DeferredRegister<T> makeDeferredRegister(@NotNull String id, @NotNull ResourceKey<Registry<T>> registry) {
        return new DeferredRegisterImpl(id, registry);
    }
}
