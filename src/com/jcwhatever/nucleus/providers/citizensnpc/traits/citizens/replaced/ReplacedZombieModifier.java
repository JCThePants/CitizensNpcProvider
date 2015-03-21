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

import org.bukkit.entity.Zombie;

import net.citizensnpcs.api.npc.NPC;
import net.citizensnpcs.api.persistence.Persist;
import net.citizensnpcs.trait.ZombieModifier;

/**
 * Replaces Citizens {@link net.citizensnpcs.trait.ZombieModifier} internal trait.
 *
 * <p>Prevents execution of trait when attached to a provider NPC. Allows normal
 * execution when attached to a Citizens NPC.</p>
 */
public class ReplacedZombieModifier extends ZombieModifier {

    // replicate persisted fields from superclass
    @Persist private boolean baby;
    @Persist private boolean villager;

    private boolean _isCitizensNPC;

    @Override
    public boolean isRunImplemented() {
        return false;
    }

    @Override
    public boolean toggleBaby() {
        this.baby = !this.baby;
        update();
        return this.baby;
    }

    @Override
    public boolean toggleVillager() {
        this.villager = !this.villager;
        update();
        return this.villager;
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

    private void update() {
        if (!(npc.getEntity() instanceof Zombie))
            return;

        Zombie zombie = (Zombie)npc.getEntity();
        zombie.setVillager(this.villager);
        zombie.setBaby(this.baby);
    }
}
