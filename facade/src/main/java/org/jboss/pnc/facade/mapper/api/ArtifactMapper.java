package org.jboss.pnc.facade.mapper.api;

import org.jboss.pnc.dto.ArtifactRef;
import org.jboss.pnc.model.Artifact;
import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

/**
 *
 * @author Honza Brázdil &lt;jbrazdil@redhat.com&gt;
 */
@Mapper(config = MapperCentralConfig.class,
        uses = {BCMapper.class, TargetRepositoryMapper.class, BuildMapper.IDMapper.class})
public interface ArtifactMapper extends EntityMapper<Artifact, org.jboss.pnc.dto.Artifact, ArtifactRef>{

    @Override
    @Mapping(target = "buildIds", source = "buildRecords")
    @Mapping(target = "dependantBuildIds", source = "dependantBuildRecords")
    @Mapping(target = "deployUrl", ignore = true)
    @Mapping(target = "publicUrl", ignore = true)
    @Mapping(target = "targetRepository", qualifiedBy = Reference.class)
    @BeanMapping(ignoreUnmappedSourceProperties = {"distributedInProductMilestones", "buildRecords", "dependantBuildRecords",
            "identifierSha256", "built", "imported", "trusted", "descriptiveString"
    })    // TODO: Remove mapped fields from ignore with next (1.3.0.Beta3 +) version of MapStruct
    org.jboss.pnc.dto.Artifact toDTO(Artifact dbEntity);

    @Override
    default Artifact toIDEntity(ArtifactRef dtoEntity) {
        return Artifact.Builder.newBuilder().id(dtoEntity.getId()).build();
    }

    @Override
    @Mapping(target = "deployUrl", ignore = true)
    @Mapping(target = "publicUrl", ignore = true)
    @BeanMapping(ignoreUnmappedSourceProperties = {"targetRepository", "buildRecords", "dependantBuildRecords","importDate",
            "distributedInProductMilestones", "identifierSha256", "built", "imported", "trusted", "descriptiveString"
    })
    ArtifactRef toRef(Artifact dbEntity);
    
    @Override
    @Mapping(target = "buildRecords", source = "buildIds")
    @Mapping(target = "dependantBuildRecords", source = "dependantBuildIds")
    /* Builder that MapStruct uses when generating mapper has method dependantBuildRecord() which confuses MapStruct as he thinks it is a new property */
    @Mapping(target = "dependantBuildRecord", ignore = true)
    /* Same as above */
    @Mapping(target = "buildRecord", ignore = true)
    @Mapping(target = "distributedInProductMilestones", ignore = true)
    @BeanMapping(ignoreUnmappedSourceProperties = {"deployUrl", "publicUrl", "buildIds", "dependantBuildIds"})
    // TODO: Remove mapped fields from ignore with next (1.3.0.Beta3 +) version of MapStruct
    Artifact toEntity(org.jboss.pnc.dto.Artifact dtoEntity);

    public static class IDMapper {

        public Integer toId(Artifact artifact) {
            return artifact.getId();
        }

        public Artifact toId(Integer artifactId) {
            return Artifact.Builder.newBuilder().id(artifactId).build();
        }
    }
}
