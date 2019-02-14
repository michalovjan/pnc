package org.jboss.pnc.facade.mapper.api;

import org.jboss.pnc.dto.TargetRepositoryRef;
import org.jboss.pnc.model.TargetRepository;
import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

/**
 * @author Jan Michalov <jmichalo@redhat.com>
 */
@Mapper(config = MapperCentralConfig.class, uses = {ArtifactMapper.class, ArtifactMapper.IDMapper.class })
public interface TargetRepositoryMapper extends EntityMapper<TargetRepository, org.jboss.pnc.dto.TargetRepository, TargetRepositoryRef> {
    @Override
    @Mapping(target = "artifacts", source = "artifactIds")
    @BeanMapping(ignoreUnmappedSourceProperties = "artifactIds")
    // TODO: Remove mapped fields from ignore with next (1.3.0.Beta3 +) version of MapStruct
    TargetRepository toEntity(org.jboss.pnc.dto.TargetRepository dtoEntity);

    @Override
    default TargetRepository toIDEntity(TargetRepositoryRef dtoEntity) {
        TargetRepository repository = new TargetRepository();
        repository.setId(dtoEntity.getId());
        return repository;
    };

    @Override
    @Mapping(target = "artifactIds", source = "artifacts")
    @BeanMapping(ignoreUnmappedSourceProperties = "artifacts")
    // TODO: Remove mapped fields from ignore with next (1.3.0.Beta3 +) version of MapStruct
    org.jboss.pnc.dto.TargetRepository toDTO(TargetRepository dbEntity);

    @Override
    @Reference
    @BeanMapping(ignoreUnmappedSourceProperties = {"artifacts"})
    TargetRepositoryRef toRef(TargetRepository dbEntity);


}
