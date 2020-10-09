/*
 * Copyright 1999-2018 Alibaba Group Holding Ltd.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.alibaba.csp.sentinel.slots.block.system;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.Arrays;
import java.util.Collections;
import java.util.concurrent.*;

import com.alibaba.csp.sentinel.Entry;
import com.alibaba.csp.sentinel.SphU;
import org.junit.Test;

import com.alibaba.csp.sentinel.Constants;
import com.alibaba.csp.sentinel.EntryType;
import com.alibaba.csp.sentinel.context.Context;
import com.alibaba.csp.sentinel.node.ClusterNode;
import com.alibaba.csp.sentinel.node.DefaultNode;
import com.alibaba.csp.sentinel.slotchain.StringResourceWrapper;
import com.alibaba.csp.sentinel.slots.block.BlockException;
import com.alibaba.csp.sentinel.slots.system.SystemRuleManager;
import com.alibaba.csp.sentinel.slots.system.SystemRule;

/**
 * @author jialiang.linjl
 */
public class SystemRuleTest {

    @Test
    public void testSystemRule_load() {
        SystemRule systemRule = new SystemRule();

        systemRule.setAvgRt(4000L);

        SystemRuleManager.loadRules(Collections.singletonList(systemRule));
    }

    @Test
    public void testSystemRule_avgRt() throws BlockException {

        SystemRule systemRule = new SystemRule();

        systemRule.setAvgRt(4L);

        Context context = mock(Context.class);
        DefaultNode node = mock(DefaultNode.class);
        ClusterNode cn = mock(ClusterNode.class);

        when(context.getOrigin()).thenReturn("");
        when(node.getClusterNode()).thenReturn(cn);

    }

    @Test
    public void test1(){
        SystemRule systemRule = new SystemRule();
        systemRule.setHighestCpuUsage(0.1D);
        SystemRuleManager.loadSystemConf(systemRule);

        try (Entry entry = SphU.entry("HelloWorld",EntryType.IN)) {
            // Your business logic here.
            System.out.println("hello world");
        } catch (BlockException e) {
            // Handle rejected request.
            e.printStackTrace();
        }
    }

    @Test
    public void test2(){
        for (int i = 0; i < 1000; i++) {
            try (Entry entry = SphU.entry("HelloWorld",EntryType.IN)) {
                // Your business logic here.
                System.out.println("hello world");
            } catch (BlockException e) {
                // Handle rejected request.
                e.printStackTrace();
            }
        }
    }

    @Test
    public void test3() throws InterruptedException {
        SystemRule systemRule = new SystemRule();
        systemRule.setMaxThread(2);
        SystemRuleManager.loadSystemConf(systemRule);

        final CountDownLatch countDownLatch = new CountDownLatch(2);
        ExecutorService ex = new ThreadPoolExecutor(3,3,3, TimeUnit.HOURS,new ArrayBlockingQueue<Runnable>(100));
        ex.submit(new Runnable() {
            @Override
            public void run() {
                try {
                    countDownLatch.await();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                System.out.println(1);
                try (Entry entry = SphU.entry("HelloWorld",EntryType.IN)) {
                    // Your business logic here.
                    System.out.println("hello world");
                } catch (BlockException e) {
                    // Handle rejected request.
                    e.printStackTrace();
                }
                System.exit(1);
            }
        });
        for (int i = 0; i < 2; i++) {
            ex.submit(new Runnable() {
                @Override
                public void run() {
                    try (Entry entry = SphU.entry("HelloWorld",EntryType.IN)) {
                        // Your business logic here.
                        System.out.println("hello world");
                        countDownLatch.countDown();
                        TimeUnit.HOURS.sleep(1L);
                    } catch (BlockException | InterruptedException e) {
                        // Handle rejected request.
                        e.printStackTrace();
                    }
                    System.out.println("退出");
                }
            });
        }
        TimeUnit.DAYS.sleep(1L);

    }


}
