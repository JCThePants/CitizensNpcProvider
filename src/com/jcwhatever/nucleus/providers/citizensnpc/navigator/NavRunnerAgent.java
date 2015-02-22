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

import com.jcwhatever.nucleus.providers.citizensnpc.Npc;
import com.jcwhatever.nucleus.providers.npc.INpc;
import com.jcwhatever.nucleus.providers.npc.navigator.INpcNavRunnerAgent;

/**
 * Implementation of {@link com.jcwhatever.nucleus.providers.npc.navigator.INpcNavRunnerAgent}.
 */
public class NavRunnerAgent implements INpcNavRunnerAgent {

    private final Npc _npc;
    private final NavRunnerContainer _runner;

    /**
     * Constructor.
     *
     * @param npc     The owning NPC.
     * @param runner  The {@link NavRunnerContainer} the agent is for.
     */
    NavRunnerAgent(Npc npc, NavRunnerContainer runner) {
        _npc = npc;
        _runner = runner;
    }

    @Override
    public INpc getNpc() {
        return _npc;
    }

    @Override
    public void remove() {
        _runner.getNavigator().removeRunner(_runner);
    }
}
