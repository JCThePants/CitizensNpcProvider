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

import net.citizensnpcs.api.util.DataKey;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import javax.annotation.Nullable;

/*
 * 
 */
public class DataNodeKey extends DataKey {

    private IDataNode _dataNode;
    private DataNodeStorage _storage;

    public DataNodeKey(String path, IDataNode dataNode, @Nullable DataNodeStorage storage) {
        super(path);

        _dataNode = dataNode;
        _storage = storage;
    }

    public IDataNode getDataNode() {
        return _dataNode;
    }

    public DataNodeStorage getStorage() {
        return _storage;
    }

    @Override
    public boolean getBoolean(String s) {
        return _dataNode.getBoolean(s);
    }

    @Override
    public double getDouble(String s) {
        return _dataNode.getDouble(s);
    }

    @Override
    public int getInt(String s) {
        return _dataNode.getInteger(s);
    }

    @Override
    public long getLong(String s) {
        return _dataNode.getLong(s);
    }

    @Override
    @Nullable
    public Object getRaw(String s) {
        return _dataNode.get(s);
    }

    @Override
    public DataNodeKey getRelative(String s) {

        String nodePath = s.isEmpty()
                ? _dataNode.getNodePath()
                : _dataNode.getNodePath() + '.' + s;

        return _storage != null
                ? _storage.getKey(nodePath)
                : new DataNodeKey(nodePath, _dataNode.getRoot().getNode(nodePath), null);
    }

    @Override
    @Nullable
    public String getString(String s) {
        return _dataNode.getString(s);
    }

    @Override
    public Iterable<DataKey> getSubKeys() {

        Collection<String> nodeNames = _dataNode.getSubNodeNames();

        List<DataKey> result = new ArrayList<>(nodeNames.size());

        for (String nodeName : nodeNames) {
            result.add(getRelative(nodeName));
        }
        return result;
    }

    @Override
    public Map<String, Object> getValuesDeep() {
        return _dataNode.getAllValues();
    }

    @Override
    public boolean keyExists(String s) {
        return _dataNode.hasNode(s);
    }

    @Override
    @Nullable
    public String name() {
        return _dataNode.getName();
    }

    @Override
    public void removeKey(String s) {
        _dataNode.remove(s);
    }

    @Override
    public void setBoolean(String s, boolean b) {
        _dataNode.set(s, b);
    }

    @Override
    public void setDouble(String s, double v) {
        _dataNode.set(s, v);
    }

    @Override
    public void setInt(String s, int i) {
        _dataNode.set(s, i);
    }

    @Override
    public void setLong(String s, long l) {
        _dataNode.set(s, l);
    }

    @Override
    public void setRaw(String s, Object o) {
        _dataNode.set(s, o);
    }

    @Override
    public void setString(String s, String s1) {
        _dataNode.set(s, s1);
    }
}
