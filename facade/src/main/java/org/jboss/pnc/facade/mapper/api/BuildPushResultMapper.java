package org.jboss.pnc.facade.mapper.api;

import org.jboss.pnc.dto.BuildPushResult;
import org.jboss.pnc.model.BuildRecordPushResult;
import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

/**
 * @author Jan Michalov <jmichalo@redhat.com>
 */
@Mapper(config = MapperCentralConfig.class, uses = {BuildMapper.IDMapper.class})
public interface BuildPushResultMapper {

    @Mapping(target = "buildId", source = "buildRecord")
    @Mapping(target = "artifactImportErrors", ignore = true)
    @BeanMapping(ignoreUnmappedSourceProperties = {"tagPrefix", "buildRecord"})
    // TODO: Remove mapped fields from ignore with next (1.3.0.Beta3 +) version of MapStruct
    BuildPushResult toDTO(BuildRecordPushResult db);

    @Mapping(target = "buildRecord", source = "buildId")
    @Mapping(target = "tagPrefix", ignore = true)
    @BeanMapping(ignoreUnmappedSourceProperties = {"artifactImportErrors", "buildId"})
    // TODO: Remove mapped fields from ignore with next (1.3.0.Beta3 +) version of MapStruct
    BuildRecordPushResult toEntity(BuildPushResult dto);
}
