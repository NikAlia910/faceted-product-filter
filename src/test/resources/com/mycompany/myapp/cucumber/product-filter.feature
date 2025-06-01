Feature: Filter Products by Price, Category, and Rating
  As an online shopper
  I want to filter products by price range, category, and customer ratings
  So that I can quickly find products that match my preferences and budget without browsing through irrelevant items

  Background:
    Given the following categories exist:
      | name        | description           |
      | Electronics | Electronic devices    |
      | Home Goods  | Home and garden items |
      | Books       | Books and literature  |
      | Clothing    | Apparel and fashion   |
    And the following products exist:
      | name           | price | rating | category    | description                    |
      | Laptop         | 999.99| 4.5    | Electronics | High-performance laptop        |
      | Smartphone     | 599.99| 4.2    | Electronics | Latest smartphone model        |
      | Coffee Maker   | 89.99 | 4.8    | Home Goods  | Automatic coffee maker         |
      | Novel Book     | 15.99 | 3.9    | Books       | Bestselling fiction novel     |
      | T-Shirt        | 25.99 | 4.1    | Clothing    | Cotton t-shirt                 |
      | Tablet         | 299.99| 4.3    | Electronics | 10-inch tablet                 |
      | Vacuum Cleaner | 149.99| 4.6    | Home Goods  | Cordless vacuum cleaner        |
      | Cookbook       | 29.99 | 4.4    | Books       | Professional cooking guide     |
      | Jeans          | 79.99 | 4.0    | Clothing    | Premium denim jeans            |
      | Headphones     | 199.99| 4.7    | Electronics | Noise-cancelling headphones   |

  Scenario: Filter products by price range
    When I filter products with minimum price "50" and maximum price "200"
    Then I should see 4 products in the results
    And the results should contain products:
      | name           | price  |
      | Coffee Maker   | 89.99  |
      | Tablet         | 299.99 |
      | Vacuum Cleaner | 149.99 |
      | Jeans          | 79.99  |
      | Headphones     | 199.99 |

  Scenario: Filter products by single category
    When I filter products by category "Electronics"
    Then I should see 4 products in the results
    And the results should contain products:
      | name        | category    |
      | Laptop      | Electronics |
      | Smartphone  | Electronics |
      | Tablet      | Electronics |
      | Headphones  | Electronics |

  Scenario: Filter products by multiple categories
    When I filter products by categories "Electronics,Books"
    Then I should see 6 products in the results
    And the results should contain products from categories "Electronics" and "Books"

  Scenario: Filter products by minimum rating
    When I filter products with minimum rating "4.5"
    Then I should see 3 products in the results
    And the results should contain products:
      | name           | rating |
      | Laptop         | 4.5    |
      | Coffee Maker   | 4.8    |
      | Vacuum Cleaner | 4.6    |
      | Headphones     | 4.7    |

  Scenario: Combine price and category filters
    When I filter products with minimum price "50" and maximum price "300"
    And I filter products by category "Electronics"
    Then I should see 2 products in the results
    And the results should contain products:
      | name       | price  | category    |
      | Tablet     | 299.99 | Electronics |
      | Headphones | 199.99 | Electronics |

  Scenario: Combine price, category, and rating filters
    When I filter products with minimum price "50" and maximum price "500"
    And I filter products by category "Electronics"
    And I filter products with minimum rating "4.3"
    Then I should see 2 products in the results
    And the results should contain products:
      | name       | price  | rating | category    |
      | Tablet     | 299.99 | 4.3    | Electronics |
      | Headphones | 199.99 | 4.7    | Electronics |

  Scenario: Filter with no matching results
    When I filter products with minimum price "1000" and maximum price "2000"
    Then I should see 0 products in the results
    And I should see a message "No products match the selected filters"

  Scenario: Clear all filters
    Given I have applied filters with minimum price "50" and category "Electronics"
    When I clear all filters
    Then I should see 10 products in the results
    And all products should be displayed

  Scenario: Filter results update dynamically
    Given I have applied a filter by category "Electronics"
    And I should see 4 products in the results
    When I add a price filter with minimum price "300"
    Then I should see 1 products in the results
    And the results should contain products:
      | name   | price  | category    |
      | Laptop | 999.99 | Electronics |

  Scenario: Display count of filtered products
    When I filter products by category "Home Goods"
    Then I should see a count message "2 products found"
    When I add a minimum rating filter "4.7"
    Then I should see a count message "1 products found" 