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
import com.jcwhatever.nucleus.providers.citizensnpc.navigator.CitizensNavigatorListener;
import com.jcwhatever.nucleus.providers.citizensnpc.traits.NpcTraitRegistry;
import com.jcwhatever.nucleus.providers.npc.INpc;
import com.jcwhatever.nucleus.providers.npc.INpcProvider;
import com.jcwhatever.nucleus.providers.npc.INpcRegistry;
import com.jcwhatever.nucleus.providers.npc.traits.NpcTraitType;
import com.jcwhatever.nucleus.providers.npc.traits.INpcTraitTypeRegistry;
import com.jcwhatever.nucleus.storage.IDataNode;
import com.jcwhatever.nucleus.storage.MemoryDataNode;
import com.jcwhatever.nucleus.utils.PreCon;

import org.bukkit.Bukkit;
import org.bukkit.entity.Entity;
import org.bukkit.plugin.Plugin;

import net.citizensnpcs.api.npc.NPC;

import java.util.Map;
import java.util.WeakHashMap;
import javax.annotation.Nullable;

/*
 * 
 */
public class CitizensProvider implements INpcProvider {

    private static CitizensProvider _instance;

    public static CitizensProvider getInstance() {
        return _instance;
    }

    private final Map<Entity, INpc> _spawned = new WeakHashMap<>(15);
    private final Map<NPC, Npc> _npcs = new WeakHashMap<>(15);
    private final NpcTraitRegistry _traits = new NpcTraitRegistry(null);

    @Override
    public String getName() {
        return "CitizensProvider";
    }

    @Override
    public String getVersion() {
        return "v0.1-beta";
    }

    @Override
    public int getLogicalVersion() {
        return 0;
    }

    @Override
    public void onRegister() {
        _instance = this;
    }

    @Override
    public void onEnable() {
        Bukkit.getPluginManager().registerEvents(new CitizensNavigatorListener(), Nucleus.getPlugin());
        Bukkit.getPluginManager().registerEvents(new CitizensNpcListener(), Nucleus.getPlugin());
    }

    @Override
    public void onDisable() {
        _instance = null;
    }

    @Override
    public INpcRegistry createRegistry(Plugin plugin, String name) {
        return new Registry(plugin, name, new MemoryDataNode(plugin));
    }

    @Override
    public INpcRegistry createRegistry(Plugin plugin, String name, IDataNode dataNode) {
        return new Registry(plugin, name, dataNode);
    }

    @Override
    public boolean isNpc(Entity entity) {
        return entity.hasMetadata("NPC");
    }

    @Override
    public INpc getNpc(Entity entity) {
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

    public void registerNPC(Npc npc) {
        _npcs.put(npc.getHandle(), npc);
    }

    public void unrregisterNPC(Npc npc) {
        _npcs.remove(npc.getHandle());
    }

    @Nullable
    public Npc getNpc(NPC npc) {
        return _npcs.get(npc);
    }
}
