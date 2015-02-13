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

import com.jcwhatever.nucleus.providers.citizensnpc.Npc;
import com.jcwhatever.nucleus.providers.citizensnpc.traits.citizens.EquipmentTrait;
import com.jcwhatever.nucleus.providers.citizensnpc.traits.citizens.EquipmentTraitType;
import com.jcwhatever.nucleus.providers.citizensnpc.traits.citizens.InventoryTrait;
import com.jcwhatever.nucleus.providers.citizensnpc.traits.citizens.InventoryTraitType;
import com.jcwhatever.nucleus.providers.citizensnpc.traits.citizens.OwnerTrait;
import com.jcwhatever.nucleus.providers.citizensnpc.traits.citizens.OwnerTraitType;
import com.jcwhatever.nucleus.providers.npc.traits.NpcTrait;
import com.jcwhatever.nucleus.providers.npc.traits.NpcTraitType;
import com.jcwhatever.nucleus.utils.PreCon;

import net.citizensnpcs.api.trait.Trait;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.annotation.Nullable;

/*
 * 
 */
public class CitizensTraitAdapter extends Trait {

    public static final EquipmentTraitType _equipmentType = new EquipmentTraitType();
    public static final InventoryTraitType _inventoryType = new InventoryTraitType();
    public static final OwnerTraitType _ownerType = new OwnerTraitType();

    private final Npc _npc;
    private final Map<String, NpcTrait> _traits = new HashMap<>(10);

    // used to store a copy of the _traits values for iteration. Iterate this to
    // prevent concurrent modification exceptions.
    private List<NpcTrait> _iterableTraits;

    /**
     * Constructor.
     *
     * @param npc  The owning NPC.
     */
    public CitizensTraitAdapter(Npc npc) {
        super("CitizensProviderTraitAdapter");

        PreCon.notNull(npc);

        _npc = npc;

        _traits.put("equipment", new EquipmentTrait(npc, _equipmentType,
                npc.getDataKey().getRelative("traits.equipment").getDataNode()));

        _traits.put("inventory", new InventoryTrait(npc, _inventoryType,
                npc.getDataKey().getRelative("traits.inventory").getDataNode()));

        _traits.put("owner", new OwnerTrait(npc, _ownerType,
                npc.getDataKey().getRelative("traits.owner").getDataNode()));
    }

    @Override
    public boolean isRunImplemented() {
        return true;
    }

    @Override
    public void onAttach() {

        List<NpcTrait> traits = getIterableTraits();

        for (NpcTrait trait : traits) {
            trait.onAdd();
        }
    }

    @Override
    public void onCopy() {
        // do nothing
    }

    @Override
    public void onDespawn() {

        List<NpcTrait> traits = getIterableTraits();

        for (NpcTrait trait : traits) {
            trait.onDespawn();
        }
    }

    @Override
    public void onRemove() {

        List<NpcTrait> traits = getIterableTraits();

        for (NpcTrait trait : traits) {
            trait.onRemove();
        }
    }

    @Override
    public void onSpawn() {

        List<NpcTrait> traits = getIterableTraits();

        for (NpcTrait trait : traits) {
            trait.onSpawn();
        }
    }

    @Override
    public void run() {

        List<NpcTrait> traits = getIterableTraits();

        for (NpcTrait trait : traits) {
            if (trait instanceof Runnable && trait.canRun()) {
                ((Runnable) trait).run();
            }
        }
    }

    public Collection<NpcTrait> all() {
        return _traits.values();
    }

    @Nullable
    public NpcTrait add(String name) {
        PreCon.notNull(name);

        if (_traits.containsKey(name))
            return _traits.get(name);

        NpcTraitType type = _npc.getRegistry().getTraitType(name);
        if (type == null)
            return null;

        return type.attachTrait(_npc);
    }

    public void add(NpcTrait trait) {
        PreCon.notNull(trait);

        if (_traits.containsKey(trait.getLookupName()))
            return;

        _traits.put(trait.getLookupName(), trait);
        updateIterableTraits();
    }

    @Nullable
    public NpcTrait get(String name) {
        PreCon.notNull(name);

        return _traits.get(name);
    }

    public boolean has(String name) {
        return _traits.containsKey(name);
    }

    public boolean remove(String name) {
        PreCon.notNull(name);

        if (!(isInternalTrait(name)) &&
                _traits.remove(name) != null) {

            updateIterableTraits();
            return true;
        }
        return false;
    }

    private boolean isInternalTrait(String name) {
        return name.equals("equipment") || name.equals("inventory") || name.equals("owner");
    }

    private List<NpcTrait> getIterableTraits() {
        if (_iterableTraits == null)
            _iterableTraits = new ArrayList<>(_traits.values());

        return _iterableTraits;
    }

    private void updateIterableTraits() {
        _iterableTraits = null;
    }

}
