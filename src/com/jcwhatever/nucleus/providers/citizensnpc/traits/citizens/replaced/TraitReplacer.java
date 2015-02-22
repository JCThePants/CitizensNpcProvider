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

import net.citizensnpcs.api.CitizensAPI;
import net.citizensnpcs.api.trait.TraitInfo;
import net.citizensnpcs.npc.CitizensTraitFactory;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.Map;

/**
 * Replaces Citizens internal traits that may interfere with the
 * providers external traits.
 */
public class TraitReplacer {

    private TraitReplacer() {}

    /**
     * Replace Citizens internal traits with provider safe versions.
     *
     * @throws NoSuchFieldException
     * @throws IllegalAccessException
     * @throws NoSuchMethodException
     */
    public static void replaceTraits() throws NoSuchFieldException, IllegalAccessException, NoSuchMethodException {

        Field registeredField = CitizensTraitFactory.class.getDeclaredField("registered");
        registeredField.setAccessible(true);
        removeFinalModifier(registeredField);

        @SuppressWarnings("unchecked")
        Map<String, TraitInfo> registered = (Map<String, TraitInfo>)registeredField.get(CitizensAPI.getTraitFactory());

        registered.put("age", TraitInfo.create(ReplacedAge.class).withName("age"));
        registered.put("gravity", TraitInfo.create(ReplacedGravity.class).withName("gravity"));
        registered.put("horsemodifiers", TraitInfo.create(ReplacedHorseModifiers.class).withName("horsemodifiers"));
        registered.put("lookclose", TraitInfo.create(ReplacedLookClose.class).withName("lookclose"));
        registered.put("skeletontype", TraitInfo.create(ReplacedNPCSkeletonType.class).withName("skeletontype"));
        registered.put("ocelotmodifiers", TraitInfo.create(ReplacedOcelotModifiers.class).withName("ocelotmodifiers"));
        registered.put("poses", TraitInfo.create(ReplacedPoses.class).withName("poses"));
        registered.put("powered", TraitInfo.create(ReplacedPowered.class).withName("powered"));
        registered.put("rabbittype", TraitInfo.create(ReplacedRabbitType.class).withName("rabbittype"));
        registered.put("saddle", TraitInfo.create(ReplacedSaddle.class).withName("saddle"));
        registered.put("sheeptrait", TraitInfo.create(ReplacedSheepTrait.class).withName("sheeptrait"));
        registered.put("slimesize", TraitInfo.create(ReplacedSlimeSize.class).withName("slimesize"));
        registered.put("profession", TraitInfo.create(ReplacedVillagerProfession.class).withName("profession"));
        registered.put("wolfmodifiers", TraitInfo.create(ReplacedWolfModifiers.class).withName("wolfmodifiers"));
        registered.put("woolcolor", TraitInfo.create(ReplacedWoolColor.class).withName("woolcolor"));
        registered.put("zombiemodifier", TraitInfo.create(ReplacedZombieModifier.class).withName("zombiemodifier"));
    }

    private static void removeFinalModifier(Field field) throws NoSuchFieldException, IllegalAccessException {
        Field modifiersField = Field.class.getDeclaredField("modifiers");
        modifiersField.setAccessible(true);
        modifiersField.setInt(field, field.getModifiers() & ~Modifier.FINAL);
    }
}
