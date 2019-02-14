package org.jboss.pnc.facade.mapper.api;

import org.jboss.pnc.dto.Environment;
import org.jboss.pnc.model.BuildEnvironment;
import org.mapstruct.Mapper;

/**
 *
 * @author Honza Brázdil &lt;jbrazdil@redhat.com&gt;
 */
@Mapper(config = MapperCentralConfig.class)
public interface EnvironmentMapper extends EntityMapper<BuildEnvironment, Environment, Environment> {

    @Override
    BuildEnvironment toEntity(Environment dtoEntity);

    @Override
    @IdEntity
    default BuildEnvironment toIDEntity(Environment dtoEntity) {
        BuildEnvironment entity = new BuildEnvironment();
        entity.setId(dtoEntity.getId());
        return entity;
    }

    @Override
    @Reference
    default Environment toRef(BuildEnvironment dbEntity) {
        return toDTO(dbEntity);
    };

    @Override
    Environment toDTO(BuildEnvironment dbEntity);

}
