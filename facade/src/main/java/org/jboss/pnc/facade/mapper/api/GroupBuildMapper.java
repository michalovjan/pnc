package org.jboss.pnc.facade.mapper.api;

import org.jboss.pnc.dto.GroupBuild;
import org.jboss.pnc.dto.GroupBuildRef;
import org.jboss.pnc.dto.GroupConfigurationRef;
import org.jboss.pnc.dto.ProductVersionRef;
import org.jboss.pnc.model.BuildConfigSetRecord;
import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

/**
 * @author Jan Michalov <jmichalo@redhat.com>
 */
@Mapper(config = MapperCentralConfig.class, uses = {ProductVersionMapper.class, GroupConfigurationMapper.class, BuildMapper.IDMapper.class, UserMapper.class})
public interface GroupBuildMapper extends EntityMapper<BuildConfigSetRecord, GroupBuild, GroupBuildRef> {

    @Override
    @Mapping(target = "buildConfigurationSet", source = "groupConfig")
    @Mapping(target = "buildRecords", source = "buildIds")
    @Mapping(target = "attributes", ignore = true)
    @Mapping(target = "user", qualifiedBy = IdEntity.class)
    @BeanMapping(ignoreUnmappedSourceProperties = {"groupConfig", "buildIds"})
    // TODO: Remove mapped fields from ignore with next (1.3.0.Beta3 +) version of MapStruct
    BuildConfigSetRecord toEntity(GroupBuild dtoEntity);

    @Override
    default BuildConfigSetRecord toIDEntity(GroupBuildRef dtoEntity) {
        BuildConfigSetRecord bcsr = new BuildConfigSetRecord();
        bcsr.setId(dtoEntity.getId());
        return bcsr;
    };

    @Override
    @Mapping(target = "groupConfig", source = "buildConfigurationSet", resultType = GroupConfigurationRef.class)
    @Mapping(target = "buildIds", source = "buildRecords")
    @Mapping(target = "user", qualifiedBy = Reference.class)
    @Mapping(target = "productVersion", resultType = ProductVersionRef.class)
    @BeanMapping(ignoreUnmappedSourceProperties = {"attributes","buildConfigurationSet", "buildRecords"})
    // TODO: Remove mapped fields from ignore with next (1.3.0.Beta3 +) version of MapStruct
    GroupBuild toDTO(BuildConfigSetRecord dbEntity);

    @Override
    @BeanMapping(ignoreUnmappedSourceProperties = {"attributes", "buildRecords", "buildConfigurationSet", "user", "productVersion"})
    GroupBuildRef toRef(BuildConfigSetRecord dbEntity);
}
