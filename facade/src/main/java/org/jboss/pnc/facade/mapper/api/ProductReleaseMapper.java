package org.jboss.pnc.facade.mapper.api;

import org.jboss.pnc.dto.ProductMilestoneRef;
import org.jboss.pnc.dto.ProductReleaseRef;
import org.jboss.pnc.dto.ProductVersionRef;
import org.jboss.pnc.model.ProductRelease;
import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

/**
 * @author Jan Michalov <jmichalo@redhat.com>
 */
@Mapper(config = MapperCentralConfig.class, uses = {ProductMilestoneMapper.class, ProductVersionMapper.class})
public interface ProductReleaseMapper extends EntityMapper<ProductRelease, org.jboss.pnc.dto.ProductRelease, ProductReleaseRef>{
    @Override
    @BeanMapping(ignoreUnmappedSourceProperties = {"productVersion"})
    ProductRelease toEntity(org.jboss.pnc.dto.ProductRelease dtoEntity);

    @Override
    default ProductRelease toIDEntity(ProductReleaseRef dtoEntity) {
        ProductRelease release = new ProductRelease();
        release.setId(dtoEntity.getId());
        return release;
    }

    @Override
    @Mapping(target = "productVersion", source = "productMilestone.productVersion", resultType = ProductVersionRef.class)
    @Mapping(target = "productMilestone", resultType = ProductMilestoneRef.class)
    org.jboss.pnc.dto.ProductRelease toDTO(ProductRelease dbEntity);

    @Override
    @BeanMapping(ignoreUnmappedSourceProperties = {"productMilestone", "productVersion"})
    ProductReleaseRef toRef(ProductRelease dbEntity);
}
