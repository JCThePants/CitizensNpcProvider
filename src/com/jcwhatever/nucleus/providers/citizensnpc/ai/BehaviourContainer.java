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

import com.jcwhatever.nucleus.providers.npc.ai.INpcBehaviour;
import com.jcwhatever.nucleus.providers.npc.ai.INpcState;

/**
 * Container for an {@link com.jcwhatever.nucleus.providers.npc.ai.INpcBehaviour}.
 *
 * <p>Holds extra objects related to the behaviour.</p>
 */
public abstract class BehaviourContainer<T extends INpcBehaviour> implements INpcBehaviour {

    private final T _behaviour;

    BehaviourContainer(T goal) {
        _behaviour = goal;
    }

    public abstract BehaviourAgent getAgent();

    /**
     * Invoked to run behaviour child behaviors.
     *
     * <p>If a child behaviour is run, the agents behaviour is not run.</p>
     *
     * @return  True if the behaviour or a child behaviour was run.
     */
    public abstract void run();

    @Override
    public void reset(INpcState state) {
        _behaviour.reset(state);
        getAgent().reset();
    }

    public boolean isFinished() {
        return getAgent().isFinished();
    }

    @Override
    public int hashCode() {
        return _behaviour.hashCode();
    }

    @Override
    public boolean equals(Object obj) {

        if (obj instanceof BehaviourContainer)
            return _behaviour.equals(((BehaviourContainer) obj)._behaviour);

        return _behaviour.equals(obj);
    }

    protected T getBehaviour() {
        return _behaviour;
    }
}
