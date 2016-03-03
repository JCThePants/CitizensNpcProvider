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
import com.jcwhatever.nucleus.providers.citizensnpc.ai.BehaviourAgent;
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
import com.jcwhatever.nucleus.providers.npc.events.NpcDespawnEvent.NpcDespawnReason;
import com.jcwhatever.nucleus.providers.npc.events.NpcDisposeEvent;
import com.jcwhatever.nucleus.providers.npc.events.NpcLeftClickEvent;
import com.jcwhatever.nucleus.providers.npc.events.NpcRightClickEvent;
import com.jcwhatever.nucleus.providers.npc.events.NpcSpawnEvent;
import com.jcwhatever.nucleus.providers.npc.events.NpcSpawnEvent.NpcSpawnReason;
import com.jcwhatever.nucleus.providers.npc.events.NpcTargetedEvent;
import com.jcwhatever.nucleus.storage.IDataNode;
import com.jcwhatever.nucleus.utils.PreCon;
import com.jcwhatever.nucleus.utils.coords.LocationUtils;
import com.jcwhatever.nucleus.utils.observer.script.IScriptUpdateSubscriber;
import com.jcwhatever.nucleus.utils.observer.script.ScriptUpdateSubscriber;
import com.jcwhatever.nucleus.utils.observer.update.NamedUpdateAgents;
import net.citizensnpcs.api.event.DespawnReason;
import net.citizensnpcs.api.npc.NPC;
import net.citizensnpcs.trait.CurrentLocation;
import net.citizensnpcs.util.NMS;
import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;

import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.WeakHashMap;

/**
 * {@link INpc} implementation.
 */
public class Npc implements INpc {

    private static final Location LOOK_LOCATION = new Location(null, 0, 0, 0);

    private String _lookupName;
    private Registry _registry;
    private DataNodeKey _dataKey;

    private final NPC _npc;
    private final NpcPool _pool;
    private final NpcNavigator _navigator;
    private final NpcGoals _goals;
    private final NpcTraits _traits;
    private final NamedUpdateAgents _agents = new NamedUpdateAgents();
    private final Map<BehaviourAgent<?, ?, ?, ?>, NamedUpdateAgents> _behaviourAgents = new WeakHashMap<>(10);
    private final Location _spawnCache = new Location(null, 0, 0, 0);
    private Map<String, Object> _meta;
    private boolean _isDisposed;
    private boolean _isSpawned;
    private boolean _hasSpawnLocation;

    // Holds entity reference, otherwise no one else may be holding it (weak references).
    // Prevents losing entity reference in WeakHashMap in CitizensProvider.
    private Entity _currentEntity;

    // Store spawn/despawn reasons to fill in functionality missing in Citizens
    // spawn event. Used to "guess" the reason an NPC is spawning.
    private NpcSpawnReason _spawnReason;
    private NpcDespawnReason _lastDespawnReason;

    /**
     * Constructor.
     *
     * @param npc   The Citizens NPC.
     * @param pool  The {@link NpcPool} that created the Npc.
     */
    public Npc(NPC npc, NpcPool pool) {
        PreCon.notNull(npc);
        PreCon.notNull(pool);

        _npc = npc;

        _navigator = new NpcNavigator(this, npc.getNavigator());
        _goals = new NpcGoals(this);
        _traits = new NpcTraits(this);
        _pool = pool;
    }

    /**
     * Initialize or re-initialize.
     *
     * @param lookupName  The unique lookup name of the NPC.
     * @param npcName     The name of the NPC.
     * @param type        The initial {@link EntityType}.
     * @param registry    The owning registry.
     * @param dataKey     The NPC's data storage.
     */
    void init(String lookupName, String npcName,
              EntityType type, Registry registry, DataNodeKey dataKey) {

        PreCon.notNull(lookupName);
        PreCon.notNull(npcName);
        PreCon.notNull(type);
        PreCon.notNull(registry);
        PreCon.notNull(dataKey);

        _isDisposed = false;

        _lookupName = lookupName;
        _registry = registry;
        _dataKey = dataKey;
        _npc.setName(npcName);

        CitizensProvider.getInstance().registerNpc(this);

        _traits.init(type);
        _navigator.init(registry);
        _goals.init();
    }

    /**
     * Get the NPC data storage.
     */
    public DataNodeKey getDataKey() {
        return _dataKey;
    }

    /**
     * Get the NPC data node storage.
     */
    public IDataNode getDataNode() {
        return _dataKey.getDataNode();
    }

    @Override
    public Registry getRegistry() {
        return _registry;
    }

    @Override
    public UUID getId() {
        return _npc.getUniqueId();
    }

    @Override
    public String getLookupName() {
        return _lookupName;
    }

    @Override
    public String getDisplayName() {
        return _npc.getName();
    }

    @Override
    public INpc setDisplayName(String name) {
        _npc.setName(name);

        return this;
    }

    @Override
    public boolean isDisplayNameVisible() {
        return _npc.data().get(NPC.NAMEPLATE_VISIBLE_METADATA, true);
    }

    @Override
    public INpc setDisplayNameVisible(boolean isVisible) {
        _npc.data().setPersistent(NPC.NAMEPLATE_VISIBLE_METADATA, isVisible);
        return this;
    }

    @Nullable
    @Override
    public Entity getEntity() {
        return _npc.getEntity();
    }

    @Override
    public boolean isSpawned() {
        return _isSpawned && _npc.isSpawned();
    }

    @Override
    public boolean spawn(Location location) {
        PreCon.notNull(location);

        checkDisposed();

        if (isSpawned()) {
            Entity entity = getEntity();
            assert entity != null;

            entity.teleport(location);
            return true;
        }

        _hasSpawnLocation = true;

        if (_npc.spawn(LocationUtils.copy(location, _spawnCache))) {
            _isSpawned = true;
            _goals.reset();
            CitizensProvider.getInstance().registerEntity(this, _npc.getEntity());
            _currentEntity = _npc.getEntity();
            return true;
        }

        return false;
    }

    @Override
    public boolean despawn() {
        return !_isDisposed && despawn(DespawnReason.PLUGIN);
    }

    @Nullable
    @Override
    public Location getLocation() {
        if (_npc.isSpawned()) {
            return _npc.getEntity().getLocation();
        }
        Location stored = _npc.getTrait(CurrentLocation.class).getLocation();
        if (stored == null || !_hasSpawnLocation)
            return null;

        return LocationUtils.copy(stored);
    }

    @Nullable
    @Override
    public Location getLocation(Location output) {
        PreCon.notNull(output);

        if (_npc.isSpawned()) {
            return _npc.getEntity().getLocation(output);
        }

        Location stored = _npc.getTrait(CurrentLocation.class).getLocation();
        if (stored == null || !_hasSpawnLocation)
            return null;

        return LocationUtils.copy(stored, output);
    }

    @Override
    public NpcNavigator getNavigator() {
        return _navigator;
    }

    @Override
    public NpcGoals getGoals() {
        return _goals;
    }

    @Override
    public NpcTraits getTraits() {
        return _traits;
    }

    @Override
    @Nullable
    public Object getMeta(String key) {
        PreCon.notNull(key);

        if (_meta == null)
            return null;

        return _meta.get(key);
    }

    @Override
    public void setMeta(String key, @Nullable Object value) {
        PreCon.notNullOrEmpty(key);

        checkDisposed();

        if (_meta == null)
            _meta = new HashMap<>(7);

        if (value == null) {
            _meta.remove(key);
        }
        else {
            _meta.put(key, value);
        }
    }

    @Nullable
    @Override
    public INpc getNPCVehicle() {
        if (!isSpawned())
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
        if (!isSpawned())
            return null;

        Entity entity = _npc.getEntity();

        Entity passenger = entity.getPassenger();
        if (passenger == null)
            return null;

        return CitizensProvider.getInstance().getNpc(passenger);
    }

    @Override
    public INpc mountNPC(INpc vehicle) {
        PreCon.notNull(vehicle);

        checkDisposed();

        if (!isSpawned())
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

        if (!isSpawned())
            return this;

        NMS.look(_npc.getEntity(), yaw, pitch);

        return this;
    }

    @Override
    public INpc lookEntity(Entity entity) {
        PreCon.notNull(entity);

        if (!isSpawned())
            return this;

        Location location = entity.getLocation(LOOK_LOCATION);

        return lookLocation(location);
    }

    @Override
    public INpc lookLocation(Location location) {
        PreCon.notNull(location);

        if (!isSpawned())
            return this;

        if (location.getWorld() == null || _npc.getStoredLocation() == null ||
                !location.getWorld().equals(_npc.getStoredLocation().getWorld())) {
            return this;
        }

        if (location.distanceSquared(_npc.getStoredLocation()) <= 0.25)
            return this;

        _npc.faceLocation(location);

        return this;
    }

    @Override
    public boolean save(IDataNode dataNode) {
        PreCon.notNull(dataNode);

        dataNode.set("lookup", _lookupName);
        dataNode.set("name", _npc.getName());
        dataNode.set("uuid", _npc.getUniqueId());
        dataNode.set("type", getTraits().getType());

        getTraits().save(dataNode.getNode("traits"));

        dataNode.save();

        return true;
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

        despawn(DespawnReason.REMOVAL);

        NpcDisposeEvent event = new NpcDisposeEvent(Npc.this);
        Nucleus.getEventManager().callBukkit(this, event);

        CitizensProvider.getInstance().unregisterNPC(this);

        _registry.remove(this);
        _agents.disposeAgents();

        for (NamedUpdateAgents agent : _behaviourAgents.values()) {
            agent.disposeAgents();
        }
        _behaviourAgents.clear();

        _traits.dispose();
        _goals.dispose();
        _navigator.dispose();
        if (_meta != null)
            _meta.clear();

        _lookupName = null;
        _registry = null;
        _dataKey = null;
        _hasSpawnLocation = false;

        _pool.recycle(this);
    }

    public NPC getHandle() {
        return _npc;
    }

    @Override
    public INpc onNpcSpawn(IScriptUpdateSubscriber<NpcSpawnEvent> subscriber) {
        PreCon.notNull(subscriber);

        checkDisposed();

        _agents.getAgent("onNpcSpawn").addSubscriber(new ScriptUpdateSubscriber<>(subscriber));

        return this;
    }

    @Override
    public INpc onNpcDespawn(IScriptUpdateSubscriber<NpcDespawnEvent> subscriber) {
        PreCon.notNull(subscriber);

        checkDisposed();

        _agents.getAgent("onNpcDespawn").addSubscriber(new ScriptUpdateSubscriber<>(subscriber));

        return this;
    }

    @Override
    public INpc onNpcClick(IScriptUpdateSubscriber<NpcClickEvent> subscriber) {
        PreCon.notNull(subscriber);

        checkDisposed();

        _agents.getAgent("onNpcClick").addSubscriber(new ScriptUpdateSubscriber<>(subscriber));

        return this;
    }

    @Override
    public INpc onNpcRightClick(IScriptUpdateSubscriber<NpcRightClickEvent> subscriber) {
        PreCon.notNull(subscriber);

        checkDisposed();

        _agents.getAgent("onNpcRightClick").addSubscriber(new ScriptUpdateSubscriber<>(subscriber));

        return this;
    }

    @Override
    public INpc onNpcLeftClick(IScriptUpdateSubscriber<NpcLeftClickEvent> subscriber) {
        PreCon.notNull(subscriber);

        checkDisposed();

        _agents.getAgent("onNpcLeftClick").addSubscriber(new ScriptUpdateSubscriber<>(subscriber));

        return this;
    }

    @Override
    public INpc onNpcEntityTarget(IScriptUpdateSubscriber<NpcTargetedEvent> subscriber) {
        PreCon.notNull(subscriber);

        checkDisposed();

        _agents.getAgent("onNpcEntityTarget").addSubscriber(new ScriptUpdateSubscriber<>(subscriber));

        return this;
    }

    @Override
    public INpc onNpcDamage(IScriptUpdateSubscriber<NpcDamageEvent> subscriber) {
        PreCon.notNull(subscriber);

        checkDisposed();

        _agents.getAgent("onNpcDamage").addSubscriber(new ScriptUpdateSubscriber<>(subscriber));

        return this;
    }

    @Override
    public INpc onNpcDamageByBlock(IScriptUpdateSubscriber<NpcDamageByBlockEvent> subscriber) {
        PreCon.notNull(subscriber);

        checkDisposed();

        _agents.getAgent("onNpcDamageByBlock").addSubscriber(new ScriptUpdateSubscriber<>(subscriber));

        return this;
    }

    @Override
    public INpc onNpcDamageByEntity(IScriptUpdateSubscriber<NpcDamageByEntityEvent> subscriber) {
        PreCon.notNull(subscriber);

        checkDisposed();

        _agents.getAgent("onNpcDamageByEntity").addSubscriber(new ScriptUpdateSubscriber<>(subscriber));

        return this;
    }

    @Override
    public INpc onNpcDeath(IScriptUpdateSubscriber<NpcDeathEvent> subscriber) {
        PreCon.notNull(subscriber);

        checkDisposed();

        _agents.getAgent("onNpcDeath").addSubscriber(new ScriptUpdateSubscriber<>(subscriber));

        return this;
    }

    public void onNpcSpawn(NpcSpawnEvent event) {
        PreCon.notNull(event);

        updateAgents("onNpcSpawn", event);

        if (_registry != null) {
            _registry.onNpcSpawn(event);

            if (!event.isCancelled()) {

                _isSpawned = true;

                getTraits().onSpawn(event.getReason());

                Scheduler.runTaskLater(Nucleus.getPlugin(), new Runnable() {
                    @Override
                    public void run() {

                        if (!_isDisposed)
                            getTraits().applyEquipment();
                    }
                });
            }
        }
    }

    public void onNpcDespawn(NpcDespawnEvent event) {
        PreCon.notNull(event);

        updateAgents("onNpcDespawn", event);

        if (_registry != null) {
            _registry.onNpcDespawn(event);

            if (!event.isCancelled()) {
                _isSpawned = false;
                getTraits().onDespawn(event.getReason());
            }
        }
    }

    public void onNpcClick(NpcClickEvent event) {
        PreCon.notNull(event);

        updateAgents("onNpcClick", event);

        if (_registry != null)
            _registry.onNpcClick(event);
    }

    public void onNpcRightClick(NpcRightClickEvent event) {
        PreCon.notNull(event);

        updateAgents("onNpcRightClick", event);

        if (_registry != null)
            _registry.onNpcRightClick(event);
    }

    public void onNpcLeftClick(NpcLeftClickEvent event) {
        PreCon.notNull(event);

        updateAgents("onNpcLeftClick", event);

        if (_registry != null)
            _registry.onNpcLeftClick(event);
    }

    public void onNpcEntityTarget(NpcTargetedEvent event) {
        PreCon.notNull(event);

        updateAgents("onNpcEntityTarget", event);

        if (_registry != null)
            _registry.onNpcEntityTarget(event);
    }

    public void onNpcDamage(NpcDamageEvent event) {
        PreCon.notNull(event);

        updateAgents("onNpcDamage", event);

        if (_registry != null)
            _registry.onNpcDamage(event);
    }

    public void onNpcDamageByBlock(NpcDamageByBlockEvent event) {
        PreCon.notNull(event);

        updateAgents("onNpcDamageByBlock", event);

        if (_registry != null)
            _registry.onNpcDamageByBlock(event);
    }

    public void onNpcDamageByEntity(NpcDamageByEntityEvent event) {
        PreCon.notNull(event);

        updateAgents("onNpcDamageByEntity", event);

        if (_registry != null)
            _registry.onNpcDamageByEntity(event);
    }

    public void onNpcDeath(NpcDeathEvent event) {
        PreCon.notNull(event);

        updateAgents("onNpcDeath", event);

        if (_registry != null)
            _registry.onNpcDeath(event);
    }

    public NamedUpdateAgents registerUpdateAgent(BehaviourAgent<?, ?, ?, ?> agent) {
        PreCon.notNull(agent);

        checkDisposed();

        if (_behaviourAgents.containsKey(agent)) {
            return _behaviourAgents.get(agent);
        }

        NamedUpdateAgents updateAgents = new NamedUpdateAgents();

        _behaviourAgents.put(agent, updateAgents);

        return updateAgents;
    }

    public void unregisterUpdateAgent(BehaviourAgent<?, ?, ?, ?> agent) {
        _behaviourAgents.remove(agent);
    }

    public void updateAgents(String agentName, Object event) {
        PreCon.notNullOrEmpty(agentName);
        PreCon.notNull(event);

        for (NamedUpdateAgents agents : _behaviourAgents.values()) {
            agents.update(agentName, event);
        }
        _agents.update(agentName, event);
    }

    /**
     * Get the reason the NPC was spawned for.
     *
     * <p>For internal use.</p>
     */
    public NpcSpawnReason getSpawnReason() {

        NpcSpawnReason spawnReason = _spawnReason;
        NpcDespawnReason despawnReason = _lastDespawnReason;

        _lastDespawnReason = null;
        _spawnReason = null;

        // attempt to guess the reason Citizens is respawning for
        if (spawnReason == null) {

            if (despawnReason == null) {
                return NpcSpawnReason.INVOKED;
            }
            else {

                switch (despawnReason) {
                    case INVOKED:
                        // fall through
                    case DEATH:
                        return NpcSpawnReason.INVOKED;
                    case RESPAWN:
                        return NpcSpawnReason.RESPAWN;
                    case CHUNK_UNLOAD:
                        return NpcSpawnReason.CHUNK_LOAD;
                    case WORLD_UNLOAD:
                        return NpcSpawnReason.WORLD_LOAD;
                }
            }

            return NpcSpawnReason.INVOKED;
        }
        else {
            return _spawnReason;
        }
    }

    /**
     * Set the last reason the NPC was despawned for.
     *
     * <p>For internal use.</p>
     *
     * @param reason  The reason.
     */
    public void setLastDespawnReason(NpcDespawnReason reason) {
        _lastDespawnReason = reason;
    }

    // despawn the NPC
    private boolean despawn(DespawnReason reason) {

        if (!isSpawned()) {

            // make Citizens2 remove chunk load references to the NPC so
            // it doesn't get respawned when the chunk loads.
            if (_lastDespawnReason == NpcDespawnReason.CHUNK_UNLOAD &&
                    reason == DespawnReason.REMOVAL) {
                _npc.despawn(reason);
            }

            return false;
        }

        Entity entity = _npc.getEntity();

        if (_npc.despawn(reason)) {
            CitizensProvider.getInstance().unregisterEntity(entity);
            _currentEntity = null;
            _isSpawned = false;
            return true;
        }
        return false;
    }

    private void checkDisposed() {
        if (_isDisposed)
            throw new IllegalStateException("Cannot use a disposed Npc.");
    }
}
