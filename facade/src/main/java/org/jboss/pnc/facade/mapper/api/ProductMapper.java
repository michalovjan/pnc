package org.jboss.pnc.facade.mapper.api;

import org.jboss.pnc.dto.ProductRef;
import org.jboss.pnc.dto.ProductVersionRef;
import org.jboss.pnc.model.Product;
import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

/**
 * @author Jan Michalov <jmichalo@redhat.com>
 */
@Mapper(config = MapperCentralConfig.class, uses = {ProductVersionMapper.class})
public interface ProductMapper extends EntityMapper<Product, org.jboss.pnc.dto.Product,ProductRef> {
    @Override
    Product toEntity(org.jboss.pnc.dto.Product dtoEntity);

    @Override
    default Product toIDEntity(ProductRef dtoEntity) {
        Product product = new Product();
        product.setId(dtoEntity.getId());
        return product;
    };

    @Override
    @Mapping(target = "productVersions", resultType = ProductVersionRef.class)
    org.jboss.pnc.dto.Product toDTO(Product dbEntity);

    @Override
    @BeanMapping(ignoreUnmappedSourceProperties = {"productVersions"})
    ProductRef toRef(Product dbEntity);
}
