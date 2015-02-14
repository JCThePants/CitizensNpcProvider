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

import com.jcwhatever.nucleus.collections.TreeNode;
import com.jcwhatever.nucleus.providers.citizensnpc.Npc;
import com.jcwhatever.nucleus.providers.npc.ai.actions.INpcAction;
import com.jcwhatever.nucleus.providers.npc.ai.actions.INpcActionSelector;
import com.jcwhatever.nucleus.providers.npc.ai.goals.INpcGoal;
import com.jcwhatever.nucleus.utils.PreCon;

import java.util.LinkedList;
import javax.annotation.Nullable;

/**
 * Implementation of an action selector.
 */
public class NpcActionSelector implements INpcActionSelector {

    private final Npc _npc;
    private final LinkedList<NpcActionSelector> _unfinished;
    private final LinkedList<NpcActionSelector> _onfinish = new LinkedList<>();
    private final TreeNode<NpcActionSelector> _node;
    private final INpcAction _action;
    private boolean _isFinished;
    private long _lastRun;

    /**
     * Constructor.
     *
     * @param npc         The owning {@code Npc}.
     * @param parent      The parent selector.
     * @param unfinished  A list to put unfinished actions into. Null if root selector.
     */
    public NpcActionSelector(Npc npc, INpcAction action,
                             @Nullable TreeNode<NpcActionSelector> parent,
                             @Nullable LinkedList<NpcActionSelector> unfinished) {
        PreCon.notNull(npc);
        PreCon.notNull(action);

        _npc = npc;
        _action = action;
        _node = new TreeNode<>(this);

        if (parent != null)
            parent.addChild(_node);

        _unfinished = unfinished != null ? unfinished : new LinkedList<NpcActionSelector>();
    }

    /**
     * Get the result of the last action run.
     */
    @Override
    public boolean isFinished() {
        return _isFinished;
    }

    /**
     * Get the {@code INpcAction} the selector is for.
     */
    public INpcAction getAction() {
        return _action;
    }

    /**
     * Run unfinished actions.
     *
     * <p>Invoke once per tick until returns true.</p>
     *
     * @return  True if all actions are finished, false if there
     * are still actions that need to be run.
     */
    public boolean runUnfinished() {

        LinkedList<NpcActionSelector> unfinished = new LinkedList<>();

        long timeStamp = System.currentTimeMillis();

        boolean hasUnfinished = false;

        while (!_unfinished.isEmpty()) {
            NpcActionSelector selector = _unfinished.removeFirst();

            runSelector(timeStamp, selector, unfinished);

            hasUnfinished = selector.runUnfinished() || hasUnfinished;
        }

        _unfinished.addAll(unfinished);

        return !hasUnfinished && unfinished.isEmpty();
    }

    @Override
    public Npc getNpc() {
        return _npc;
    }

    @Override
    public void finish() {
        finish(null);
    }

    @Override
    public void finish(@Nullable INpcAction action) {

        _isFinished = true;

        runFinishActions();

        if (action != null) {
            action.reset();
            runAction(action, _unfinished);
        }
    }

    @Override
    public void cancel() {
        cancel(null);
    }

    @Override
    public void cancel(@Nullable INpcAction action) {

        _isFinished = true;
        _unfinished.clear();

        for (TreeNode<NpcActionSelector> node : _node.getChildren()) {
            node.getValue().cancel(null);
        }

        _node.clear();
    }

    @Override
    public NpcActionSelector run(INpcAction action) {
        PreCon.notNull(action);

        action.reset();
        return runAction(action, _unfinished);
    }

    @Override
    public INpcActionSelector next(INpcAction action) {
        PreCon.notNull(action);

        NpcActionSelector selector = new NpcActionSelector(_npc, action, _node, _unfinished);
        _onfinish.add(selector);

        return selector;
    }

    /*
     * Run an action. Provide a list to add unfinished selectors to.
     */
    private NpcActionSelector runAction(INpcAction action, LinkedList<NpcActionSelector> unfinished) {

        NpcActionSelector selector = new NpcActionSelector(_npc, action, _node, unfinished);

        if (action instanceof INpcGoal && !((INpcGoal) action).canRun()) {
            selector._isFinished = true;
            return selector;
        }

        runSelector(-1, selector, unfinished);

        return selector;
    }

    /*
     * Run an action from a selector. Provide a list to add unfinished selectors to.
     */
    private void runSelector(long timeStamp, NpcActionSelector selector,
                             LinkedList<NpcActionSelector> unfinished) {

        if (selector.hasRun(timeStamp))
            return;

        INpcAction action = selector.getAction();

        if (action instanceof INpcGoal && !((INpcGoal) action).canRun())
            return;

        // run the action
        action.run(selector);

        selector._lastRun = timeStamp;

        if (!selector.isFinished()) {
            unfinished.add(selector);
        }
    }

    // used to prevent an action from being run more than once
    private boolean hasRun(long timeStamp) {
        return timeStamp != -1 && _lastRun == timeStamp;
    }

    // run the actions added with the #next method
    private void runFinishActions() {

        while (!_onfinish.isEmpty()) {
            NpcActionSelector selector = _onfinish.removeFirst();

            runSelector(-1, selector, _unfinished);
        }
    }
}
