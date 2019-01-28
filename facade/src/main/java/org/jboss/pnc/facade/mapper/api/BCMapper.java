package org.jboss.pnc.facade.mapper.api;

import org.jboss.pnc.dto.BuildConfigurationRef;
import org.jboss.pnc.dto.ProjectRef;
import org.jboss.pnc.facade.mapper.api.BCMapper.IDMapper;
import org.jboss.pnc.model.BuildConfiguration;
import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

/**
 *
 * @author Honza Brázdil &lt;jbrazdil@redhat.com&gt;
 */
@Mapper(config = MapperCentralConfig.class,
        uses = {ProjectMapper.class, ProductVersionRefMapper.class, EnvironmentMapper.class,
            IDMapper.class, SCMRepositoryMapper.class, GroupConfigurationRefMapper.class})
public interface BCMapper extends EntityMapper<BuildConfiguration, org.jboss.pnc.dto.BuildConfiguration, BuildConfigurationRef> {

    @Override
    @Mapping(target = "lastModificationTime", source = "modificationTime")
    @Mapping(target = "buildEnvironment", source = "environment")
    @Mapping(target = "buildConfigurationSets", source = "groupConfigs")
    @Mapping(target = "dependencies", source = "dependencyIds")
    @Mapping(target = "repositoryConfiguration", source = "repository", qualifiedBy = IdEntity.class)
    @Mapping(target = "active", ignore = true)
    @Mapping(target = "dependants", ignore = true)
    @Mapping(target = "indirectDependencies", ignore = true)
    @Mapping(target = "allDependencies", ignore = true)
    @BeanMapping(ignoreUnmappedSourceProperties = {"modificationTime", "repository", "environment",
        "dependencyIds", "groupConfigs"
    }) // TODO: Remove mapped fields from ignore with next (1.3.0.Beta3 +) version of MapStruct
    BuildConfiguration toEntity(org.jboss.pnc.dto.BuildConfiguration dtoEntity);

    @Override
    default BuildConfiguration toIDEntity(BuildConfigurationRef dtoEntity) {
        BuildConfiguration entity = new BuildConfiguration();
        entity.setId(dtoEntity.getId());
        return entity;
    }

    @Override
    @Mapping(target = "modificationTime", source = "lastModificationTime")
    @BeanMapping(ignoreUnmappedSourceProperties = {"repositoryConfiguration", "project",
        "productVersion", "buildEnvironment", "buildConfigurationSets", "dependencies",
        "indirectDependencies", "allDependencies", "dependants", "currentProductMilestone", "active",
        "genericParameters", "lastModificationTime"
    }) // TODO: Remove mapped fields from ignore with next (1.3.0.Beta3 +) version of MapStruct
    BuildConfigurationRef toRef(BuildConfiguration dbEntity);

    
    @Override
    @Mapping(target = "modificationTime", source = "lastModificationTime")
    @Mapping(target = "environment", source = "buildEnvironment")
    @Mapping(target = "groupConfigs", source = "buildConfigurationSets")
    @Mapping(target = "dependencyIds", source = "dependencies")
    @Mapping(target = "repository", source = "repositoryConfiguration", qualifiedBy = Reference.class)
    @Mapping(target = "project", qualifiedBy = Reference.class)
    @BeanMapping(ignoreUnmappedSourceProperties = {"dependants", "active", "indirectDependencies",
        "allDependencies", "currentProductMilestone", "lastModificationTime", "buildEnvironment",
        "buildConfigurationSets", "dependencies", "repositoryConfiguration"
    }) // TODO: Remove mapped fields from ignore with next (1.3.0.Beta3 +) version of MapStruct
    org.jboss.pnc.dto.BuildConfiguration toDTO(BuildConfiguration dbEntity);

    public static class IDMapper {

        public Integer toId(BuildConfiguration bc) {
            return bc.getId();
        }

        public BuildConfiguration toId(Integer bcId) {
            BuildConfiguration bc = new BuildConfiguration();
            bc.setId(bcId);
            return bc;
        }
    }
}
