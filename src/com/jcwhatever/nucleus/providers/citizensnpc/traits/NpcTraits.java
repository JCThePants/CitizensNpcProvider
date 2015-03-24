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

package com.jcwhatever.nucleus.providers.citizensnpc.traits;

import com.jcwhatever.nucleus.Nucleus;
import com.jcwhatever.nucleus.mixins.IDisposable;
import com.jcwhatever.nucleus.providers.citizensnpc.Msg;
import com.jcwhatever.nucleus.providers.citizensnpc.Npc;
import com.jcwhatever.nucleus.providers.citizensnpc.traits.citizens.EquipmentTrait;
import com.jcwhatever.nucleus.providers.npc.INpc;
import com.jcwhatever.nucleus.providers.npc.events.NpcDespawnEvent.NpcDespawnReason;
import com.jcwhatever.nucleus.providers.npc.events.NpcEntityTypeChangeEvent;
import com.jcwhatever.nucleus.providers.npc.events.NpcSpawnEvent.NpcSpawnReason;
import com.jcwhatever.nucleus.providers.npc.traits.INpcTraits;
import com.jcwhatever.nucleus.providers.npc.traits.NpcTrait;
import com.jcwhatever.nucleus.storage.IDataNode;
import com.jcwhatever.nucleus.utils.PreCon;
import com.jcwhatever.nucleus.utils.kits.IKit;

import org.bukkit.Location;
import org.bukkit.entity.EntityType;
import org.bukkit.inventory.ItemStack;

import net.citizensnpcs.api.event.DespawnReason;
import net.citizensnpcs.api.npc.NPC;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import javax.annotation.Nullable;

/**
 * Implementation of {@link com.jcwhatever.nucleus.providers.npc.traits.INpcTraits}.
 */
public class NpcTraits implements INpcTraits, IDisposable {

    private final Npc _npc;
    private final NPC _handle;
    private final CitizensTraitAdapter _adapter;

    private EntityType _entityType;
    private IKit _kit;
    private boolean _isDisposed;

    /**
     * Constructor.
     *
     * @param npc          The Npc the traits are for.
     * @param initialType  The initial entity type.
     */
    public NpcTraits(Npc npc, EntityType initialType) {
        PreCon.notNull(npc);
        PreCon.notNull(initialType);

        _npc = npc;
        _entityType = initialType;
        _handle = npc.getHandle();

        _adapter = new CitizensTraitAdapter(npc);
        _handle.addTrait(_adapter);
    }

    /**
     * Save traits to a data node.
     *
     * @param dataNode  The data node to save to.
     */
    public void save(IDataNode dataNode) {
        PreCon.notNull(dataNode);

        dataNode.set("skin", getSkinName());
        dataNode.set("kit", getKit() != null ? getKit().getName() : null);

        Collection<NpcTrait> traits =_adapter.all();

        List<String> traitNames = new ArrayList<>(traits.size());

        for (NpcTrait trait : traits) {

            if (isDefaultTrait(trait))
                continue;

            traitNames.add(trait.getLookupName());
            String nodePath = "data." + trait.getType().getPlugin().getName() + '.' + trait.getName();

            try {
                trait.save(dataNode.getNode(nodePath));
            }
            catch (Throwable e) {
                Msg.severe("Error while saving trait '{0}' in Npc '{1}'.",
                        trait.getLookupName(), getNpc().getName());
                e.printStackTrace();
            }
        }

        dataNode.set("names", traitNames);
        dataNode.save();
    }

    /**
     * Load traits from a data node.
     *
     * @param dataNode  The data node to load from.
     */
    public void load(IDataNode dataNode) {
        PreCon.notNull(dataNode);

        checkDisposed();

        String skinName = dataNode.getString("skin");
        String kitName = dataNode.getString("kit");

        if (skinName != null)
            setSkinName(skinName);

        if (kitName != null)
            setKitName(kitName);

        // get trait names
        List<String> traitNames = dataNode.getStringList("names", null);
        if (traitNames == null)
            return;

        // set to track traits whose data is not loaded from data node.
        Set<String> unloaded = new HashSet<>(traitNames);

        IDataNode data = dataNode.getNode("data");

        // iterate plugin name nodes
        for (IDataNode pluginNode : data) {

            String pluginName = pluginNode.getName();

            // iterate trait name nodes
            for (IDataNode traitNode : data) {

                String lookupName = pluginName + ':' + traitNode.getName();
                unloaded.remove(lookupName);

                // get current trait
                NpcTrait trait = _adapter.has(lookupName)
                        ? _adapter.get(lookupName)
                        : add(lookupName);

                if (trait == null) {
                    Msg.debug("Failed to find trait '{0}' while loading Npc '{1}'",
                            lookupName, getNpc().getName());
                    continue;
                }

                try {
                    trait.load(traitNode);
                }
                catch (Throwable e) {
                    Msg.severe("Error while loading trait '{0}' in Npc '{1}'.", lookupName, getNpc().getName());
                    e.printStackTrace();
                }
            }
        }

        // load leftover traits
        for (String lookupName : unloaded) {
            if (!_adapter.has(lookupName))
                add(lookupName);
        }
    }

    /**
     * Invoked when the Npc is spawned.
     *
     * @param reason  The reason the Npc is being spawned.
     */
    public void onSpawn(NpcSpawnReason reason) {
        _adapter.onSpawn(reason);
    }

    /**
     * Invoked when the Npc is despawned.
     *
     * @param reason  The reason the Npc is being despawned.
     */
    public void onDespawn(NpcDespawnReason reason) {
        _adapter.onDespawn(reason);
    }

    @Override
    public INpc getNpc() {
        return _npc;
    }

    @Override
    public boolean isVulnerable() {
        return !_handle.isProtected();
    }

    @Override
    public INpcTraits setVulnerable(boolean isVulnerable) {

        checkDisposed();

        _handle.setProtected(!isVulnerable);

        return this;
    }

    @Override
    public EntityType getType() {
        return _entityType;
    }

    @Override
    public INpcTraits setType(EntityType type) {
        PreCon.notNull(type);

        checkDisposed();

        NpcEntityTypeChangeEvent event = new NpcEntityTypeChangeEvent(getNpc(), getType(), type);
        Nucleus.getEventManager().callBukkit(this, event);

        if (event.isCancelled() || event.getNewType() == event.getOldType())
            return this;

        _handle.setBukkitEntityType(event.getNewType());
        _entityType = type;

        return this;
    }

    @Override
    public String getSkinName() {
        String name = _npc.getHandle().data().get("player-skin-name");
        return name == null
                ? _npc.getNPCName()
                : name;
    }

    @Override
    public INpcTraits setSkinName(@Nullable String skinName) {

        checkDisposed();

        if (skinName == null) {
            _npc.getHandle().data().remove("player-skin-name");
        } else {
            _npc.getHandle().data().set("player-skin-name", skinName);
        }

        if (_npc.isSpawned()) {

            Location location = _npc.getLocation();
            assert location != null;

            _npc.getHandle().despawn(DespawnReason.PENDING_RESPAWN);
            _npc.spawn(location);
        }

        return this;
    }

    @Nullable
    @Override
    public IKit getKit() {
        return _kit;
    }

    @Override
    public INpcTraits setKit(@Nullable IKit kit) {

        checkDisposed();

        _kit = kit;

        applyEquipment();

        return this;
    }

    @Override
    public INpcTraits setKitName(@Nullable String kitName) {

        checkDisposed();

        if (kitName == null)
            return setKit(null);

        IKit kit = Nucleus.getKitManager().get(kitName);
        if (kit == null) {
            throw new IllegalArgumentException("A kit named " + kitName + " was not found.");
        }

        return setKit(kit);
    }

    public void applyEquipment() {

        checkDisposed();

        EquipmentTrait trait = (EquipmentTrait)get("equipment");
        assert trait != null;

        if (_kit == null) {
            trait.set(0, null);
            trait.set(1, null);
            trait.set(2, null);
            trait.set(3, null);
            trait.set(4, null);
            return;
        }

        ItemStack[] items = _kit.getItems();
        if (items.length > 0) {
            trait.set(0, items[0]);
        }
        else {
            trait.set(0, null);
        }

        trait.set(1, _kit.getHelmet());
        trait.set(2, _kit.getChestplate());
        trait.set(3, _kit.getLeggings());
        trait.set(4, _kit.getBoots());
    }

    @Override
    public Collection<NpcTrait> all() {
        return _adapter.all();
    }

    @Override
    @Nullable
    public NpcTrait add(String name) {
        PreCon.notNullOrEmpty(name);

        checkDisposed();

        if (_adapter.has(name))
            return _adapter.get(name);

        return _adapter.add(name);
    }

    @Override
    public INpcTraits add(NpcTrait trait) {
        PreCon.notNull(trait);

        checkDisposed();

        _adapter.add(trait);

        return this;
    }

    @Nullable
    @Override
    public NpcTrait get(String name) {
        PreCon.notNull(name);

        return _adapter.get(name);
    }

    @Override
    public boolean has(String name) {
        PreCon.notNull(name);

        return _adapter.has(name);
    }

    @Override
    public boolean isEnabled(String name) {
        PreCon.notNull(name);

        NpcTrait trait = _adapter.get(name);
        return trait != null && trait.isEnabled();
    }

    @Override
    public boolean remove(String name) {
        PreCon.notNull(name);

        return _adapter.remove(name);
    }

    @Override
    public boolean isDisposed() {
        return _isDisposed;
    }

    @Override
    public void dispose() {

        if (!getNpc().isDisposed())
            throw new IllegalStateException("NpcTraits cannot be disposed until its parent Npc is disposed.");

        if (_isDisposed)
            return;

        _isDisposed = true;

        _kit = null;
        _adapter.dispose();
    }

    private void checkDisposed() {
        if (_isDisposed)
            throw new IllegalStateException("Cannot use disposed NpcTraits.");
    }

    private boolean isDefaultTrait(NpcTrait trait) {
        return trait.getLookupName().equals("equipment") ||
                trait.getLookupName().equals("inventory") ||
                trait.getLookupName().equals("owner");
    }
}
