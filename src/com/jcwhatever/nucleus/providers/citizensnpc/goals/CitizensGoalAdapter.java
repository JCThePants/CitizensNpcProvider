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

import com.jcwhatever.nucleus.providers.npc.goals.INpcGoal;
import com.jcwhatever.nucleus.providers.npc.goals.NpcGoalResult;

import net.citizensnpcs.api.ai.Goal;
import net.citizensnpcs.api.ai.GoalSelector;

/*
 * 
 */
public class CitizensGoalAdapter implements Goal, INpcGoal {

    private final NpcGoals _goals;
    private final INpcGoal _goal;

    public CitizensGoalAdapter(NpcGoals goals, INpcGoal goal) {
        _goals = goals;
        _goal = goal;
    }

    @Override
    public void reset() {
        _goal.reset();
    }

    @Override
    public NpcGoalResult run() {
        return _goal.run();
    }

    @Override
    public boolean shouldRun() {
        return _goal.shouldRun();
    }

    @Override
    public void run(GoalSelector goalSelector) {
        _goals.pushSelector(goalSelector);
        NpcGoalResult result = run();

        switch (result) {
            case CONTINUE:
                break;

            case FINISH:
                goalSelector.finish();
                break;

            case FINISH_REMOVE:
                goalSelector.finishAndRemove();
                break;
        }
        _goals.popSelector();
    }

    @Override
    public boolean shouldExecute(GoalSelector goalSelector) {
        _goals.pushSelector(goalSelector);
        boolean result = shouldRun();
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
