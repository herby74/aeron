/*
 * Copyright 2014-2019 Real Logic Ltd.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.aeron.cluster;

import org.agrona.ErrorHandler;
import org.agrona.concurrent.AgentTerminationException;

import java.lang.management.ManagementFactory;
import java.lang.management.ThreadInfo;
import java.lang.management.ThreadMXBean;
import java.util.concurrent.atomic.AtomicBoolean;

import static org.junit.Assert.fail;

class TestUtil
{
    public static final Runnable TERMINATION_HOOK =
        () ->
        {
            throw new AgentTerminationException();
        };

    public static void checkInterruptedStatus()
    {
        if (Thread.currentThread().isInterrupted())
        {
            fail("unexpected interrupt - test likely to have timed out");
        }
    }

    public static Runnable dynamicTerminationHook(
        final AtomicBoolean terminationExpected, final AtomicBoolean wasTerminated)
    {
        return () ->
        {
            if (null == terminationExpected || !terminationExpected.get())
            {
                throw new AgentTerminationException();
            }

            if (null != wasTerminated)
            {
                wasTerminated.set(true);
            }
        };
    }

    public static ErrorHandler errorHandler(final int nodeId)
    {
        return (ex) ->
        {
            System.err.println("\n*** Error in node " + nodeId + " followed by system thread dump ***\n\n");
            ex.printStackTrace();

            System.err.println();
            System.err.println(TestUtil.threadDump());
        };
    }

    // TODO: Use version from Agrona after next release
    public static String threadDump()
    {
        final StringBuilder sb = new StringBuilder();
        final ThreadMXBean threadMXBean = ManagementFactory.getThreadMXBean();

        for (final ThreadInfo info : threadMXBean.getThreadInfo(threadMXBean.getAllThreadIds(), Integer.MAX_VALUE))
        {
            sb.append('"').append(info.getThreadName()).append("\": ").append(info.getThreadState());

            for (final StackTraceElement stackTraceElement : info.getStackTrace())
            {
                sb.append("\n    at ").append(stackTraceElement.toString());
            }

            sb.append("\n\n");
        }

        return sb.toString();
    }
}
