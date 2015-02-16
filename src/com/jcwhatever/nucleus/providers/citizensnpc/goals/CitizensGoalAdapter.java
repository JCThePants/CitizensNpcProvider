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

package com.jcwhatever.nucleus.providers.citizensnpc.goals;

import com.jcwhatever.nucleus.providers.npc.ai.actions.INpcActionSelector;
import com.jcwhatever.nucleus.providers.npc.ai.goals.INpcGoal;

import net.citizensnpcs.api.ai.Goal;
import net.citizensnpcs.api.ai.GoalSelector;

/**
 * Adapts a Nucleus based goal to a Citizens based goal.
 */
public class CitizensGoalAdapter implements Goal, INpcGoal {

    private final NpcGoals _goals;
    private final INpcGoal _goal;
    private NpcActionSelector _selector;
    private boolean _isGoalFinished;

    /**
     * Constructor.
     *
     * @param goals  The {@link NpcGoals}.
     * @param goal   The {@link INpcGoal} to run.
     */
    public CitizensGoalAdapter(NpcGoals goals, INpcGoal goal) {
        _goals = goals;
        _goal = goal;
    }

    @Override
    public void reset() {
        _goal.reset();
        _selector = null;
    }

    @Override
    public void run(INpcActionSelector selector) {
        _goal.run(selector);
    }

    @Override
    public boolean canRun() {
        return _goal.canRun();
    }

    @Override
    public void run(GoalSelector goalSelector) {
        _goals.pushSelector(goalSelector);

        if (_selector == null)
            _selector = new NpcActionSelector(_goals.getNpc(), _goal, null, null);

        // run goal as action if not complete
        if (!_isGoalFinished) {

            run(_selector);

            _isGoalFinished = _selector.isFinished();
        }

        if (_selector.runUnfinished() && _isGoalFinished)
            goalSelector.finish();

        _goals.popSelector();
    }

    @Override
    public boolean shouldExecute(GoalSelector goalSelector) {
        _goals.pushSelector(goalSelector);

        boolean result = canRun();

        _goals.popSelector();

        return result;
    }

    @Override
    public int hashCode() {
        return _goal.hashCode();
    }

    @Override
    public boolean equals(Object object) {
        if (object instanceof CitizensGoalAdapter) {
            return ((CitizensGoalAdapter) object)._goal.equals(_goal);
        }
        return _goal.equals(object);
    }
}
