package org.jboss.pnc.facade.mapper.api;

import org.jboss.pnc.dto.GroupConfigurationRef;
import org.jboss.pnc.model.BuildConfigurationSet;
import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;

/**
 *
 * @author Honza Brázdil &lt;jbrazdil@redhat.com&gt;
 */
@Mapper(config = MapperCentralConfig.class)
public interface GroupConfigurationRefMapper extends EntityMapper<BuildConfigurationSet, GroupConfigurationRef, GroupConfigurationRef> {

    @Override
    default BuildConfigurationSet toIDEntity(GroupConfigurationRef dtoEntity) {
        BuildConfigurationSet entity = new BuildConfigurationSet();
        entity.setId(dtoEntity.getId());
        return entity;
    }

    @Override
    @BeanMapping(ignoreUnmappedSourceProperties = {"productVersion", "buildConfigurations",
        "buildConfigSetRecords", "archived", "active", "currentProductMilestone"})
    GroupConfigurationRef toRef(BuildConfigurationSet dbEntity);

    //TODO remaining two methods, see SCMRepositoryMapper for example
}
