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
    default BuildEnvironment toIDEntity(Environment dtoEntity) {
        BuildEnvironment entity = new BuildEnvironment();
        entity.setId(dtoEntity.getId());
        return entity;
    }

    @Override
    Environment toRef(BuildEnvironment dbEntity);

    //TODO remaining two methods, see SCMRepositoryMapper for example
}
