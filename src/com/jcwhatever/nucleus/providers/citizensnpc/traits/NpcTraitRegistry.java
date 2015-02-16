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

import com.jcwhatever.nucleus.providers.npc.traits.INpcTraitTypeRegistry;
import com.jcwhatever.nucleus.providers.npc.traits.NpcTraitType;
import com.jcwhatever.nucleus.utils.PreCon;

import java.util.HashMap;
import java.util.Map;
import javax.annotation.Nullable;

/**
 * Implementation of {@link com.jcwhatever.nucleus.providers.npc.traits.INpcTraitTypeRegistry}.
 */
public class NpcTraitRegistry implements INpcTraitTypeRegistry {

    private final NpcTraitRegistry _parent;
    private final Map<String, NpcTraitType> _typeMap = new HashMap<>(10);

    /**
     * Constructor.
     *
     * @param parent  The parent registry. Null for root.
     */
    public NpcTraitRegistry(@Nullable NpcTraitRegistry parent) {
        _parent = parent;
    }

    @Override
    public INpcTraitTypeRegistry registerTrait(NpcTraitType traitType) {
        PreCon.notNull(traitType);

        _typeMap.put(traitType.getLookupName(), traitType);

        return this;
    }

    @Override
    public boolean isTraitRegistered(String name) {
        PreCon.notNull(name);

        return _typeMap.containsKey(name.toLowerCase()) ||
                (_parent != null && _parent.isTraitRegistered(name));
    }

    @Nullable
    @Override
    public NpcTraitType getTraitType(String name) {
        PreCon.notNull(name);

        NpcTraitType result = null;
        NpcTraitRegistry registry = this;

        while (registry != null && (result = registry._typeMap.get(name)) == null) {
            registry = registry._parent;
        }

        return result;
    }
}
