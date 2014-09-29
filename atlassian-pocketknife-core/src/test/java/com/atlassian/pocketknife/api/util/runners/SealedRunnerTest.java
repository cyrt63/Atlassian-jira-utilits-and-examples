package com.atlassian.pocketknife.api.util.runners;

import com.google.common.collect.Lists;
import org.junit.Assert;
import org.junit.Test;

public class SealedRunnerTest {
    @Test
    public void testSealedRunner() throws Exception {
        final Seal seal = new Seal("for runnable");
        SealedRunner runner = new SealedRunner(Lists.newArrayList("first.key", "second.key"), new Runnable() {
            @Override
            public void run() {
                seal.breakSeal();
            }
        });
        Assert.assertFalse(seal.isSealBroken());
        runner.breakSeal("first.key");
        Assert.assertFalse(seal.isSealBroken());
        runner.breakSeal("second.key");
        Assert.assertTrue(seal.isSealBroken());
    }

    @Test
    public void testMultipleSealBreaks() throws Exception {
        final Seal seal = new Seal("for runnable");
        SealedRunner runner = new SealedRunner(Lists.newArrayList("first.key", "second.key"), new Runnable() {
            @Override
            public void run() {
                seal.breakSeal();
            }
        });
        Assert.assertFalse(seal.isSealBroken());
        runner.breakSeal("first.key");
        Assert.assertFalse(seal.isSealBroken());
        runner.breakSeal("first.key");
        Assert.assertFalse(seal.isSealBroken());
        runner.breakSeal("second.key");
        Assert.assertTrue(seal.isSealBroken());
        runner.breakSeal("second.key");
        Assert.assertTrue(seal.isSealBroken());
    }

    @Test
    public void testSealRepair() throws Exception {
        final Seal seal = new Seal("for runnable");
        SealedRunner runner = new SealedRunner(Lists.newArrayList("first.key", "second.key"), new Runnable() {
            @Override
            public void run() {
                seal.breakSeal();
            }
        });
        Assert.assertFalse(seal.isSealBroken());
        runner.breakSeal("first.key");
        Assert.assertFalse(seal.isSealBroken());
        runner.repairSeal("first.key");
        Assert.assertFalse(seal.isSealBroken());
        runner.breakSeal("second.key");
        Assert.assertFalse(seal.isSealBroken());
        runner.repairSeal("second.key");
        Assert.assertFalse(seal.isSealBroken());
    }

    @Test
    public void testEmptyRunner() throws Exception {
        final Seal seal = new Seal("for runnable");
        SealedRunner runner = new SealedRunner(Lists.<String>newArrayList(), new Runnable() {
            @Override
            public void run() {
                seal.breakSeal();
            }
        });
        Assert.assertFalse(seal.isSealBroken());
        runner.breakSeal("first.key");
        Assert.assertFalse(seal.isSealBroken());
        runner.breakSeal("second.key");
        Assert.assertFalse(seal.isSealBroken());
    }

    @Test
    public void testNulls() throws Exception {
        final Seal seal = new Seal("for runnable");
        SealedRunner runner = new SealedRunner(Lists.newArrayList("first.key", "second.key"), new Runnable() {
            @Override
            public void run() {
                seal.breakSeal();
            }
        });
        Assert.assertFalse(seal.isSealBroken());
        runner.breakSeal(null);
        Assert.assertFalse(seal.isSealBroken());
        runner.repairSeal(null);
        Assert.assertFalse(seal.isSealBroken());
    }

    @Test
    public void testRunnableOnlyRunsOnce() throws Exception {
        final Seal seal = new Seal("for runnable");
        SealedRunner runner = new SealedRunner(Lists.newArrayList("first.key", "second.key"), new Runnable() {
            @Override
            public void run() {
                seal.breakSeal();
            }
        });
        Assert.assertFalse(seal.isSealBroken());
        runner.breakSeal("first.key");
        Assert.assertFalse(seal.isSealBroken());
        runner.breakSeal("second.key");
        Assert.assertTrue(seal.isSealBroken());
        Assert.assertEquals(seal.getTimesBroken(), 1);
        runner.breakSeal("first.key");
        runner.breakSeal("second.key");
        Assert.assertEquals(seal.getTimesBroken(), 1);
        runner.repairSeal("first.key");
        runner.repairSeal("second.key");
        runner.breakSeal("first.key");
        runner.breakSeal("second.key");
        Assert.assertEquals(seal.getTimesBroken(), 1);
    }
}
