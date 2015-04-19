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

import com.jcwhatever.nucleus.providers.npc.traits.NpcTrait;
import com.jcwhatever.nucleus.providers.npc.traits.NpcTraitType;
import com.jcwhatever.nucleus.utils.PreCon;
import com.jcwhatever.nucleus.utils.performance.pool.IPoolElementFactory;
import com.jcwhatever.nucleus.utils.performance.pool.SimplePool;

import java.util.Map;
import java.util.WeakHashMap;
import javax.annotation.Nullable;

/**
 * Allows reusing trait instances to prevent buildup of long lived objects in heap
 * due to the transient nature of script generated NPC's.
 */
public class TraitPool {

    private static IPoolElementFactory<NpcTrait> ELEMENT_FACTORY = new IPoolElementFactory<NpcTrait>() {
        @Override
        public NpcTrait create() {
            throw new UnsupportedOperationException();
        }
    };

    private Map<NpcTraitType, SimplePool<NpcTrait>> _pools = new WeakHashMap<>(35);

    /**
     * Get a pooled trait.
     *
     * @param type  The type of trait to get from the pool.
     *
     * @return  A trait removed from the pool or null if the pool is empty.
     */
    @Nullable
    public NpcTrait getPooled(NpcTraitType type) {
        PreCon.notNull(type);

        SimplePool<NpcTrait> pooled = _pools.get(type);
        if (pooled == null || pooled.size() == 0)
            return null;

        return pooled.retrieve();
    }

    /**
     * Add a trait to the pool.
     *
     * @param trait  The unused trait to add.
     *
     * @return  True if the trait is added, false if rejected.
     */
    public boolean pool(NpcTrait trait) {
        PreCon.notNull(trait);

        if (!trait.isReusable())
            return false;

        SimplePool<NpcTrait> pooled = _pools.get(trait.getType());
        if (pooled == null) {
            pooled = new SimplePool<NpcTrait>(25, ELEMENT_FACTORY);
            _pools.put(trait.getType(), pooled);
        }

        if (!pooled.contains(trait))
            pooled.recycle(trait);

        return true;
    }
}
