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

import com.jcwhatever.nucleus.mixins.IDisposable;
import com.jcwhatever.nucleus.providers.citizensnpc.Msg;
import com.jcwhatever.nucleus.providers.citizensnpc.Npc;
import com.jcwhatever.nucleus.providers.npc.ai.actions.INpcAction;
import com.jcwhatever.nucleus.providers.npc.ai.actions.INpcActionAgent;
import com.jcwhatever.nucleus.providers.npc.ai.goals.INpcGoal;
import com.jcwhatever.nucleus.providers.npc.ai.goals.INpcGoalAgent;
import com.jcwhatever.nucleus.providers.npc.ai.goals.INpcGoalPriority;
import com.jcwhatever.nucleus.providers.npc.ai.goals.INpcGoals;
import com.jcwhatever.nucleus.utils.PreCon;

import java.util.ArrayList;
import java.util.List;
import java.util.ListIterator;
import javax.annotation.Nullable;

/**
 * Implementation of {@link INpcGoals}.
 *
 * <p>Main goal pool for an NPC.</p>
 */
public class NpcGoals extends BehaviourPool<INpcGoal, INpcGoalAgent>
        implements INpcGoals, IDisposable {

    private final Npc _npc;
    private final List<GoalContainer> _candidates = new ArrayList<>(5);

    private List<GoalContainer> _filter;
    private boolean _isDisposed;

    /**
     * Constructor.
     *
     * @param npc  The {@link Npc} the goal manager is for.
     */
    public NpcGoals(Npc npc) {
        super(npc);
        _npc = npc;
    }

    /**
     * Initialize or re-initialize.
     */
    public void init() {
        _isDisposed = false;
    }

    @Override
    public INpcGoals add(int priority, INpcGoal goal) {
        PreCon.greaterThanZero(priority, "priority");
        PreCon.notNull(goal, "goal");

        checkDisposed();

        return add(new StaticGoalPriority(priority), goal);
    }

    @Override
    public INpcGoals add(INpcGoalPriority priority, INpcGoal goal) {
        PreCon.notNull(priority, "priority");
        PreCon.notNull(goal, "goal");

        checkDisposed();

        Msg.debug("[AI] [GOALS] [NPC:{0}] add : {1}", getNpc().getLookupName(), goal.getName());

        GoalContainer container = new GoalContainer(priority, goal, this);
        insertBehaviour(container);

        return this;
    }

    @Override
    public INpcGoals add(INpcGoal goal) {
        PreCon.notNull(goal, "goal");

        return add(1, goal);
    }

    @Override
    public INpcGoals pause() {

        Msg.debug("[AI] [GOALS] [NPC:{0}] pause", getNpc().getLookupName());

        setRunning(false);
        return this;
    }

    @Override
    public INpcGoals resume() {

        checkDisposed();

        Msg.debug("[AI] [GOALS] [NPC:{0}] resume", getNpc().getLookupName());

        setRunning(true);
        return this;
    }

    @Override
    public boolean isDisposed() {
        return _isDisposed;
    }

    @Override
    public void dispose() {

        if (!getNpc().isDisposed())
            throw new IllegalStateException("NpcGoals can only be disposed after its parent NPC is disposed.");

        if (_isDisposed)
            return;

        _isDisposed = true;

        pause();
        setCurrent(null, false);
        _candidates.clear();
        _filter = null;
    }

    /**
     * Create a new agent for a GoalContainer.
     */
    GoalAgent createAgent(GoalContainer container) {
        return new GoalAgent(container);
    }

    @Override
    protected GoalContainer createContainer(INpcGoal behaviour, boolean forMatch) {
        return forMatch ? new GoalContainer(behaviour) : new GoalContainer(behaviour, this);
    }

    @Override
    @Nullable
    protected BehaviourContainer<INpcGoal, INpcGoalAgent> getBehaviour(String name) {
        for (GoalContainer goal : _candidates) {
            if (goal.getName().equals(name))
                return goal;
        }
        return null;
    }

    @Override
    protected void onFinish() {
        GoalContainer goal = getCurrent();
        if (goal != null) {
            goal.reset(_npc);
        }
        setCurrent(null, true);
    }

    @Override
    protected GoalContainer getCurrent() {
        return (GoalContainer)super.getCurrent();
    }

    @Override
    protected void insertBehaviour(BehaviourContainer<INpcGoal, INpcGoalAgent> behaviour) {

        GoalContainer goal = (GoalContainer)behaviour;

        ListIterator<GoalContainer> iterator = _candidates.listIterator();

        // insert goal into the correct index in the list. Sorted from highest priority to lowest.
        while (true) {

            if (iterator.hasNext()) {

                GoalContainer container = iterator.next();

                int currPriority = container.getPriority();

                if (goal.getPriority() >= currPriority) {
                    iterator.add(goal);
                    break;
                }
            }
            else {
                iterator.add(goal);
                break;
            }
        }
    }

    @Override
    protected List<? extends BehaviourContainer<INpcGoal, INpcGoalAgent>> getPoolList() {
        return _candidates;
    }

    @Override
    protected List<? extends BehaviourContainer<INpcGoal, INpcGoalAgent>> getFilteredPool() {

        if (_filter == null)
            _filter = new ArrayList<>(5);
        else
            _filter.clear();

        int priority = 0;
        int currentPriority = getCurrent() != null
                ? getCurrent().getPriority()
                : 0;

        // get highest priority candidates.
        for (GoalContainer goal : _candidates) {

            if (!goal.canRun(getNpc()))
                continue;

            int candidatePriority = goal.getPriority();

            // skip goals with less priority than current goal
            if (candidatePriority < currentPriority)
                continue;

            // clear lower priority results.
            if (candidatePriority > priority)
                _filter.clear();

            // add candidate with priority equal to or greater than
            // largest priority found so far.
            _filter.add(goal);
            priority = candidatePriority;
        }

        return _filter;
    }

    public class GoalAgent
            extends BehaviourAgent<INpcGoal, INpcAction, INpcGoalAgent, INpcActionAgent>
            implements INpcGoalAgent {

        private ActionPool actions;

        GoalAgent (GoalContainer container) {
            super(_npc, container);

            actions = new ActionPool(getNpc());
        }

        @Override
        public ActionPool getPool() {
            return actions;
        }

        @Override
        public void finishAndRemove() {

            if (_isDisposed)
                return;

            GoalContainer container = (GoalContainer)getContainer();

            _candidates.remove(container);
            if (NpcGoals.this.getCurrent() == container)
                NpcGoals.this.setCurrent(null, false);

            finish();
        }
    }

    private void checkDisposed() {
        if (_isDisposed)
            throw new IllegalStateException("Cannot use disposed NpcGoals.");
    }
}
