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
import java.util.LinkedList;

/*
 * 
 */
public class SerialActions extends CompositeBehaviours<INpcAction>
        implements INpcAction {

    private LinkedList<BehaviourContainer<INpcAction>> _queue = new LinkedList<>();
    private BehaviourContainer<INpcAction> _current;

    /**
     * Constructor.
     *
     * @param actions  A collection of actions that will be composite.
     */
    public SerialActions(Npc npc, Collection<INpcAction> actions) {
        super(npc, actions);

        _queue.addAll(getBehaviours());
    }

    @Override
    public void reset(INpcState state) {
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
                _current = _queue.removeFirst();
            }
        }

        _current.run();
    }

    @Override
    public void pause(INpcState state) {

        if (_current != null)
            _current.getBehaviour().pause(state);
    }

    @Override
    protected BehaviourContainer<INpcAction> createContainer(INpcAction behaviour) {
        return new ActionContainer(getNpc(), behaviour);
    }
}