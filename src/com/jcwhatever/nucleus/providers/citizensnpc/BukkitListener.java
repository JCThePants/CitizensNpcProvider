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

package com.jcwhatever.nucleus.providers.citizensnpc;

import com.jcwhatever.nucleus.Nucleus;
import com.jcwhatever.nucleus.providers.npc.events.NpcClickEvent;
import com.jcwhatever.nucleus.providers.npc.events.NpcDamageByBlockEvent;
import com.jcwhatever.nucleus.providers.npc.events.NpcDamageByEntityEvent;
import com.jcwhatever.nucleus.providers.npc.events.NpcDamageEvent;
import com.jcwhatever.nucleus.providers.npc.events.NpcDeathEvent;
import com.jcwhatever.nucleus.providers.npc.events.NpcDespawnEvent;
import com.jcwhatever.nucleus.providers.npc.events.NpcDespawnEvent.NpcDespawnReason;
import com.jcwhatever.nucleus.providers.npc.events.NpcLeftClickEvent;
import com.jcwhatever.nucleus.providers.npc.events.NpcPushEvent;
import com.jcwhatever.nucleus.providers.npc.events.NpcRightClickEvent;
import com.jcwhatever.nucleus.providers.npc.events.NpcSpawnEvent;
import com.jcwhatever.nucleus.providers.npc.events.NpcTargetedEvent;

import org.bukkit.entity.Entity;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByBlockEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDeathEvent;

import net.citizensnpcs.api.event.EntityTargetNPCEvent;
import net.citizensnpcs.api.event.NPCClickEvent;
import net.citizensnpcs.api.event.NPCDespawnEvent;
import net.citizensnpcs.api.event.NPCLeftClickEvent;
import net.citizensnpcs.api.event.NPCPushEvent;
import net.citizensnpcs.api.event.NPCRightClickEvent;
import net.citizensnpcs.api.event.NPCSpawnEvent;

public class BukkitListener implements Listener {

    @EventHandler(priority = EventPriority.HIGHEST)
    private void onSpawn(NPCSpawnEvent event) {
        Npc npc = CitizensProvider.getInstance().getNpc(event.getNPC());
        if (npc == null || npc.isSpawned())
            return;

        CitizensProvider.getInstance().registerEntity(npc, event.getNPC().getEntity());

        NpcSpawnEvent e = new NpcSpawnEvent(npc, npc.getSpawnReason());
        e.setCancelled(event.isCancelled());

        Nucleus.getEventManager().callBukkit(this, e);
        if (e.isCancelled()) {
            event.setCancelled(true);
            return;
        }

        npc.onNpcSpawn(e);
        event.setCancelled(e.isCancelled());
    }

    // detect entity death
    @EventHandler(priority = EventPriority.MONITOR)
    private void onDeathDespawn(EntityDeathEvent event) {

        // check for "cancelled" event
        if (event.getEntity().getHealth() > 0.0D)
            return;

        Npc npc = CitizensProvider.getInstance().getNpc(event.getEntity());
        if (npc == null)
            return;

        Entity entity = event.getEntity();
        CitizensProvider.getInstance().unregisterEntity(entity);

        npc.setLastDespawnReason(NpcDespawnReason.DEATH);

        NpcDespawnEvent e = new NpcDespawnEvent(npc, NpcDespawnReason.DEATH);
        Nucleus.getEventManager().callBukkit(this, e);
        npc.onNpcDespawn(e);
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    private void onDespawn(NPCDespawnEvent event) {

        Npc npc = CitizensProvider.getInstance().getNpc(event.getNPC());
        if (npc == null || !npc.isSpawned())
            return;

        Entity entity = event.getNPC().getEntity();
        NpcDespawnReason reason;

        switch (event.getReason()) {
            case CHUNK_UNLOAD:
                reason = NpcDespawnReason.CHUNK_UNLOAD;
                break;
            case DEATH:
                return; // finished, handled in different event
            case PENDING_RESPAWN:
                reason = NpcDespawnReason.RESPAWN;
                break;
            case PLUGIN:
                reason = NpcDespawnReason.INVOKED;
                break;
            case REMOVAL:
                reason = NpcDespawnReason.DISPOSED;
                break;
            case WORLD_UNLOAD:
                reason = NpcDespawnReason.WORLD_UNLOAD;
                break;
            default:
                reason = NpcDespawnReason.INVOKED;
                break;
        }

        NpcDespawnEvent e = new NpcDespawnEvent(npc, reason);
        if (e.isCancellable())
            e.setCancelled(event.isCancelled());

        Nucleus.getEventManager().callBukkit(this, e);

        if (!e.isCancelled())
            npc.onNpcDespawn(e);

        if (!e.isCancelled()) {
            npc.setLastDespawnReason(reason);

            if (entity != null)
                CitizensProvider.getInstance().unregisterEntity(entity);
        }

        event.setCancelled(e.isCancelled());
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    private void onClick(NPCClickEvent event) {

        Npc npc = CitizensProvider.getInstance().getNpc(event.getNPC());
        if (npc == null)
            return;

        NpcClickEvent e = new NpcClickEvent(npc, event.getClicker());
        e.setCancelled(event.isCancelled());

        Nucleus.getEventManager().callBukkit(this, e);

        if (!e.isCancelled())
            npc.onNpcClick(e);

        event.setCancelled(e.isCancelled());
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    private void onLeftClick(NPCLeftClickEvent event) {

        Npc npc = CitizensProvider.getInstance().getNpc(event.getNPC());
        if (npc == null)
            return;

        NpcLeftClickEvent e = new NpcLeftClickEvent(npc, event.getClicker());
        e.setCancelled(event.isCancelled());

        Nucleus.getEventManager().callBukkit(this, e);

        if (!e.isCancelled())
            npc.onNpcLeftClick(e);

        event.setCancelled(e.isCancelled());
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    private void onRightClick(NPCRightClickEvent event) {

        Npc npc = CitizensProvider.getInstance().getNpc(event.getNPC());
        if (npc == null)
            return;

        NpcRightClickEvent e = new NpcRightClickEvent(npc, event.getClicker());
        e.setCancelled(event.isCancelled());

        Nucleus.getEventManager().callBukkit(this, e);

        if (!e.isCancelled())
            npc.onNpcRightClick(e);

        event.setCancelled(e.isCancelled());
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    private void onEntityTarget(EntityTargetNPCEvent event) {

        Npc npc = CitizensProvider.getInstance().getNpc(event.getNPC());
        if (npc == null)
            return;

        NpcTargetedEvent e = new NpcTargetedEvent(npc, event.getEntity());
        e.setCancelled(event.isCancelled());

        Nucleus.getEventManager().callBukkit(this, e);

        if (!e.isCancelled())
            npc.onNpcEntityTarget(e);

        event.setCancelled(e.isCancelled());
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    private void onDamage(EntityDamageEvent event) {

        Npc npc = CitizensProvider.getInstance().getNpc(event.getEntity());
        if (npc == null)
            return;

        NpcDamageEvent e = new NpcDamageEvent(npc, event);
        e.setCancelled(event.isCancelled());

        Nucleus.getEventManager().callBukkit(this, e);

        if (!e.isCancelled())
            npc.onNpcDamage(e);

        event.setCancelled(e.isCancelled());
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    private void onDamageByBlock(EntityDamageByBlockEvent event) {

        Npc npc = CitizensProvider.getInstance().getNpc(event.getEntity());
        if (npc == null)
            return;

        NpcDamageByBlockEvent e = new NpcDamageByBlockEvent(npc, event);
        e.setCancelled(event.isCancelled());

        Nucleus.getEventManager().callBukkit(this, e);

        if (!e.isCancelled())
            npc.onNpcDamageByBlock(e);

        event.setCancelled(e.isCancelled());
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    private void onDamageByEntity(EntityDamageByEntityEvent event) {

        Npc npc = CitizensProvider.getInstance().getNpc(event.getEntity());
        if (npc == null)
            return;

        NpcDamageByEntityEvent e = new NpcDamageByEntityEvent(npc, event);
        e.setCancelled(event.isCancelled());

        Nucleus.getEventManager().callBukkit(this, e);

        if (!e.isCancelled())
            npc.onNpcDamageByEntity(e);

        event.setCancelled(e.isCancelled());
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    private void onDeath(EntityDeathEvent event) {

        Npc npc = CitizensProvider.getInstance().getNpc(event.getEntity());
        if (npc == null)
            return;

        NpcDeathEvent e = new NpcDeathEvent(npc, event);
        Nucleus.getEventManager().callBukkit(this, e);
        npc.onNpcDeath(e);
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    private void onPush(NPCPushEvent event) {

        Npc npc = CitizensProvider.getInstance().getNpc(event.getNPC());
        if (npc == null)
            return;

        NpcPushEvent e = new NpcPushEvent(npc, event.getCollisionVector());
        e.setCancelled(event.isCancelled());
        Nucleus.getEventManager().callBukkit(this, e);

        event.setCancelled(e.isCancelled());
    }
}
