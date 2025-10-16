package au.org.ala.biocache.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;

/**
 * DTO for species list information including counts
 */
@Schema(name = "SpeciesList", description = "Species List")
public class SpeciesListDTO {

    private String name;
    private long count;
    private long speciesCount;

    public SpeciesListDTO(){}

    public SpeciesListDTO(String name){
        this.name = name;
    }

    public SpeciesListDTO(String name, long speciesCount, long count){
        this.name = name;
        this.speciesCount = speciesCount;
        this.count = count;
    }

    public long getCount() {
        return count;
    }

    public void setCount(long count) {
        this.count = count;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @Override
    public String toString() {
        return "SpeciesListDTO[" + "name=" + name + ", count=" + count + ']';
    }

    /**
     * @return the speciesCount
     */
    public long getSpeciesCount() {
        return speciesCount;
    }

    /**
     * @param speciesCount the speciesCount to set
     */
    public void setSpeciesCount(long speciesCount) {
        this.speciesCount = speciesCount;
    }
}
