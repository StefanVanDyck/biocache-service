package au.org.ala.biocache.dto;

import org.junit.Test;

import static org.junit.Assert.*;

/**
 * Unit tests for facet range fields on SearchRequestDTO.
 */
public class SearchRequestDTOFacetRangeTest {

    // ---- Default values ----

    @Test
    public void testDefaults_facetRangesIsEmpty() {
        SearchRequestDTO dto = new SearchRequestDTO();
        assertNotNull(dto.getFacetRanges());
        assertEquals(0, dto.getFacetRanges().length);
    }

    @Test
    public void testDefaults_facetRangeStartIsEmpty() {
        SearchRequestDTO dto = new SearchRequestDTO();
        assertEquals("", dto.getFacetRangeStart());
    }

    @Test
    public void testDefaults_facetRangeEndIsEmpty() {
        SearchRequestDTO dto = new SearchRequestDTO();
        assertEquals("", dto.getFacetRangeEnd());
    }

    @Test
    public void testDefaults_facetRangeGapIsEmpty() {
        SearchRequestDTO dto = new SearchRequestDTO();
        assertEquals("", dto.getFacetRangeGap());
    }

    // ---- Setter / Getter ----

    @Test
    public void testSetFacetRanges_singleValue() {
        SearchRequestDTO dto = new SearchRequestDTO();
        dto.setFacetRanges(new String[]{"year"});
        assertArrayEquals(new String[]{"year"}, dto.getFacetRanges());
    }

    @Test
    public void testSetFacetRanges_multipleValues() {
        SearchRequestDTO dto = new SearchRequestDTO();
        dto.setFacetRanges(new String[]{"year", "month"});
        assertArrayEquals(new String[]{"year", "month"}, dto.getFacetRanges());
    }

    @Test
    public void testSetFacetRanges_commaSeparatedSplit() {
        SearchRequestDTO dto = new SearchRequestDTO();
        dto.setFacetRanges(new String[]{"year,month"});
        assertArrayEquals(new String[]{"year", "month"}, dto.getFacetRanges());
    }

    @Test
    public void testSetFacetRanges_emptyStringsFiltered() {
        SearchRequestDTO dto = new SearchRequestDTO();
        dto.setFacetRanges(new String[]{"year", "", "month"});
        assertArrayEquals(new String[]{"year", "month"}, dto.getFacetRanges());
    }

    @Test
    public void testSetFacetRanges_nullBecomesEmpty() {
        SearchRequestDTO dto = new SearchRequestDTO();
        dto.setFacetRanges(null);
        assertNotNull(dto.getFacetRanges());
        assertEquals(0, dto.getFacetRanges().length);
    }

    @Test
    public void testSetFacetRangeStart() {
        SearchRequestDTO dto = new SearchRequestDTO();
        dto.setFacetRangeStart("2000");
        assertEquals("2000", dto.getFacetRangeStart());
    }

    @Test
    public void testSetFacetRangeEnd() {
        SearchRequestDTO dto = new SearchRequestDTO();
        dto.setFacetRangeEnd("2024");
        assertEquals("2024", dto.getFacetRangeEnd());
    }

    @Test
    public void testSetFacetRangeGap() {
        SearchRequestDTO dto = new SearchRequestDTO();
        dto.setFacetRangeGap("5");
        assertEquals("5", dto.getFacetRangeGap());
    }

    // ---- toString includes facet range params ----

    @Test
    public void testToString_includesFacetRanges() {
        SearchRequestDTO dto = new SearchRequestDTO();
        dto.setFacetRanges(new String[]{"year"});
        dto.setFacetRangeStart("2000");
        dto.setFacetRangeEnd("2024");
        dto.setFacetRangeGap("1");

        String result = dto.toString();
        assertTrue("toString should contain facetRanges=year", result.contains("facetRanges=year"));
        assertTrue("toString should contain facetRangeStart=2000", result.contains("facetRangeStart=2000"));
        assertTrue("toString should contain facetRangeEnd=2024", result.contains("facetRangeEnd=2024"));
        assertTrue("toString should contain facetRangeGap=1", result.contains("facetRangeGap=1"));
    }

    @Test
    public void testToString_omitsFacetRangeWhenEmpty() {
        SearchRequestDTO dto = new SearchRequestDTO();
        String result = dto.toString();
        assertFalse("toString should not contain facetRanges when empty", result.contains("facetRanges="));
        assertFalse("toString should not contain facetRangeStart when empty", result.contains("facetRangeStart="));
        assertFalse("toString should not contain facetRangeEnd when empty", result.contains("facetRangeEnd="));
        assertFalse("toString should not contain facetRangeGap when empty", result.contains("facetRangeGap="));
    }

    @Test
    public void testToString_omitsFacetRangesWhenFacetDisabled() {
        SearchRequestDTO dto = new SearchRequestDTO();
        dto.setFacet(false);
        dto.setFacetRanges(new String[]{"year"});
        String result = dto.toString();
        assertFalse("toString should not contain facetRanges when faceting is disabled", result.contains("facetRanges=year"));
    }

    @Test
    public void testToString_multipleFacetRanges() {
        SearchRequestDTO dto = new SearchRequestDTO();
        dto.setFacetRanges(new String[]{"year", "month"});

        String result = dto.toString();
        assertTrue("toString should contain facetRanges=year", result.contains("facetRanges=year"));
        assertTrue("toString should contain facetRanges=month", result.contains("facetRanges=month"));
    }

    // ---- equals / hashCode ----

    @Test
    public void testEquals_sameRangeParams() {
        SearchRequestDTO a = new SearchRequestDTO();
        a.setFacetRanges(new String[]{"year"});
        a.setFacetRangeStart("2000");
        a.setFacetRangeEnd("2024");
        a.setFacetRangeGap("1");

        SearchRequestDTO b = new SearchRequestDTO();
        b.setFacetRanges(new String[]{"year"});
        b.setFacetRangeStart("2000");
        b.setFacetRangeEnd("2024");
        b.setFacetRangeGap("1");

        assertEquals(a, b);
        assertEquals(a.hashCode(), b.hashCode());
    }

    @Test
    public void testEquals_differentRangeParams() {
        SearchRequestDTO a = new SearchRequestDTO();
        a.setFacetRanges(new String[]{"year"});
        a.setFacetRangeGap("1");

        SearchRequestDTO b = new SearchRequestDTO();
        b.setFacetRanges(new String[]{"year"});
        b.setFacetRangeGap("5");

        assertNotEquals(a, b);
    }

    @Test
    public void testEquals_differentFacetRangeFields() {
        SearchRequestDTO a = new SearchRequestDTO();
        a.setFacetRanges(new String[]{"year"});

        SearchRequestDTO b = new SearchRequestDTO();
        b.setFacetRanges(new String[]{"month"});

        assertNotEquals(a, b);
    }
}
