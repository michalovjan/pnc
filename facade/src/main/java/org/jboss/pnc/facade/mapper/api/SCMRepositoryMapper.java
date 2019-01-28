package org.jboss.pnc.facade.mapper.api;

import org.jboss.pnc.dto.SCMRepository;
import org.jboss.pnc.model.RepositoryConfiguration;
import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

/**
 *
 * @author Honza Brázdil &lt;jbrazdil@redhat.com&gt;
 */
@Mapper(config = MapperCentralConfig.class)
public interface SCMRepositoryMapper extends EntityMapper<RepositoryConfiguration, SCMRepository, SCMRepository> {

    @Override
    @Mapping(target="internalUrlNormalized", ignore = true)
    @Mapping(target="externalUrlNormalized", ignore = true)
    @Mapping(target="buildConfigurations", ignore = true)
    RepositoryConfiguration toEntity(SCMRepository dtoEntity);

    @Override
    @IdEntity
    default RepositoryConfiguration toIDEntity(SCMRepository dtoEntity) {
        RepositoryConfiguration entity = new RepositoryConfiguration();
        entity.setId(dtoEntity.getId());
        return entity;
    }

    @Override
    @Reference
    default SCMRepository toRef(RepositoryConfiguration dbEntity){
        return toDTO(dbEntity);
    }

    @Override
    @BeanMapping(ignoreUnmappedSourceProperties = {"internalUrlNormalized", "externalUrlNormalized",
        "buildConfigurations"})
    SCMRepository toDTO(RepositoryConfiguration dbEntity);

}
