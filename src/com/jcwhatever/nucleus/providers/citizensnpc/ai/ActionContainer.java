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

import com.jcwhatever.nucleus.providers.citizensnpc.Msg;
import com.jcwhatever.nucleus.providers.citizensnpc.Npc;
import com.jcwhatever.nucleus.providers.npc.ai.INpcState;
import com.jcwhatever.nucleus.providers.npc.ai.actions.INpcAction;
import com.jcwhatever.nucleus.providers.npc.ai.actions.INpcActionAgent;

/**
 * Container for an {@link INpcAction}.
 *
 * <p>Holds extra objects related to the action.</p>
 */
public class ActionContainer extends BehaviourContainer<INpcAction> implements INpcAction {

    private final ActionAgent _agent;

    /**
     * Constructor.
     *
     * <p>Used for matching objects only.</p>
     *
     * @param action  The action to wrap.
     */
    ActionContainer(INpcAction action) {
        super(action);
        _agent = null;
    }

    /**
     * Constructor.
     *
     * @param npc     The owning {@link Npc}.
     * @param action  The action to wrap.
     */
    ActionContainer(Npc npc, INpcAction action) {
        super(action);

        _agent = new ActionAgent(npc, this);
    }

    /**
     * Get the wrapped action.
     */
    public INpcAction getAction() {
        return getBehaviour();
    }

    @Override
    public ActionAgent getAgent() {
        return _agent;
    }

    /**
     * Unsupported. Use {@link #run}.
     */
    @Override
    public void firstRun(INpcActionAgent agent) {
        throw new UnsupportedOperationException();
    }

    /**
     * Not supported. Use {@link #run()}.
     */
    @Override
    public void run(INpcActionAgent agent) {
        throw new UnsupportedOperationException("Incorrect use of ActionContainer.");
    }

    @Override
    public void run() {

        // only run action if no child actions were run.
        if (!_agent.runPool()) {

            if (_agent.isFirstRun()) {
                getAction().firstRun(_agent);
            }
            getAction().run(_agent);
        }
    }

    @Override
    public boolean canRun(INpcState state) {
        boolean result = getAction().canRun(state);

        Msg.debug("[AI] [ACTION_CONTAINER] [NPC:{0}] [{1}] canRun = {2}",
                _agent.getNpc().getName(), getName(), result);

        return result;
    }

    @Override
    public float getCost(INpcState state) {
        float result = getAction().getCost(state);

        Msg.debug("[AI] [ACTION_CONTAINER] [NPC:{0}] [{1}] getCost = {2}",
                _agent.getNpc().getName(), getName(), result);

        return result;
    }

    @Override
    public void pause(INpcState state) {

        Msg.debug("[AI] [GOAL_CONTAINER] [NPC:{0}] [{1}] pause",
                _agent.getNpc().getName(), getName());

        getAction().pause(state);
    }
}

