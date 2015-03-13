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

import org.bukkit.inventory.ItemStack;

import net.citizensnpcs.api.exception.NPCLoadException;
import net.citizensnpcs.api.trait.trait.Inventory;

/**
 * Adapter for Citizens inventory trait.
 */
public class InventoryTrait extends NpcTrait {

    private final Inventory _trait;

    /**
     * Constructor.
     *
     * @param npc       The NPC the trait is for.
     * @param type      The parent type that instantiated the trait.
     * @param dataNode  The traits data storage node.
     */
    public InventoryTrait(INpc npc, NpcTraitType type, IDataNode dataNode) {
        super(npc, type);

        _trait = ((Npc)npc).getHandle().getTrait(Inventory.class);

        try {
            _trait.load(new DataNodeKey(dataNode.getNodePath(), dataNode, null));
        } catch (NPCLoadException e) {
            e.printStackTrace();
        }
    }

    public ItemStack[] getContents() {
        return _trait.getContents();
    }

    public InventoryTrait setContents(ItemStack[] contents) {
        _trait.setContents(contents);

        return this;
    }

    @Override
    public String toString() {
        return _trait.toString();
    }

    @Override
    public boolean isDisposed() {
        return false;
    }

    @Override
    public void dispose() {
        // do nothing, not disposable
    }
}
