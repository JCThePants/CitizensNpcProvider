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

package com.jcwhatever.nucleus.providers.citizensnpc.navigator;

import com.jcwhatever.nucleus.providers.npc.navigator.INpcNavRunner;

/**
 * Container for a {@link com.jcwhatever.nucleus.providers.npc.navigator.INpcNavRunner}.
 *
 * <p>Implements {@link java.lang.Runnable} to act as an adapter for Citizens.</p>
 */
public class NavRunnerContainer implements Runnable {

    private final NpcNavigator _navigator;
    private final INpcNavRunner _runner;
    private final NavRunnerAgent _agent;

    /**
     * Constructor.
     *
     * @param navigator  The owning {@link NpcNavigator}.
     * @param runner     The runner to encapsulate.
     */
    NavRunnerContainer(NpcNavigator navigator, INpcNavRunner runner) {
        _navigator = navigator;
        _runner = runner;
        _agent = new NavRunnerAgent(navigator.getNpc(), this);
    }

    /**
     * Constructor.
     *
     * <p>Used for matching {@link INpcNavRunner} instances.</p>
     *
     * @param runner  The runner.
     */
    NavRunnerContainer(INpcNavRunner runner) {
        _runner = runner;
        _navigator = null;
        _agent = null;
    }

    @Override
    public void run() {
        _runner.run(_agent);
    }

    @Override
    public int hashCode() {
        return _runner.hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof NavRunnerContainer) {
            return ((NavRunnerContainer) obj)._runner.equals(_runner);
        }
        return _runner.equals(obj);
    }

    /**
     * Get the owning {@link NpcNavigator}.
     */
    NpcNavigator getNavigator() {
        return _navigator;
    }
}
