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

package com.jcwhatever.nucleus.providers.citizensnpc.traits.citizens.replaced;

import com.jcwhatever.nucleus.providers.citizensnpc.CitizensProvider;

import org.bukkit.entity.Ageable;
import org.bukkit.entity.Entity;

import net.citizensnpcs.api.npc.NPC;
import net.citizensnpcs.api.persistence.Persist;
import net.citizensnpcs.trait.Age;

import javax.annotation.Nullable;

/**
 * Replaces Citizens {@link net.citizensnpcs.trait.Age} internal trait.
 *
 * <p>Prevents execution of trait when attached to a provider NPC. Allows normal
 * execution when attached to a Citizens NPC.</p>
 */
public class ReplacedAge extends Age {

    // replicate persisted fields from superclass
    @Persist private int age = 0;
    @Persist private boolean locked = true;

    private boolean _isCitizensNPC;

    @Override
    public boolean isRunImplemented() {
        return false;
    }

    @Override
    public void onAttach() {

        NPC npc = getNPC();

        _isCitizensNPC = CitizensProvider.getInstance() == null ||
                CitizensProvider.getInstance().getNpc(npc) == null;

        if (_isCitizensNPC)
            super.onAttach();
    }

    @Override
    public void onSpawn() {
        if (!_isCitizensNPC)
            return;

        Ageable ageable = ageable();

        if(ageable != null) {
            Ageable entity = (Ageable)npc.getEntity();
            entity.setAge(this.age);
            entity.setAgeLock(this.locked);
        }
    }

    @Override
    public void run() {
        // do nothing
    }

    @Override
    public void setAge(int age) {
        this.age = age;

        Ageable ageable = ageable();

        if(ageable != null)
            ageable.setAge(age);
    }

    @Override
    public boolean toggle() {
        this.locked = !this.locked;

        Ageable ageable = ageable();

        if(ageable != null)
            ageable.setAgeLock(this.locked);

        return this.locked;
    }

    @Nullable
    private Ageable ageable() {
        if (!npc.isSpawned())
            return null;

        Entity entity = npc.getEntity();

        if (entity instanceof Ageable)
            return (Ageable)entity;

        return null;
    }
}
