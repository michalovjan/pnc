package org.jboss.pnc.facade.mapper.api;

import org.jboss.pnc.dto.BuildConfigurationRef;
import org.jboss.pnc.dto.GroupConfigurationRef;
import org.jboss.pnc.dto.ProductMilestoneRef;
import org.jboss.pnc.dto.ProductRef;
import org.jboss.pnc.dto.ProductReleaseRef;
import org.jboss.pnc.dto.ProductVersionRef;
import org.jboss.pnc.model.ProductVersion;
import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

/**
 *
 * @author Honza Brázdil &lt;jbrazdil@redhat.com&gt;
 */
@Mapper(config = MapperCentralConfig.class, uses = {ProductMilestoneMapper.class, ProductMapper.class, GroupConfigurationMapper.class, BCMapper.class, ProductReleaseMapper.class})
public interface ProductVersionMapper extends EntityMapper<ProductVersion, org.jboss.pnc.dto.ProductVersion, ProductVersionRef> {

    @Override
    default ProductVersion toIDEntity(ProductVersionRef dtoEntity) {
        ProductVersion entity = new ProductVersion();
        entity.setId(dtoEntity.getId());
        return entity;
    }

    @Override
    @Mapping(target = "buildConfigurationSets", source = "groupConfigurations")
    @Mapping(target = "productReleases", ignore = true)
    @BeanMapping(ignoreUnmappedSourceProperties = {"productReleases", "groupConfigurations"})
    // TODO: Remove mapped fields from ignore with next (1.3.0.Beta3 +) version of MapStruct
    ProductVersion toEntity(org.jboss.pnc.dto.ProductVersion dtoEntity);

    @Override
    @BeanMapping(ignoreUnmappedSourceProperties = {"product", "productReleases", "productMilestones", "currentProductMilestone",
            "buildConfigurationSets", "buildConfigurations"})
    ProductVersionRef toRef(ProductVersion dbEntity);

    @Override
    @Mapping(target = "groupConfigurations", source = "buildConfigurationSets", resultType = GroupConfigurationRef.class)
    @Mapping(target = "product", resultType = ProductRef.class)
    @Mapping(target = "productReleases", resultType = ProductReleaseRef.class)
    @Mapping(target = "currentProductMilestone", resultType = ProductMilestoneRef.class)
    @Mapping(target = "buildConfigurations", resultType = BuildConfigurationRef.class)
    @Mapping(target = "productMilestones", resultType = ProductMilestoneRef.class)
    @BeanMapping(ignoreUnmappedSourceProperties = {"buildConfigurationSets"})
    // TODO: Remove mapped fields from ignore with next (1.3.0.Beta3 +) version of MapStruct
    org.jboss.pnc.dto.ProductVersion toDTO(ProductVersion dbEntity);
}
