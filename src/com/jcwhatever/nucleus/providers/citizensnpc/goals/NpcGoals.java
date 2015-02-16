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

import com.jcwhatever.nucleus.providers.citizensnpc.Npc;
import com.jcwhatever.nucleus.providers.npc.ai.goals.INpcGoal;
import com.jcwhatever.nucleus.providers.npc.ai.goals.INpcGoals;
import com.jcwhatever.nucleus.utils.PreCon;

import net.citizensnpcs.api.ai.Goal;
import net.citizensnpcs.api.ai.GoalController;
import net.citizensnpcs.api.ai.GoalSelector;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Set;

/**
 * An {@link Npc} goal manager.
 */
public class NpcGoals implements INpcGoals {

    private Npc _npc;
    private final GoalController _controller;
    private Set<INpcGoal> _goals = new HashSet<>(5);
    private LinkedList<GoalSelector> _selectorStack = new LinkedList<>();

    /**
     * Constructor.
     *
     * @param npc  The {@link Npc} the goal manager is for.
     */
    public NpcGoals(Npc npc) {
        _npc = npc;
        _controller = npc.getHandle().getDefaultGoalController();
    }

    @Override
    public Npc getNpc() {
        return _npc;
    }

    @Override
    public Collection<INpcGoal> all() {
        return new ArrayList<>(_goals);
    }

    @Override
    public NpcGoals add(int priority, INpcGoal goal) {

        CitizensGoalAdapter adapter = new CitizensGoalAdapter(this, goal);

        _controller.addGoal(adapter, priority);

        return this;
    }

    @Override
    public boolean remove(INpcGoal goal) {

        CitizensGoalAdapter adapter = new CitizensGoalAdapter(this, goal);
        boolean hasGoal = _goals.remove(adapter);

        _controller.removeGoal(adapter);

        return hasGoal;
    }

    @Override
    public NpcGoals clear() {
        _controller.clear();

        return this;
    }

    @Override
    public boolean isRunning() {
        return _controller.isExecutingGoal();
    }

    @Override
    public NpcGoals pause() {
        _controller.setPaused(true);

        return this;
    }

    @Override
    public NpcGoals resume() {
        _controller.setPaused(false);

        return this;
    }

    @Override
    public NpcGoals setGoal(INpcGoal goal) {

        GoalSelector selector = _selectorStack.peek();
        if (selector == null) {
             add(0, new CitizensGoalAdapter(this, goal));
            return this;
        }

        selector.select(new CitizensGoalAdapter(this, goal));

        return this;
    }

    @Override
    public NpcGoals runGoals(INpcGoal... goals) {

        GoalSelector selector = _selectorStack.peek();
        if (selector == null) {
            for (int i=goals.length - 1, priority=0; i >= 0; i--, priority++) {
                add(priority, goals[i]);
            }
            return this;
        }

        Goal[] adapted = new Goal[goals.length];

        for (int i=0; i < adapted.length; i++) {
            adapted[i] = new CitizensGoalAdapter(this, goals[i]);
        }

        selector.selectAdditional(adapted);

        return this;
    }

    void pushSelector(GoalSelector selector) {
        PreCon.notNull(selector);

        _selectorStack.push(selector);
    }

    void popSelector() {
        _selectorStack.pop();
    }
}
