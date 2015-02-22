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
import com.jcwhatever.nucleus.kits.IKit;
import com.jcwhatever.nucleus.providers.citizensnpc.Npc;
import com.jcwhatever.nucleus.providers.citizensnpc.traits.citizens.EquipmentTrait;
import com.jcwhatever.nucleus.providers.npc.INpc;
import com.jcwhatever.nucleus.providers.npc.events.NpcEntityTypeChangeEvent;
import com.jcwhatever.nucleus.providers.npc.traits.INpcTraits;
import com.jcwhatever.nucleus.providers.npc.traits.NpcTrait;
import com.jcwhatever.nucleus.storage.IDataNode;
import com.jcwhatever.nucleus.utils.PreCon;

import org.bukkit.Location;
import org.bukkit.entity.EntityType;
import org.bukkit.inventory.ItemStack;

import net.citizensnpcs.api.event.DespawnReason;
import net.citizensnpcs.api.npc.NPC;
import net.citizensnpcs.api.trait.trait.MobType;

import java.util.Collection;
import javax.annotation.Nullable;

/**
 * Implementation of {@link com.jcwhatever.nucleus.providers.npc.traits.INpcTraits}.
 */
public class NpcTraits implements INpcTraits {

    private final Npc _npc;
    private final NPC _handle;
    private final CitizensTraitAdapter _adapter;
    private final EntityType _initialType;
    private IKit _kit;

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
        _initialType = initialType;
        _handle = npc.getHandle();

        _adapter = new CitizensTraitAdapter(npc);
        _handle.addTrait(_adapter);
    }

    @Override
    public INpc getNpc() {
        return _npc;
    }

    @Override
    public IDataNode getTraitNode(NpcTrait trait) {
        return _npc.getDataNode().getNode("traits").getNode(trait.getLookupName());
    }

    @Override
    public boolean isInvulnerable() {
        return _handle.isProtected();
    }

    @Override
    public INpcTraits invulnerable() {

        _handle.setProtected(true);

        return this;
    }

    @Override
    public INpcTraits vulnerable() {

        _handle.setProtected(false);

        return this;
    }

    @Override
    public EntityType getType() {

        Location location = getNpc().getLocation();
        if (location == null)
            return _initialType; // correct type may not be initialized by citizens yet

        MobType type = _handle.getTrait(MobType.class);
        return type.getType();
    }

    @Override
    public INpcTraits setType(EntityType type) {
        PreCon.notNull(type);

        NpcEntityTypeChangeEvent event = new NpcEntityTypeChangeEvent(getNpc(), getType(), type);
        Nucleus.getEventManager().callBukkit(this, event);

        if (event.isCancelled() || event.getNewType() == event.getOldType())
            return this;

        _handle.setBukkitEntityType(event.getNewType());

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

        _kit = kit;

        applyEquipment();

        return this;
    }

    @Override
    public INpcTraits setKitName(@Nullable String kitName) {

        if (kitName == null)
            return setKit(null);

        IKit kit = Nucleus.getKitManager().get(kitName);
        if (kit == null) {
            throw new IllegalArgumentException("A kit named " + kitName + " was not found.");
        }

        return setKit(kit);
    }

    public void applyEquipment() {

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

        if (_adapter.has(name))
            return _adapter.get(name);

        return _adapter.add(name);
    }

    @Override
    public INpcTraits add(NpcTrait trait) {
        PreCon.notNull(trait);

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
}
