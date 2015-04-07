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
import com.jcwhatever.nucleus.providers.citizensnpc.ai.NpcGoals.GoalAgent;
import com.jcwhatever.nucleus.providers.npc.ai.INpcState;
import com.jcwhatever.nucleus.providers.npc.ai.goals.INpcGoal;
import com.jcwhatever.nucleus.providers.npc.ai.goals.INpcGoalAgent;
import com.jcwhatever.nucleus.providers.npc.ai.goals.INpcGoalPriority;

/**
 * Container for an {@link INpcGoal}.
 *
 * <p>Holds extra objects related to the goal</p>
 */
public class GoalContainer
        extends BehaviourContainer<INpcGoal, INpcGoalAgent>
        implements INpcGoal {

    private final INpcGoalPriority _priority;
    private final NpcGoals _goals;

    private GoalAgent _agent;

    /**
     * Constructor.
     *
     * <p>Use for object matching only.</p>
     *
     * @param goal  The goal to wrap.
     */
    GoalContainer(INpcGoal goal) {
        this(null, goal, null);
    }

    /**
     * Constructor.
     *
     * <p>Uses {@link StaticGoalPriority} with a default priority value.</p>
     *
     * @param goal   The goal to wrap.
     * @param goals  The parent {@link NpcGoals}.
     */
    GoalContainer(INpcGoal goal, NpcGoals goals) {
        this(new StaticGoalPriority(), goal, goals);
    }

    /**
     * Constructor.
     *
     * @param priority  The goal priority.
     * @param goal      The goal to wrap.
     * @param goals     The parent {@link NpcGoals}.
     */
    GoalContainer(INpcGoalPriority priority, INpcGoal goal, NpcGoals goals) {
        super(goal);

        _priority = priority;
        _goals = goals;
    }

    /**
     * Get the goal priority.
     */
    public int getPriority() {
        return _priority.getPriority(_goals.getNpc());
    }

    @Override
    public GoalAgent getAgent() {
        if (_agent == null)
            _agent = _goals.createAgent(this);

        return _agent;
    }

    /**
     * Unsupported. Use {@link #run}.
     */
    @Override
    public void firstRun(INpcGoalAgent agent) {
        throw new UnsupportedOperationException();
    }

    /**
     * Unsupported. Use {@link #run()}.
     */
    @Override
    public void run(INpcGoalAgent agent) {
        throw new UnsupportedOperationException("Incorrect use of GoalContainer.");
    }

    @Override
    public void run() {

        // only run action if no child actions were run.
        if (!getAgent().runPool()) {

            if (getAgent().isFirstRun())
                getBehaviour().firstRun(getAgent());

            getBehaviour().run(getAgent());
        }
    }

    @Override
    public boolean canRun(INpcState state) {
        boolean result = getBehaviour().canRun(state);

        Msg.debug("[AI] [GOAL_CONTAINER] [NPC:{0}] [{1}] canRun = {2}",
                _goals.getNpc().getLookupName(), getName(), result);

        return result;
    }

    @Override
    public float getCost(INpcState state) {
        float result = getBehaviour().getCost(state);

        Msg.debug("[AI] [GOAL_CONTAINER] [NPC:{0}] [{1}] getCost = {2}",
                _goals.getNpc().getLookupName(), getName(), result);

        return result;
    }

    @Override
    public void pause(INpcState state) {

        Msg.debug("[AI] [GOAL_CONTAINER] [NPC:{0}] [{1}] pause",
                _goals.getNpc().getLookupName(),  getName());

        getBehaviour().pause(state);
    }
}

