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
import com.jcwhatever.nucleus.internal.NucMsg;
import com.jcwhatever.nucleus.providers.citizensnpc.storage.DataNodeNPCStore;
import com.jcwhatever.nucleus.providers.citizensnpc.traits.NpcTraitRegistry;
import com.jcwhatever.nucleus.providers.citizensnpc.traits.TraitPool;
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

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Implementation of {@link com.jcwhatever.nucleus.providers.npc.INpcProvider}.
 */
public class Registry implements INpcRegistry {

    private static int _registryId = 0;

    private final Plugin _plugin;
    private final String _name;
    private final String _searchName;
    private final Map<String, Npc> _npcMap = new HashMap<>(10);
    private final Map<String, INpc> _wrappedMap = new HashMap<>(10);
    private final NpcTraitRegistry _traits;
    private final NamedUpdateAgents _agents = new NamedUpdateAgents();
    private final DataNodeNPCStore _dataStore;
    private final NpcPool _npcPool;
    private final TraitPool _traitPool = new TraitPool();

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

        _npcPool = new NpcPool(nextId());

        _plugin = plugin;
        _name = name;
        _searchName = name.toLowerCase();
        _dataStore = new DataNodeNPCStore(dataNode);
        _traits = new NpcTraitRegistry(CitizensProvider.getInstance().getTraitRegistry());
    }

    /**
     * Get the registry data store.
     */
    public DataNodeNPCStore getDataStore() {
        return _dataStore;
    }

    public TraitPool getTraitPool() {
        return _traitPool;
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
    public INpc create(@Nullable String lookupName, String npcName, EntityType type) {
        PreCon.notNull(npcName);
        PreCon.notNull(type);

        checkDisposed();

        if (lookupName != null && _npcMap.containsKey(lookupName.toLowerCase())) {
            NucMsg.debug("Attempted to create an NPC with a lookup name that already exists: {0}", lookupName);
            return null;
        }

        // create npc from pool
        Npc npc = _npcPool.createNpc(lookupName, npcName, UUID.randomUUID(), type, this);
        NpcWrapper wrapper = new NpcWrapper(npc);

        // store npc in registry
        _npcMap.put(npc.getLookupName(), npc);
        _wrappedMap.put(npc.getLookupName(), wrapper);

        // call create event
        NpcCreateEvent event = new NpcCreateEvent(wrapper);
        Nucleus.getEventManager().callBukkit(this, event);

        return wrapper;
    }

    @Nullable
    @Override
    public INpc create(@Nullable String lookupName, String npcName, String type) {
        PreCon.notNull(npcName);
        PreCon.notNullOrEmpty(type);

        checkDisposed();

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
        return create(null, npcName, type);
    }

    @Nullable
    @Override
    public INpc create(String npcName, String type) {
        PreCon.notNull(npcName);
        PreCon.notNullOrEmpty(type);

        checkDisposed();

        try {
            EntityType entityType = EntityType.valueOf(type.toUpperCase());
            return create(null, npcName, entityType);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    @Nullable
    @Override
    public INpc load(IDataNode dataNode) {
        PreCon.notNull(dataNode);

        String lookupName = dataNode.getString("lookup");
        String name = dataNode.getString("name");
        UUID id = dataNode.getUUID("uuid");
        EntityType type = dataNode.getEnum("type", EntityType.class);

        if (lookupName == null || name == null || id == null || type == null) {
            Msg.debug("Failed to load Npc from data node.");
            return null;
        }

        INpc current = _wrappedMap.get(lookupName);
        if (current != null) {
            Msg.debug("Failed to load Npc because it's already loaded.");
            return current;
        }

        Npc npc = _npcPool.createNpc(lookupName, name, id, type, this);
        if (npc != null) {
            npc.getTraits().load(dataNode.getNode("traits"));

            NpcWrapper wrapper = new NpcWrapper(npc);
            _npcMap.put(npc.getLookupName(), npc);
            _wrappedMap.put(npc.getLookupName(), wrapper);
            return wrapper;
        }

        return null;
    }

    @Override
    public boolean loadAll(IDataNode dataNode) {
        PreCon.notNull(dataNode);

        for (IDataNode npcNode : dataNode) {
            load(npcNode);
        }

        return true;
    }

    @Override
    public boolean saveAll(IDataNode dataNode) {
        PreCon.notNull(dataNode);

        Collection<Npc> npcs = _npcMap.values();

        for (INpc npc : npcs) {
            npc.save(dataNode.getNode(npc.getLookupName()));
        }

        dataNode.save();

        return true;
    }

    @Override
    public Collection<INpc> all() {
        return Collections.unmodifiableCollection(_wrappedMap.values());
    }

    @Nullable
    @Override
    public INpc get(String name) {
        PreCon.notNull(name);

        return _wrappedMap.get(name.toLowerCase());
    }

    @Nullable
    @Override
    public INpc get(Entity entity) {

        INpc npc = CitizensProvider.getInstance().getNpc(entity);
        if (npc == null || npc.getRegistry() != this)
            return null;

        return _wrappedMap.get(npc.getLookupName().toLowerCase());
    }

    @Override
    public boolean isDisposed() {
        return _isDisposed;
    }

    @Override
    public void dispose() {

        if (_isDisposed)
            return;

        _isDisposed = true;

        List<INpc> list = new ArrayList<>(_wrappedMap.values());
        for (INpc npc : list) {
            npc.dispose();
        }

        _agents.disposeAgents();
        _dataStore.getStorage().getDataNode().clear();
        _traits.dispose();

        _npcPool.dispose();
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

    @Override
    public INpcRegistry onNavStart(IScriptUpdateSubscriber<INpc> subscriber) {
        PreCon.notNull(subscriber);

        checkDisposed();

        _agents.getAgent("onNavStart").addSubscriber(new ScriptUpdateSubscriber<>(subscriber));

        return this;
    }

    @Override
    public INpcRegistry onNavPause(IScriptUpdateSubscriber<INpc> subscriber) {
        PreCon.notNull(subscriber);

        checkDisposed();

        _agents.getAgent("onNavPause").addSubscriber(new ScriptUpdateSubscriber<>(subscriber));

        return this;
    }

    @Override
    public INpcRegistry onNavCancel(IScriptUpdateSubscriber<INpc> subscriber) {
        PreCon.notNull(subscriber);

        checkDisposed();

        _agents.getAgent("onNavCancel").addSubscriber(new ScriptUpdateSubscriber<>(subscriber));

        return this;
    }

    @Override
    public INpcRegistry onNavComplete(IScriptUpdateSubscriber<INpc> subscriber) {
        PreCon.notNull(subscriber);

        checkDisposed();

        _agents.getAgent("onNavComplete").addSubscriber(new ScriptUpdateSubscriber<>(subscriber));

        return this;
    }

    @Override
    public INpcRegistry onNavTimeout(IScriptUpdateSubscriber<INpc> subscriber) {
        PreCon.notNull(subscriber);

        checkDisposed();

        _agents.getAgent("onNavTimeout").addSubscriber(new ScriptUpdateSubscriber<>(subscriber));

        return this;
    }

    @Override
    public INpcRegistry onNpcSpawn(IScriptUpdateSubscriber<NpcSpawnEvent> subscriber) {
        PreCon.notNull(subscriber);

        checkDisposed();

        _agents.getAgent("onNpcSpawn").addSubscriber(new ScriptUpdateSubscriber<>(subscriber));

        return this;
    }

    @Override
    public INpcRegistry onNpcDespawn(IScriptUpdateSubscriber<NpcDespawnEvent> subscriber) {
        PreCon.notNull(subscriber);

        checkDisposed();

        _agents.getAgent("onNpcDespawn").addSubscriber(new ScriptUpdateSubscriber<>(subscriber));

        return this;
    }

    @Override
    public INpcRegistry onNpcClick(IScriptUpdateSubscriber<NpcClickEvent> subscriber) {
        PreCon.notNull(subscriber);

        checkDisposed();

        _agents.getAgent("onNpcClick").addSubscriber(new ScriptUpdateSubscriber<>(subscriber));

        return this;
    }

    @Override
    public INpcRegistry onNpcRightClick(IScriptUpdateSubscriber<NpcRightClickEvent> subscriber) {
        PreCon.notNull(subscriber);

        checkDisposed();

        _agents.getAgent("onNpcRightClick").addSubscriber(new ScriptUpdateSubscriber<>(subscriber));

        return this;
    }

    @Override
    public INpcRegistry onNpcLeftClick(IScriptUpdateSubscriber<NpcLeftClickEvent> subscriber) {
        PreCon.notNull(subscriber);

        checkDisposed();

        _agents.getAgent("onNpcLeftClick").addSubscriber(new ScriptUpdateSubscriber<>(subscriber));

        return this;
    }

    @Override
    public INpcRegistry onNpcEntityTarget(IScriptUpdateSubscriber<NpcTargetedEvent> subscriber) {
        PreCon.notNull(subscriber);

        checkDisposed();

        _agents.getAgent("onNpcEntityTarget").addSubscriber(new ScriptUpdateSubscriber<>(subscriber));

        return this;
    }

    @Override
    public INpcRegistry onNpcDamage(IScriptUpdateSubscriber<NpcDamageEvent> subscriber) {
        PreCon.notNull(subscriber);

        checkDisposed();

        _agents.getAgent("onNpcDamage").addSubscriber(new ScriptUpdateSubscriber<>(subscriber));

        return this;
    }

    @Override
    public INpcRegistry onNpcDamageByBlock(IScriptUpdateSubscriber<NpcDamageByBlockEvent> subscriber) {
        PreCon.notNull(subscriber);

        checkDisposed();

        _agents.getAgent("onNpcDamageByBlock").addSubscriber(new ScriptUpdateSubscriber<>(subscriber));

        return this;
    }

    @Override
    public INpcRegistry onNpcDamageByEntity(IScriptUpdateSubscriber<NpcDamageByEntityEvent> subscriber) {
        PreCon.notNull(subscriber);

        checkDisposed();

        _agents.getAgent("onNpcDamageByEntity").addSubscriber(new ScriptUpdateSubscriber<>(subscriber));

        return this;
    }

    @Override
    public INpcRegistry onNpcDeath(IScriptUpdateSubscriber<NpcDeathEvent> subscriber) {
        PreCon.notNull(subscriber);

        checkDisposed();

        _agents.getAgent("onNpcDeath").addSubscriber(new ScriptUpdateSubscriber<>(subscriber));

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

    // invoked from Npc#dispose
    void remove(Npc npc) {
        PreCon.notNull(npc);
        _npcMap.remove(npc.getLookupName());
        _wrappedMap.remove(npc.getLookupName());
    }

    private int nextId() {
        if (_registryId == Integer.MAX_VALUE)
            _registryId = 0;

        return _registryId++;
    }

    private void checkDisposed() {
        if (_isDisposed)
            throw new IllegalStateException("Cannot use a disposed Registry.");
    }
}
