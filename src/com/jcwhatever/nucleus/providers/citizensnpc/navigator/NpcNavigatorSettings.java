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
import com.jcwhatever.nucleus.utils.PreCon;
import net.citizensnpcs.api.ai.AttackStrategy;
import net.citizensnpcs.api.ai.Navigator;
import net.citizensnpcs.api.ai.NavigatorParameters;
import net.citizensnpcs.api.ai.StuckAction;
import net.citizensnpcs.api.ai.event.NavigatorCallback;
import net.citizensnpcs.api.astar.pathfinder.BlockExaminer;

import java.util.Iterator;

/**
 * Implementation of {@link INpcNavSettings}.
 */
public class NpcNavigatorSettings implements INpcNavSettings {

    private final NavigatorParameters _settings;
    private final CitizensStuckAdapter _stuckAdapter;

    private final AttackStrategy _defaultAttackStrategy;
    private final BlockExaminer[] _defaultBlockExaminers;
    private final boolean _defaultUseNewPathfinder;
    private final float _defaultBaseSpeed;
    private final float _defaultRange;
    private final double _defaultAttackRange;
    private final int _defaultStationaryTicks;
    private final StuckAction _defaultStuckAction;
    private final boolean _defaultAvoidsWater;
    private final double _defaultDistanceMargin;
    private final double _defaultPathDistanceMargin;

    private double _tolerance;

    /**
     * Constructor.
     *
     * @param npc        The NPC the navigator settings are for.
     * @param navigator  The Citizens {@link net.citizensnpcs.api.ai.Navigator}.
     * @param settings   The owning {@link net.citizensnpcs.api.ai.NavigatorParameters}.
     */
    public NpcNavigatorSettings(Npc npc, Navigator navigator, NavigatorParameters settings) {
        PreCon.notNull(npc);
        PreCon.notNull(navigator);
        PreCon.notNull(settings);

        _settings = settings;
        _stuckAdapter = new CitizensStuckAdapter(settings.stuckAction());
        _tolerance = Math.sqrt(settings.distanceMargin());

        _defaultAttackStrategy = settings.defaultAttackStrategy();
        _defaultBlockExaminers = settings.examiners().clone();
        _defaultUseNewPathfinder = settings.useNewPathfinder();
        _defaultBaseSpeed = settings.baseSpeed();
        _defaultRange = settings.range();
        _defaultAttackRange = settings.attackRange();
        _defaultStationaryTicks = settings.stationaryTicks();
        _defaultStuckAction = settings.stuckAction();
        _defaultAvoidsWater = settings.avoidWater();
        _defaultDistanceMargin = settings.distanceMargin();
        _defaultPathDistanceMargin = settings.pathDistanceMargin();

        reset();
    }

    public void reset() {

        _settings
                .useNewPathfinder(_defaultUseNewPathfinder)
                .baseSpeed(_defaultBaseSpeed)
                .range(_defaultRange)
                .defaultAttackStrategy(_defaultAttackStrategy)
                .attackRange(_defaultAttackRange)
                .stationaryTicks(_defaultStationaryTicks)
                .stuckAction(_defaultStuckAction)
                .avoidWater(_defaultAvoidsWater)
                .distanceMargin(_defaultDistanceMargin)
                .pathDistanceMargin(_defaultPathDistanceMargin)
                .stationaryTicks(_defaultStationaryTicks);

        _settings.modifiedSpeed(1f);

        Iterator<NavigatorCallback> callbacks = _settings.callbacks().iterator();
        while (callbacks.hasNext()) {
            callbacks.next();
            callbacks.remove();
        }

        _settings.clearExaminers();
        for (BlockExaminer examiner : _defaultBlockExaminers) {
            _settings.examiner(examiner);
        }
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
    public double getTolerance() {
        return _tolerance;
    }

    @Override
    public INpcNavSettings setTolerance(double tolerance) {

        _settings.distanceMargin(tolerance * tolerance);
        _tolerance = tolerance;

        return this;
    }

    @Override
    public boolean avoidsWater() {
        return _settings.avoidWater();
    }

    @Override
    public INpcNavSettings setAvoidsWater(boolean avoidsWater) {
        _settings.avoidWater(avoidsWater);
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
