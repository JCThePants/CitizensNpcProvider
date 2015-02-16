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
import com.jcwhatever.nucleus.providers.citizensnpc.storage.DataNodeNPCStore;
import com.jcwhatever.nucleus.providers.citizensnpc.traits.NpcTraitRegistry;
import com.jcwhatever.nucleus.providers.npc.INpc;
import com.jcwhatever.nucleus.providers.npc.INpcRegistry;
import com.jcwhatever.nucleus.providers.npc.events.NpcClickEvent;
import com.jcwhatever.nucleus.providers.npc.events.NpcCreateEvent;
import com.jcwhatever.nucleus.providers.npc.events.NpcDamageByBlockEvent;
import com.jcwhatever.nucleus.providers.npc.events.NpcDamageByEntityEvent;
import com.jcwhatever.nucleus.providers.npc.events.NpcDamageEvent;
import com.jcwhatever.nucleus.providers.npc.events.NpcDeathEvent;
import com.jcwhatever.nucleus.providers.npc.events.NpcDespawnEvent;
import com.jcwhatever.nucleus.providers.npc.events.NpcLeftClickEvent;
import com.jcwhatever.nucleus.providers.npc.events.NpcRightClickEvent;
import com.jcwhatever.nucleus.providers.npc.events.NpcSpawnEvent;
import com.jcwhatever.nucleus.providers.npc.events.NpcTargetedEvent;
import com.jcwhatever.nucleus.providers.npc.traits.INpcTraitTypeRegistry;
import com.jcwhatever.nucleus.providers.npc.traits.NpcTraitType;
import com.jcwhatever.nucleus.storage.IDataNode;
import com.jcwhatever.nucleus.utils.PreCon;
import com.jcwhatever.nucleus.utils.observer.script.IScriptUpdateSubscriber;
import com.jcwhatever.nucleus.utils.observer.script.ScriptUpdateSubscriber;
import com.jcwhatever.nucleus.utils.observer.update.NamedUpdateAgents;

import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.plugin.Plugin;

import net.citizensnpcs.api.CitizensAPI;
import net.citizensnpcs.api.npc.NPC;
import net.citizensnpcs.api.npc.NPCRegistry;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.annotation.Nullable;

/**
 * Implementation of {@link com.jcwhatever.nucleus.providers.npc.INpcProvider}.
 */
public class Registry implements INpcRegistry {

    private final Plugin _plugin;
    private final String _name;
    private final String _searchName;
    private final NPCRegistry _registry;
    private final Map<String, INpc> _npcMap = new HashMap<>(10);
    private final NpcTraitRegistry _traits;
    private final NamedUpdateAgents _agents = new NamedUpdateAgents();
    private final DataNodeNPCStore _dataStore;

    private boolean _isDisposed;

    /**
     * Constructor.
     *
     * @param plugin    The registry's owning plugin.
     * @param name      The name of the registry.
     * @param dataNode  The registry's data node.
     */
    public Registry(Plugin plugin, String name, IDataNode dataNode) {
        PreCon.notNull(plugin);
        PreCon.notNullOrEmpty(name);
        PreCon.notNull(dataNode);

        _plugin = plugin;
        _name = name;
        _searchName = name.toLowerCase();
        _dataStore = new DataNodeNPCStore(dataNode);
        _traits = new NpcTraitRegistry(CitizensProvider.getInstance().getTraitRegistry());

        _registry = CitizensAPI.createNamedNPCRegistry(plugin.getName() + ':' + name, _dataStore);
    }

    /**
     * Get the registry data store.
     */
    public DataNodeNPCStore getDataStore() {
        return _dataStore;
    }

    @Override
    public Plugin getPlugin() {
        return _plugin;
    }

    @Override
    public String getName() {
        return _name;
    }

    @Override
    public String getSearchName() {
        return _searchName;
    }

    @Nullable
    @Override
    public INpc create(String lookupName, String npcName, EntityType type) {
        PreCon.notNullOrEmpty(lookupName);
        PreCon.notNull(npcName);
        PreCon.notNull(type);

        if (_npcMap.containsKey(lookupName.toLowerCase()))
            return null;

        NPC handle = _registry.createNPC(type, npcName);

        Npc npc = new Npc(this, lookupName, handle, type,
                _dataStore.getStorage().getKey(String.valueOf(handle.getId())));

        _npcMap.put(lookupName.toLowerCase(), npc);

        CitizensProvider.getInstance().registerNPC(npc);

        NpcCreateEvent event = new NpcCreateEvent(npc);
        Nucleus.getEventManager().callBukkit(this, event);

        return npc;
    }

    @Nullable
    @Override
    public INpc create(String lookupName, String npcName, String type) {
        PreCon.notNullOrEmpty(type);

        try {
            EntityType entityType = EntityType.valueOf(type.toUpperCase());
            return create(lookupName, npcName, entityType);
        }
        catch (Exception e) {
            return null;
        }
    }

    @Nullable
    @Override
    public INpc create(String npcName, EntityType type) {
        PreCon.notNull(npcName);
        PreCon.notNull(type);

        NPC handle = _registry.createNPC(type, npcName);

        String lookupName = "nolookup__" + handle.getId();

        Npc npc = new Npc(this, lookupName, handle, type,
                _dataStore.getStorage().getKey(String.valueOf(handle.getId())));

        CitizensProvider.getInstance().registerNPC(npc);

        NpcCreateEvent event = new NpcCreateEvent(npc);
        Nucleus.getEventManager().callBukkit(this, event);

        return npc;
    }

    @Nullable
    @Override
    public INpc create(String npcName, String type) {
        PreCon.notNullOrEmpty(type);

        try {
            EntityType entityType = EntityType.valueOf(type.toUpperCase());
            return create(npcName, entityType);
        } catch (Exception e) {
            return null;
        }
    }

    @Override
    public Collection<INpc> all() {
        return _npcMap.values();
    }

    @Nullable
    @Override
    public INpc get(String name) {
        PreCon.notNull(name);

        return _npcMap.get(name.toLowerCase());
    }

    @Nullable
    @Override
    public INpc get(Entity entity) {

        INpc npc = CitizensProvider.getInstance().getNpc(entity);
        if (npc == null || npc.getRegistry() != this)
            return null;

        return npc;
    }

    @Override
    public boolean isDisposed() {
        return _isDisposed;
    }

    @Override
    public void dispose() {

        List<INpc> list = new ArrayList<>(_npcMap.values());
        for (INpc npc : list) {
            npc.dispose();
        }
        _agents.disposeAgents();
        _isDisposed = true;
    }

    @Override
    public INpcTraitTypeRegistry registerTrait(NpcTraitType traitType) {
        return _traits.registerTrait(traitType);
    }

    @Override
    public boolean isTraitRegistered(String name) {
        return _traits.isTraitRegistered(name);
    }

    @Nullable
    @Override
    public NpcTraitType getTraitType(String name) {
        return _traits.getTraitType(name);
    }

    public void remove(Npc npc) {
        PreCon.notNull(npc);

        _npcMap.remove(npc.getSearchName());
        _registry.deregister(npc.getHandle());
        CitizensProvider.getInstance().unrregisterNPC(npc);
    }

    @Override
    public INpcRegistry onNavStart(IScriptUpdateSubscriber<INpc> subscriber) {
        PreCon.notNull(subscriber);

        _agents.getAgent("onNavStart").register(new ScriptUpdateSubscriber<>(subscriber));

        return this;
    }

    @Override
    public INpcRegistry onNavPause(IScriptUpdateSubscriber<INpc> subscriber) {
        PreCon.notNull(subscriber);

        _agents.getAgent("onNavPause").register(new ScriptUpdateSubscriber<>(subscriber));

        return this;
    }

    @Override
    public INpcRegistry onNavCancel(IScriptUpdateSubscriber<INpc> subscriber) {
        PreCon.notNull(subscriber);

        _agents.getAgent("onNavCancel").register(new ScriptUpdateSubscriber<>(subscriber));

        return this;
    }

    @Override
    public INpcRegistry onNavComplete(IScriptUpdateSubscriber<INpc> subscriber) {
        PreCon.notNull(subscriber);

        _agents.getAgent("onNavComplete").register(new ScriptUpdateSubscriber<>(subscriber));

        return this;
    }

    @Override
    public INpcRegistry onNavTimeout(IScriptUpdateSubscriber<INpc> subscriber) {
        PreCon.notNull(subscriber);

        _agents.getAgent("onNavTimeout").register(new ScriptUpdateSubscriber<>(subscriber));

        return this;
    }

    @Override
    public INpcRegistry onNpcSpawn(IScriptUpdateSubscriber<NpcSpawnEvent> subscriber) {
        PreCon.notNull(subscriber);

        _agents.getAgent("onNpcSpawn").register(new ScriptUpdateSubscriber<>(subscriber));

        return this;
    }

    @Override
    public INpcRegistry onNpcDespawn(IScriptUpdateSubscriber<NpcDespawnEvent> subscriber) {
        PreCon.notNull(subscriber);

        _agents.getAgent("onNpcDespawn").register(new ScriptUpdateSubscriber<>(subscriber));

        return this;
    }

    @Override
    public INpcRegistry onNpcClick(IScriptUpdateSubscriber<NpcClickEvent> subscriber) {
        PreCon.notNull(subscriber);

        _agents.getAgent("onNpcClick").register(new ScriptUpdateSubscriber<>(subscriber));

        return this;
    }

    @Override
    public INpcRegistry onNpcRightClick(IScriptUpdateSubscriber<NpcRightClickEvent> subscriber) {
        PreCon.notNull(subscriber);

        _agents.getAgent("onNpcRightClick").register(new ScriptUpdateSubscriber<>(subscriber));

        return this;
    }

    @Override
    public INpcRegistry onNpcLeftClick(IScriptUpdateSubscriber<NpcLeftClickEvent> subscriber) {
        PreCon.notNull(subscriber);

        _agents.getAgent("onNpcLeftClick").register(new ScriptUpdateSubscriber<>(subscriber));

        return this;
    }

    @Override
    public INpcRegistry onNpcEntityTarget(IScriptUpdateSubscriber<NpcTargetedEvent> subscriber) {
        PreCon.notNull(subscriber);

        _agents.getAgent("onNpcEntityTarget").register(new ScriptUpdateSubscriber<>(subscriber));

        return this;
    }

    @Override
    public INpcRegistry onNpcDamage(IScriptUpdateSubscriber<NpcDamageEvent> subscriber) {
        PreCon.notNull(subscriber);

        _agents.getAgent("onNpcDamage").register(new ScriptUpdateSubscriber<>(subscriber));

        return this;
    }

    @Override
    public INpcRegistry onNpcDamageByBlock(IScriptUpdateSubscriber<NpcDamageByBlockEvent> subscriber) {
        PreCon.notNull(subscriber);

        _agents.getAgent("onNpcDamageByBlock").register(new ScriptUpdateSubscriber<>(subscriber));

        return this;
    }

    @Override
    public INpcRegistry onNpcDamageByEntity(IScriptUpdateSubscriber<NpcDamageByEntityEvent> subscriber) {
        PreCon.notNull(subscriber);

        _agents.getAgent("onNpcDamageByEntity").register(new ScriptUpdateSubscriber<>(subscriber));

        return this;
    }

    @Override
    public INpcRegistry onNpcDeath(IScriptUpdateSubscriber<NpcDeathEvent> subscriber) {
        PreCon.notNull(subscriber);

        _agents.getAgent("onNpcDeath").register(new ScriptUpdateSubscriber<>(subscriber));

        return this;
    }


    public void onNavStart(INpc npc) {
        PreCon.notNull(npc);

        _agents.update("onNavStart", npc);
    }

    public void onNavPause(INpc npc) {
        PreCon.notNull(npc);

        _agents.update("onNavPause", npc);
    }

    public void onNavCancel(INpc npc) {
        PreCon.notNull(npc);

        _agents.update("onNavCancel", npc);
    }

    public void onNavComplete(INpc npc) {
        PreCon.notNull(npc);

        _agents.update("onNavComplete", npc);
    }

    public void onNavTimeout(INpc npc) {
        PreCon.notNull(npc);

        _agents.update("onNavTimeout", npc);
    }

    public void onNpcSpawn(NpcSpawnEvent event) {
        PreCon.notNull(event);

        _agents.update("onNpcSpawn", event);
    }

    public void onNpcDespawn(NpcDespawnEvent event) {
        PreCon.notNull(event);

        _agents.update("onNpcDespawn", event);
    }

    public void onNpcClick(NpcClickEvent event) {
        PreCon.notNull(event);

        _agents.update("onNpcClick", event);
    }

    public void onNpcRightClick(NpcRightClickEvent event) {
        PreCon.notNull(event);

        _agents.update("onNpcRightClick", event);
    }

    public void onNpcLeftClick(NpcLeftClickEvent event) {
        PreCon.notNull(event);

        _agents.update("onNpcLeftClick", event);
    }

    public void onNpcEntityTarget(NpcTargetedEvent event) {
        PreCon.notNull(event);

        _agents.update("onNpcEntityTarget", event);
    }

    public void onNpcDamage(NpcDamageEvent event) {
        PreCon.notNull(event);

        _agents.update("onNpcDamage", event);
    }

    public void onNpcDamageByBlock(NpcDamageByBlockEvent event) {
        PreCon.notNull(event);

        _agents.update("onNpcDamageByBlock", event);
    }

    public void onNpcDamageByEntity(NpcDamageByEntityEvent event) {
        PreCon.notNull(event);

        _agents.update("onNpcDamageByEntity", event);
    }

    public void onNpcDeath(NpcDeathEvent event) {
        PreCon.notNull(event);

        _agents.update("onNpcDeath", event);
    }
}
