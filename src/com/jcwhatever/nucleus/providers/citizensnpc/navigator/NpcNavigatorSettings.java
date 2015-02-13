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
import com.jcwhatever.nucleus.providers.npc.navigator.INpcNavSettings;
import com.jcwhatever.nucleus.providers.npc.navigator.INpcNavTimeout;
import com.jcwhatever.nucleus.providers.npc.navigator.flock.INpcFlocker;
import com.jcwhatever.nucleus.utils.PreCon;

import net.citizensnpcs.api.ai.Navigator;
import net.citizensnpcs.api.ai.NavigatorParameters;

import javax.annotation.Nullable;

/*
 * 
 */
public class NpcNavigatorSettings implements INpcNavSettings {

    private final NavigatorParameters _settings;
    private final CitizensStuckAdapter _stuckAdapter;
    private final CitizensFlockAdapter _flockAdapter;

    public NpcNavigatorSettings(Npc npc, Navigator navigator, NavigatorParameters settings) {
        PreCon.notNull(npc);
        PreCon.notNull(navigator);
        PreCon.notNull(settings);

        _settings = settings;
        _stuckAdapter = new CitizensStuckAdapter(settings.stuckAction());
        _flockAdapter = new CitizensFlockAdapter(npc, settings);

        settings.addRunCallback(_flockAdapter);
    }

    @Override
    public double getSpeed() {
        return _settings.speed();
    }

    @Override
    public INpcNavSettings setSpeed(double speed) {

        _settings.speedModifier((float)speed);

        return this;
    }

    @Override
    public boolean avoidsWater() {
        return _settings.avoidWater();
    }

    @Override
    public INpcNavSettings avoidWater() {

        _settings.avoidWater(true);

        return this;
    }

    @Override
    public INpcNavSettings ignoreWater() {

        _settings.avoidWater(false);

        return this;
    }

    @Nullable
    @Override
    public INpcFlocker getFlocker() {
        return _flockAdapter.getFlocker();
    }

    @Override
    public INpcNavSettings setFlocker(@Nullable INpcFlocker flocker) {
        _flockAdapter.setFlocker(flocker);

        return this;
    }

    @Override
    public int getTimeout() {
        return _settings.stationaryTicks();
    }

    @Override
    public INpcNavSettings setTimeout(int ticks) {
        _settings.stationaryTicks(ticks);
        return this;
    }

    @Override
    public INpcNavTimeout getTimeoutHandler() {
        return _stuckAdapter.getTimeoutHandler();
    }

    @Override
    public INpcNavSettings setTimeoutHandler(INpcNavTimeout timeoutHandler) {

        _stuckAdapter.setTimeoutHandler(timeoutHandler);

        return this;
    }
}
