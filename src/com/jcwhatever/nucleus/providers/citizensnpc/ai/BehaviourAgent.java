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

import com.jcwhatever.nucleus.providers.citizensnpc.Npc;
import com.jcwhatever.nucleus.providers.npc.INpc;
import com.jcwhatever.nucleus.providers.npc.ai.INpcBehaviour;
import com.jcwhatever.nucleus.providers.npc.ai.INpcBehaviourAgent;
import com.jcwhatever.nucleus.providers.npc.ai.INpcState;
import com.jcwhatever.nucleus.providers.npc.ai.actions.INpcAction;
import com.jcwhatever.nucleus.providers.npc.events.NpcClickEvent;
import com.jcwhatever.nucleus.providers.npc.events.NpcDamageByBlockEvent;
import com.jcwhatever.nucleus.providers.npc.events.NpcDamageByEntityEvent;
import com.jcwhatever.nucleus.providers.npc.events.NpcDamageEvent;
import com.jcwhatever.nucleus.providers.npc.events.NpcDeathEvent;
import com.jcwhatever.nucleus.providers.npc.events.NpcDespawnEvent;
import com.jcwhatever.nucleus.providers.npc.events.NpcLeftClickEvent;
import com.jcwhatever.nucleus.providers.npc.events.NpcRightClickEvent;
import com.jcwhatever.nucleus.providers.npc.events.NpcSpawnEvent;
import com.jcwhatever.nucleus.providers.npc.events.NpcTargetedEvent;
import com.jcwhatever.nucleus.utils.PreCon;
import com.jcwhatever.nucleus.utils.observer.script.IScriptUpdateSubscriber;
import com.jcwhatever.nucleus.utils.observer.script.ScriptUpdateSubscriber;
import com.jcwhatever.nucleus.utils.observer.update.NamedUpdateAgents;

import java.util.Arrays;

/*
 * 
 */
public abstract class BehaviourAgent<T extends INpcBehaviour, P extends INpcBehaviour>
        implements INpcBehaviourAgent {

    private final Npc _npc;
    private final BehaviourContainer<T> _container;
    private final NamedUpdateAgents _subscriberAgents;

    private long _runCount = 0;
    private boolean _isFinished;

    BehaviourAgent (Npc npc, BehaviourContainer<T> container) {
        _npc = npc;
        _container = container;
        _subscriberAgents = npc.registerUpdateAgent(this);
    }

    @Override
    public abstract BehaviourPool<P> getPool();

    @Override
    public long getRunCount() {
        return _runCount;
    }

    @Override
    public void finish() {
        _isFinished = true;
    }

    @Override
    public INpcState getState() {
        return _npc;
    }

    @Override
    public INpcAction createParallelActions(INpcAction... actions) {
        PreCon.notNull(actions);

        return new ParallelActions(getNpc(), Arrays.asList(actions));
    }

    @Override
    public INpcAction createBlendedActions(INpcAction... actions) {
        PreCon.notNull(actions);

        return new BlendedActions(getNpc(), Arrays.asList(actions));
    }

    @Override
    public INpcAction createSerialActions(INpcAction... actions) {
        PreCon.notNull(actions);

        return new SerialActions(getNpc(), Arrays.asList(actions));
    }

    @Override
    public INpcBehaviourAgent onNpcSpawn(
            IScriptUpdateSubscriber<NpcSpawnEvent> subscriber) {

        PreCon.notNull(subscriber);

        _subscriberAgents.getAgent("onNpcSpawn")
                .register(new ScriptUpdateSubscriber<>(subscriber));
        return this;
    }

    @Override
    public INpcBehaviourAgent onNpcDespawn(
            IScriptUpdateSubscriber<NpcDespawnEvent> subscriber) {

        PreCon.notNull(subscriber);

        _subscriberAgents.getAgent("onNpcDespawn")
                .register(new ScriptUpdateSubscriber<>(subscriber));
        return this;
    }

    @Override
    public INpcBehaviourAgent onNpcClick(
            IScriptUpdateSubscriber<NpcClickEvent> subscriber) {

        PreCon.notNull(subscriber);

        _subscriberAgents.getAgent("onNpcClick")
                .register(new ScriptUpdateSubscriber<>(subscriber));
        return this;
    }

    @Override
    public INpcBehaviourAgent onNpcRightClick(
            IScriptUpdateSubscriber<NpcRightClickEvent> subscriber) {

        PreCon.notNull(subscriber);

        _subscriberAgents.getAgent("onNpcRightClick")
                .register(new ScriptUpdateSubscriber<>(subscriber));
        return this;
    }

    @Override
    public INpcBehaviourAgent onNpcLeftClick(
            IScriptUpdateSubscriber<NpcLeftClickEvent> subscriber) {

        PreCon.notNull(subscriber);

        _subscriberAgents.getAgent("onNpcLeftClick")
                .register(new ScriptUpdateSubscriber<>(subscriber));
        return this;
    }

    @Override
    public INpcBehaviourAgent onNpcEntityTarget(
            IScriptUpdateSubscriber<NpcTargetedEvent> subscriber) {

        PreCon.notNull(subscriber);

        _subscriberAgents.getAgent("onNpcEntityTarget")
                .register(new ScriptUpdateSubscriber<>(subscriber));
        return this;
    }

    @Override
    public INpcBehaviourAgent onNpcDamage(
            IScriptUpdateSubscriber<NpcDamageEvent> subscriber) {

        PreCon.notNull(subscriber);

        _subscriberAgents.getAgent("onNpcDamage")
                .register(new ScriptUpdateSubscriber<>(subscriber));
        return this;
    }

    @Override
    public INpcBehaviourAgent onNpcDamageByBlock(
            IScriptUpdateSubscriber<NpcDamageByBlockEvent> subscriber) {

        PreCon.notNull(subscriber);

        _subscriberAgents.getAgent("onNpcDamageByBlock")
                .register(new ScriptUpdateSubscriber<>(subscriber));
        return this;
    }

    @Override
    public INpcBehaviourAgent onNpcDamageByEntity(
            IScriptUpdateSubscriber<NpcDamageByEntityEvent> subscriber) {

        PreCon.notNull(subscriber);

        _subscriberAgents.getAgent("onNpcDamageByEntity")
                .register(new ScriptUpdateSubscriber<>(subscriber));
        return this;
    }

    @Override
    public INpcBehaviourAgent onNpcDeath(
            IScriptUpdateSubscriber<NpcDeathEvent> subscriber) {

        PreCon.notNull(subscriber);

        _subscriberAgents.getAgent("onNpcDeath")
                .register(new ScriptUpdateSubscriber<>(subscriber));
        return this;
    }

    @Override
    public INpcBehaviourAgent onNavStart(IScriptUpdateSubscriber<INpc> subscriber) {
        PreCon.notNull(subscriber);

        _subscriberAgents.getAgent("onNavStart")
                .register(new ScriptUpdateSubscriber<>(subscriber));
        return this;
    }

    @Override
    public INpcBehaviourAgent onNavPause(IScriptUpdateSubscriber<INpc> subscriber) {
        PreCon.notNull(subscriber);

        _subscriberAgents.getAgent("onNavPause")
                .register(new ScriptUpdateSubscriber<>(subscriber));
        return this;
    }

    @Override
    public INpcBehaviourAgent onNavCancel(IScriptUpdateSubscriber<INpc> subscriber) {
        PreCon.notNull(subscriber);

        _subscriberAgents.getAgent("onNavCancel")
                .register(new ScriptUpdateSubscriber<>(subscriber));
        return this;
    }

    @Override
    public INpcBehaviourAgent onNavComplete(IScriptUpdateSubscriber<INpc> subscriber) {
        PreCon.notNull(subscriber);

        _subscriberAgents.getAgent("onNavComplete")
                .register(new ScriptUpdateSubscriber<>(subscriber));
        return this;
    }

    @Override
    public INpcBehaviourAgent onNavTimeout(IScriptUpdateSubscriber<INpc> subscriber) {
        PreCon.notNull(subscriber);

        _subscriberAgents.getAgent("onNavTimeout")
                .register(new ScriptUpdateSubscriber<>(subscriber));
        return this;
    }

    Npc getNpc() {
        return _npc;
    }

    BehaviourContainer<T> getContainer() {
        return _container;
    }

    boolean isFinished() {
        return _isFinished;
    }

    boolean runPool() {
        _runCount++;
        return getPool().run();
    }

    void reset() {
        _isFinished = false;
        //_subscriberAgents.disposeAgents();
    }
}
