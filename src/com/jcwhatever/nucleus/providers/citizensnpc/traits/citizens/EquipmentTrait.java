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

package com.jcwhatever.nucleus.providers.citizensnpc.traits.citizens;

import com.jcwhatever.nucleus.providers.citizensnpc.Npc;
import com.jcwhatever.nucleus.providers.citizensnpc.storage.DataNodeKey;
import com.jcwhatever.nucleus.providers.npc.INpc;
import com.jcwhatever.nucleus.providers.npc.traits.NpcTraitType;
import com.jcwhatever.nucleus.providers.npc.traits.NpcTrait;
import com.jcwhatever.nucleus.storage.IDataNode;
import com.jcwhatever.nucleus.utils.PreCon;

import org.bukkit.inventory.ItemStack;

import net.citizensnpcs.api.exception.NPCLoadException;
import net.citizensnpcs.api.trait.trait.Equipment;
import net.citizensnpcs.api.trait.trait.Equipment.EquipmentSlot;

import javax.annotation.Nullable;

/**
 * Adapter for Citizens equipment trait.
 */
public class EquipmentTrait extends NpcTrait {

    private final Equipment _trait;

    /**
     * Constructor.
     *
     * @param npc  The NPC the trait is for.
     * @param type The parent type that instantiated the trait.
     */
    public EquipmentTrait(INpc npc, NpcTraitType type, IDataNode dataNode) {
        super(npc, type);

        _trait = ((Npc)npc).getHandle().getTrait(Equipment.class);

        try {
            _trait.load(new DataNodeKey(dataNode.getNodePath(), dataNode, null));
        } catch (NPCLoadException e) {
            e.printStackTrace();
        }
    }

    /**
     * Get the {@link ItemStack} in the specified slot.
     *
     * @param slot  The slot. Can be a number (0-4) or the name of the slot:
     *              'HAND', 'HELMET', 'CHESTPLATE', 'LEGGINGS' or 'BOOTS'
     *
     * @return  The {@link ItemStack} in the slot or null if there is none.
     */
    @Nullable
    public ItemStack get(Object slot) {
        PreCon.notNull(slot);

        return _trait.get(getSlot(slot));
    }

    /**
     * Set the {@link ItemStack} in the specified equipment slot.
     *
     * @param slot  The slot. Can be a number (0-4) or the name of the slot:
     *              'HAND', 'HELMET', 'CHESTPLATE', 'LEGGINGS' or 'BOOTS'
     * @param item  The {@link ItemStack} to set into the slot. Null to empty the slot.
     *
     * @return  Self for chaining.
     */
    public EquipmentTrait set(Object slot, @Nullable ItemStack item) {
        PreCon.notNull(slot);

        _trait.set(getSlot(slot), item);

        return this;
    }

    @Override
    public boolean isDisposed() {
        return false;
    }

    @Override
    public void dispose() {
        // do nothing, not disposable
    }

    @Override
    public String toString() {
        return _trait.toString();
    }

    private EquipmentSlot getSlot(Object slot) {

        EquipmentSlot equipmentSlot;

        if (slot instanceof Number) {
            int index = ((Number)slot).intValue();

            switch (index) {
                case 0:
                    equipmentSlot = EquipmentSlot.HAND;
                    break;
                case 1:
                    equipmentSlot = EquipmentSlot.HELMET;
                    break;
                case 2:
                    equipmentSlot = EquipmentSlot.CHESTPLATE;
                    break;
                case 3:
                    equipmentSlot = EquipmentSlot.LEGGINGS;
                    break;
                case 4:
                    equipmentSlot = EquipmentSlot.BOOTS;
                    break;
                default:
                    throw new IllegalArgumentException("Value of 'slot' must be a number from 0 to 4.");
            }
        }
        else if (slot instanceof EquipmentSlot) {
            equipmentSlot = (EquipmentSlot)slot;
        }
        else if (slot instanceof String) {

            String slotName = ((String)slot).toUpperCase();

            try {
                equipmentSlot = EquipmentSlot.valueOf(slotName);
            }
            catch (Exception e) {
                throw new IllegalArgumentException("Value of 'slot' must be one of: " +
                        "'HAND', 'HELMET', 'CHESTPLATE', 'LEGGINGS' or 'BOOTS'");
            }
        }
        else {
            throw new IllegalArgumentException("Value of 'slot' must be one of: " +
                    "'HAND', 'HELMET', 'CHESTPLATE', 'LEGGINGS' or 'BOOTS'");
        }

        return equipmentSlot;
    }
}
