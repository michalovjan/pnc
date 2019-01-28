package org.jboss.pnc.facade.mapper.api;

import org.jboss.pnc.model.User;
import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

/**
 *
 * @author Honza Brázdil &lt;jbrazdil@redhat.com&gt;
 */
@Mapper(config = MapperCentralConfig.class)
public interface UserMapper extends EntityMapper<User, org.jboss.pnc.dto.User, org.jboss.pnc.dto.User> {

    @Override
    @Mapping(target="email", ignore = true)
    @Mapping(target="firstName", ignore = true)
    @Mapping(target="lastName", ignore = true)
    @Mapping(target="loginToken", ignore = true)
    @Mapping(target="buildRecords", ignore = true)
    User toEntity(org.jboss.pnc.dto.User dtoEntity);

    @Override
    @IdEntity
    default User toIDEntity(org.jboss.pnc.dto.User dtoEntity) {
        User entity = new User();
        entity.setId(dtoEntity.getId());
        return entity;
    }

    @Override
    @Reference
    default org.jboss.pnc.dto.User toRef(User dbEntity){
        return toDTO(dbEntity);
    }

    @Override
    @BeanMapping(ignoreUnmappedSourceProperties = {"email", "firstName", "lastName", "loginToken",
        "buildRecords", "buildConfigurations"})
    org.jboss.pnc.dto.User toDTO(User dbEntity);

}
