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

import org.bukkit.entity.Creeper;

import net.citizensnpcs.api.npc.NPC;
import net.citizensnpcs.api.persistence.Persist;
import net.citizensnpcs.trait.Powered;

/**
 * Replaces Citizens {@link net.citizensnpcs.trait.Powered} internal trait.
 *
 * <p>Prevents execution of trait when attached to a provider NPC. Allows normal
 * execution when attached to a Citizens NPC.</p>
 */
public class ReplacedPowered extends Powered {

    // replicate persisted fields from superclass
    @Persist("") private boolean powered;

    private boolean _isCitizensNPC;

    @Override
    public boolean isRunImplemented() {
        return false;
    }

    @Override
    public void onAttach() {

        NPC npc = getNPC();

        _isCitizensNPC =  CitizensProvider.getInstance().getNpc(npc) == null;
    }

    @Override
    public void onSpawn() {
        if (_isCitizensNPC)
            update();
    }

    @Override
    public boolean toggle() {
        this.powered = !this.powered;
        update();

        return this.powered;
    }

    public String toString() {
        return "Powered{" + this.powered + '}';
    }

    private void update() {
        if(!(this.npc.getEntity() instanceof Creeper))
            return;

        Creeper creeper = (Creeper)npc.getEntity();
        creeper.setPowered(this.powered);
    }
}
