package com.alibaba.csp.sentinel.slots.block.flow;

import com.alibaba.csp.sentinel.Entry;
import com.alibaba.csp.sentinel.SphU;
import com.alibaba.csp.sentinel.slots.block.BlockException;
import com.alibaba.csp.sentinel.slots.block.RuleConstant;
import org.junit.Test;

import java.util.Collections;
import java.util.concurrent.*;

public class FlowRuleTests {
    @Test
    public void test1() throws InterruptedException {
        FlowRule r = new FlowRule();
        r.setGrade(RuleConstant.FLOW_GRADE_THREAD).setCount(2).setResource("hello world");
        FlowRuleManager.loadRules(Collections.singletonList(r));

        ThreadPoolExecutor tpe =
                new ThreadPoolExecutor(3, 3, 3, TimeUnit.HOURS, new ArrayBlockingQueue<Runnable>(100));

        final CountDownLatch countDownLatch = new CountDownLatch(1);
        tpe.submit(new Runnable() {
            @Override
            public void run() {
                try (
                        Entry entry = SphU.entry("hello world")
                ){
                    System.out.println("IN");
                    countDownLatch.countDown();
                    TimeUnit.DAYS.sleep(1L);
                } catch (BlockException | InterruptedException e) {
                    e.printStackTrace();
                }
            }
        });

        tpe.submit(new Runnable() {
            @Override
            public void run() {
                try {
                    countDownLatch.await();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                try (
                        Entry entry = SphU.entry("hello world")
                ){
                    System.out.println("IN");
                    TimeUnit.DAYS.sleep(1L);
                } catch (BlockException | InterruptedException e) {
                    e.printStackTrace();
                }
            }
        });


        TimeUnit.DAYS.sleep(1L);
    }
}
