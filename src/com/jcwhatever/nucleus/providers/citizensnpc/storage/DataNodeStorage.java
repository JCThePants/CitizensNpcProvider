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

import net.citizensnpcs.api.util.Storage;

import java.util.HashMap;
import java.util.Map;

/**
 * Provides {@link com.jcwhatever.nucleus.storage.IDataNode} based storage.
 * Use with an {@code net.citizensnpcs.api.npc.NPCDataStore}.
 */
public class DataNodeStorage implements Storage {

    private Map<String, DataNodeKey> _cachedKeys = new HashMap<>(50);

    public final IDataNode _dataNode;

    public DataNodeStorage(IDataNode dataNode) {
        PreCon.notNull(dataNode);

        _dataNode = dataNode;
    }

    public IDataNode getDataNode() {
        return _dataNode;
    }

    @Override
    public DataNodeKey getKey(String s) {

        DataNodeKey key = _cachedKeys.get(s);
        if (key != null)
            return key;

        DataNodeKey nodeKey = new DataNodeKey(s, _dataNode.getNode(s), this);

        _cachedKeys.put(s, nodeKey);

        return nodeKey;
    }

    @Override
    public boolean load() {
        return _dataNode.isLoaded() || _dataNode.load();
    }

    @Override
    public void save() {
        _dataNode.saveSync();
    }
}
