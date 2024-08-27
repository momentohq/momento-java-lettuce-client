package momento.lettuce.utils;

import io.lettuce.core.ExpireArgs;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class ExpireConditionTest {
    @Test
    public void testFromEmptyExpireArgs() {
        var expireArgs = new ExpireArgs();
        var expireCondition = ExpireCondition.fromExpireArgs(expireArgs);
        assertEquals(false, expireCondition.requiresExistingExpiry());
        assertEquals(false, expireCondition.requiresNoExpiry());
        assertEquals(false, expireCondition.requiresLessThan());
        assertEquals(false, expireCondition.requiresGreaterThan());
    }

    @Test
    public void testFromExpireArgsWithXX() {
        var expireArgs = new ExpireArgs().xx();
        var expireCondition = ExpireCondition.fromExpireArgs(expireArgs);
        assertEquals(true, expireCondition.requiresExistingExpiry());
        assertEquals(false, expireCondition.requiresNoExpiry());
        assertEquals(false, expireCondition.requiresLessThan());
        assertEquals(false, expireCondition.requiresGreaterThan());
    }

    @Test
    public void testFromExpireArgsWithNX() {
        var expireArgs = new ExpireArgs().nx();
        var expireCondition = ExpireCondition.fromExpireArgs(expireArgs);
        assertEquals(false, expireCondition.requiresExistingExpiry());
        assertEquals(true, expireCondition.requiresNoExpiry());
        assertEquals(false, expireCondition.requiresLessThan());
        assertEquals(false, expireCondition.requiresGreaterThan());
    }

    @Test
    public void testFromExpireArgsWithXXandNX() {
        var expireArgs = new ExpireArgs().xx().nx();
        var expireCondition = ExpireCondition.fromExpireArgs(expireArgs);

        // Lettuce prefers existing expiry over no expiry when both are set
        assertEquals(true, expireCondition.requiresExistingExpiry());
        assertEquals(false, expireCondition.requiresNoExpiry());

        assertEquals(false, expireCondition.requiresLessThan());
        assertEquals(false, expireCondition.requiresGreaterThan());
    }

    @Test
    public void testFromExpireArgsWithLT() {
        var expireArgs = new ExpireArgs().lt();
        var expireCondition = ExpireCondition.fromExpireArgs(expireArgs);
        assertEquals(false, expireCondition.requiresExistingExpiry());
        assertEquals(false, expireCondition.requiresNoExpiry());
        assertEquals(true, expireCondition.requiresLessThan());
        assertEquals(false, expireCondition.requiresGreaterThan());
    }

    @Test
    public void testFromExpireArgsWithGT() {
        var expireArgs = new ExpireArgs().gt();
        var expireCondition = ExpireCondition.fromExpireArgs(expireArgs);
        assertEquals(false, expireCondition.requiresExistingExpiry());
        assertEquals(false, expireCondition.requiresNoExpiry());
        assertEquals(false, expireCondition.requiresLessThan());
        assertEquals(true, expireCondition.requiresGreaterThan());
    }

    @Test
    public void testFromExpireArgsWithLTandGT() {
        var expireArgs = new ExpireArgs().lt().gt();
        var expireCondition = ExpireCondition.fromExpireArgs(expireArgs);
        assertEquals(false, expireCondition.requiresExistingExpiry());
        assertEquals(false, expireCondition.requiresNoExpiry());

        // Lettuce prefers less than over greater than when both are set
        assertEquals(true, expireCondition.requiresLessThan());
        assertEquals(false, expireCondition.requiresGreaterThan());
    }

    @Test
    public void testFromExpireArgsWithLTandNX() {
        var expireArgs = new ExpireArgs().lt().nx();
        var expireCondition = ExpireCondition.fromExpireArgs(expireArgs);
        assertEquals(false, expireCondition.requiresExistingExpiry());
        assertEquals(true, expireCondition.requiresNoExpiry());
        assertEquals(true, expireCondition.requiresLessThan());
        assertEquals(false, expireCondition.requiresGreaterThan());
    }

    @Test
    public void testFromExpireArgsWithGTandNX() {
        var expireArgs = new ExpireArgs().gt().nx();
        var expireCondition = ExpireCondition.fromExpireArgs(expireArgs);
        assertEquals(false, expireCondition.requiresExistingExpiry());
        assertEquals(true, expireCondition.requiresNoExpiry());
        assertEquals(false, expireCondition.requiresLessThan());
        assertEquals(true, expireCondition.requiresGreaterThan());
    }

    @Test
    public void testFromExpireArgsWithLTandXX() {
        var expireArgs = new ExpireArgs().lt().xx();
        var expireCondition = ExpireCondition.fromExpireArgs(expireArgs);
        assertEquals(true, expireCondition.requiresExistingExpiry());
        assertEquals(false, expireCondition.requiresNoExpiry());
        assertEquals(true, expireCondition.requiresLessThan());
        assertEquals(false, expireCondition.requiresGreaterThan());
    }

    @Test
    public void testFromExpireArgsWithGTandXX() {
        var expireArgs = new ExpireArgs().gt().xx();
        var expireCondition = ExpireCondition.fromExpireArgs(expireArgs);
        assertEquals(true, expireCondition.requiresExistingExpiry());
        assertEquals(false, expireCondition.requiresNoExpiry());
        assertEquals(false, expireCondition.requiresLessThan());
        assertEquals(true, expireCondition.requiresGreaterThan());
    }
}
