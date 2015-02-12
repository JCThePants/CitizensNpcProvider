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
import com.jcwhatever.nucleus.providers.npc.traits.NpcTraitType;
import com.jcwhatever.nucleus.providers.npc.traits.NpcTrait;
import com.jcwhatever.nucleus.storage.IDataNode;

import org.bukkit.command.CommandSender;

import net.citizensnpcs.api.exception.NPCLoadException;
import net.citizensnpcs.api.trait.trait.Owner;

import java.util.UUID;

/*
 * 
 */
public class OwnerTrait extends NpcTrait {

    private final Owner _trait;

    /**
     * Constructor.
     *
     * @param npc       The NPC the trait is for.
     * @param type      The parent type that instantiated the trait.
     * @param dataNode  The traits data storage node.
     */
    public OwnerTrait(Npc npc, NpcTraitType type, IDataNode dataNode) {
        super(npc, type);

        _trait = npc.getHandle().getTrait(Owner.class);

        try {
            _trait.load(new DataNodeKey(dataNode.getNodePath(), dataNode, null));
        } catch (NPCLoadException e) {
            e.printStackTrace();
        }
    }

    public String getOwner() {
        return _trait.getOwner();
    }

    public UUID getOwnerId() {
        return _trait.getOwnerId();
    }

    public boolean isOwnedBy(CommandSender sender) {
        return _trait.isOwnedBy(sender);
    }

    public boolean isOwnedBy(String name) {
        return _trait.isOwnedBy(name);
    }

    public OwnerTrait setOwner(CommandSender sender) {
        _trait.setOwner(sender);

        return this;
    }

    public OwnerTrait setOwner(String owner) {
        _trait.setOwner(owner);

        return this;
    }

    public OwnerTrait setOwner(String owner, UUID uuid) {
        _trait.setOwner(owner, uuid);

        return this;
    }

    @Override
    public void save(IDataNode dataNode) {
        _trait.save(new DataNodeKey(dataNode.getNodePath(), dataNode, null));
    }

    @Override
    public boolean isDisposed() {
        return false;
    }

    @Override
    public void dispose() {

    }
}
