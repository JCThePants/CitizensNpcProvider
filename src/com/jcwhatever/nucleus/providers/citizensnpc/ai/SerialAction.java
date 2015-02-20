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

import java.util.Collection;
import java.util.LinkedList;

/**
 * A composite of {@link INpcAction} that run in serial order.
 *
 * <p>Each action is run until it finishes. When an action is finished, the next action
 * is run. The composite is not finished until all actions have completed.</p>
 *
 * <p>Actions that cannot run are skipped.</p>
 */
public class SerialAction extends CompositeBehaviour<INpcAction, INpcActionAgent>
        implements INpcAction {

    private LinkedList<BehaviourContainer<INpcAction, INpcActionAgent>> _queue = new LinkedList<>();
    private BehaviourContainer<INpcAction, INpcActionAgent> _current;

    /**
     * Constructor.
     *
     * @param npc      The owning NPC.
     * @param name     The name of the action.
     * @param actions  A collection of actions that will be composite.
     */
    public SerialAction(Npc npc, String name, Collection<INpcAction> actions) {
        super(npc, name, actions);

        _queue.addAll(getBehaviours());
    }

    @Override
    public void reset(INpcState state) {

        Msg.debug("[AI] [SERIAL_ACTION] [NPC:{0}] [{1}] reset", getNpc().getName(), getName());

        _queue.clear();
        _queue.addAll(getBehaviours());
        _current = null;
        super.reset(state);
    }

    /**
     * Actions in the composite are run in order they are given, and only if the action
     * returns true when {@link #canRun} method is invoked.
     *
     * <p>{@inheritDoc}</p>
     */
    @Override
    public void run(INpcActionAgent agent) {

        if (_current == null || _current.getAgent().isFinished()) {

            if (_queue.isEmpty()) {
                agent.finish();
                return;
            } else {

                if (_current != null)
                    _current.getAgent().setCurrent(false);

                _current = _queue.removeFirst();
                _current.getAgent().setCurrent(true);
            }
        }

        _current.run();
    }

    @Override
    public void pause(INpcState state) {

        Msg.debug("[AI] [SERIAL_ACTION] [NPC:{0}] [{1}] pause",
                getNpc().getName(), getName());

        if (_current != null)
            _current.getBehaviour().pause(state);
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