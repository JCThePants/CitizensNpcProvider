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

import net.citizensnpcs.Citizens;
import net.citizensnpcs.api.CitizensAPI;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;

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

        Field traitFactoryField = Citizens.class.getDeclaredField("traitFactory");
        traitFactoryField.setAccessible(true);
        removeFinalModifier(traitFactoryField);

        traitFactoryField.set(CitizensAPI.getPlugin(), new ReplacedTraitFactory());
    }

    private static void removeFinalModifier(Field field) throws NoSuchFieldException, IllegalAccessException {
        Field modifiersField = Field.class.getDeclaredField("modifiers");
        modifiersField.setAccessible(true);
        modifiersField.setInt(field, field.getModifiers() & ~Modifier.FINAL);
    }
}
