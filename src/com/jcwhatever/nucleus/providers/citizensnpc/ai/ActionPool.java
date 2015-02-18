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
import com.jcwhatever.nucleus.providers.npc.ai.actions.INpcActionPool;

import java.util.ArrayList;
import java.util.List;

/*
 * 
 */
public class ActionPool extends BehaviourPool<INpcAction> implements INpcActionPool {

    private final List<ActionContainer> _pool = new ArrayList<>(5);
    private List<ActionContainer> _filter;

    ActionPool(Npc npc) {
        super(npc);
    }

    @Override
    protected List<? extends BehaviourContainer<INpcAction>> getPoolList() {
        return _pool;
    }

    @Override
    protected List<? extends BehaviourContainer<INpcAction>> getFilteredPool() {

        if (_filter == null)
            _filter = new ArrayList<>(_pool.size());

        _filter.clear();

        for (ActionContainer action : _pool) {

            if (!action.canRun(getNpc()))
                continue;

            _filter.add(action);
        }

        return _filter;
    }

    @Override
    protected void insertBehaviour(BehaviourContainer<INpcAction> behaviour) {
        _pool.add((ActionContainer)behaviour);
    }

    @Override
    protected BehaviourContainer<INpcAction> createContainer(INpcAction behaviour, boolean forMatch) {
        return forMatch
                ? new ActionContainer(behaviour)
                : new ActionContainer(getNpc(), behaviour);
    }

    @Override
    protected void onFinish() {
        reset();
    }
}
