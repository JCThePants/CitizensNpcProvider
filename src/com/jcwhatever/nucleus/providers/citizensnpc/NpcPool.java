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
import com.jcwhatever.nucleus.mixins.IDisposable;
import com.jcwhatever.nucleus.providers.citizensnpc.storage.DataNodeKey;
import com.jcwhatever.nucleus.providers.citizensnpc.storage.DataNodeNPCStore;
import com.jcwhatever.nucleus.storage.MemoryDataNode;
import com.jcwhatever.nucleus.utils.PreCon;

import org.bukkit.entity.EntityType;

import net.citizensnpcs.api.CitizensAPI;
import net.citizensnpcs.api.npc.NPC;
import net.citizensnpcs.api.npc.NPCRegistry;

import java.util.HashSet;
import java.util.LinkedList;
import java.util.Set;
import java.util.UUID;
import javax.annotation.Nullable;

/**
 * Pool of {@link Npc} used to reuse transient {@link Npc} instances
 * to reduce the amount of old generation objects created.
 */
public class NpcPool implements IDisposable {

    private static final String REGISTRY_NAME = "CitizensNpcProvider_NpcPool";

    // Used to create a unique ID across all provider registries to
    // overcome Citizens2 registry/ID conflict bug.
    // start at 1000 to reduce chance of conflict with NPC's created with Citizens.
    private static int _transientId = 1000;

    private final LinkedList<Npc> _pool = new LinkedList<Npc>();
    private final Set<Npc> _inUse = new HashSet<>(20);
    private final NPCRegistry _registry;
    private final int _id;

    private boolean _isDisposed;

    /**
     * Constructor.
     *
     * @param id  The pool ID.
     */
    public NpcPool(int id) {
        _id = id;
        DataNodeNPCStore dataStore = new DataNodeNPCStore(new MemoryDataNode(Nucleus.getPlugin()));
        _registry = CitizensAPI.createNamedNPCRegistry(REGISTRY_NAME + _id, dataStore);
    }

    /**
     * Create a new {@link Npc} or retrieve an existing one from the pool.
     *
     * @param lookupName  The lookup name.
     * @param npcName     The NPC's name.
     * @param id          The NPC's unique ID.
     * @param type        The {@link org.bukkit.entity.EntityType}.
     * @param registry    The NPC's owning registry.
     *
     * @return  The {@link Npc} instance.
     */
    public Npc createNpc(@Nullable String lookupName, String npcName,
                         UUID id, EntityType type, Registry registry) {
        PreCon.notNull(npcName);
        PreCon.notNull(id);
        PreCon.notNull(type);
        PreCon.notNull(registry);

        Npc npc;

        if (_pool.isEmpty()) {

            NPC handle = _registry.createNPC(type, id, nextId(), npcName);
            npc = new Npc(handle, this);
        }
        else {
            npc = _pool.remove();
        }

        _inUse.add(npc);

        DataNodeKey dataKey = registry.getDataStore().getStorage()
                .getKey(String.valueOf(npc.getHandle().getId()));

        if (lookupName == null)
            lookupName = "nolookup__" + npc.getHandle().getId();

        npc.init(lookupName, npcName, type, registry, dataKey);
        return npc;
    }

    /**
     * Recycle an {@link Npc} instance into the pool.
     *
     * @param npc  The {@link Npc} to recycle.
     */
    public void recycle(Npc npc) {
        _inUse.remove(npc);
        _pool.add(npc);
    }

    /*
     * Get a new NPC ID, unique across all registries.
     */
    private int nextId() {
        if (_transientId == Integer.MAX_VALUE)
            _transientId = 1000;

        return _transientId++;
    }

    @Override
    public boolean isDisposed() {
        return _isDisposed;
    }

    @Override
    public void dispose() {

        _isDisposed = true;

        for (Npc npc : _inUse) {
            npc.dispose();
        }

        while (!_pool.isEmpty()) {
            Npc npc = _pool.remove();

            if (!npc.isDisposed())
                npc.dispose();

            npc.getHandle().destroy();
        }

        _inUse.clear();

        CitizensAPI.removeNamedNPCRegistry(REGISTRY_NAME + _id);
    }
}
