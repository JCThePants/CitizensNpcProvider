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
import com.jcwhatever.nucleus.providers.npc.ai.INpcBehaviourPool;
import com.jcwhatever.nucleus.utils.PreCon;

import java.util.List;
import javax.annotation.Nullable;

/*
 * 
 */
public abstract class BehaviourPool<T extends INpcBehaviour>
        implements INpcBehaviourPool<T> {

    private final Npc _npc;

    private BehaviourContainer<T> _current;
    private float _currentCost;
    private boolean _isRunning = true;

    /**
     * Constructor.
     *
     * @param npc  The {@link Npc} the goal manager is for.
     */
    public BehaviourPool(Npc npc) {
        _npc = npc;
    }

    protected abstract List<? extends BehaviourContainer<T>> getPoolList();

    protected abstract List<? extends BehaviourContainer<T>> getFilteredPool();

    protected abstract void insertBehaviour(BehaviourContainer<T> container);

    protected abstract BehaviourContainer<T> createContainer(T behaviour, boolean forMatch);

    protected abstract void onFinish();

    @Override
    public BehaviourPool<T> reset() {
        setCurrent(null, true);

        for (BehaviourContainer<T> container : getPoolList()) {
            container.pause(_npc);
            container.reset(_npc);
        }

        return this;
    }

    @Override
    public Npc getNpc() {
        return _npc;
    }

    @Override
    public INpcBehaviourPool add(T behaviour) {
        PreCon.notNull(behaviour);

        insertBehaviour(createContainer(behaviour, false));

        return this;
    }

    @Nullable
    @Override
    public boolean remove(T behaviour) {
        PreCon.notNull(behaviour);

        BehaviourContainer<T> current = getCurrent();

        if (current != null && current.getBehaviour() == behaviour) {
            setCurrent(null, false);
            return true;
        }
        else {
            return getPoolList().remove(createContainer(behaviour, true));
        }
    }

    @Override
    public BehaviourPool<T> clear() {

        getPoolList().clear();
        _current = null;

        return this;
    }

    public boolean isRunning() {
        return _isRunning;
    }

    public void setRunning(boolean isRunning) {
        _isRunning = isRunning;
    }

    /**
     * Select and run a behaviour from the pool.
     *
     * @return  True if a behaviour was run, otherwise false.
     */
    boolean run() {

        if (!_isRunning)
            return false;

        BehaviourContainer<T> current = getCurrent();

        // remove current if finished
        if (current != null && current.isFinished()) {
            onFinish();
            return false;
        }

        List<? extends BehaviourContainer<T>> candidates = getFilteredPool();

        if (!candidates.isEmpty()) {

            BehaviourContainer<T> newGoal = null;

            if (candidates.size() == 1) {
                newGoal = candidates.get(0);
            }
            else if (candidates.size() != 0) {
                newGoal = getLowestCost(candidates);
            }

            if (newGoal != null) {

                assert newGoal != current;

                // make new goal the current goal
                setCurrent(newGoal, true);
            }
        }

        current = getCurrent();

        // run current goal.
        if (current != null) {
            current.run();
            return true;
        }

        return false;
    }

    protected BehaviourContainer<T> getCurrent() {
        return _current;
    }

    protected void setCurrent(@Nullable BehaviourContainer<T> behaviour, boolean putBack) {

        BehaviourContainer<T> current = getCurrent();

        assert current != behaviour;

        if (current != null) {

            // run pause
            current.pause(_npc);

            if (putBack)
                insertBehaviour(current);
        }

        _current = behaviour;
        _currentCost = behaviour != null ? behaviour.getCost(_npc) : 0;

        if (behaviour != null)
            getPoolList().remove(behaviour);
    }

    // Get the lowest cost behaviour
    protected BehaviourContainer<T> getLowestCost(List<? extends BehaviourContainer<T>> candidates) {

        float cost = _current != null ? _currentCost : Float.MAX_VALUE;
        BehaviourContainer<T> result = null;

        for (BehaviourContainer<T> behaviour : candidates) {

            float candidateCost = behaviour.getCost(_npc);

            if (candidateCost < cost) {
                cost = candidateCost;
                result = behaviour;
            }
        }
        return result;
    }
}
