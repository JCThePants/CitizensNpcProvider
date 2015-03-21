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

import org.bukkit.entity.Horse;
import org.bukkit.entity.Horse.Color;
import org.bukkit.entity.Horse.Style;
import org.bukkit.entity.Horse.Variant;
import org.bukkit.inventory.ItemStack;

import net.citizensnpcs.api.npc.NPC;
import net.citizensnpcs.api.persistence.Persist;
import net.citizensnpcs.trait.HorseModifiers;

/**
 * Replaces Citizens {@link net.citizensnpcs.trait.HorseModifiers} internal trait.
 *
 * <p>Prevents execution of trait when attached to a provider NPC. Allows normal
 * execution when attached to a Citizens NPC.</p>
 */
public class ReplacedHorseModifiers extends HorseModifiers {

    // replicate persisted fields from superclass
    @Persist("armor") private ItemStack armor = null;
    @Persist("carryingChest") private boolean carryingChest;
    @Persist("color") private Color color = Color.CREAMY;
    @Persist("saddle") private ItemStack saddle;
    @Persist("style") private Style style = Style.NONE;
    @Persist("type") private Variant type = Variant.HORSE;

    private boolean _isCitizensNPC;

    @Override
    public boolean isRunImplemented() {
        return _isCitizensNPC;
    }

    @Override
    public void setCarryingChest(boolean carryingChest) {
        this.carryingChest = carryingChest;
        update();
    }

    @Override
    public Color getColor() {
        return this.color;
    }

    @Override
    public void setColor(Color color) {
        this.color = color;
        update();
    }

    @Override
    public Style getStyle() {
        return this.style;
    }

    @Override
    public void setStyle(Style style) {
        this.style = style;
        update();
    }

    @Override
    public Variant getType() {
        return this.type;
    }

    @Override
    public void setType(Variant type) {
        this.type = type;
        update();
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
    public void run() {
        if(!(npc.getEntity() instanceof Horse))
            return;

        Horse horse = (Horse)this.npc.getEntity();
        this.saddle = horse.getInventory().getSaddle();
        this.armor = horse.getInventory().getArmor();
    }

    private void update() {
        if(!(npc.getEntity() instanceof Horse))
            return;

        Horse horse = (Horse)npc.getEntity();
        horse.setCarryingChest(this.carryingChest);
        horse.setColor(this.color);
        horse.setStyle(this.style);
        horse.setVariant(this.type);
        horse.getInventory().setArmor(this.armor);
        horse.getInventory().setSaddle(this.saddle);
    }
}
