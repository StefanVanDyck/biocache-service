package au.org.ala.biocache.dao;

import au.org.ala.biocache.dto.SpatialSearchRequestDTO;
import au.org.ala.biocache.util.QueryFormatUtils;
import au.org.ala.biocache.util.RangeBasedFacets;
import au.org.ala.biocache.util.solr.FieldMappingUtil;
import org.apache.solr.client.solrj.SolrQuery;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests for facet range support in SearchDAOImpl.initSolrQuery().
 */
@RunWith(MockitoJUnitRunner.class)
public class SearchDAOImplFacetRangeTest {

    @InjectMocks
    SearchDAOImpl searchDAO;

    @Mock
    QueryFormatUtils queryFormatUtils;

    @Mock
    RangeBasedFacets rangeBasedFacets;

    @Mock
    FieldMappingUtil fieldMappingUtil;

    @Before
    public void setUp() {
        // Set required @Value fields that initSolrQuery depends on
        ReflectionTestUtils.setField(searchDAO, "flimitMax", -1);
        ReflectionTestUtils.setField(searchDAO, "pageSizeMax", -1);
    }

    private SpatialSearchRequestDTO createBasicParams() {
        SpatialSearchRequestDTO params = new SpatialSearchRequestDTO();
        params.setQ("*:*");
        params.setFormattedQuery("*:*");
        params.setFacet(true);
        params.setFacets(new String[0]);
        params.setPivotFacets(new String[0]);
        params.setFacetRanges(new String[0]);
        return params;
    }

    // ---- Facet range fields are added to SolrQuery ----

    @Test
    public void testInitSolrQuery_facetRangeFieldsAdded() throws Exception {
        SpatialSearchRequestDTO params = createBasicParams();
        params.setFacetRanges(new String[]{"year"});
        params.setFacetRangeStart("2000");
        params.setFacetRangeEnd("2024");
        params.setFacetRangeGap("1");

        SolrQuery solrQuery = searchDAO.initSolrQuery(params, false, null);

        String[] ranges = solrQuery.getParams("facet.range");
        assertNotNull("facet.range should be present", ranges);
        assertEquals(1, ranges.length);
        assertEquals("year", ranges[0]);

        assertEquals("2000", solrQuery.get("facet.range.start"));
        assertEquals("2024", solrQuery.get("facet.range.end"));
        assertEquals("1", solrQuery.get("facet.range.gap"));
    }

    @Test
    public void testInitSolrQuery_multipleFacetRangeFields() throws Exception {
        SpatialSearchRequestDTO params = createBasicParams();
        params.setFacetRanges(new String[]{"year", "month"});
        params.setFacetRangeStart("0");
        params.setFacetRangeEnd("100");
        params.setFacetRangeGap("10");

        SolrQuery solrQuery = searchDAO.initSolrQuery(params, false, null);

        String[] ranges = solrQuery.getParams("facet.range");
        assertNotNull("facet.range should be present", ranges);
        assertEquals(2, ranges.length);
        assertTrue(Arrays.asList(ranges).contains("year"));
        assertTrue(Arrays.asList(ranges).contains("month"));
    }

    @Test
    public void testInitSolrQuery_facetRangeOtherIncluded() throws Exception {
        SpatialSearchRequestDTO params = createBasicParams();
        params.setFacetRanges(new String[]{"year"});
        params.setFacetRangeStart("2000");
        params.setFacetRangeEnd("2024");
        params.setFacetRangeGap("1");

        SolrQuery solrQuery = searchDAO.initSolrQuery(params, false, null);

        String[] other = solrQuery.getParams("facet.range.other");
        assertNotNull("facet.range.other should be present", other);
        assertTrue("Should contain 'before'", Arrays.asList(other).contains("before"));
        assertTrue("Should contain 'after'", Arrays.asList(other).contains("after"));
    }

    // ---- No facet range when empty ----

    @Test
    public void testInitSolrQuery_noFacetRangeWhenEmpty() throws Exception {
        SpatialSearchRequestDTO params = createBasicParams();

        SolrQuery solrQuery = searchDAO.initSolrQuery(params, false, null);

        assertNull("facet.range should not be present", solrQuery.getParams("facet.range"));
        assertNull("facet.range.start should not be present", solrQuery.get("facet.range.start"));
        assertNull("facet.range.end should not be present", solrQuery.get("facet.range.end"));
        assertNull("facet.range.gap should not be present", solrQuery.get("facet.range.gap"));
    }

    // ---- Partial range params ----

    @Test
    public void testInitSolrQuery_facetRangeWithGapOnly() throws Exception {
        SpatialSearchRequestDTO params = createBasicParams();
        params.setFacetRanges(new String[]{"year"});
        params.setFacetRangeGap("5");

        SolrQuery solrQuery = searchDAO.initSolrQuery(params, false, null);

        String[] ranges = solrQuery.getParams("facet.range");
        assertNotNull("facet.range should be present", ranges);
        assertEquals("year", ranges[0]);

        assertEquals("5", solrQuery.get("facet.range.gap"));
        assertNull("facet.range.start should not be set", solrQuery.get("facet.range.start"));
        assertNull("facet.range.end should not be set", solrQuery.get("facet.range.end"));
    }

    @Test
    public void testInitSolrQuery_facetRangeWithStartAndEndOnly() throws Exception {
        SpatialSearchRequestDTO params = createBasicParams();
        params.setFacetRanges(new String[]{"year"});
        params.setFacetRangeStart("2000");
        params.setFacetRangeEnd("2024");

        SolrQuery solrQuery = searchDAO.initSolrQuery(params, false, null);

        assertEquals("2000", solrQuery.get("facet.range.start"));
        assertEquals("2024", solrQuery.get("facet.range.end"));
        assertNull("facet.range.gap should not be set", solrQuery.get("facet.range.gap"));
    }

    // ---- Range params only applied when facetRanges has fields ----

    @Test
    public void testInitSolrQuery_gapIgnoredWithoutFacetRangeFields() throws Exception {
        SpatialSearchRequestDTO params = createBasicParams();
        // Set gap but no facet range fields
        params.setFacetRangeGap("5");
        params.setFacetRangeStart("0");
        params.setFacetRangeEnd("100");

        SolrQuery solrQuery = searchDAO.initSolrQuery(params, false, null);

        assertNull("facet.range should not be present", solrQuery.getParams("facet.range"));
        assertNull("facet.range.gap should not be set without range fields", solrQuery.get("facet.range.gap"));
        assertNull("facet.range.start should not be set without range fields", solrQuery.get("facet.range.start"));
        assertNull("facet.range.end should not be set without range fields", solrQuery.get("facet.range.end"));
    }

    // ---- facet.range.other not duplicated when extra params also present ----

    @Test
    public void testInitSolrQuery_rangeOtherNotDuplicatedWithExtraParams() throws Exception {
        SpatialSearchRequestDTO params = createBasicParams();
        params.setFacetRanges(new String[]{"year"});
        params.setFacetRangeGap("1");
        params.setFacetRangeStart("2000");
        params.setFacetRangeEnd("2024");

        // Extra params that don't include range params
        Map<String, String[]> extraParams = new HashMap<>();
        extraParams.put("someCustomParam", new String[]{"value"});

        SolrQuery solrQuery = searchDAO.initSolrQuery(params, false, extraParams);

        String[] other = solrQuery.getParams("facet.range.other");
        assertNotNull("facet.range.other should be present", other);
        // Should have exactly 2 values: "before" and "after" (not duplicated)
        long beforeCount = Arrays.stream(other).filter("before"::equals).count();
        long afterCount = Arrays.stream(other).filter("after"::equals).count();
        assertEquals("'before' should appear exactly once", 1, beforeCount);
        assertEquals("'after' should appear exactly once", 1, afterCount);
    }

    // ---- Extra params still work without DTO range fields ----

    @Test
    public void testInitSolrQuery_extraParamsStillPassedThrough() throws Exception {
        SpatialSearchRequestDTO params = createBasicParams();

        Map<String, String[]> extraParams = new HashMap<>();
        extraParams.put("facet.range", new String[]{"price"});
        extraParams.put("facet.range.start", new String[]{"0"});
        extraParams.put("facet.range.end", new String[]{"1000"});
        extraParams.put("facet.range.gap", new String[]{"100"});

        SolrQuery solrQuery = searchDAO.initSolrQuery(params, false, extraParams);

        // Extra params should still be passed through
        String[] ranges = solrQuery.getParams("facet.range");
        assertNotNull(ranges);
        assertTrue(Arrays.asList(ranges).contains("price"));

        // facet.range.other should be added for the extra params
        String[] other = solrQuery.getParams("facet.range.other");
        assertNotNull(other);
        assertTrue(Arrays.asList(other).contains("before"));
        assertTrue(Arrays.asList(other).contains("after"));
    }

    // ---- Facet disabled ----

    @Test
    public void testInitSolrQuery_facetRangeNotAddedWhenFacetDisabled() throws Exception {
        SpatialSearchRequestDTO params = createBasicParams();
        params.setFacet(false);
        params.setFacetRanges(new String[]{"year"});
        params.setFacetRangeGap("1");
        params.setFacetRangeStart("2000");
        params.setFacetRangeEnd("2024");

        SolrQuery solrQuery = searchDAO.initSolrQuery(params, false, null);

        assertNull("facet.range should not be set when faceting is disabled",
                solrQuery.getParams("facet.range"));
    }

    // ---- Date-based range facets ----

    @Test
    public void testInitSolrQuery_dateBasedFacetRange() throws Exception {
        SpatialSearchRequestDTO params = createBasicParams();
        params.setFacetRanges(new String[]{"eventDate"});
        params.setFacetRangeStart("2000-01-01T00:00:00Z");
        params.setFacetRangeEnd("2024-01-01T00:00:00Z");
        params.setFacetRangeGap("+1YEAR");

        SolrQuery solrQuery = searchDAO.initSolrQuery(params, false, null);

        String[] ranges = solrQuery.getParams("facet.range");
        assertNotNull(ranges);
        assertEquals("eventDate", ranges[0]);
        assertEquals("2000-01-01T00:00:00Z", solrQuery.get("facet.range.start"));
        assertEquals("2024-01-01T00:00:00Z", solrQuery.get("facet.range.end"));
        assertEquals("+1YEAR", solrQuery.get("facet.range.gap"));
    }
}
