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
import com.jcwhatever.nucleus.providers.npc.ai.INpcBehaviour;
import com.jcwhatever.nucleus.providers.npc.ai.INpcState;
import com.jcwhatever.nucleus.utils.PreCon;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/*
 * 
 */
public abstract class CompositeBehaviours<T extends INpcBehaviour>
        implements INpcBehaviour {

    private final Npc _npc;

    private List<BehaviourContainer<T>> _behaviours;
    private Set<BehaviourContainer<T>> _canRun;

    /**
     * Constructor.
     *
     * @param behaviours  A collection of actions that will be composite.
     */
    public CompositeBehaviours(Npc npc, Collection<T> behaviours) {
        PreCon.notNull(npc);
        PreCon.notNull(behaviours);

        _npc = npc;
        _behaviours = new ArrayList<>(behaviours.size());

        for (T behaviour : behaviours) {
            _behaviours.add(createContainer(behaviour));
        }
    }

    protected abstract BehaviourContainer<T> createContainer(T behaviour);

    public Npc getNpc() {
        return _npc;
    }

    @Override
    public void reset(INpcState state) {

        for (BehaviourContainer<T> container : _behaviours) {
            container.reset(state);
        }
    }

    @Override
    public boolean canRun(INpcState state) {

        boolean canRun = false;

        if (_canRun == null) {
            _canRun = new HashSet<>(_behaviours.size());
        } else {
            _canRun.clear();
        }

        for (BehaviourContainer<T> container : _behaviours) {

            if (container.getBehaviour().canRun(state)) {
                _canRun.add(container);
                canRun = true;
            }
        }

        return canRun;
    }

    @Override
    public float getCost(INpcState state) {
        float cost = 0;

        if (_canRun == null)
            return 1.0f;

        for (BehaviourContainer<T> container : _behaviours) {

            if (_canRun.contains(container)) {
                cost += container.getBehaviour().getCost(state);
            }
        }

        return Math.max(1.0f, cost);
    }

    protected List<BehaviourContainer<T>> getBehaviours() {
        return _behaviours;
    }
}
