package org.jboss.pnc.facade.mapper.api;

import org.jboss.pnc.dto.ProductVersionRef;
import org.jboss.pnc.model.ProductVersion;
import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;

/**
 *
 * @author Honza Brázdil &lt;jbrazdil@redhat.com&gt;
 */
@Mapper(config = MapperCentralConfig.class)
public interface ProductVersionRefMapper extends EntityMapper<ProductVersion, ProductVersionRef, ProductVersionRef> {

    @Override
    default ProductVersion toIDEntity(ProductVersionRef dtoEntity) {
        ProductVersion entity = new ProductVersion();
        entity.setId(dtoEntity.getId());
        return entity;
    }

    @Override
    @BeanMapping(ignoreUnmappedSourceProperties = {"product", "productReleases", "productMilestones",
        "currentProductMilestone", "buildConfigurationSets", "buildConfigurations", "attributes"})
    ProductVersionRef toRef(ProductVersion dbEntity);

    //TODO remaining two methods, see SCMRepositoryMapper for example
}
