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
import com.jcwhatever.nucleus.providers.npc.INpc;
import com.jcwhatever.nucleus.providers.npc.traits.INpcTraits;
import com.jcwhatever.nucleus.providers.npc.traits.NpcTrait;
import com.jcwhatever.nucleus.utils.PreCon;

import org.bukkit.entity.EntityType;

import net.citizensnpcs.api.npc.NPC;
import net.citizensnpcs.api.trait.trait.MobType;

import java.util.Collection;
import javax.annotation.Nullable;

/*
 * 
 */
public class NpcTraits implements INpcTraits {

    private final Npc _npc;
    private final NPC _handle;
    private final CitizensTraitAdapter _adapter;

    public NpcTraits(Npc npc) {
        _npc = npc;
        _handle = npc.getHandle();

        _adapter = new CitizensTraitAdapter(npc);
        npc.getHandle().addTrait(_adapter);
    }

    @Override
    public INpc getNpc() {
        return _npc;
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

        MobType type = _handle.getTrait(MobType.class);
        return type.getType();
    }

    @Override
    public INpcTraits setType(EntityType type) {

        _handle.setBukkitEntityType(type);

        return this;
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
    public boolean remove(String name) {
        PreCon.notNull(name);

        return _adapter.remove(name);
    }
}
