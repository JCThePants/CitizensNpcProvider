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

import com.jcwhatever.nucleus.providers.citizensnpc.ai.NpcGoals;
import com.jcwhatever.nucleus.providers.citizensnpc.navigator.NpcNavigator;
import com.jcwhatever.nucleus.providers.citizensnpc.storage.DataNodeKey;
import com.jcwhatever.nucleus.providers.citizensnpc.traits.NpcTraits;
import com.jcwhatever.nucleus.providers.npc.INpc;
import com.jcwhatever.nucleus.providers.npc.events.NpcClickEvent;
import com.jcwhatever.nucleus.providers.npc.events.NpcDamageByBlockEvent;
import com.jcwhatever.nucleus.providers.npc.events.NpcDamageByEntityEvent;
import com.jcwhatever.nucleus.providers.npc.events.NpcDamageEvent;
import com.jcwhatever.nucleus.providers.npc.events.NpcDeathEvent;
import com.jcwhatever.nucleus.providers.npc.events.NpcDespawnEvent;
import com.jcwhatever.nucleus.providers.npc.events.NpcLeftClickEvent;
import com.jcwhatever.nucleus.providers.npc.events.NpcRightClickEvent;
import com.jcwhatever.nucleus.providers.npc.events.NpcSpawnEvent;
import com.jcwhatever.nucleus.providers.npc.events.NpcTargetedEvent;
import com.jcwhatever.nucleus.storage.IDataNode;
import com.jcwhatever.nucleus.utils.PreCon;
import com.jcwhatever.nucleus.utils.observer.script.IScriptUpdateSubscriber;
import net.citizensnpcs.api.npc.NPC;
import org.bukkit.Location;
import org.bukkit.entity.Entity;

import javax.annotation.Nullable;

/**
 * Public wrapper to prevent accessing re-purposed NPC instances.
 */
public class NpcWrapper implements INpc {

    private final int _hash;
    private final String _npcId;

    private Npc _npc;
    private String _display;

    /**
     * Constructor.
     *
     * @param npc   The Npc to wrap.
     */
    public NpcWrapper(@Nullable Npc npc) {
        _npc = npc == null ? null : npc.isDisposed() ? null : npc;

        if (npc != null) {
            _hash = npc.hashCode();
            _npcId = npc.getLookupName();
            _display = npc.getDisplayName();
        }
        else {
            _hash = 0;
            _npcId = null;
        }
    }

    /**
     * Get the NPC data storage.
     */
    public DataNodeKey getDataKey() {
        if (_npc == null)
            return null;

        return _npc.getDataKey();
    }

    /**
     * Get the NPC data node storage.
     */
    public IDataNode getDataNode() {
        checkDisposed();
        return _npc.getDataNode();
    }

    @Override
    public Registry getRegistry() {
        checkDisposed();
        return _npc.getRegistry();
    }

    @Override
    public String getLookupName() {
        return _npcId;
    }

    @Override
    public String getDisplayName() {
        if (_npc == null) {
            return _display;
        }
        return _display = _npc.getDisplayName();
    }

    @Override
    public INpc setDisplayName(String name) {
        PreCon.notNull(name);

        if (_npc != null) {
            _npc.setDisplayName(name);
            _display = name;
        }
        return this;
    }

    @Nullable
    @Override
    public Entity getEntity() {
        if (_npc == null)
            return null;

        return _npc.getEntity();
    }

    @Override
    public boolean isSpawned() {
        return _npc != null && _npc.isSpawned();
    }

    @Override
    public boolean spawn(Location location) {
        return _npc != null && _npc.spawn(location);
    }

    @Override
    public boolean despawn() {
        return _npc != null && _npc.despawn();
    }

    @Nullable
    @Override
    public Location getLocation() {
        if (_npc == null)
            return null;

        return _npc.getLocation();
    }

    @Nullable
    @Override
    public Location getLocation(Location location) {
        if (_npc == null)
            return null;

        return _npc.getLocation(location);
    }

    @Override
    public NpcNavigator getNavigator() {
        checkDisposed();
        return _npc.getNavigator();
    }

    @Override
    public NpcGoals getGoals() {
        checkDisposed();
        return _npc.getGoals();
    }

    @Override
    public NpcTraits getTraits() {
        checkDisposed();
        return _npc.getTraits();
    }

    @Override
    @Nullable
    public Object getMeta(String key) {
        checkDisposed();
        return _npc.getMeta(key);
    }

    @Override
    public void setMeta(String key, @Nullable Object value) {
        checkDisposed();
        _npc.setMeta(key, value);
    }

    @Nullable
    @Override
    public INpc getNPCVehicle() {
        checkDisposed();
        return _npc.getNPCVehicle();
    }

    @Nullable
    @Override
    public INpc getNPCPassenger() {
        checkDisposed();
        return _npc.getNPCPassenger();
    }

    @Override
    public INpc mountNPC(INpc vehicle) {
        checkDisposed();
        _npc.mountNPC(vehicle);
        return this;
    }

    @Override
    public INpc look(float yaw, float pitch) {
        checkDisposed();
        _npc.look(yaw, pitch);
        return this;
    }

    @Override
    public INpc lookEntity(Entity entity) {
        checkDisposed();
        _npc.lookEntity(entity);
        return this;
    }

    @Override
    public INpc lookLocation(Location location) {
        checkDisposed();
        _npc.lookLocation(location);
        return this;
    }

    @Override
    public boolean save(IDataNode dataNode) {
        checkDisposed();
        return _npc.save(dataNode);
    }

    @Override
    public boolean isDisposed() {
        return _npc == null;
    }

    @Override
    public void dispose() {
        if (_npc == null)
            return;

        _npc.dispose();
        _npc = null;
    }

    public NPC getHandle() {
        checkDisposed();
        return _npc.getHandle();
    }

    @Override
    public INpc onNpcSpawn(IScriptUpdateSubscriber<NpcSpawnEvent> subscriber) {
        checkDisposed();
        _npc.onNpcSpawn(subscriber);
        return this;
    }

    @Override
    public INpc onNpcDespawn(IScriptUpdateSubscriber<NpcDespawnEvent> subscriber) {
        checkDisposed();
        _npc.onNpcDespawn(subscriber);
        return this;
    }

    @Override
    public INpc onNpcClick(IScriptUpdateSubscriber<NpcClickEvent> subscriber) {
        checkDisposed();
        _npc.onNpcClick(subscriber);
        return this;
    }

    @Override
    public INpc onNpcRightClick(IScriptUpdateSubscriber<NpcRightClickEvent> subscriber) {
        checkDisposed();
        _npc.onNpcRightClick(subscriber);
        return this;
    }

    @Override
    public INpc onNpcLeftClick(IScriptUpdateSubscriber<NpcLeftClickEvent> subscriber) {
        checkDisposed();
        _npc.onNpcLeftClick(subscriber);
        return this;
    }

    @Override
    public INpc onNpcEntityTarget(IScriptUpdateSubscriber<NpcTargetedEvent> subscriber) {
        checkDisposed();
        _npc.onNpcEntityTarget(subscriber);
        return this;
    }

    @Override
    public INpc onNpcDamage(IScriptUpdateSubscriber<NpcDamageEvent> subscriber) {
        checkDisposed();
        _npc.onNpcDamage(subscriber);
        return this;
    }

    @Override
    public INpc onNpcDamageByBlock(IScriptUpdateSubscriber<NpcDamageByBlockEvent> subscriber) {
        checkDisposed();
        _npc.onNpcDamageByBlock(subscriber);
        return this;
    }

    @Override
    public INpc onNpcDamageByEntity(IScriptUpdateSubscriber<NpcDamageByEntityEvent> subscriber) {
        checkDisposed();
        _npc.onNpcDamageByEntity(subscriber);
        return this;
    }

    @Override
    public INpc onNpcDeath(IScriptUpdateSubscriber<NpcDeathEvent> subscriber) {
        checkDisposed();
        _npc.onNpcDeath(subscriber);
        return this;
    }

    @Override
    public int hashCode() {
        return _hash;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof NpcWrapper) {
            NpcWrapper other = (NpcWrapper)obj;
            return _hash == other._hash
                    && ((_npcId != null && _npcId.equals(other._npcId))
                    || (_npcId == null && other._npcId == null));
        }
        else if (obj instanceof INpc) {
            INpc npc = (INpc)obj;
            return _hash == npc.hashCode()
                    && npc.getLookupName().equals(_npcId);
        }
        return false;
    }

    private void checkDisposed() {
        if (_npc == null)
            throw new IllegalStateException("Cannot use a disposed Npc.");
    }
}
