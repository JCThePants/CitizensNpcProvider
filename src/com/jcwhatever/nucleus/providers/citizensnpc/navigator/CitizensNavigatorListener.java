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

package com.jcwhatever.nucleus.providers.citizensnpc.navigator;

import com.jcwhatever.nucleus.providers.citizensnpc.CitizensProvider;
import com.jcwhatever.nucleus.providers.citizensnpc.Npc;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

import net.citizensnpcs.api.ai.event.NavigationBeginEvent;
import net.citizensnpcs.api.ai.event.NavigationCancelEvent;
import net.citizensnpcs.api.ai.event.NavigationCompleteEvent;
import net.citizensnpcs.api.ai.event.NavigationStuckEvent;
import net.citizensnpcs.api.npc.NPC;

public class CitizensNavigatorListener implements Listener {

    @EventHandler
    private void onNavStart(NavigationBeginEvent event) {
        NPC handle = event.getNPC();

        Npc npc = CitizensProvider.getInstance().getNpc(handle);
        if (npc == null)
            return;

        ((NpcNavigator)npc.getNavigator()).onStart();
    }

    @EventHandler
    private void onNavCancel(NavigationCancelEvent event) {
        NPC handle = event.getNPC();

        Npc npc = CitizensProvider.getInstance().getNpc(handle);
        if (npc == null)
            return;

        ((NpcNavigator)npc.getNavigator()).onCancel();
    }

    @EventHandler
    private void onNavComplete(NavigationCompleteEvent event) {
        NPC handle = event.getNPC();

        Npc npc = CitizensProvider.getInstance().getNpc(handle);
        if (npc == null)
            return;

        ((NpcNavigator)npc.getNavigator()).onComplete();
    }

    @EventHandler
    private void onNavTimeout(NavigationStuckEvent event) {
        NPC handle = event.getNPC();

        Npc npc = CitizensProvider.getInstance().getNpc(handle);
        if (npc == null)
            return;

        ((NpcNavigator)npc.getNavigator()).onTimeout();
    }
}
