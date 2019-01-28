package org.jboss.pnc.facade.mapper.api;

import org.jboss.pnc.dto.ArtifactRef;
import org.jboss.pnc.dto.BuildConfigurationRef;
import org.jboss.pnc.model.Artifact;
import org.mapstruct.BeanMapping;
import org.mapstruct.Mapping;

/**
 *
 * @author Honza Brázdil &lt;jbrazdil@redhat.com&gt;
 */
// TODO uncomment and finish
/*@Mapper(config = MapperCentralConfig.class,
        uses = {BCMapper.class})*/
public interface ArtifactMapper extends EntityMapper<Artifact, org.jboss.pnc.dto.Artifact, ArtifactRef>{

    @Override
    @Mapping(target = "buildConfigurations", resultType = BuildConfigurationRef.class)
    org.jboss.pnc.dto.Artifact toDTO(Artifact dbEntity);

    @Override
    default Artifact toIDEntity(ArtifactRef dtoEntity) {
        return Artifact.Builder.newBuilder().id(dtoEntity.getId()).build();
    }

    @Override
    @BeanMapping(ignoreUnmappedSourceProperties = {"buildConfigurations"})
    ArtifactRef toRef(Artifact dbEntity);
    
    @Override
    Artifact toEntity(org.jboss.pnc.dto.Artifact dtoEntity);

}
