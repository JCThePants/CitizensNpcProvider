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

package com.jcwhatever.nucleus.providers.citizensnpc.storage;

import com.jcwhatever.nucleus.storage.IDataNode;
import com.jcwhatever.nucleus.utils.PreCon;

import org.bukkit.entity.EntityType;

import net.citizensnpcs.api.npc.NPC;
import net.citizensnpcs.api.npc.NPCDataStore;
import net.citizensnpcs.api.npc.NPCRegistry;

import java.util.Iterator;
import java.util.UUID;

/**
 * NPC Data storage that utilizes an {@link com.jcwhatever.nucleus.storage.IDataNode}.
 */
public class DataNodeNPCStore implements NPCDataStore {

    private final DataNodeStorage _storage;

    public DataNodeNPCStore(IDataNode dataNode) {
        PreCon.notNull(dataNode);

        _storage = new DataNodeStorage(dataNode);
    }

    public DataNodeNPCStore(DataNodeStorage storage) {
        PreCon.notNull(storage);

        _storage = storage;
    }

    public DataNodeStorage getStorage() {
        return _storage;
    }

    @Override
    public void clearData(NPC npc) {
        _storage.getDataNode().remove("npc." + npc.getId());
    }

    @Override
    public int createUniqueNPCId(NPCRegistry registry) {

        IDataNode storage = _storage.getDataNode();

        int newId = storage.getInteger("last-created-npc-id", -1);

        if (registry.getById(newId + 1) == null) {
            newId++;
        }
        else {
            Iterator<NPC> iterator = registry.iterator();

            newId = 0;

            while(iterator.hasNext()) {
                NPC npc = iterator.next();
                newId = Math.max(npc.getId(), newId);
            }
            newId++;
        }

        storage.set("last-created-npc-id", newId);
        return newId;
    }

    @Override
    public void loadInto(NPCRegistry registry) {

        IDataNode storage = _storage.getDataNode().getNode("npc");

        for (IDataNode npcNode : storage) {

            assert npcNode.getName() != null;

            int id = Integer.parseInt(npcNode.getName());
            String name = npcNode.getString("name");

            UUID uuid = npcNode.getUUID("uuid");
            if (uuid == null) {
                uuid = UUID.randomUUID();
            }

            EntityType entityType = npcNode.getEnum("traits.type", EntityType.PLAYER, EntityType.class);

            NPC npc = registry.createNPC(entityType, uuid, id, name);
            npc.load(_storage.getKey(npcNode.getNodePath()));
        }
    }

    @Override
    public void saveToDisk() {
        _storage.getDataNode().save(); // using async causes issues in scripts
    }

    @Override
    public void saveToDiskImmediate() {
        _storage.getDataNode().saveSync();
    }

    @Override
    public void store(NPC npc) {
        npc.save(_storage.getKey("npc." + npc.getId()));
    }

    @Override
    public void storeAll(NPCRegistry registry) {
        for (NPC npc : registry) {
            store(npc);
        }
    }
}
