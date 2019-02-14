package org.jboss.pnc.facade.mapper.api;

import org.jboss.pnc.dto.ProductMilestoneRef;
import org.jboss.pnc.dto.ProductReleaseRef;
import org.jboss.pnc.dto.ProductVersionRef;
import org.jboss.pnc.model.ProductMilestone;
import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

/**
 * @author Jan Michalov <jmichalo@redhat.com>
 */
@Mapper(config = MapperCentralConfig.class, uses = {ProductVersionMapper.class, ProductReleaseMapper.class, BuildMapper.IDMapper.class, ArtifactMapper.IDMapper.class})
public interface ProductMilestoneMapper extends EntityMapper<ProductMilestone, org.jboss.pnc.dto.ProductMilestone, ProductMilestoneRef>{

    @Override
    @Mapping(target = "distributedArtifacts", source = "distributedArtifactIds")
    @BeanMapping(ignoreUnmappedSourceProperties = {"distributedArtifactIds"})
    // TODO: Remove mapped fields from ignore with next (1.3.0.Beta3 +) version of MapStruct
    ProductMilestone toEntity(org.jboss.pnc.dto.ProductMilestone dtoEntity);

    @Override
    default ProductMilestone toIDEntity(ProductMilestoneRef dtoEntity) {
        ProductMilestone milestone = new ProductMilestone();
        milestone.setId(dtoEntity.getId());
        return milestone;
    };

    @Override
    @Mapping(target = "distributedArtifactIds", source = "distributedArtifacts")
    @Mapping(target = "productVersion", resultType = ProductVersionRef.class)
    @Mapping(target = "productRelease", resultType = ProductReleaseRef.class)
    @BeanMapping(ignoreUnmappedSourceProperties = {"distributedArtifacts"})
    // TODO: Remove mapped fields from ignore with next (1.3.0.Beta3 +) version of MapStruct
    org.jboss.pnc.dto.ProductMilestone toDTO(ProductMilestone dbEntity);

    @Override
    @BeanMapping(ignoreUnmappedSourceProperties = {"productVersion", "productRelease", "performedBuilds", "distributedArtifacts"})
    ProductMilestoneRef toRef(ProductMilestone dbEntity);
}
