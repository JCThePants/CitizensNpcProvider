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
import com.jcwhatever.nucleus.providers.npc.navigator.flock.INpcFlocker;
import com.jcwhatever.nucleus.utils.PreCon;

import net.citizensnpcs.api.ai.NavigatorParameters;

import javax.annotation.Nullable;

/*
 * 
 */
public class CitizensFlockAdapter implements Runnable {


    private final Npc _npc;
    private final NavigatorParameters _settings;
    private INpcFlocker _flocker;

    public CitizensFlockAdapter(Npc npc, NavigatorParameters settings) {
        PreCon.notNull(npc);
        PreCon.notNull(settings);

        _npc = npc;
        _settings = settings;
    }

    public INpcFlocker getFlocker() {
        return _flocker;
    }

    public void setFlocker(@Nullable INpcFlocker flocker) {
        _flocker = flocker;
    }


    @Override
    public void run() {
        // TODO
    }
}
