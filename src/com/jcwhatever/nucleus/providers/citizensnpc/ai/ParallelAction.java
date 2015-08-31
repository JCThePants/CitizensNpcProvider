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
import com.jcwhatever.nucleus.providers.npc.ai.INpcState;
import com.jcwhatever.nucleus.providers.npc.ai.actions.INpcAction;
import com.jcwhatever.nucleus.providers.npc.ai.actions.INpcActionAgent;

import java.util.Collection;

/**
 * A composite of {@link INpcAction} that run in parallel.
 *
 * <p>The composite is not finished until all child actions are finished.</p>
 */
public class ParallelAction
        extends CompositeBehaviour<INpcAction, INpcActionAgent>
        implements INpcAction {

    /**
     * Constructor.
     *
     * @param npc      The owning NPC.
     * @param name     The name of the action.
     * @param actions  A collection of actions that will be run in parallel.
     */
    public ParallelAction(Npc npc, String name, Collection<INpcAction> actions) {
        super(npc, name, actions);
    }

    /**
     * Actions in the composite are run in order they are given, and only if the action
     * returns true when {@link #canRun} method is invoked.
     *
     * <p>{@inheritDoc}</p>
     */
    @Override
    public void run(INpcActionAgent agent) {

        int finishCount = 0;

        for (BehaviourContainer<INpcAction, INpcActionAgent> container : getBehaviours()) {

            if (container.getAgent().isFinished()) {
                container.getAgent().setCurrent(false);
                finishCount++;
                continue;
            }

            if (container.canRun(getNpc())) {
                container.getAgent().setCurrent(true);
                container.run();
            }
            else {
                finishCount++;
            }
        }

        if (finishCount == getBehaviours().size()) {
            agent.finish();
        }
    }

    @Override
    public void pause(INpcState state) {
        for (BehaviourContainer<INpcAction, INpcActionAgent> container : getBehaviours()) {

            if (!container.getAgent().isFinished()) {
                container.getBehaviour().pause(state);
            }
        }
    }

    @Override
    public void firstRun(INpcActionAgent agent) {
        // do nothing
    }

    @Override
    protected BehaviourContainer<INpcAction, INpcActionAgent> createContainer(INpcAction behaviour) {
        return new ActionContainer(getNpc(), behaviour);
    }
}

