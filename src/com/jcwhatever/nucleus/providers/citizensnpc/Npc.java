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
import com.jcwhatever.nucleus.providers.citizensnpc.goals.NpcGoals;
import com.jcwhatever.nucleus.providers.citizensnpc.navigator.NpcNavigator;
import com.jcwhatever.nucleus.providers.citizensnpc.storage.DataNodeKey;
import com.jcwhatever.nucleus.providers.citizensnpc.traits.NpcTraits;
import com.jcwhatever.nucleus.providers.npc.INpc;
import com.jcwhatever.nucleus.providers.npc.INpcRegistry;
import com.jcwhatever.nucleus.providers.npc.events.NpcClickEvent;
import com.jcwhatever.nucleus.providers.npc.events.NpcDamageByBlockEvent;
import com.jcwhatever.nucleus.providers.npc.events.NpcDamageByEntityEvent;
import com.jcwhatever.nucleus.providers.npc.events.NpcDamageEvent;
import com.jcwhatever.nucleus.providers.npc.events.NpcDeathEvent;
import com.jcwhatever.nucleus.providers.npc.events.NpcDespawnEvent;
import com.jcwhatever.nucleus.providers.npc.events.NpcDisposeEvent;
import com.jcwhatever.nucleus.providers.npc.events.NpcLeftClickEvent;
import com.jcwhatever.nucleus.providers.npc.events.NpcRightClickEvent;
import com.jcwhatever.nucleus.providers.npc.events.NpcSpawnEvent;
import com.jcwhatever.nucleus.providers.npc.events.NpcTargetedEvent;
import com.jcwhatever.nucleus.providers.npc.goals.INpcGoals;
import com.jcwhatever.nucleus.providers.npc.navigator.INpcNav;
import com.jcwhatever.nucleus.providers.npc.traits.INpcTraits;
import com.jcwhatever.nucleus.utils.MetaKey;
import com.jcwhatever.nucleus.utils.MetaStore;
import com.jcwhatever.nucleus.utils.PreCon;
import com.jcwhatever.nucleus.utils.observer.script.IScriptUpdateSubscriber;
import com.jcwhatever.nucleus.utils.observer.script.ScriptUpdateSubscriber;
import com.jcwhatever.nucleus.utils.observer.update.NamedUpdateAgents;

import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;

import net.citizensnpcs.api.npc.NPC;
import net.citizensnpcs.util.NMS;

import javax.annotation.Nullable;

/*
 * 
 */
public class Npc implements INpc {

    private final String _name;
    private final String _searchName;
    private final Registry _registry;
    private final NPC _npc;
    private final NpcNavigator _navigator;
    private final NpcGoals _goals;
    private final NpcTraits _traits;
    private final MetaStore _meta = new MetaStore();
    private final NamedUpdateAgents _agents = new NamedUpdateAgents();
    private final DataNodeKey _dataKey;
    private boolean _isDisposed;


    public Npc(Registry registry, String name, NPC npc, EntityType type, DataNodeKey dataKey) {
        PreCon.notNull(registry);
        PreCon.notNull(name);
        PreCon.notNull(npc);
        PreCon.notNull(type);
        PreCon.notNull(dataKey);

        _name = name;
        _searchName = name.toLowerCase();
        _registry = registry;
        _npc = npc;
        _dataKey = dataKey;
        _navigator = new NpcNavigator(this, _registry, npc.getNavigator());
        _goals = new NpcGoals(this);
        _traits = new NpcTraits(this, type);
    }

    public DataNodeKey getDataKey() {
        return _dataKey;
    }

    @Override
    public String getName() {
        return _name;
    }

    @Override
    public String getSearchName() {
        return _searchName;
    }

    @Override
    public INpcRegistry getRegistry() {
        return _registry;
    }

    @Override
    public String getNPCName() {
        return _npc.getName();
    }

    @Nullable
    @Override
    public Entity getEntity() {
        if (!_npc.isSpawned())
            return null;

        return _npc.getEntity();
    }

    @Override
    public boolean isSpawned() {
        return _npc.isSpawned();
    }

    @Override
    public INpc spawn(Location location) {
        PreCon.notNull(location);

        if (_npc.spawn(location)) {
            CitizensProvider.getInstance().registerEntity(this, _npc.getEntity());
            _traits.applyEquipment();
        }

        return this;
    }

    @Override
    public boolean despawn() {

        if (!_npc.isSpawned())
            return false;

        Entity entity = _npc.getEntity();

        if (_npc.despawn()) {
            CitizensProvider.getInstance().unregisterEntity(entity);
            return true;
        }
        return false;
    }

    @Nullable
    @Override
    public Location getLocation() {
        return _npc.getStoredLocation();
    }

    @Override
    public Location getLocation(Location location) {
        PreCon.notNull(location);

        Location stored = _npc.getStoredLocation();

        location.setWorld(stored.getWorld());
        location.setX(stored.getX());
        location.setY(stored.getY());
        location.setZ(stored.getZ());
        location.setYaw(stored.getYaw());
        location.setPitch(stored.getPitch());

        return location;
    }

    @Override
    public INpcNav getNavigator() {
        return _navigator;
    }

    @Override
    public INpcGoals getGoals() {
        return _goals;
    }

    @Override
    public INpcTraits getTraits() {
        return _traits;
    }

    @Nullable
    @Override
    public INpc getNPCVehicle() {
        if (!_npc.isSpawned())
            return null;

        Entity entity = _npc.getEntity();

        Entity vehicle = entity.getVehicle();
        if (vehicle == null)
            return null;

        return CitizensProvider.getInstance().getNpc(vehicle);
    }

    @Nullable
    @Override
    public INpc getNPCPassenger() {
        if (!_npc.isSpawned())
            return null;

        Entity entity = _npc.getEntity();

        Entity passenger = entity.getPassenger();
        if (passenger == null)
            return null;

        return CitizensProvider.getInstance().getNpc(passenger);
    }

    @Override
    public INpc mountNPC(INpc vehicle) {

        if (!_npc.isSpawned())
            return this;

        if (!vehicle.isSpawned())
            return this;


        Entity vehicleEntity = vehicle.getEntity();
        assert vehicleEntity != null;

        vehicleEntity.setPassenger(_npc.getEntity());

        return this;
    }

    @Override
    public INpc look(float yaw, float pitch) {

        if (!_npc.isSpawned())
            return this;

        NMS.look(_npc.getEntity(), yaw, pitch);

        return this;
    }

    @Override
    public INpc lookAt(Entity entity) {
        PreCon.notNull(entity);

        if (!_npc.isSpawned())
            return this;

        Location location = entity instanceof LivingEntity
                ? ((LivingEntity) entity).getEyeLocation()
                : entity.getLocation();

        _npc.faceLocation(location);

        return this;
    }

    @Override
    public INpc lookTowards(Location location) {
        PreCon.notNull(location);

        if (!_npc.isSpawned())
            return this;

        _npc.faceLocation(location);

        return this;
    }

    @Override
    public boolean isDisposed() {
        return _isDisposed;
    }

    @Override
    public void dispose() {
        if (_isDisposed)
            return;

        NpcDisposeEvent event = new NpcDisposeEvent(this);
        Nucleus.getEventManager().callBukkit(this, event);

        _registry.remove(this);
        _agents.disposeAgents();
        _isDisposed = true;
    }

    @Nullable
    @Override
    public <T> T getMeta(MetaKey<T> key) {
        return _meta.getMeta(key);
    }

    @Nullable
    @Override
    public Object getMetaObject(Object key) {
        return _meta.getMetaObject(key);
    }

    @Override
    public <T> void setMeta(MetaKey<T> key, @Nullable T value) {
        _meta.setMeta(key, value);
    }

    public NPC getHandle() {
        return _npc;
    }

    @Override
    public INpc onNpcSpawn(IScriptUpdateSubscriber<NpcSpawnEvent> subscriber) {
        PreCon.notNull(subscriber);

        _agents.getAgent("onNpcSpawn").register(new ScriptUpdateSubscriber<>(subscriber));

        return this;
    }

    @Override
    public INpc onNpcDespawn(IScriptUpdateSubscriber<NpcDespawnEvent> subscriber) {
        PreCon.notNull(subscriber);

        _agents.getAgent("onNpcDespawn").register(new ScriptUpdateSubscriber<>(subscriber));

        return this;
    }

    @Override
    public INpc onNpcClick(IScriptUpdateSubscriber<NpcClickEvent> subscriber) {
        PreCon.notNull(subscriber);

        _agents.getAgent("onNpcClick").register(new ScriptUpdateSubscriber<>(subscriber));

        return this;
    }

    @Override
    public INpc onNpcRightClick(IScriptUpdateSubscriber<NpcRightClickEvent> subscriber) {
        PreCon.notNull(subscriber);

        _agents.getAgent("onNpcRightClick").register(new ScriptUpdateSubscriber<>(subscriber));

        return this;
    }

    @Override
    public INpc onNpcLeftClick(IScriptUpdateSubscriber<NpcLeftClickEvent> subscriber) {
        PreCon.notNull(subscriber);

        _agents.getAgent("onNpcLeftClick").register(new ScriptUpdateSubscriber<>(subscriber));

        return this;
    }

    @Override
    public INpc onNpcEntityTarget(IScriptUpdateSubscriber<NpcTargetedEvent> subscriber) {
        PreCon.notNull(subscriber);

        _agents.getAgent("onNpcEntityTarget").register(new ScriptUpdateSubscriber<>(subscriber));

        return this;
    }

    @Override
    public INpc onNpcDamage(IScriptUpdateSubscriber<NpcDamageEvent> subscriber) {
        PreCon.notNull(subscriber);

        _agents.getAgent("onNpcDamage").register(new ScriptUpdateSubscriber<>(subscriber));

        return this;
    }

    @Override
    public INpc onNpcDamageByBlock(IScriptUpdateSubscriber<NpcDamageByBlockEvent> subscriber) {
        PreCon.notNull(subscriber);

        _agents.getAgent("onNpcDamageByBlock").register(new ScriptUpdateSubscriber<>(subscriber));

        return this;
    }

    @Override
    public INpc onNpcDamageByEntity(IScriptUpdateSubscriber<NpcDamageByEntityEvent> subscriber) {
        PreCon.notNull(subscriber);

        _agents.getAgent("onNpcDamageByEntity").register(new ScriptUpdateSubscriber<>(subscriber));

        return this;
    }

    @Override
    public INpc onNpcDeath(IScriptUpdateSubscriber<NpcDeathEvent> subscriber) {
        PreCon.notNull(subscriber);

        _agents.getAgent("onNpcDeath").register(new ScriptUpdateSubscriber<>(subscriber));

        return this;
    }


    public void onNpcSpawn(NpcSpawnEvent event) {
        PreCon.notNull(event);

        _agents.update("onNpcSpawn", event);
        _registry.onNpcSpawn(event);
    }

    public void onNpcDespawn(NpcDespawnEvent event) {
        PreCon.notNull(event);

        _agents.update("onNpcDespawn", event);
        _registry.onNpcDespawn(event);
    }

    public void onNpcClick(NpcClickEvent event) {
        PreCon.notNull(event);

        _agents.update("onNpcClick", event);
        _registry.onNpcClick(event);
    }

    public void onNpcRightClick(NpcRightClickEvent event) {
        PreCon.notNull(event);

        _agents.update("onNpcRightClick", event);
        _registry.onNpcRightClick(event);
    }

    public void onNpcLeftClick(NpcLeftClickEvent event) {
        PreCon.notNull(event);

        _agents.update("onNpcLeftClick", event);
        _registry.onNpcLeftClick(event);
    }

    public void onNpcEntityTarget(NpcTargetedEvent event) {
        PreCon.notNull(event);

        _agents.update("onNpcEntityTarget", event);
        _registry.onNpcEntityTarget(event);
    }

    public void onNpcDamage(NpcDamageEvent event) {
        PreCon.notNull(event);

        _agents.update("onNpcDamage", event);
        _registry.onNpcDamage(event);
    }

    public void onNpcDamageByBlock(NpcDamageByBlockEvent event) {
        PreCon.notNull(event);

        _agents.update("onNpcDamageByBlock", event);
        _registry.onNpcDamageByBlock(event);
    }

    public void onNpcDamageByEntity(NpcDamageByEntityEvent event) {
        PreCon.notNull(event);

        _agents.update("onNpcDamageByEntity", event);
        _registry.onNpcDamageByEntity(event);
    }

    public void onNpcDeath(NpcDeathEvent event) {
        PreCon.notNull(event);

        _agents.update("onNpcDeath", event);
        _registry.onNpcDeath(event);
    }
}
