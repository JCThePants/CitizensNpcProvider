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
import com.jcwhatever.nucleus.messaging.IMessenger;

/**
 * Console message helper utility.
 */
public class Msg {

    private Msg() {}

    /**
     * Write a debug message to the console and log. Message is
     * disregarded unless debugging is turned on.
     *
     * @param message  The message to write.
     * @param params   Optional format parameters.
     */
    public static void debug(String message, Object...params) {
        if (!Nucleus.getPlugin().isDebugging())
            return;

        msg().debug(prefix(message), params);
    }

    /**
     * Write an info message to the console and log.
     *
     * @param message  The message to write.
     * @param params   Optional format parameters.
     */
    public static void info(String message, Object...params) {
        msg().info(prefix(message), params);
    }

    /**
     * Write a warning message to the console and log.
     *
     * @param message  The message to write.
     * @param params   Optional format parameters.
     */
    public static void warning(String message, Object...params) {
        msg().warning(prefix(message), params);
    }

    /**
     * Write a severe error message to the console and log.
     *
     * @param message  The message to write.
     * @param params   Optional format parameters.
     */
    public static void severe(String message, Object...params) {
        msg().severe(prefix(message), params);
    }

    private static String prefix(String message) {
        return "[CitizensNPCProvider] " + message;
    }

    private static IMessenger msg() {
        return Nucleus.getPlugin().getAnonMessenger();
    }
}
