package com.mycompany.myapp.repository;

import com.mycompany.myapp.domain.Product;
import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

/**
 * Spring Data JPA repository for the Product entity.
 */
@Repository
public interface ProductRepository extends JpaRepository<Product, Long> {
    @Query("select product from Product product where product.user.login = ?#{authentication.name}")
    List<Product> findByUserIsCurrentUser();

    default Optional<Product> findOneWithEagerRelationships(Long id) {
        return this.findOneWithToOneRelationships(id);
    }

    default List<Product> findAllWithEagerRelationships() {
        return this.findAllWithToOneRelationships();
    }

    default Page<Product> findAllWithEagerRelationships(Pageable pageable) {
        return this.findAllWithToOneRelationships(pageable);
    }

    @Query(
        value = "select product from Product product left join fetch product.category left join fetch product.user",
        countQuery = "select count(product) from Product product"
    )
    Page<Product> findAllWithToOneRelationships(Pageable pageable);

    @Query("select product from Product product left join fetch product.category left join fetch product.user")
    List<Product> findAllWithToOneRelationships();

    @Query("select product from Product product left join fetch product.category left join fetch product.user where product.id =:id")
    Optional<Product> findOneWithToOneRelationships(@Param("id") Long id);

    /**
     * Find products with filtering criteria.
     * This query supports filtering by price range, category IDs, and minimum rating.
     * All parameters are optional and will be ignored if null.
     */
    @Query(
        value = """
        select distinct product from Product product
        left join fetch product.category
        left join fetch product.user
        where (:minPrice is null or product.price >= :minPrice)
        and (:maxPrice is null or product.price <= :maxPrice)
        and (:categoryIds is null or product.category.id in :categoryIds)
        and (:minRating is null or product.rating >= :minRating)
        """,
        countQuery = """
        select count(distinct product) from Product product
        where (:minPrice is null or product.price >= :minPrice)
        and (:maxPrice is null or product.price <= :maxPrice)
        and (:categoryIds is null or product.category.id in :categoryIds)
        and (:minRating is null or product.rating >= :minRating)
        """
    )
    Page<Product> findFilteredProducts(
        Pageable pageable,
        @Param("minPrice") BigDecimal minPrice,
        @Param("maxPrice") BigDecimal maxPrice,
        @Param("categoryIds") List<Long> categoryIds,
        @Param("minRating") Double minRating
    );
}
