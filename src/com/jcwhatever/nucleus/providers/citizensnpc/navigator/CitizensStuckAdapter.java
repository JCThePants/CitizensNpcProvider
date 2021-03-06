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
import com.jcwhatever.nucleus.providers.npc.INpc;
import com.jcwhatever.nucleus.providers.npc.navigator.INpcNavTimeout;

import net.citizensnpcs.api.ai.Navigator;
import net.citizensnpcs.api.ai.StuckAction;
import net.citizensnpcs.api.npc.NPC;

import javax.annotation.Nullable;

/**
 * Adapts Nucleus path timeout handlers to Citizens {@link net.citizensnpcs.api.ai.StuckAction}.
 */
public class CitizensStuckAdapter implements StuckAction {

    private StuckAction _defaultAction;
    private INpcNavTimeout _action;

    /**
     * Constructor.
     *
     * @param defaultAction  The default stuck action. Null for none.
     */
    public CitizensStuckAdapter(@Nullable StuckAction defaultAction) {
        _defaultAction = defaultAction;
    }

    /**
     * Get the {@link INpcNavTimeout}.
     */
    @Nullable
    public INpcNavTimeout getTimeoutHandler() {
        return _action;
    }

    /**
     * Set the {@link INpcNavTimeout}.
     *
     * @param timeout  The timeout handler.
     */
    public void setTimeoutHandler(@Nullable INpcNavTimeout timeout) {
        _action = timeout;
    }

    @Override
    public boolean run(NPC handle, Navigator navigator) {

        INpc npc = CitizensProvider.getInstance().getNpc(handle);
        if (npc == null || _action == null) {
            return _defaultAction != null && _defaultAction.run(handle, navigator);
        }

        return !_action.shouldCancel(npc);
    }
}
