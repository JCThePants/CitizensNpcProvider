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

import net.citizensnpcs.api.npc.NPC;
import net.citizensnpcs.api.trait.Trait;
import net.citizensnpcs.api.trait.TraitInfo;
import net.citizensnpcs.npc.CitizensTraitFactory;
import net.citizensnpcs.trait.Age;
import net.citizensnpcs.trait.Gravity;
import net.citizensnpcs.trait.HorseModifiers;
import net.citizensnpcs.trait.LookClose;
import net.citizensnpcs.trait.NPCSkeletonType;
import net.citizensnpcs.trait.OcelotModifiers;
import net.citizensnpcs.trait.Poses;
import net.citizensnpcs.trait.Powered;
import net.citizensnpcs.trait.RabbitType;
import net.citizensnpcs.trait.Saddle;
import net.citizensnpcs.trait.SheepTrait;
import net.citizensnpcs.trait.SlimeSize;
import net.citizensnpcs.trait.VillagerProfession;
import net.citizensnpcs.trait.WolfModifiers;
import net.citizensnpcs.trait.WoolColor;
import net.citizensnpcs.trait.ZombieModifier;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Replaces the {@link net.citizensnpcs.npc.CitizensTraitFactory} to ensure replaced traits
 * are returned.
 */
public class ReplacedTraitFactory extends CitizensTraitFactory {

    private Map<String, TraitInfo> _nameMap = new HashMap<>(20);
    private Map<Class<?>, TraitInfo> _classMap = new HashMap<>(20);

    private List<TraitInfo> _defaultTraits;

    public ReplacedTraitFactory() {
        super();

        try {
            setup();
        } catch (NoSuchFieldException | IllegalAccessException | NoSuchMethodException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void addDefaultTraits(NPC npc) {
        for (TraitInfo info : _defaultTraits) {
            TraitInfo replaced = _nameMap.get(info.getTraitName());
            if (replaced == null)
                npc.addTrait(info.tryCreateInstance());
            else
                npc.addTrait(replaced.tryCreateInstance());
        }
    }

    @Override
    public <T extends Trait> T getTrait(Class<T> clazz) {
        TraitInfo trait = _classMap.get(clazz);
        if (trait != null)
            return trait.tryCreateInstance();

        return super.getTrait(clazz);
    }

    @Override
    public Class<? extends Trait> getTraitClass(String name) {
        TraitInfo info = _nameMap.get(name.toLowerCase());
        if (info != null)
            return info.getTraitClass();

        return super.getTraitClass(name);
    }

    /*
     * Replace Citizens internal traits with provider safe versions.
     */
    private void setup() throws NoSuchFieldException, IllegalAccessException, NoSuchMethodException {
        replace(Age.class, TraitInfo.create(ReplacedAge.class).withName("age"));
        replace(Gravity.class, TraitInfo.create(ReplacedGravity.class).withName("gravity"));
        replace(HorseModifiers.class, TraitInfo.create(ReplacedHorseModifiers.class).withName("horsemodifiers"));
        replace(LookClose.class, TraitInfo.create(ReplacedLookClose.class).withName("lookclose"));
        replace(NPCSkeletonType.class, TraitInfo.create(ReplacedNPCSkeletonType.class).withName("skeletontype"));
        replace(OcelotModifiers.class, TraitInfo.create(ReplacedOcelotModifiers.class).withName("ocelotmodifiers"));
        replace(Poses.class, TraitInfo.create(ReplacedPoses.class).withName("poses"));
        replace(Powered.class, TraitInfo.create(ReplacedPowered.class).withName("powered"));
        replace(RabbitType.class, TraitInfo.create(ReplacedRabbitType.class).withName("rabbittype"));
        replace(Saddle.class, TraitInfo.create(ReplacedSaddle.class).withName("saddle"));
        replace(SheepTrait.class, TraitInfo.create(ReplacedSheepTrait.class).withName("sheeptrait"));
        replace(SlimeSize.class, TraitInfo.create(ReplacedSlimeSize.class).withName("slimesize"));
        replace(VillagerProfession.class, TraitInfo.create(ReplacedVillagerProfession.class).withName("profession"));
        replace(WolfModifiers.class, TraitInfo.create(ReplacedWolfModifiers.class).withName("wolfmodifiers"));
        replace(WoolColor.class, TraitInfo.create(ReplacedWoolColor.class).withName("woolcolor"));
        replace(ZombieModifier.class, TraitInfo.create(ReplacedZombieModifier.class).withName("zombiemodifier"));

        Field defaultTraitsField = CitizensTraitFactory.class.getDeclaredField("defaultTraits");
        defaultTraitsField.setAccessible(true);
        removeFinalModifier(defaultTraitsField);

        @SuppressWarnings("unchecked")
        List<TraitInfo> defaultTraits = (List<TraitInfo>) defaultTraitsField.get(this);

        _defaultTraits = defaultTraits;
    }

    private void replace(Class<?> traitClass, TraitInfo replacement) {
        _nameMap.put(replacement.getTraitName(), replacement);
        _classMap.put(traitClass, replacement);
    }

    private static void removeFinalModifier(Field field) throws NoSuchFieldException, IllegalAccessException {
        Field modifiersField = Field.class.getDeclaredField("modifiers");
        modifiersField.setAccessible(true);
        modifiersField.setInt(field, field.getModifiers() & ~Modifier.FINAL);
    }
}
