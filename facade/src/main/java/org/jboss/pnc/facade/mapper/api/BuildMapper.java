package org.jboss.pnc.facade.mapper.api;

import org.jboss.pnc.dto.Build;
import org.jboss.pnc.dto.BuildConfigurationRevisionRef;
import org.jboss.pnc.dto.BuildRef;
import org.jboss.pnc.enums.BuildCoordinationStatus;
import org.jboss.pnc.model.BuildRecord;
import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

/**
 *
 * @author Honza Brázdil &lt;jbrazdil@redhat.com&gt;
 */
@Mapper(config = MapperCentralConfig.class,
        uses = {BCMapper.class, UserMapper.class, BuildCoordinationStatus.class})
public interface BuildMapper extends EntityMapper<BuildRecord, Build, BuildRef>{

    @Override
    @Mapping(target = "buildConfigurationRevision", resultType = BuildConfigurationRevisionRef.class)
    Build toDTO(BuildRecord dbEntity);

    @Override
    default BuildRecord toIDEntity(BuildRef dtoEntity) {
        BuildRecord entity = new BuildRecord();
        entity.setId(dtoEntity.getId());
        return entity;
    }

    @Override
    @BeanMapping(ignoreUnmappedSourceProperties = {"buildConfigurations"})
    BuildRef toRef(BuildRecord dbEntity);
    
    @Override
    BuildRecord toEntity(Build dtoEntity);

}
