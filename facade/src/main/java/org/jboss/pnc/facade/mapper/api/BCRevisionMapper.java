package org.jboss.pnc.facade.mapper.api;

import org.jboss.pnc.dto.BuildConfigurationRevision;
import org.jboss.pnc.dto.BuildConfigurationRevisionRef;
import org.jboss.pnc.dto.ProjectRef;
import org.jboss.pnc.model.BuildConfigurationAudited;
import org.jboss.pnc.model.IdRev;
import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

/**
 * @author Jan Michalov <jmichalo@redhat.com>
 */
@Mapper(config = MapperCentralConfig.class, uses = {ProjectMapper.class, EnvironmentMapper.class, SCMRepositoryMapper.class}, imports = IdRev.class)
public interface BCRevisionMapper {

     @Mapping(target = "repository", source = "repositoryConfiguration", qualifiedBy = Reference.class)
     @Mapping(target = "environment", source = "buildEnvironment", qualifiedBy = Reference.class)
     @Mapping(target = "project", resultType = ProjectRef.class)
     @Mapping(target = "creationTime", ignore = true)
     @Mapping(target = "modificationTime", ignore = true)
     @BeanMapping(ignoreUnmappedSourceProperties = {"idRev", "buildRecords", "buildConfiguration",
             "repositoryConfiguration", "buildEnvironment"
     })
     // TODO: Remove mapped fields from ignore with next (1.3.0.Beta3 +) version of MapStruct
     BuildConfigurationRevision toDTO(BuildConfigurationAudited dbEntity);

     default BuildConfigurationAudited toIDEntity(BuildConfigurationRevisionRef dtoEntity) {
         BuildConfigurationAudited entity = new BuildConfigurationAudited();
         entity.setId(dtoEntity.getId());
         entity.setRev(dtoEntity.getRev());
         return entity;
     };

     @Mapping(target = "repositoryConfiguration", source = "repository", qualifiedBy = IdEntity.class)
     @Mapping(target = "buildEnvironment", source = "environment", qualifiedBy = IdEntity.class)
     @Mapping(target = "idRev", expression = "java( new IdRev( dtoEntity.getId(), dtoEntity.getRev() ) )")
     @Mapping(target = "buildRecords", ignore = true)
     @Mapping(target = "buildConfiguration", ignore = true)
     @BeanMapping(ignoreUnmappedSourceProperties = {"creationTime", "modificationTime", "repository", "environment"})
     // TODO: Remove mapped fields from ignore with next (1.3.0.Beta3 +) version of MapStruct
     BuildConfigurationAudited toEntity(BuildConfigurationRevision dtoEntity);

     @Mapping(target = "creationTime", ignore = true)
     @Mapping(target = "modificationTime", ignore = true)
     @BeanMapping(ignoreUnmappedSourceProperties = {"idRev", "buildRecords", "buildConfiguration",
             "repositoryConfiguration", "buildEnvironment", "project", "genericParameters"
     })
     BuildConfigurationRevisionRef toRef(BuildConfigurationAudited dbEntity);
}
