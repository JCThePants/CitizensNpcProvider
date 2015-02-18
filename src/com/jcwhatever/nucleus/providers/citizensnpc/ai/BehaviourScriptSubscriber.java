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

import com.jcwhatever.nucleus.utils.PreCon;
import com.jcwhatever.nucleus.utils.observer.script.IScriptUpdateSubscriber;
import com.jcwhatever.nucleus.utils.observer.script.ScriptUpdateSubscriber;

/**
 * An extended implementation of {@link ScriptUpdateSubscriber} that prevents the subscriber
 * from being notified if the parent {@link BehaviourAgent}'s
 * {@link com.jcwhatever.nucleus.providers.npc.ai.INpcBehaviour} is finished or is not the current
 * behaviour in its parent {@link BehaviourPool}.
 */
public class BehaviourScriptSubscriber<A> extends ScriptUpdateSubscriber<A> {

    private final BehaviourAgent<?, ?> _behaviourAgent;

    /**
     * Constructor.
     *
     * @param subscriber The subscriber passed in from a script.
     */
    public BehaviourScriptSubscriber(BehaviourAgent<?, ?> behaviourAgent,
                                     IScriptUpdateSubscriber<A> subscriber) {
        super(subscriber);

        PreCon.notNull(behaviourAgent);

        _behaviourAgent = behaviourAgent;
    }

    /**
     * Does not forward argument to subscriber if the behaviour agent
     * is finished or is not the current behaviour in its pool.
     *
     * <p>{@inheritDoc}</p>
     */
    @Override
    public void on(A argument) {

        if (_behaviourAgent.isCurrent()) {
            super.on(argument);
        }
    }
}
