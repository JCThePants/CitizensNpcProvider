/*
 * This file is part of CitizensNpcProvider for NucleusFramework, licensed under the MIT License (MIT).
 *
 * Copyright (c) JCThePants (www.jcwhatever.com)
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */

package com.jcwhatever.nucleus.providers.citizensnpc;

import com.jcwhatever.nucleus.Nucleus;
import com.jcwhatever.nucleus.managed.scheduler.Scheduler;
import com.jcwhatever.nucleus.providers.Provider;
import com.jcwhatever.nucleus.providers.citizensnpc.ai.AiRunner;
import com.jcwhatever.nucleus.providers.citizensnpc.navigator.CitizensNavigatorListener;
import com.jcwhatever.nucleus.providers.citizensnpc.traits.NpcTraitRegistry;
import com.jcwhatever.nucleus.providers.citizensnpc.traits.TraitRegistration;
import com.jcwhatever.nucleus.providers.citizensnpc.traits.citizens.replaced.TraitReplacer;
import com.jcwhatever.nucleus.providers.npc.INpcProvider;
import com.jcwhatever.nucleus.providers.npc.INpcRegistry;
import com.jcwhatever.nucleus.providers.npc.traits.INpcTraitTypeRegistry;
import com.jcwhatever.nucleus.providers.npc.traits.NpcTraitType;
import com.jcwhatever.nucleus.providers.npc.traits.NpcTraitType.NpcTraitRegistration;
import com.jcwhatever.nucleus.storage.MemoryDataNode;
import com.jcwhatever.nucleus.utils.PreCon;
import net.citizensnpcs.api.npc.NPC;
import org.bukkit.Bukkit;
import org.bukkit.entity.Entity;
import org.bukkit.plugin.Plugin;

import javax.annotation.Nullable;
import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;
import java.util.WeakHashMap;

/**
 * Provides Citizens based NPC support to NucleusFramework NPC api.
 */
public class CitizensProvider extends Provider implements INpcProvider {

    public static NpcTraitRegistration REGISTRATION = new TraitRegistration();
    private static CitizensProvider _instance;

    /**
     * Get the current instance of the provider.
     */
    public static CitizensProvider getInstance() {
        return _instance;
    }

    /**
     * Get the current spawned {@link Npc}'s.
     */
    public static Collection<Npc> getSpawned() {
        return new ArrayList<>(_instance._spawned.values());
    }

    private final Map<Entity, Npc> _spawned = new WeakHashMap<>(15);
    private final Map<NPC, Npc> _npcs = new WeakHashMap<>(15);
    private final NpcTraitRegistry _traits = new NpcTraitRegistry(null);

    private File _skinFolder;

    public CitizensProvider() {
        _instance = this;
    }

    @Override
    public INpcRegistry createRegistry(Plugin plugin, String name) {
        return new Registry(plugin, name, new MemoryDataNode(plugin));
    }

    @Override
    public boolean isNpc(Entity entity) {
        return entity.hasMetadata("NPC");
    }

    @Override
    public Npc getNpc(Entity entity) {
        return _spawned.get(entity);
    }

    @Override
    public INpcTraitTypeRegistry registerTrait(NpcTraitType traitType) {
        PreCon.notNull(traitType);

        _traits.registerTrait(traitType);

        return this;
    }

    @Override
    public boolean isTraitRegistered(String name) {
        return _traits.isTraitRegistered(name);
    }

    @Override
    public NpcTraitType getTraitType(String name) {
        return _traits.getTraitType(name);
    }

    public NpcTraitRegistry getTraitRegistry() {
        return _traits;
    }

    public void registerEntity(Npc npc, Entity entity) {
        PreCon.notNull(npc);
        PreCon.notNull(entity);

        _spawned.put(entity, npc);
    }

    public void unregisterEntity(Entity entity) {
        PreCon.notNull(entity);

        _spawned.remove(entity);
    }

    public void registerNpc(Npc npc) {
        _npcs.put(npc.getHandle(), npc);
    }

    public void unregisterNPC(Npc npc) {
        _npcs.remove(npc.getHandle());
    }

    @Nullable
    public Npc getNpc(NPC npc) {
        return _npcs.get(npc);
    }

    public File getSkinFolder() {
        return _skinFolder;
    }

    @Override
    protected void onEnable() {

        _skinFolder = new File(getDataFolder(), "skins");
        if (!_skinFolder.exists() && !_skinFolder.mkdirs()) {
            throw new RuntimeException("Failed to create Citizens Provider skins folder: "
                    + _skinFolder.getAbsolutePath());
        }

        Bukkit.getPluginManager().registerEvents(new CitizensNavigatorListener(), Nucleus.getPlugin());
        Bukkit.getPluginManager().registerEvents(new BukkitListener(), Nucleus.getPlugin());

        Scheduler.runTaskRepeat(Nucleus.getPlugin(), 1, 1, new AiRunner());

        try {
            TraitReplacer.replaceTraits();
        } catch (NoSuchFieldException | IllegalAccessException | NoSuchMethodException e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void onDisable() {
        _instance = null;
    }
}
