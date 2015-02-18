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
import com.jcwhatever.nucleus.providers.citizensnpc.Registry;
import com.jcwhatever.nucleus.providers.npc.INpc;
import com.jcwhatever.nucleus.providers.npc.navigator.INpcNav;
import com.jcwhatever.nucleus.providers.npc.navigator.INpcNavSettings;
import com.jcwhatever.nucleus.utils.PreCon;
import com.jcwhatever.nucleus.utils.observer.script.IScriptUpdateSubscriber;
import com.jcwhatever.nucleus.utils.observer.script.ScriptUpdateSubscriber;
import com.jcwhatever.nucleus.utils.observer.update.NamedUpdateAgents;

import org.bukkit.Location;
import org.bukkit.entity.Entity;

import net.citizensnpcs.api.ai.EntityTarget;
import net.citizensnpcs.api.ai.Navigator;
import net.citizensnpcs.api.ai.TargetType;

import javax.annotation.Nullable;

/**
 * Implementation of {@link com.jcwhatever.nucleus.providers.npc.navigator.INpcNav}.
 */
public class NpcNavigator implements INpcNav {

    private final Npc _npc;
    private final Registry _registry;
    private final Navigator _navigator;
    private final NpcNavigatorSettings _settings;
    private final NamedUpdateAgents _agents = new NamedUpdateAgents();
    private boolean _isHostile;
    private boolean _isVehicleProxy;

    /**
     * Constructor.
     *
     * @param npc        The NPC the navigator is for.
     * @param registry   The owning registry.
     * @param navigator  The citizens {@link net.citizensnpcs.api.ai.Navigator}.
     */
    public NpcNavigator(Npc npc, Registry registry, Navigator navigator) {
        PreCon.notNull(npc);
        PreCon.notNull(registry);
        PreCon.notNull(navigator);

        _npc = npc;
        _registry = registry;
        _navigator = navigator;
        _settings = new NpcNavigatorSettings(npc, navigator, navigator.getLocalParameters());
    }

    @Override
    public INpc getNpc() {
        return _npc;
    }

    @Override
    public INpcNavSettings getSettings() {
        return _settings;
    }

    @Override
    public boolean isRunning() {
        return _navigator.isNavigating();
    }

    @Override
    public boolean isHostile() {
        EntityTarget target = _navigator.getEntityTarget();
        return target != null && target.isAggressive();
    }

    @Override
    public boolean isVehicleProxy() {
        return _isVehicleProxy;
    }

    @Override
    public NpcNavigator setVehicleProxy(boolean isProxy) {
        _isVehicleProxy = isProxy;

        return this;
    }

    @Override
    public boolean isTargetingLocation() {
        return _navigator.getTargetType() == TargetType.LOCATION;
    }

    @Override
    public boolean isTargetingEntity() {
        return _navigator.getTargetType() == TargetType.ENTITY;
    }

    @Nullable
    @Override
    public Location getTargetLocation() {
        return _navigator.getTargetAsLocation();
    }

    @Nullable
    @Override
    public Entity getTargetEntity() {

        EntityTarget target = _navigator.getEntityTarget();
        if (target == null)
            return null;

        return target.getTarget();
    }

    @Override
    public INpcNav start() {
        _navigator.setPaused(false);

        if (_isVehicleProxy) {
            INpc vehicle = _npc.getNPCVehicle();
            if (vehicle != null)
                vehicle.getNavigator().start();
        }

        return this;
    }

    @Override
    public INpcNav pause() {
        _navigator.setPaused(true);

        onPause();

        if (_isVehicleProxy) {
            INpc vehicle = _npc.getNPCVehicle();
            if (vehicle != null)
                vehicle.getNavigator().pause();
        }

        return this;
    }

    @Override
    public INpcNav cancel() {
        _navigator.cancelNavigation();

        if (_isVehicleProxy) {
            INpc vehicle = _npc.getNPCVehicle();
            if (vehicle != null)
                vehicle.getNavigator().cancel();
        }

        return this;
    }

    @Override
    public INpcNav setTarget(Location location) {
        PreCon.notNull(location);

        _navigator.cancelNavigation();
        _navigator.setTarget(location);

        if (_isVehicleProxy) {
            INpc vehicle = _npc.getNPCVehicle();
            if (vehicle != null)
                vehicle.getNavigator().setTarget(location);
        }

        return this;
    }

    @Override
    public INpcNav setTarget(Entity entity) {
        PreCon.notNull(entity);

        _navigator.cancelNavigation();
        _navigator.setTarget(entity, _isHostile);

        if (_isVehicleProxy) {
            INpc vehicle = _npc.getNPCVehicle();
            if (vehicle != null)
                vehicle.getNavigator().setTarget(entity);
        }

        return this;
    }

    @Override
    public INpcNav setHostile(boolean isHostile) {
        _isHostile = isHostile;

        EntityTarget target = _navigator.getEntityTarget();
        if (target == null || target.getTarget() == null || target.isAggressive() == isHostile)
            return this;

        setTarget(target.getTarget());

        return this;
    }

    @Override
    public INpcNav onNavStart(IScriptUpdateSubscriber<INpc> subscriber) {
        PreCon.notNull(subscriber);

        _agents.getAgent("onNavStart").register(new ScriptUpdateSubscriber<>(subscriber));

        return this;
    }

    @Override
    public INpcNav onNavPause(IScriptUpdateSubscriber<INpc> subscriber) {
        PreCon.notNull(subscriber);

        _agents.getAgent("onNavPause").register(new ScriptUpdateSubscriber<>(subscriber));

        return this;
    }

    @Override
    public INpcNav onNavCancel(IScriptUpdateSubscriber<INpc> subscriber) {
        PreCon.notNull(subscriber);

        _agents.getAgent("onNavCancel").register(new ScriptUpdateSubscriber<>(subscriber));

        return this;
    }

    @Override
    public INpcNav onNavComplete(IScriptUpdateSubscriber<INpc> subscriber) {
        PreCon.notNull(subscriber);

        _agents.getAgent("onNavComplete").register(new ScriptUpdateSubscriber<>(subscriber));

        return this;
    }

    @Override
    public INpcNav onNavTimeout(IScriptUpdateSubscriber<INpc> subscriber) {
        PreCon.notNull(subscriber);

        _agents.getAgent("onNavTimeout").register(new ScriptUpdateSubscriber<>(subscriber));

        return this;
    }

    public void onStart() {
        _npc.updateAgents("onNavStart", _npc);
        _agents.update("onNavStart", _npc);
        _registry.onNavStart(_npc);
    }

    public void onPause() {
        _npc.updateAgents("onNavPause", _npc);
        _agents.update("onNavPause", _npc);
        _registry.onNavPause(_npc);
    }

    public void onCancel() {
        _npc.updateAgents("onNavCancel", _npc);
        _agents.update("onNavCancel", _npc);
        _registry.onNavCancel(_npc);
    }

    public void onComplete() {
        _npc.updateAgents("onNavComplete", _npc);
        _agents.update("onNavComplete", _npc);
        _registry.onNavComplete(_npc);
    }

    public void onTimeout() {
        _npc.updateAgents("onNavTimeout", _npc);
        _agents.update("onNavTimeout", _npc);
        _registry.onNavTimeout(_npc);
    }
}
