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

package com.jcwhatever.nucleus.providers.citizensnpc.ai;

import com.jcwhatever.nucleus.providers.citizensnpc.Npc;
import com.jcwhatever.nucleus.providers.npc.ai.actions.INpcAction;
import com.jcwhatever.nucleus.providers.npc.ai.actions.INpcActionAgent;

/**
 * Implementation of a {@link BehaviourAgent} for use with {@link INpcAction}'s.
 *
 * <p>The behaviour pool is used to select 1 of possibly many behaviours
 * based on the cost of each behaviour or other factors determined by
 * the implementation.</p>
 */
public class ActionAgent
        extends BehaviourAgent<INpcAction, INpcAction, INpcActionAgent, INpcActionAgent>
        implements INpcActionAgent {

    private ActionPool childPool;

    /**
     * Constructor.
     *
     * @param npc        The owning NPC.
     * @param container  The {@link ActionContainer} the behaviour is for.
     */
    ActionAgent (Npc npc, ActionContainer container) {
        super(npc, container);

        childPool = new ActionPool(npc);
    }

    @Override
    public BehaviourPool<INpcAction, INpcActionAgent> getPool() {
        return childPool;
    }
}
