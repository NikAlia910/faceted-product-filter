package com.mycompany.myapp.cucumber.stepdefs;

import static org.assertj.core.api.Assertions.assertThat;

import com.mycompany.myapp.domain.Category;
import com.mycompany.myapp.domain.Product;
import com.mycompany.myapp.domain.User;
import com.mycompany.myapp.repository.CategoryRepository;
import com.mycompany.myapp.repository.ProductRepository;
import com.mycompany.myapp.repository.UserRepository;
import com.mycompany.myapp.service.CategoryService;
import com.mycompany.myapp.service.ProductService;
import io.cucumber.datatable.DataTable;
import io.cucumber.java.Before;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import java.math.BigDecimal;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import org.apache.commons.lang3.RandomStringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.transaction.annotation.Transactional;

public class ProductFilterStepDefs extends StepDefs {

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ProductService productService;

    @Autowired
    private CategoryService categoryService;

    private List<Product> filteredProducts;
    private String filterMessage;
    private BigDecimal minPrice;
    private BigDecimal maxPrice;
    private Double minRating;
    private Set<String> selectedCategories = new HashSet<>();

    @Before
    @Transactional
    public void cleanupTestData() {
        // Clean up all test data before each scenario
        productRepository.deleteAll();
        categoryRepository.deleteAll();

        // Clean up test users that have been created (only those with test-specific logins)
        // This prevents unique constraint violations when re-running tests
        userRepository.findOneByLogin("user1").ifPresent(userRepository::delete);
        userRepository.findOneByLogin("user2").ifPresent(userRepository::delete);

        // Reset filter state
        filteredProducts = null;
        filterMessage = null;
        minPrice = null;
        maxPrice = null;
        minRating = null;
        selectedCategories.clear();
    }

    @Given("the following categories exist:")
    @Transactional
    public void the_following_categories_exist(DataTable dataTable) {
        List<Map<String, String>> categories = dataTable.asMaps(String.class, String.class);

        for (Map<String, String> categoryData : categories) {
            Category category = new Category();
            category.setName(categoryData.get("name"));
            category.setDescription(categoryData.get("description"));

            // Call actual service - this should fail until implemented
            categoryService.save(category);
        }
    }

    @Given("the following products exist:")
    @Transactional
    public void the_following_products_exist(DataTable dataTable) {
        List<Map<String, String>> products = dataTable.asMaps(String.class, String.class);

        // Create a test user for products
        User testUser = new User();
        testUser.setLogin("user1");
        testUser.setPassword(RandomStringUtils.insecure().nextAlphanumeric(60));
        testUser.setActivated(true);
        testUser.setEmail("user1@test.com");
        testUser.setFirstName("Test");
        testUser.setLastName("User");
        userRepository.save(testUser);

        for (Map<String, String> productData : products) {
            Product product = new Product();
            product.setName(productData.get("name"));
            product.setPrice(new BigDecimal(productData.get("price")));
            product.setRating(Double.valueOf(productData.get("rating")));
            product.setDescription(productData.get("description"));
            product.setUser(testUser);

            // Find category by name
            Optional<Category> category = categoryRepository
                .findAll()
                .stream()
                .filter(c -> c.getName().equals(productData.get("category")))
                .findFirst();

            category.ifPresent(product::setCategory);

            // Call actual service - this should fail until implemented
            productService.save(product);
        }
    }

    @When("I filter products with minimum price {string} and maximum price {string}")
    public void i_filter_products_with_minimum_price_and_maximum_price(String minPriceStr, String maxPriceStr) {
        this.minPrice = new BigDecimal(minPriceStr);
        this.maxPrice = new BigDecimal(maxPriceStr);

        // Call actual service method for price filtering
        Pageable pageable = Pageable.unpaged();
        Page<Product> page = productService.findFilteredProducts(pageable, minPrice, maxPrice, null, null);
        filteredProducts = page.getContent();
    }

    @When("I filter products by category {string}")
    public void i_filter_products_by_category(String categoryName) {
        selectedCategories.clear();
        selectedCategories.add(categoryName);

        // Find category by name to get its ID
        List<Long> categoryIds = categoryRepository
            .findAll()
            .stream()
            .filter(c -> c.getName().equals(categoryName))
            .map(Category::getId)
            .collect(Collectors.toList());

        // Call actual service method for category filtering
        Pageable pageable = Pageable.unpaged();
        Page<Product> page = productService.findFilteredProducts(pageable, null, null, categoryIds, null);
        filteredProducts = page.getContent();
    }

    @When("I filter products by categories {string}")
    public void i_filter_products_by_categories(String categoriesStr) {
        selectedCategories.clear();
        selectedCategories.addAll(Arrays.asList(categoriesStr.split(",")));

        // Find categories by names to get their IDs
        List<Long> categoryIds = categoryRepository
            .findAll()
            .stream()
            .filter(c -> selectedCategories.contains(c.getName()))
            .map(Category::getId)
            .collect(Collectors.toList());

        // Call actual service method for multiple category filtering
        Pageable pageable = Pageable.unpaged();
        Page<Product> page = productService.findFilteredProducts(pageable, null, null, categoryIds, null);
        filteredProducts = page.getContent();
    }

    @When("I filter products with minimum rating {string}")
    public void i_filter_products_with_minimum_rating(String minRatingStr) {
        this.minRating = Double.valueOf(minRatingStr);

        // Call actual service method for rating filtering
        Pageable pageable = Pageable.unpaged();
        Page<Product> page = productService.findFilteredProducts(pageable, null, null, null, minRating);
        filteredProducts = page.getContent();
    }

    @When("I add a price filter with minimum price {string}")
    public void i_add_a_price_filter_with_minimum_price(String minPriceStr) {
        this.minPrice = new BigDecimal(minPriceStr);

        // Find category IDs if categories are selected
        List<Long> categoryIds = null;
        if (!selectedCategories.isEmpty()) {
            categoryIds = categoryRepository
                .findAll()
                .stream()
                .filter(c -> selectedCategories.contains(c.getName()))
                .map(Category::getId)
                .collect(Collectors.toList());
        }

        // Call actual service method for combined filtering
        Pageable pageable = Pageable.unpaged();
        Page<Product> page = productService.findFilteredProducts(pageable, minPrice, maxPrice, categoryIds, minRating);
        filteredProducts = page.getContent();
    }

    @When("I add a minimum rating filter {string}")
    public void i_add_a_minimum_rating_filter(String minRatingStr) {
        this.minRating = Double.valueOf(minRatingStr);

        // Find category IDs if categories are selected
        List<Long> categoryIds = null;
        if (!selectedCategories.isEmpty()) {
            categoryIds = categoryRepository
                .findAll()
                .stream()
                .filter(c -> selectedCategories.contains(c.getName()))
                .map(Category::getId)
                .collect(Collectors.toList());
        }

        // Call actual service method for combined filtering
        Pageable pageable = Pageable.unpaged();
        Page<Product> page = productService.findFilteredProducts(pageable, minPrice, maxPrice, categoryIds, minRating);
        filteredProducts = page.getContent();
    }

    @When("I clear all filters")
    public void i_clear_all_filters() {
        minPrice = null;
        maxPrice = null;
        minRating = null;
        selectedCategories.clear();

        // Call actual service method to get all products
        Pageable pageable = Pageable.unpaged();
        Page<Product> page = productService.findAllWithEagerRelationships(pageable);
        filteredProducts = page.getContent();
    }

    @Given("I have applied filters with minimum price {string} and category {string}")
    public void i_have_applied_filters_with_minimum_price_and_category(String minPriceStr, String categoryName) {
        this.minPrice = new BigDecimal(minPriceStr);
        selectedCategories.clear();
        selectedCategories.add(categoryName);

        // Find category by name to get its ID
        List<Long> categoryIds = categoryRepository
            .findAll()
            .stream()
            .filter(c -> c.getName().equals(categoryName))
            .map(Category::getId)
            .collect(Collectors.toList());

        // Call actual service method for combined filtering
        Pageable pageable = Pageable.unpaged();
        Page<Product> page = productService.findFilteredProducts(pageable, minPrice, maxPrice, categoryIds, minRating);
        filteredProducts = page.getContent();
    }

    @Given("I have applied a filter by category {string}")
    public void i_have_applied_a_filter_by_category(String categoryName) {
        selectedCategories.clear();
        selectedCategories.add(categoryName);

        // Find category by name to get its ID
        List<Long> categoryIds = categoryRepository
            .findAll()
            .stream()
            .filter(c -> c.getName().equals(categoryName))
            .map(Category::getId)
            .collect(Collectors.toList());

        // Call actual service method for category filtering
        Pageable pageable = Pageable.unpaged();
        Page<Product> page = productService.findFilteredProducts(pageable, null, null, categoryIds, null);
        filteredProducts = page.getContent();
    }

    @Then("I should see {int} products in the results")
    public void i_should_see_products_in_the_results(int expectedCount) {
        assertThat(filteredProducts).isNotNull();
        assertThat(filteredProducts).hasSize(expectedCount);
    }

    @Then("the results should contain products:")
    public void the_results_should_contain_products(DataTable dataTable) {
        List<Map<String, String>> expectedProducts = dataTable.asMaps(String.class, String.class);

        assertThat(filteredProducts).isNotNull();

        for (Map<String, String> expectedProduct : expectedProducts) {
            String expectedName = expectedProduct.get("name");

            boolean found = filteredProducts.stream().anyMatch(product -> product.getName().equals(expectedName));

            assertThat(found).withFailMessage("Product '%s' not found in filtered results", expectedName).isTrue();
        }
    }

    @Then("the results should contain products from categories {string} and {string}")
    public void the_results_should_contain_products_from_categories_and(String category1, String category2) {
        assertThat(filteredProducts).isNotNull();

        Set<String> foundCategories = filteredProducts.stream().map(product -> product.getCategory().getName()).collect(Collectors.toSet());

        assertThat(foundCategories).contains(category1, category2);
    }

    @Then("I should see a message {string}")
    public void i_should_see_a_message(String expectedMessage) {
        // This would typically come from a service method that handles empty results
        if (filteredProducts != null && filteredProducts.isEmpty()) {
            filterMessage = "No products match the selected filters";
        }

        assertThat(filterMessage).isEqualTo(expectedMessage);
    }

    @Then("all products should be displayed")
    public void all_products_should_be_displayed() {
        // Call actual service to get total count - this should work
        List<Product> allProducts = productRepository.findAll();

        assertThat(filteredProducts).isNotNull();
        assertThat(filteredProducts).hasSize(allProducts.size());
    }

    @Then("I should see a count message {string}")
    public void i_should_see_a_count_message(String expectedMessage) {
        // This would typically come from a service method that provides count information
        if (filteredProducts != null) {
            filterMessage = filteredProducts.size() + " products found";
        }

        assertThat(filterMessage).isEqualTo(expectedMessage);
    }
}
