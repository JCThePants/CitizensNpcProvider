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
import com.jcwhatever.nucleus.providers.npc.ai.goals.INpcGoal;
import com.jcwhatever.nucleus.providers.npc.ai.goals.INpcGoalAgent;
import com.jcwhatever.nucleus.providers.npc.ai.goals.INpcGoals;
import com.jcwhatever.nucleus.utils.PreCon;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;

/*
 * 
 */
public class NpcGoals extends BehaviourPool<INpcGoal> implements INpcGoals {

    private final Npc _npc;

    private List<GoalContainer> _candidates = new ArrayList<>(3);

    /**
     * Constructor.
     *
     * @param npc  The {@link Npc} the goal manager is for.
     */
    public NpcGoals(Npc npc) {
        super(npc);
        _npc = npc;
    }

    @Override
    public INpcGoals add(int priority, INpcGoal goal) {
        PreCon.greaterThanZero(priority, "priority");
        PreCon.notNull(goal, "goal");

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
        setRunning(false);
        return this;
    }

    @Override
    public INpcGoals resume() {
        setRunning(true);
        return this;
    }

    /**
     * Create a new agent for a GoalContainer.
     */
    GoalAgent createAgent(GoalContainer container) {
        return new GoalAgent(container);
    }

    @Override
    protected GoalContainer createContainer(INpcGoal behaviour, boolean forMatch) {
        return forMatch ? new GoalContainer(behaviour) : new GoalContainer(1, behaviour, this);
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
    protected void insertBehaviour(BehaviourContainer<INpcGoal> behaviour) {

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
    protected List<? extends BehaviourContainer<INpcGoal>> getPoolList() {
        return _candidates;
    }

    @Override
    protected List<? extends BehaviourContainer<INpcGoal>> getFilteredPool() {
        int priority = 0;
        LinkedList<GoalContainer> finalCandidates = new LinkedList<>();

        // get highest priority candidates.
        for (GoalContainer goal : _candidates) {

            int candidatePriority = goal.getPriority();
            int currentPriority = getCurrent() != null
                    ? getCurrent().getPriority()
                    : 0;

            // skip goals that cannot run
            if (candidatePriority < currentPriority || !goal.canRun(_npc))
                continue;

            // compare priority with current final candidates priority
            if (candidatePriority > priority || candidatePriority == priority) {

                // clear candidates and add higher priority candidate.
                finalCandidates.add(goal);
                priority = candidatePriority;
            }
            else {
                break;
            }
        }

        return finalCandidates;
    }

    public class GoalAgent extends BehaviourAgent<INpcGoal, INpcAction> implements INpcGoalAgent {

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
            GoalContainer container = (GoalContainer)getContainer();

            _candidates.remove(container);
            if (getCurrent() == container)
                setCurrent(null, false);

            finish();
        }
    }
}
