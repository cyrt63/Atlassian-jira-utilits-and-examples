package com.atlassian.pocketknife.api.util.runners;

import com.google.common.collect.Lists;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

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
        assertFalse(seal.isSealBroken());
        runner.breakSeal("first.key");
        assertFalse(seal.isSealBroken());
        runner.breakSeal("second.key");
        assertTrue(seal.isSealBroken());
    }

    @Test
    public void testSealedRunnerCaseSensitivity() throws Exception {
        final Seal seal = new Seal("for runnable");
        SealedRunner runner = new SealedRunner(Lists.newArrayList("first.key", "FIRST.KEY"), new Runnable() {
            @Override
            public void run() {
                seal.breakSeal();
            }
        });
        assertFalse(seal.isSealBroken());
        runner.breakSeal("first.key");
        assertFalse(seal.isSealBroken());
        runner.breakSeal("FIRST.KEY");
        assertTrue(seal.isSealBroken());
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
        assertFalse(seal.isSealBroken());
        runner.breakSeal("first.key");
        assertFalse(seal.isSealBroken());
        runner.breakSeal("first.key");
        assertFalse(seal.isSealBroken());
        runner.breakSeal("second.key");
        assertTrue(seal.isSealBroken());
        runner.breakSeal("second.key");
        assertTrue(seal.isSealBroken());
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
        assertFalse(seal.isSealBroken());
        runner.breakSeal("first.key");
        assertFalse(seal.isSealBroken());
        runner.repairSeal("first.key");
        assertFalse(seal.isSealBroken());
        runner.breakSeal("second.key");
        assertFalse(seal.isSealBroken());
        runner.repairSeal("second.key");
        assertFalse(seal.isSealBroken());
    }

    @Test(expected = IllegalStateException.class)
    public void test_repair_throws_exception_if_run() throws Exception {
        final Seal seal = new Seal("for runnable");
        SealedRunner runner = new SealedRunner(Lists.newArrayList("first.key", "second.key"), new Runnable() {
            @Override
            public void run() {
                seal.breakSeal();
            }
        });
        runner.breakSeal("first.key");
        runner.breakSeal("second.key");
        assertTrue(runner.hasRun());

        runner.repairSeal("first.key");
    }

    @Test(expected = IllegalArgumentException.class)
    public void testEmptyRunner() throws Exception {
        final Seal seal = new Seal("for runnable");
        SealedRunner runner = new SealedRunner(Lists.<String>newArrayList(), new Runnable() {
            @Override
            public void run() {
                seal.breakSeal();
            }
        });
        assertFalse(seal.isSealBroken());
        runner.breakSeal("first.key");
    }

    @Test(expected = IllegalArgumentException.class)
    public void testNulls() throws Exception {
        final Seal seal = new Seal("for runnable");
        SealedRunner runner = new SealedRunner(Lists.newArrayList("first.key", "second.key"), new Runnable() {
            @Override
            public void run() {
                seal.breakSeal();
            }
        });
        assertFalse(seal.isSealBroken());
        runner.breakSeal(null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testNulls2() throws Exception {
        final Seal seal = new Seal("for runnable");
        SealedRunner runner = new SealedRunner(Lists.newArrayList("first.key", "second.key"), new Runnable() {
            @Override
            public void run() {
                seal.breakSeal();
            }
        });
        assertFalse(seal.isSealBroken());
        runner.repairSeal(null);
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
        assertFalse(seal.isSealBroken());
        runner.breakSeal("first.key");
        assertFalse(seal.isSealBroken());
        runner.breakSeal("second.key");
        assertTrue(seal.isSealBroken());
        assertEquals(seal.getTimesBroken(), 1);
        runner.breakSeal("first.key");
        runner.breakSeal("second.key");
        assertEquals(seal.getTimesBroken(), 1);
        runner.breakSeal("first.key");
        runner.breakSeal("second.key");
        assertEquals(seal.getTimesBroken(), 1);
    }
}
