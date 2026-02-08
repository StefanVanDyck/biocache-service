package au.org.ala.biocache.dao;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.runners.MockitoJUnitRunner;
import org.springframework.http.HttpStatus;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.server.ResponseStatusException;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

/**
 * Unit tests for parameter validation in SearchDAOImpl (capFlimit, capPageSize).
 */
@RunWith(MockitoJUnitRunner.class)
public class SearchDAOImplTest {

    @InjectMocks
    SearchDAOImpl searchDAO;

    private Method capFlimitMethod;
    private Method capPageSizeMethod;

    @Before
    public void setUp() throws Exception {
        // Access private methods via reflection
        capFlimitMethod = SearchDAOImpl.class.getDeclaredMethod("capFlimit", int.class);
        capFlimitMethod.setAccessible(true);

        capPageSizeMethod = SearchDAOImpl.class.getDeclaredMethod("capPageSize", int.class);
        capPageSizeMethod.setAccessible(true);
    }

    // ---- capFlimit tests ----

    @Test
    public void testCapFlimit_withinLimit_passesThrough() throws Exception {
        ReflectionTestUtils.setField(searchDAO, "flimitMax", 1000);
        int result = (int) capFlimitMethod.invoke(searchDAO, 500);
        assertEquals(500, result);
    }

    @Test
    public void testCapFlimit_atExactLimit_passesThrough() throws Exception {
        ReflectionTestUtils.setField(searchDAO, "flimitMax", 1000);
        int result = (int) capFlimitMethod.invoke(searchDAO, 1000);
        assertEquals(1000, result);
    }

    @Test
    public void testCapFlimit_zeroValue_passesThrough() throws Exception {
        ReflectionTestUtils.setField(searchDAO, "flimitMax", 1000);
        int result = (int) capFlimitMethod.invoke(searchDAO, 0);
        assertEquals(0, result);
    }

    @Test
    public void testCapFlimit_exceedsLimit_throws400() throws Exception {
        ReflectionTestUtils.setField(searchDAO, "flimitMax", 1000);
        try {
            capFlimitMethod.invoke(searchDAO, 1001);
            fail("Expected ResponseStatusException");
        } catch (InvocationTargetException e) {
            ResponseStatusException rse = (ResponseStatusException) e.getCause();
            assertEquals(HttpStatus.BAD_REQUEST, rse.getStatus());
            assertEquals("Requested flimit=1001 exceeds the maximum allowed value of 1000", rse.getReason());
        }
    }

    @Test
    public void testCapFlimit_negativeOne_throws400() throws Exception {
        ReflectionTestUtils.setField(searchDAO, "flimitMax", 1000);
        try {
            capFlimitMethod.invoke(searchDAO, -1);
            fail("Expected ResponseStatusException");
        } catch (InvocationTargetException e) {
            ResponseStatusException rse = (ResponseStatusException) e.getCause();
            assertEquals(HttpStatus.BAD_REQUEST, rse.getStatus());
            assertEquals("Requested flimit=-1 exceeds the maximum allowed value of 1000", rse.getReason());
        }
    }

    @Test
    public void testCapFlimit_veryLargeValue_throws400() throws Exception {
        ReflectionTestUtils.setField(searchDAO, "flimitMax", 1000);
        try {
            capFlimitMethod.invoke(searchDAO, Integer.MAX_VALUE);
            fail("Expected ResponseStatusException");
        } catch (InvocationTargetException e) {
            ResponseStatusException rse = (ResponseStatusException) e.getCause();
            assertEquals(HttpStatus.BAD_REQUEST, rse.getStatus());
        }
    }

    @Test
    public void testCapFlimit_disabled_allowsAnyValue() throws Exception {
        ReflectionTestUtils.setField(searchDAO, "flimitMax", -1);
        int result = (int) capFlimitMethod.invoke(searchDAO, 999999);
        assertEquals(999999, result);
    }

    @Test
    public void testCapFlimit_disabled_allowsUnlimited() throws Exception {
        ReflectionTestUtils.setField(searchDAO, "flimitMax", -1);
        int result = (int) capFlimitMethod.invoke(searchDAO, -1);
        assertEquals(-1, result);
    }

    @Test
    public void testCapFlimit_disabled_allowsZero() throws Exception {
        ReflectionTestUtils.setField(searchDAO, "flimitMax", -1);
        int result = (int) capFlimitMethod.invoke(searchDAO, 0);
        assertEquals(0, result);
    }

    // ---- capPageSize tests ----

    @Test
    public void testCapPageSize_withinLimit_passesThrough() throws Exception {
        ReflectionTestUtils.setField(searchDAO, "pageSizeMax", 1000);
        int result = (int) capPageSizeMethod.invoke(searchDAO, 500);
        assertEquals(500, result);
    }

    @Test
    public void testCapPageSize_atExactLimit_passesThrough() throws Exception {
        ReflectionTestUtils.setField(searchDAO, "pageSizeMax", 1000);
        int result = (int) capPageSizeMethod.invoke(searchDAO, 1000);
        assertEquals(1000, result);
    }

    @Test
    public void testCapPageSize_zeroValue_passesThrough() throws Exception {
        ReflectionTestUtils.setField(searchDAO, "pageSizeMax", 1000);
        int result = (int) capPageSizeMethod.invoke(searchDAO, 0);
        assertEquals(0, result);
    }

    @Test
    public void testCapPageSize_exceedsLimit_throws400() throws Exception {
        ReflectionTestUtils.setField(searchDAO, "pageSizeMax", 1000);
        try {
            capPageSizeMethod.invoke(searchDAO, 1001);
            fail("Expected ResponseStatusException");
        } catch (InvocationTargetException e) {
            ResponseStatusException rse = (ResponseStatusException) e.getCause();
            assertEquals(HttpStatus.BAD_REQUEST, rse.getStatus());
            assertEquals("Requested pageSize=1001 exceeds the maximum allowed value of 1000", rse.getReason());
        }
    }

    @Test
    public void testCapPageSize_negativeOne_throws400() throws Exception {
        ReflectionTestUtils.setField(searchDAO, "pageSizeMax", 1000);
        try {
            capPageSizeMethod.invoke(searchDAO, -1);
            fail("Expected ResponseStatusException");
        } catch (InvocationTargetException e) {
            ResponseStatusException rse = (ResponseStatusException) e.getCause();
            assertEquals(HttpStatus.BAD_REQUEST, rse.getStatus());
            assertEquals("Requested pageSize=-1 exceeds the maximum allowed value of 1000", rse.getReason());
        }
    }

    @Test
    public void testCapPageSize_veryLargeValue_throws400() throws Exception {
        ReflectionTestUtils.setField(searchDAO, "pageSizeMax", 1000);
        try {
            capPageSizeMethod.invoke(searchDAO, Integer.MAX_VALUE);
            fail("Expected ResponseStatusException");
        } catch (InvocationTargetException e) {
            ResponseStatusException rse = (ResponseStatusException) e.getCause();
            assertEquals(HttpStatus.BAD_REQUEST, rse.getStatus());
        }
    }

    @Test
    public void testCapPageSize_disabled_allowsAnyValue() throws Exception {
        ReflectionTestUtils.setField(searchDAO, "pageSizeMax", -1);
        int result = (int) capPageSizeMethod.invoke(searchDAO, 999999);
        assertEquals(999999, result);
    }

    @Test
    public void testCapPageSize_disabled_allowsUnlimited() throws Exception {
        ReflectionTestUtils.setField(searchDAO, "pageSizeMax", -1);
        int result = (int) capPageSizeMethod.invoke(searchDAO, -1);
        assertEquals(-1, result);
    }

    @Test
    public void testCapPageSize_disabled_allowsZero() throws Exception {
        ReflectionTestUtils.setField(searchDAO, "pageSizeMax", -1);
        int result = (int) capPageSizeMethod.invoke(searchDAO, 0);
        assertEquals(0, result);
    }

    // ---- Edge case: max set to zero ----

    @Test
    public void testCapFlimit_maxZero_onlyZeroAllowed() throws Exception {
        ReflectionTestUtils.setField(searchDAO, "flimitMax", 0);
        int result = (int) capFlimitMethod.invoke(searchDAO, 0);
        assertEquals(0, result);
    }

    @Test
    public void testCapFlimit_maxZero_rejectsPositive() throws Exception {
        ReflectionTestUtils.setField(searchDAO, "flimitMax", 0);
        try {
            capFlimitMethod.invoke(searchDAO, 1);
            fail("Expected ResponseStatusException");
        } catch (InvocationTargetException e) {
            ResponseStatusException rse = (ResponseStatusException) e.getCause();
            assertEquals(HttpStatus.BAD_REQUEST, rse.getStatus());
        }
    }

    @Test
    public void testCapPageSize_maxZero_onlyZeroAllowed() throws Exception {
        ReflectionTestUtils.setField(searchDAO, "pageSizeMax", 0);
        int result = (int) capPageSizeMethod.invoke(searchDAO, 0);
        assertEquals(0, result);
    }

    @Test
    public void testCapPageSize_maxZero_rejectsPositive() throws Exception {
        ReflectionTestUtils.setField(searchDAO, "pageSizeMax", 0);
        try {
            capPageSizeMethod.invoke(searchDAO, 1);
            fail("Expected ResponseStatusException");
        } catch (InvocationTargetException e) {
            ResponseStatusException rse = (ResponseStatusException) e.getCause();
            assertEquals(HttpStatus.BAD_REQUEST, rse.getStatus());
        }
    }
}
