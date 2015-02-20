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
import com.jcwhatever.nucleus.providers.npc.ai.INpcBehaviour;
import com.jcwhatever.nucleus.providers.npc.ai.INpcBehaviourAgent;
import com.jcwhatever.nucleus.providers.npc.ai.INpcBehaviourPool;
import com.jcwhatever.nucleus.utils.PreCon;

import java.util.List;
import javax.annotation.Nullable;

/**
 * Abstract implementation of a behaviour pool/collection.
 *
 * <p>The behaviour pool is used to select 1 of possibly many behaviours
 * based on the cost of each behaviour or other factors determined by
 * the implementation.</p>
 */
public abstract class BehaviourPool<T extends INpcBehaviour, A extends INpcBehaviourAgent>
        implements INpcBehaviourPool<T> {

    private final Npc _npc;

    private BehaviourContainer<T, A> _currentOverride;
    private BehaviourContainer<T, A> _current;
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

    protected abstract List<? extends BehaviourContainer<T, A>> getPoolList();

    protected abstract List<? extends BehaviourContainer<T, A>> getFilteredPool();

    protected abstract void insertBehaviour(BehaviourContainer<T, A> container);

    protected abstract BehaviourContainer<T, A> createContainer(T behaviour, boolean forMatch);

    @Nullable
    protected abstract BehaviourContainer<T, A> getBehaviour(String name);

    protected abstract void onFinish();

    @Override
    public BehaviourPool<T, A> reset() {

        Msg.debug("[AI] [BEHAVIOUR_POOL] [NPC:{0}] reset", _npc.getName());

        setCurrent(null, true);

        for (BehaviourContainer<T, A> container : getPoolList()) {
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

        Msg.debug("[AI] [BEHAVIOUR_POOL] [NPC:{0}] add : ", _npc.getName(), behaviour.getName());

        insertBehaviour(createContainer(behaviour, false));

        return this;
    }

    @Nullable
    @Override
    public boolean remove(T behaviour) {
        PreCon.notNull(behaviour);

        Msg.debug("[AI] [BEHAVIOUR_POOL] [NPC:{0}] remove : {1}", _npc.getName(), behaviour.getName());

        BehaviourContainer<T, A> current = getCurrent();

        if (current != null && current.getBehaviour() == behaviour) {
            setCurrent(null, false);
            return true;
        }
        else {
            return getPoolList().remove(createContainer(behaviour, true));
        }
    }

    @Override
    public BehaviourPool<T, A> clear() {

        Msg.debug("[AI] [BEHAVIOUR_POOL] [NPC:{0}] clear", _npc.getName());

        getPoolList().clear();
        _current = null;

        return this;
    }

    @Override
    public BehaviourPool<T, A> run(T behaviour) {

        Msg.debug("[AI] [BEHAVIOUR_POOL] [NPC:{0}] run : {1}", _npc.getName(), behaviour.getName());

        _currentOverride = createContainer(behaviour, false);
        _currentOverride.reset(_npc);

        return this;
    }

    @Override
    public BehaviourPool<T, A> select(String behaviourName) {
        PreCon.notNull(behaviourName);

        Msg.debug("[AI] [BEHAVIOUR_POOL] [NPC:{0}] attempt select : {1}", _npc.getName(), behaviourName);

        BehaviourContainer<T, A> behaviour = getBehaviour(behaviourName);
        if (behaviour == null)
            return this;

        _currentOverride = behaviour;

        Msg.debug("[AI] [BEHAVIOUR_POOL] [NPC:{0}] select success : {1}", _npc.getName(), behaviourName);

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

        // run overriding behaviour, if any
        if (_currentOverride != null) {
            if (_currentOverride.isFinished()) {
                _currentOverride = null;
                return getCurrent() != null;
            }

            _currentOverride.run();

            return true;
        }

        BehaviourContainer<T, A> current = getCurrent();

        // remove current if finished
        if (current != null && current.isFinished()) {
            onFinish();
            return false;
        }

        List<? extends BehaviourContainer<T, A>> candidates = getFilteredPool();

        if (!candidates.isEmpty()) {

            BehaviourContainer<T, A> newGoal = null;

            if (candidates.size() == 1) {
                newGoal = candidates.get(0);
            }
            else if (candidates.size() != 0) {
                newGoal = getLowestCost(candidates);
            }

            if (newGoal != null) {

                assert !newGoal.equals(current);

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

    protected BehaviourContainer<T, A> getCurrent() {
        return _current;
    }

    protected void setCurrent(@Nullable BehaviourContainer<T, A> behaviour, boolean putBack) {

        Msg.debug("[AI] [BEHAVIOUR_POOL] [NPC:{0}] setCurrent : {1}",
                _npc.getName(), behaviour == null ? "<<none>>" : behaviour.getName());

        BehaviourContainer<T, A> current = getCurrent();

        assert current != behaviour;

        if (current != null) {

            // run pause
            current.pause(_npc);

            if (putBack)
                insertBehaviour(current);

            current.getAgent().setCurrent(false);
        }

        _current = behaviour;
        _currentCost = behaviour != null ? behaviour.getCost(_npc) : 0;

        if (behaviour != null) {
            getPoolList().remove(behaviour);
            behaviour.getAgent().setCurrent(true);
        }
    }

    // Get the lowest cost behaviour
    protected BehaviourContainer<T, A> getLowestCost(List<? extends BehaviourContainer<T, A>> candidates) {

        float cost = _current != null ? _currentCost : Float.MAX_VALUE;
        BehaviourContainer<T, A> result = null;

        for (BehaviourContainer<T, A> behaviour : candidates) {

            float candidateCost = behaviour.getCost(_npc);

            if (candidateCost < cost) {
                cost = candidateCost;
                result = behaviour;
            }
        }
        return result;
    }
}
