package au.org.ala.biocache.dao;

import au.org.ala.biocache.dto.FieldStatsItem;
import au.org.ala.biocache.dto.SearchRequestDTO;
import au.org.ala.biocache.dto.SearchResultDTO;
import au.org.ala.biocache.util.QueryFormatUtils;
import au.org.ala.biocache.util.RangeBasedFacets;
import au.org.ala.biocache.util.solr.FieldMappingUtil;
import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.client.solrj.response.FieldStatsInfo;
import org.apache.solr.client.solrj.response.QueryResponse;
import org.apache.solr.common.SolrDocumentList;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.springframework.test.util.ReflectionTestUtils;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests for field stats extraction in SearchDAOImpl.processSolrResponse().
 */
@RunWith(MockitoJUnitRunner.class)
public class SearchDAOImplFieldStatsTest {

    @InjectMocks
    SearchDAOImpl searchDAO;

    @Mock
    QueryFormatUtils queryFormatUtils;

    @Mock
    RangeBasedFacets rangeBasedFacets;

    @Mock
    FieldMappingUtil fieldMappingUtil;

    private Method processSolrResponse;

    @Before
    public void setUp() throws Exception {
        ReflectionTestUtils.setField(searchDAO, "flimitMax", -1);
        ReflectionTestUtils.setField(searchDAO, "pageSizeMax", -1);

        // Get access to the private processSolrResponse method
        processSolrResponse = SearchDAOImpl.class.getDeclaredMethod(
                "processSolrResponse", SearchRequestDTO.class, QueryResponse.class,
                SolrQuery.class, Class.class);
        processSolrResponse.setAccessible(true);
    }

    private SearchRequestDTO createBasicParams() {
        SearchRequestDTO params = new SearchRequestDTO();
        params.setQ("*:*");
        params.setFormattedQuery("*:*");
        params.setFacet(false);
        params.setFacets(new String[0]);
        return params;
    }

    private QueryResponse createMockResponse(Map<String, FieldStatsInfo> statsMap) {
        QueryResponse qr = mock(QueryResponse.class);

        SolrDocumentList sdl = new SolrDocumentList();
        sdl.setNumFound(100);
        sdl.setStart(0);
        when(qr.getResults()).thenReturn(sdl);
        when(qr.getFacetFields()).thenReturn(null);
        when(qr.getFacetPivot()).thenReturn(null);
        when(qr.getFacetDates()).thenReturn(null);
        when(qr.getFacetQuery()).thenReturn(null);
        when(qr.getFacetRanges()).thenReturn(null);
        when(qr.getFieldStatsInfo()).thenReturn(statsMap);

        return qr;
    }

    // ---- Stats extraction when stats are present ----

    @Test
    public void testProcessSolrResponse_extractsFieldStats() throws Exception {
        // Create a mock FieldStatsInfo for 'year'
        FieldStatsInfo yearStats = mock(FieldStatsInfo.class);
        when(yearStats.getMin()).thenReturn(1950);
        when(yearStats.getMax()).thenReturn(2024);
        when(yearStats.getCount()).thenReturn(5000L);
        when(yearStats.getMissing()).thenReturn(42L);
        when(yearStats.getSum()).thenReturn(null);
        when(yearStats.getMean()).thenReturn(1990.5);
        when(yearStats.getStddev()).thenReturn(15.3);
        when(yearStats.getName()).thenReturn("year");
        when(yearStats.getCountDistinct()).thenReturn(75L);

        Map<String, FieldStatsInfo> statsMap = new HashMap<>();
        statsMap.put("year", yearStats);

        QueryResponse qr = createMockResponse(statsMap);
        SearchRequestDTO params = createBasicParams();
        SolrQuery solrQuery = new SolrQuery("*:*");

        SearchResultDTO result = (SearchResultDTO) processSolrResponse.invoke(
                searchDAO, params, qr, solrQuery, null);

        assertNotNull("fieldStats should not be null when stats are present",
                result.getFieldStats());
        assertEquals(1, result.getFieldStats().size());
        assertTrue(result.getFieldStats().containsKey("year"));

        FieldStatsItem item = result.getFieldStats().get("year");
        assertEquals(1950, item.getMin());
        assertEquals(2024, item.getMax());
        assertEquals(Long.valueOf(5000), item.getCount());
        assertEquals(Long.valueOf(42), item.getMissing());
        assertEquals(1990.5, item.getMean());
        assertEquals(Double.valueOf(15.3), item.getStddev());
        assertEquals("year", item.getLabel());
        assertEquals(Long.valueOf(75), item.getCountDistinct());
    }

    @Test
    public void testProcessSolrResponse_multipleStatsFields() throws Exception {
        FieldStatsInfo yearStats = mock(FieldStatsInfo.class);
        when(yearStats.getMin()).thenReturn(1980);
        when(yearStats.getMax()).thenReturn(2023);
        when(yearStats.getCount()).thenReturn(1000L);
        when(yearStats.getMissing()).thenReturn(10L);
        when(yearStats.getSum()).thenReturn(null);
        when(yearStats.getMean()).thenReturn(2000.0);
        when(yearStats.getStddev()).thenReturn(12.0);
        when(yearStats.getName()).thenReturn("year");
        when(yearStats.getCountDistinct()).thenReturn(44L);

        FieldStatsInfo monthStats = mock(FieldStatsInfo.class);
        when(monthStats.getMin()).thenReturn(1);
        when(monthStats.getMax()).thenReturn(12);
        when(monthStats.getCount()).thenReturn(1000L);
        when(monthStats.getMissing()).thenReturn(5L);
        when(monthStats.getSum()).thenReturn(null);
        when(monthStats.getMean()).thenReturn(6.5);
        when(monthStats.getStddev()).thenReturn(3.5);
        when(monthStats.getName()).thenReturn("month");
        when(monthStats.getCountDistinct()).thenReturn(12L);

        Map<String, FieldStatsInfo> statsMap = new HashMap<>();
        statsMap.put("year", yearStats);
        statsMap.put("month", monthStats);

        QueryResponse qr = createMockResponse(statsMap);
        SearchRequestDTO params = createBasicParams();
        SolrQuery solrQuery = new SolrQuery("*:*");

        SearchResultDTO result = (SearchResultDTO) processSolrResponse.invoke(
                searchDAO, params, qr, solrQuery, null);

        assertNotNull(result.getFieldStats());
        assertEquals(2, result.getFieldStats().size());

        FieldStatsItem yearItem = result.getFieldStats().get("year");
        assertEquals(1980, yearItem.getMin());
        assertEquals(2023, yearItem.getMax());

        FieldStatsItem monthItem = result.getFieldStats().get("month");
        assertEquals(1, monthItem.getMin());
        assertEquals(12, monthItem.getMax());
    }

    // ---- Stats extraction when no stats are present ----

    @Test
    public void testProcessSolrResponse_noStatsReturnsNull() throws Exception {
        QueryResponse qr = createMockResponse(null);
        SearchRequestDTO params = createBasicParams();
        SolrQuery solrQuery = new SolrQuery("*:*");

        SearchResultDTO result = (SearchResultDTO) processSolrResponse.invoke(
                searchDAO, params, qr, solrQuery, null);

        assertNull("fieldStats should be null when no stats are returned",
                result.getFieldStats());
    }

    @Test
    public void testProcessSolrResponse_emptyStatsReturnsNull() throws Exception {
        QueryResponse qr = createMockResponse(new HashMap<>());
        SearchRequestDTO params = createBasicParams();
        SolrQuery solrQuery = new SolrQuery("*:*");

        SearchResultDTO result = (SearchResultDTO) processSolrResponse.invoke(
                searchDAO, params, qr, solrQuery, null);

        assertNull("fieldStats should be null when stats map is empty",
                result.getFieldStats());
    }
}
