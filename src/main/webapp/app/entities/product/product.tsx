import React, { useEffect, useState } from 'react';
import { Link, useLocation, useNavigate } from 'react-router-dom';
import { Button, Row, Col, Card, CardBody, CardImg, CardTitle, CardText, Badge, Container, Alert } from 'reactstrap';
import { JhiItemCount, JhiPagination, getPaginationState } from 'react-jhipster';
import { FontAwesomeIcon } from '@fortawesome/react-fontawesome';
import { faSort, faSortDown, faSortUp, faStar, faEye, faEdit, faTrash, faPlus, faSync } from '@fortawesome/free-solid-svg-icons';
import { ASC, DESC, ITEMS_PER_PAGE, SORT } from 'app/shared/util/pagination.constants';
import { overridePaginationStateWithQueryParams } from 'app/shared/util/entity-utils';
import { useAppDispatch, useAppSelector } from 'app/config/store';

import { getEntities, getFilteredEntities, setFilters, clearFilters, IProductFilters } from './product.reducer';
import ProductFilter from './product-filter';

export const Product = () => {
  const dispatch = useAppDispatch();

  const pageLocation = useLocation();
  const navigate = useNavigate();

  const [paginationState, setPaginationState] = useState(
    overridePaginationStateWithQueryParams(getPaginationState(pageLocation, ITEMS_PER_PAGE, 'id'), pageLocation.search),
  );

  const productState = useAppSelector(state => state.product);
  const { entities: productList, filteredEntities, isFiltering, loading, totalItems, filteredTotalItems, filters } = productState;

  const currentProducts = isFiltering ? filteredEntities : productList;
  const currentTotalItems = isFiltering ? filteredTotalItems : totalItems;

  const getAllEntities = () => {
    if (isFiltering && Object.keys(filters).length > 0) {
      dispatch(
        getFilteredEntities({
          page: paginationState.activePage - 1,
          size: paginationState.itemsPerPage,
          sort: `${paginationState.sort},${paginationState.order}`,
          filters,
        }),
      );
    } else {
      dispatch(
        getEntities({
          page: paginationState.activePage - 1,
          size: paginationState.itemsPerPage,
          sort: `${paginationState.sort},${paginationState.order}`,
        }),
      );
    }
  };

  const sortEntities = () => {
    getAllEntities();
    const endURL = `?page=${paginationState.activePage}&sort=${paginationState.sort},${paginationState.order}`;
    if (pageLocation.search !== endURL) {
      navigate(`${pageLocation.pathname}${endURL}`);
    }
  };

  useEffect(() => {
    sortEntities();
  }, [paginationState.activePage, paginationState.order, paginationState.sort, filters]);

  useEffect(() => {
    const params = new URLSearchParams(pageLocation.search);
    const page = params.get('page');
    const sort = params.get(SORT);
    if (page && sort) {
      const sortSplit = sort.split(',');
      setPaginationState({
        ...paginationState,
        activePage: +page,
        sort: sortSplit[0],
        order: sortSplit[1],
      });
    }
  }, [pageLocation.search]);

  const sort = p => () => {
    setPaginationState({
      ...paginationState,
      order: paginationState.order === ASC ? DESC : ASC,
      sort: p,
    });
  };

  const handlePagination = currentPage =>
    setPaginationState({
      ...paginationState,
      activePage: currentPage,
    });

  const handleSyncList = () => {
    sortEntities();
  };

  const handleFiltersChange = (newFilters: IProductFilters) => {
    dispatch(setFilters(newFilters));
    setPaginationState({
      ...paginationState,
      activePage: 1,
    });

    if (Object.keys(newFilters).length > 0) {
      dispatch(
        getFilteredEntities({
          page: 0,
          size: paginationState.itemsPerPage,
          sort: `${paginationState.sort},${paginationState.order}`,
          filters: newFilters,
        }),
      );
    } else {
      dispatch(
        getEntities({
          page: 0,
          size: paginationState.itemsPerPage,
          sort: `${paginationState.sort},${paginationState.order}`,
        }),
      );
    }
  };

  const handleClearFilters = () => {
    dispatch(clearFilters());
    setPaginationState({
      ...paginationState,
      activePage: 1,
    });
    dispatch(
      getEntities({
        page: 0,
        size: paginationState.itemsPerPage,
        sort: `${paginationState.sort},${paginationState.order}`,
      }),
    );
  };

  const getSortIconByFieldName = (fieldName: string) => {
    const sortFieldName = paginationState.sort;
    const order = paginationState.order;
    if (sortFieldName !== fieldName) {
      return faSort;
    }
    return order === ASC ? faSortUp : faSortDown;
  };

  const renderStarRating = (rating: number) => {
    return (
      <div className="d-flex align-items-center">
        {[1, 2, 3, 4, 5].map(star => (
          <FontAwesomeIcon key={star} icon={faStar} className={`me-1 ${star <= rating ? 'text-warning' : 'text-muted'}`} size="sm" />
        ))}
        <span className="ms-1 small text-muted">({rating})</span>
      </div>
    );
  };

  const renderProductCard = (product, index) => (
    <Col lg={4} md={6} sm={12} key={`entity-${index}`} className="mb-4">
      <Card className="h-100 shadow-sm product-card" data-cy="entityTable">
        {product.imageUrl && <CardImg top src={product.imageUrl} alt={product.name} style={{ height: '200px', objectFit: 'cover' }} />}
        <CardBody className="d-flex flex-column">
          <div className="flex-grow-1">
            <CardTitle tag="h5" className="mb-2">
              <Link to={`/product/${product.id}`} className="text-decoration-none">
                {product.name}
              </Link>
            </CardTitle>
            <CardText className="text-muted small mb-2" style={{ minHeight: '3rem' }}>
              {product.description}
            </CardText>
            <div className="mb-2">
              <Badge color="info" className="me-2">
                {product.category ? product.category.name : 'No Category'}
              </Badge>
              <Badge color="secondary">{product.user ? product.user.login : 'Unknown'}</Badge>
            </div>
            <div className="mb-2">{renderStarRating(product.rating || 0)}</div>
          </div>
          <div className="mt-auto">
            <div className="d-flex justify-content-between align-items-center mb-3">
              <h4 className="text-primary mb-0">${product.price}</h4>
            </div>
            <div className="btn-group w-100" role="group">
              <Button tag={Link} to={`/product/${product.id}`} color="info" size="sm" data-cy="entityDetailsButton" className="flex-fill">
                <FontAwesomeIcon icon={faEye} /> View
              </Button>
              <Button
                tag={Link}
                to={`/product/${product.id}/edit?page=${paginationState.activePage}&sort=${paginationState.sort},${paginationState.order}`}
                color="primary"
                size="sm"
                data-cy="entityEditButton"
                className="flex-fill"
              >
                <FontAwesomeIcon icon={faEdit} /> Edit
              </Button>
              <Button
                onClick={() =>
                  (window.location.href = `/product/${product.id}/delete?page=${paginationState.activePage}&sort=${paginationState.sort},${paginationState.order}`)
                }
                color="danger"
                size="sm"
                data-cy="entityDeleteButton"
                className="flex-fill"
              >
                <FontAwesomeIcon icon={faTrash} /> Delete
              </Button>
            </div>
          </div>
        </CardBody>
      </Card>
    </Col>
  );

  return (
    <Container fluid>
      <Row>
        {/* Filter Sidebar */}
        <Col lg={3} md={4} className="mb-4">
          <ProductFilter
            onFiltersChange={handleFiltersChange}
            onClearFilters={handleClearFilters}
            currentFilters={filters}
            productsCount={currentTotalItems}
          />
        </Col>

        {/* Main Content */}
        <Col lg={9} md={8}>
          {/* Header */}
          <div className="d-flex justify-content-between align-items-center mb-4">
            <div>
              <h2 id="product-heading" data-cy="ProductHeading" className="mb-1">
                Products
              </h2>
              <p className="text-muted mb-0">
                {isFiltering ? 'Filtered results' : 'All products'}
                {currentTotalItems > 0 && ` (${currentTotalItems} total)`}
              </p>
            </div>
            <div className="d-flex gap-2">
              <Button color="info" onClick={handleSyncList} disabled={loading} size="sm">
                <FontAwesomeIcon icon={faSync} spin={loading} /> Refresh
              </Button>
              <Button tag={Link} to="/product/new" color="primary" id="jh-create-entity" data-cy="entityCreateButton" size="sm">
                <FontAwesomeIcon icon={faPlus} /> New Product
              </Button>
            </div>
          </div>

          {/* Sort Controls */}
          <div className="d-flex justify-content-between align-items-center mb-3 p-2 bg-light rounded">
            <span className="small text-muted">Sort by:</span>
            <div className="btn-group btn-group-sm" role="group">
              <Button color={paginationState.sort === 'name' ? 'primary' : 'outline-secondary'} onClick={sort('name')} size="sm">
                Name <FontAwesomeIcon icon={getSortIconByFieldName('name')} />
              </Button>
              <Button color={paginationState.sort === 'price' ? 'primary' : 'outline-secondary'} onClick={sort('price')} size="sm">
                Price <FontAwesomeIcon icon={getSortIconByFieldName('price')} />
              </Button>
              <Button color={paginationState.sort === 'rating' ? 'primary' : 'outline-secondary'} onClick={sort('rating')} size="sm">
                Rating <FontAwesomeIcon icon={getSortIconByFieldName('rating')} />
              </Button>
            </div>
          </div>

          {/* Products Grid */}
          {currentProducts && currentProducts.length > 0 ? (
            <Row>{currentProducts.map((product, i) => renderProductCard(product, i))}</Row>
          ) : (
            !loading && (
              <Alert color="warning" className="text-center">
                <FontAwesomeIcon icon="exclamation-triangle" className="me-2" />
                {isFiltering ? 'No products match the selected filters' : 'No Products found'}
                {isFiltering && (
                  <div className="mt-2">
                    <Button color="link" onClick={handleClearFilters} className="p-0">
                      Clear all filters to see all products
                    </Button>
                  </div>
                )}
              </Alert>
            )
          )}

          {/* Pagination */}
          {currentTotalItems ? (
            <div className={currentProducts && currentProducts.length > 0 ? 'mt-4' : 'd-none'}>
              <div className="justify-content-center d-flex">
                <JhiItemCount page={paginationState.activePage} total={currentTotalItems} itemsPerPage={paginationState.itemsPerPage} />
              </div>
              <div className="justify-content-center d-flex">
                <JhiPagination
                  activePage={paginationState.activePage}
                  onSelect={handlePagination}
                  maxButtons={5}
                  itemsPerPage={paginationState.itemsPerPage}
                  totalItems={currentTotalItems}
                />
              </div>
            </div>
          ) : (
            ''
          )}
        </Col>
      </Row>
    </Container>
  );
};

export default Product;
