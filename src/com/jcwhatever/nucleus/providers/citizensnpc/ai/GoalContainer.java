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

import com.jcwhatever.nucleus.providers.citizensnpc.ai.NpcGoals.GoalAgent;
import com.jcwhatever.nucleus.providers.npc.ai.INpcState;
import com.jcwhatever.nucleus.providers.npc.ai.goals.INpcGoal;
import com.jcwhatever.nucleus.providers.npc.ai.goals.INpcGoalAgent;

/*
 * 
 */
public class GoalContainer extends BehaviourContainer<INpcGoal> implements INpcGoal {

    private final int _priority;
    private final NpcGoals _goals;

    private GoalAgent _agent;

    GoalContainer(int priority, INpcGoal goal, NpcGoals goals) {
        super(goal);

        _priority = priority;
        _goals = goals;
    }

    // used for object matching
    GoalContainer(INpcGoal goal) {
        super(goal);
        _priority = 0;
        _agent = null;
        _goals = null;
    }

    public int getPriority() {
        return _priority;
    }

    @Override
    public GoalAgent getAgent() {
        if (_agent == null)
            _agent = _goals.createAgent(this);

        return _agent;
    }

    /**
     * Invoked to run goal or child behaviors.
     *
     * <p>If a child behaviour is run, the agents goal is not run.</p>
     */
    @Override
    public void run() {

        // only run action if no child actions were run.
        if (!getAgent().runPool())
            getBehaviour().run(getAgent());
    }

    @Override
    public void run(INpcGoalAgent agent) {
        throw new UnsupportedOperationException("Incorrect use of GoalContainer.");
    }

    @Override
    public boolean canRun(INpcState state) {
        return getBehaviour().canRun(state);
    }

    @Override
    public float getCost(INpcState state) {
        return getBehaviour().getCost(state);
    }

    @Override
    public void pause(INpcState state) {
        getBehaviour().pause(state);
    }
}

