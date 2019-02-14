package org.jboss.pnc.facade.mapper.api;

import org.jboss.pnc.dto.BuildConfigurationRef;
import org.jboss.pnc.dto.ProjectRef;
import org.jboss.pnc.model.Project;
import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

/**
 *
 * @author Honza Brázdil &lt;jbrazdil@redhat.com&gt;
 */
@Mapper(config = MapperCentralConfig.class,
        uses = {BCMapper.class})
public interface ProjectMapper extends EntityMapper<Project, org.jboss.pnc.dto.Project, ProjectRef>{

    @Override
    @Mapping(target = "buildConfigurations", resultType = BuildConfigurationRef.class)
    org.jboss.pnc.dto.Project toDTO(Project dbEntity);

    @Override
    default Project toIDEntity(ProjectRef dtoEntity) {
        Project entity = new Project();
        entity.setId(dtoEntity.getId());
        return entity;
    }

    @Override
    @BeanMapping(ignoreUnmappedSourceProperties = {"buildConfigurations"})
    ProjectRef toRef(Project dbEntity);
    
    @Override
    Project toEntity(org.jboss.pnc.dto.Project dtoEntity);

}
