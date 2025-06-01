import React, { useState, useEffect } from 'react';
import { Card, CardBody, CardHeader, Form, FormGroup, Label, Input, Button, Row, Col, Badge } from 'reactstrap';
import { FontAwesomeIcon } from '@fortawesome/react-fontawesome';
import { faFilter, faTimes, faCheck, faStar } from '@fortawesome/free-solid-svg-icons';
import { useAppDispatch, useAppSelector } from 'app/config/store';
import { IProductFilters } from './product.reducer';
import { getEntities as getCategoriesEntities } from '../category/category.reducer';

interface IProductFilterProps {
  onFiltersChange: (filters: IProductFilters) => void;
  onClearFilters: () => void;
  currentFilters: IProductFilters;
  productsCount: number;
}

export const ProductFilter: React.FC<IProductFilterProps> = ({ onFiltersChange, onClearFilters, currentFilters, productsCount }) => {
  const dispatch = useAppDispatch();
  const categoryList = useAppSelector(state => state.category.entities);
  const categoryLoading = useAppSelector(state => state.category.loading);

  const [localFilters, setLocalFilters] = useState<IProductFilters>(currentFilters);
  const [isExpanded, setIsExpanded] = useState(true);

  useEffect(() => {
    dispatch(getCategoriesEntities({}));
  }, [dispatch]);

  useEffect(() => {
    setLocalFilters(currentFilters);
  }, [currentFilters]);

  const handlePriceChange = (field: 'minPrice' | 'maxPrice', value: string) => {
    const numValue = value === '' ? undefined : parseFloat(value);
    setLocalFilters(prev => ({
      ...prev,
      [field]: numValue,
    }));
  };

  const handleCategoryChange = (categoryId: number, checked: boolean) => {
    setLocalFilters(prev => {
      const currentCategoryIds = prev.categoryIds || [];
      const newCategoryIds = checked ? [...currentCategoryIds, categoryId] : currentCategoryIds.filter(id => id !== categoryId);

      return {
        ...prev,
        categoryIds: newCategoryIds.length > 0 ? newCategoryIds : undefined,
      };
    });
  };

  const handleRatingChange = (rating: number) => {
    setLocalFilters(prev => ({
      ...prev,
      minRating: prev.minRating === rating ? undefined : rating,
    }));
  };

  const applyFilters = () => {
    onFiltersChange(localFilters);
  };

  const clearAllFilters = () => {
    const emptyFilters = {};
    setLocalFilters(emptyFilters);
    onClearFilters();
  };

  const hasActiveFilters = () => {
    return (
      Object.keys(currentFilters).length > 0 &&
      Object.values(currentFilters).some(value => value !== undefined && value !== null && (Array.isArray(value) ? value.length > 0 : true))
    );
  };

  const renderStarRating = (rating: number) => {
    return (
      <div className="d-flex align-items-center">
        {[1, 2, 3, 4, 5].map(star => (
          <FontAwesomeIcon key={star} icon={faStar} className={`me-1 ${star <= rating ? 'text-warning' : 'text-muted'}`} size="sm" />
        ))}
        <span className="ms-2 small text-muted">{rating} stars & up</span>
      </div>
    );
  };

  return (
    <Card className="product-filter-card shadow-sm" data-cy="productFilterCard">
      <CardHeader
        className="d-flex justify-content-between align-items-center bg-light cursor-pointer"
        onClick={() => setIsExpanded(!isExpanded)}
        data-cy="filterHeader"
      >
        <div className="d-flex align-items-center">
          <FontAwesomeIcon icon={faFilter} className="me-2 text-primary" />
          <strong>Filter Products</strong>
          {hasActiveFilters() && (
            <Badge color="primary" className="ms-2" data-cy="activeFiltersCount">
              {Object.keys(currentFilters).length} active
            </Badge>
          )}
        </div>
        <div className="d-flex align-items-center">
          <span className="text-muted me-2 small" data-cy="productsCount">
            {productsCount} products found
          </span>
          <FontAwesomeIcon icon={isExpanded ? 'chevron-up' : 'chevron-down'} className="text-muted" />
        </div>
      </CardHeader>

      {isExpanded && (
        <CardBody className="p-3">
          <Form>
            {/* Price Range Filter */}
            <FormGroup className="mb-3">
              <Label className="fw-bold text-dark mb-2">
                <FontAwesomeIcon icon="dollar-sign" className="me-2 text-success" />
                Price Range
              </Label>
              <Row>
                <Col sm={6}>
                  <div className="mb-2">
                    <Label for="minPrice" className="form-label small">
                      Min Price
                    </Label>
                    <Input
                      type="number"
                      id="minPrice"
                      placeholder="0"
                      min="0"
                      step="0.01"
                      value={localFilters.minPrice || ''}
                      onChange={e => handlePriceChange('minPrice', e.target.value)}
                      className="form-control-sm"
                      data-cy="minPriceInput"
                      aria-label="Minimum price filter"
                    />
                  </div>
                </Col>
                <Col sm={6}>
                  <div className="mb-2">
                    <Label for="maxPrice" className="form-label small">
                      Max Price
                    </Label>
                    <Input
                      type="number"
                      id="maxPrice"
                      placeholder="∞"
                      min="0"
                      step="0.01"
                      value={localFilters.maxPrice || ''}
                      onChange={e => handlePriceChange('maxPrice', e.target.value)}
                      className="form-control-sm"
                      data-cy="maxPriceInput"
                      aria-label="Maximum price filter"
                    />
                  </div>
                </Col>
              </Row>
            </FormGroup>

            {/* Category Filter */}
            <FormGroup className="mb-3">
              <Label className="fw-bold text-dark mb-2">
                <FontAwesomeIcon icon="tags" className="me-2 text-info" />
                Categories
              </Label>
              {categoryLoading ? (
                <div className="text-muted small">Loading categories...</div>
              ) : (
                <div className="category-checkboxes" data-cy="categoryFilters">
                  {categoryList.map(category => (
                    <div key={category.id} className="form-check mb-1">
                      <Input
                        type="checkbox"
                        id={`category-${category.id}`}
                        className="form-check-input"
                        checked={localFilters.categoryIds?.includes(category.id) || false}
                        onChange={e => handleCategoryChange(category.id, e.target.checked)}
                        data-cy={`categoryFilter-${category.name}`}
                        aria-label={`Filter by ${category.name} category`}
                      />
                      <Label
                        for={`category-${category.id}`}
                        className="form-check-label small d-flex justify-content-between align-items-center"
                      >
                        <span>{category.name}</span>
                        {category.description && (
                          <span className="text-muted ms-2" style={{ fontSize: '0.8rem' }}>
                            {category.description}
                          </span>
                        )}
                      </Label>
                    </div>
                  ))}
                </div>
              )}
            </FormGroup>

            {/* Rating Filter */}
            <FormGroup className="mb-3">
              <Label className="fw-bold text-dark mb-2">
                <FontAwesomeIcon icon={faStar} className="me-2 text-warning" />
                Minimum Rating
              </Label>
              <div className="rating-filters" data-cy="ratingFilters">
                {[5, 4, 3, 2, 1].map(rating => (
                  <div
                    key={rating}
                    className={`rating-option p-2 mb-1 border rounded cursor-pointer d-flex align-items-center justify-content-between ${
                      localFilters.minRating === rating ? 'bg-primary text-white border-primary' : 'bg-light border-light'
                    }`}
                    onClick={() => handleRatingChange(rating)}
                    data-cy={`ratingFilter-${rating}`}
                    role="button"
                    tabIndex={0}
                    onKeyDown={e => e.key === 'Enter' && handleRatingChange(rating)}
                    aria-label={`Filter products with ${rating} stars and above`}
                  >
                    {renderStarRating(rating)}
                    {localFilters.minRating === rating && <FontAwesomeIcon icon={faCheck} className="ms-2" />}
                  </div>
                ))}
              </div>
            </FormGroup>

            {/* Action Buttons */}
            <div className="d-grid gap-2">
              <Button color="primary" onClick={applyFilters} data-cy="applyFiltersButton" aria-label="Apply selected filters">
                <FontAwesomeIcon icon={faFilter} className="me-2" />
                Apply Filters
              </Button>

              {hasActiveFilters() && (
                <Button
                  color="outline-secondary"
                  size="sm"
                  onClick={clearAllFilters}
                  data-cy="clearFiltersButton"
                  aria-label="Clear all filters"
                >
                  <FontAwesomeIcon icon={faTimes} className="me-2" />
                  Clear All Filters
                </Button>
              )}
            </div>
          </Form>
        </CardBody>
      )}
    </Card>
  );
};

export default ProductFilter;
