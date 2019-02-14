package org.jboss.pnc.facade.mapper.api;

import org.jboss.pnc.dto.BuildConfigurationRef;
import org.jboss.pnc.dto.GroupConfiguration;
import org.jboss.pnc.dto.GroupConfigurationRef;
import org.jboss.pnc.dto.ProductVersionRef;
import org.jboss.pnc.model.BuildConfigurationSet;
import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;

/**
 *
 * @author Honza Brázdil &lt;jbrazdil@redhat.com&gt;
 */
@Mapper(config = MapperCentralConfig.class, uses = {ProductVersionMapper.class, BCMapper.class})
public interface GroupConfigurationMapper extends EntityMapper<BuildConfigurationSet, GroupConfiguration, GroupConfigurationRef> {

    @Override
    default BuildConfigurationSet toIDEntity(GroupConfigurationRef dtoEntity) {
        BuildConfigurationSet entity = new BuildConfigurationSet();
        entity.setId(dtoEntity.getId());
        return entity;
    }

    @Override
    @Mapping(target = "active", ignore = true)
    @Mapping(target = "buildConfigSetRecords", ignore = true)
    @Mapping(target = "archived", ignore = true)
    BuildConfigurationSet toEntity(GroupConfiguration dtoEntity);

    @Override
    @BeanMapping(ignoreUnmappedSourceProperties = {"productVersion", "buildConfigurations",
        "buildConfigSetRecords", "archived", "active", "currentProductMilestone"})
    GroupConfigurationRef toRef(BuildConfigurationSet dbEntity);

    @Override
    @Mapping(target = "productVersion", resultType = ProductVersionRef.class)
    @Mapping(target = "buildConfigurations", resultType = BuildConfigurationRef.class)
    @BeanMapping(ignoreUnmappedSourceProperties = {"buildConfigSetRecords", "active", "currentProductMilestone", "archived"})
    GroupConfiguration toDTO(BuildConfigurationSet dbEntity);
}
