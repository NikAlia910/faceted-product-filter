package gatling.simulations;

import static io.gatling.javaapi.core.CoreDsl.*;
import static io.gatling.javaapi.http.HttpDsl.*;

import io.gatling.javaapi.core.*;
import io.gatling.javaapi.http.*;
import java.time.Duration;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Comprehensive API Performance Test for JHipster Application
 *
 * Features:
 * - JWT Authentication with proper token handling
 * - Full API endpoint coverage
 * - Coverage reporting
 * - Performance assertions
 * - Realistic test data generation
 * - Modular, reusable methods
 *
 * Usage: mvn gatling:test -Dgatling.simulationClass=gatling.simulations.ComprehensiveApiPerformanceTest
 */
public class ComprehensiveApiPerformanceTest extends Simulation {

    // Environment Configuration
    private final String baseUrl = System.getProperty("baseUrl", "http://localhost:8080");
    private final int users = Integer.getInteger("users", 10);
    private final int rampDurationSeconds = Integer.getInteger("rampDuration", 10);

    // Test Data Counters for uniqueness
    private static final AtomicInteger categoryCounter = new AtomicInteger(1);
    private static final AtomicInteger productCounter = new AtomicInteger(1);
    private static final AtomicInteger userCounter = new AtomicInteger(1);

    // Endpoint tracking for coverage reporting
    private static final Set<String> DISCOVERED_ENDPOINTS = Set.of(
        "POST /api/authenticate",
        "GET /api/account",
        "GET /api/categories",
        "POST /api/categories",
        "GET /api/categories/{id}",
        "PUT /api/categories/{id}",
        "PATCH /api/categories/{id}",
        "DELETE /api/categories/{id}",
        "GET /api/products",
        "POST /api/products",
        "GET /api/products/{id}",
        "PUT /api/products/{id}",
        "PATCH /api/products/{id}",
        "DELETE /api/products/{id}",
        "GET /api/products/filter",
        "GET /api/admin/users",
        "POST /api/admin/users",
        "GET /api/admin/users/{login}",
        "PUT /api/admin/users",
        "DELETE /api/admin/users/{login}",
        "GET /api/authorities",
        "GET /api/users"
    );

    private static final Set<String> TESTED_ENDPOINTS = new HashSet<>();

    // HTTP Protocol Configuration
    HttpProtocolBuilder httpProtocol = http
        .baseUrl(baseUrl)
        .inferHtmlResources()
        .acceptHeader("application/json")
        .acceptEncodingHeader("gzip, deflate")
        .acceptLanguageHeader("en-US,en;q=0.9")
        .connectionHeader("keep-alive")
        .userAgentHeader("Gatling Performance Test")
        .contentTypeHeader("application/json");

    // ========================================
    // AUTHENTICATION METHODS
    // ========================================

    /**
     * Authenticate user and extract JWT token
     */
    ChainBuilder authenticateUser() {
        return exec(session -> {
            System.out.println("=== Starting authentication for user session ===");
            return session;
        })
            .exec(
                http("Authenticate User")
                    .post("/api/authenticate")
                    .header("Content-Type", "application/json")
                    .body(
                        StringBody(
                            """
                            {
                                "username": "admin",
                                "password": "admin",
                                "rememberMe": false
                            }
                            """
                        )
                    )
                    .check(status().in(200, 201))
                    .check(responseTimeInMillis().lte(2000))
                    .check(jsonPath("$.id_token").exists().saveAs("jwt_token"))
            )
            .exitHereIfFailed()
            .exec(session -> {
                String token = session.getString("jwt_token");
                System.out.println(
                    "JWT Token extracted: " + (token != null ? token.substring(0, Math.min(50, token.length())) + "..." : "null")
                );
                TESTED_ENDPOINTS.add("POST /api/authenticate");
                return session;
            })
            .pause(1, 2);
    }

    /**
     * Verify authentication status
     */
    ChainBuilder verifyAuthentication() {
        return exec(
            http("Verify Authentication")
                .get("/api/account")
                .header("Authorization", session -> "Bearer " + session.getString("jwt_token"))
                .check(status().is(200))
                .check(responseTimeInMillis().lte(1000))
                .check(jsonPath("$.login").exists())
        ).exec(session -> {
            TESTED_ENDPOINTS.add("GET /api/account");
            return session;
        });
    }

    // ========================================
    // CATEGORY CRUD OPERATIONS
    // ========================================

    ChainBuilder createCategory() {
        return exec(session -> {
            String timestamp = getCurrentTimestamp();
            int counter = categoryCounter.getAndIncrement();
            return session
                .set("categoryName", "Category_" + counter + "_" + timestamp)
                .set("categoryDescription", "Test category created at " + timestamp);
        })
            .exec(
                http("Create Category")
                    .post("/api/categories")
                    .header("Authorization", session -> "Bearer " + session.getString("jwt_token"))
                    .header("Content-Type", "application/json")
                    .body(
                        StringBody(session ->
                            """
                            {
                                "name": "%s",
                                "description": "%s"
                            }
                            """.formatted(session.getString("categoryName"), session.getString("categoryDescription"))
                        )
                    )
                    .check(status().in(201))
                    .check(responseTimeInMillis().lte(1000))
                    .check(jsonPath("$.id").exists().saveAs("categoryId"))
                    .check(header("Location").exists().saveAs("categoryLocation"))
            )
            .exitHereIfFailed()
            .exec(session -> {
                String categoryId = session.getString("categoryId");
                System.out.println("Created category with ID: " + categoryId + " for session: " + session.userId());
                TESTED_ENDPOINTS.add("POST /api/categories");
                return session;
            });
    }

    ChainBuilder getAllCategories() {
        return exec(
            http("Get All Categories")
                .get("/api/categories")
                .header("Authorization", session -> "Bearer " + session.getString("jwt_token"))
                .check(status().is(200))
                .check(responseTimeInMillis().lte(1000))
                .check(jsonPath("$").exists())
        ).exec(session -> {
            TESTED_ENDPOINTS.add("GET /api/categories");
            return session;
        });
    }

    ChainBuilder getCategoryById() {
        return exec(session -> {
            String categoryId = session.getString("categoryId");
            System.out.println("DEBUG - Getting category by ID: " + categoryId + " for session: " + session.userId());
            return session;
        })
            .exec(
                http("Get Category By ID")
                    .get(session -> "/api/categories/" + session.getString("categoryId"))
                    .header("Authorization", session -> "Bearer " + session.getString("jwt_token"))
                    .check(status().is(200))
                    .check(responseTimeInMillis().lte(1000))
                    .check(jsonPath("$.id").is(session -> session.getString("categoryId")))
                    .check(jsonPath("$.name").exists())
            )
            .exec(session -> {
                TESTED_ENDPOINTS.add("GET /api/categories/{id}");
                return session;
            });
    }

    ChainBuilder updateCategory() {
        return exec(session -> {
            // Create a short, unique name (max 50 chars) to avoid database constraint issues
            String userId = session.getString("categoryId");
            String shortName = "Upd_" + userId + "_" + (System.currentTimeMillis() % 10000); // Max ~20 chars
            String safeDescription = "Updated category " + userId;
            return session.set("updatedCategoryName", shortName).set("updatedCategoryDescription", safeDescription);
        })
            .exec(session -> {
                System.out.println(
                    "DEBUG - Updating category ID: " +
                    session.getString("categoryId") +
                    " with name: " +
                    session.getString("updatedCategoryName")
                );
                return session;
            })
            .exec(
                http("Update Category")
                    .put(session -> "/api/categories/" + session.getString("categoryId"))
                    .header("Authorization", session -> "Bearer " + session.getString("jwt_token"))
                    .header("Content-Type", "application/json")
                    .body(
                        StringBody(session ->
                            """
                            {
                                "id": %s,
                                "name": "%s",
                                "description": "%s"
                            }
                            """.formatted(
                                    session.getString("categoryId"), // ID as number, not string
                                    session.getString("updatedCategoryName"),
                                    session.getString("updatedCategoryDescription")
                                )
                        )
                    )
                    .check(status().is(200))
                    .check(responseTimeInMillis().lte(1000))
                    .check(jsonPath("$.name").is(session -> session.getString("updatedCategoryName")))
            )
            .exec(session -> {
                TESTED_ENDPOINTS.add("PUT /api/categories/{id}");
                return session;
            });
    }

    ChainBuilder partialUpdateCategory() {
        return exec(session -> {
            String categoryId = session.getString("categoryId");
            String patchDesc = "Patched_" + categoryId + "_" + (System.currentTimeMillis() % 10000);
            return session.set("patchDescription", patchDesc);
        })
            .exec(
                http("Partial Update Category")
                    .patch(session -> "/api/categories/" + session.getString("categoryId"))
                    .header("Authorization", session -> "Bearer " + session.getString("jwt_token"))
                    .header("Content-Type", "application/json")
                    .body(
                        StringBody(session ->
                            """
                            {
                                "id": %s,
                                "description": "%s"
                            }
                            """.formatted(
                                    session.getString("categoryId"), // ID as number
                                    session.getString("patchDescription")
                                )
                        )
                    )
                    .check(status().is(200))
                    .check(responseTimeInMillis().lte(1000))
            )
            .exec(session -> {
                TESTED_ENDPOINTS.add("PATCH /api/categories/{id}");
                return session;
            });
    }

    ChainBuilder deleteCategory() {
        return exec(
            http("Delete Category")
                .delete(session -> "/api/categories/" + session.getString("categoryId"))
                .header("Authorization", session -> "Bearer " + session.getString("jwt_token"))
                .check(status().is(204))
                .check(responseTimeInMillis().lte(1000))
        ).exec(session -> {
            System.out.println("Deleted category with ID: " + session.getString("categoryId"));
            TESTED_ENDPOINTS.add("DELETE /api/categories/{id}");
            return session;
        });
    }

    // ========================================
    // PRODUCT CRUD OPERATIONS
    // ========================================

    ChainBuilder createProduct() {
        return exec(session -> {
            String timestamp = getCurrentTimestamp();
            int counter = productCounter.getAndIncrement();
            return session
                .set("productName", "Product_" + counter + "_" + timestamp)
                .set("productDescription", "Test product created at " + timestamp)
                .set("productPrice", String.valueOf(99.99 + (counter % 100)))
                .set("productImageUrl", "https://example.com/product" + counter + ".jpg")
                .set("productRating", String.valueOf(3.0 + (counter % 3)));
        })
            .exec(
                http("Create Product")
                    .post("/api/products")
                    .header("Authorization", session -> "Bearer " + session.getString("jwt_token"))
                    .header("Content-Type", "application/json")
                    .body(
                        StringBody(session ->
                            """
                            {
                                "name": "%s",
                                "description": "%s",
                                "price": %s,
                                "imageUrl": "%s",
                                "rating": %s,
                                "category": {"id": %s}
                            }
                            """.formatted(
                                    session.getString("productName"),
                                    session.getString("productDescription"),
                                    session.getString("productPrice"),
                                    session.getString("productImageUrl"),
                                    session.getString("productRating"),
                                    session.getString("categoryId")
                                )
                        )
                    )
                    .check(status().in(201))
                    .check(responseTimeInMillis().lte(1000))
                    .check(jsonPath("$.id").exists().saveAs("productId"))
            )
            .exitHereIfFailed()
            .exec(session -> {
                System.out.println("Created product with ID: " + session.getString("productId"));
                TESTED_ENDPOINTS.add("POST /api/products");
                return session;
            });
    }

    ChainBuilder getAllProducts() {
        return exec(
            http("Get All Products")
                .get("/api/products")
                .header("Authorization", session -> "Bearer " + session.getString("jwt_token"))
                .check(status().is(200))
                .check(responseTimeInMillis().lte(1000))
                .check(jsonPath("$").exists())
        ).exec(session -> {
            TESTED_ENDPOINTS.add("GET /api/products");
            return session;
        });
    }

    ChainBuilder getProductById() {
        return exec(
            http("Get Product By ID")
                .get(session -> "/api/products/" + session.getString("productId"))
                .header("Authorization", session -> "Bearer " + session.getString("jwt_token"))
                .check(status().is(200))
                .check(responseTimeInMillis().lte(1000))
                .check(jsonPath("$.id").is(session -> session.getString("productId")))
                .check(jsonPath("$.name").exists())
        ).exec(session -> {
            TESTED_ENDPOINTS.add("GET /api/products/{id}");
            return session;
        });
    }

    ChainBuilder updateProduct() {
        return exec(session -> {
            // Create a short, unique name to avoid constraint issues
            String productId = session.getString("productId");
            String shortName = "UpdProd_" + productId + "_" + (System.currentTimeMillis() % 10000);
            String safeDescription = "Updated product " + productId;
            return session.set("updatedProductName", shortName).set("updatedProductDescription", safeDescription);
        })
            .exec(
                http("Update Product")
                    .put(session -> "/api/products/" + session.getString("productId"))
                    .header("Authorization", session -> "Bearer " + session.getString("jwt_token"))
                    .header("Content-Type", "application/json")
                    .body(
                        StringBody(session ->
                            """
                            {
                                "id": %s,
                                "name": "%s",
                                "description": "%s",
                                "price": %s,
                                "imageUrl": "%s",
                                "rating": %s,
                                "category": {"id": %s}
                            }
                            """.formatted(
                                    session.getString("productId"), // ID as number
                                    session.getString("updatedProductName"),
                                    session.getString("updatedProductDescription"),
                                    session.getString("productPrice"),
                                    session.getString("productImageUrl"),
                                    session.getString("productRating"),
                                    session.getString("categoryId")
                                )
                        )
                    )
                    .check(status().is(200))
                    .check(responseTimeInMillis().lte(1000))
                    .check(jsonPath("$.name").is(session -> session.getString("updatedProductName")))
            )
            .exec(session -> {
                TESTED_ENDPOINTS.add("PUT /api/products/{id}");
                return session;
            });
    }

    ChainBuilder partialUpdateProduct() {
        return exec(
            http("Partial Update Product")
                .patch(session -> "/api/products/" + session.getString("productId"))
                .header("Authorization", session -> "Bearer " + session.getString("jwt_token"))
                .header("Content-Type", "application/json")
                .body(
                    StringBody(session ->
                        """
                        {
                            "id": %s,
                            "rating": 4.5
                        }
                        """.formatted(session.getString("productId"))
                    )
                )
                .check(status().is(200))
                .check(responseTimeInMillis().lte(1000))
        ).exec(session -> {
            TESTED_ENDPOINTS.add("PATCH /api/products/{id}");
            return session;
        });
    }

    ChainBuilder getFilteredProducts() {
        return exec(
            http("Get Filtered Products")
                .get("/api/products/filter?minPrice=50&maxPrice=200&minRating=3.0")
                .header("Authorization", session -> "Bearer " + session.getString("jwt_token"))
                .check(status().is(200))
                .check(responseTimeInMillis().lte(1000))
                .check(jsonPath("$").exists())
        ).exec(session -> {
            TESTED_ENDPOINTS.add("GET /api/products/filter");
            return session;
        });
    }

    ChainBuilder deleteProduct() {
        return exec(
            http("Delete Product")
                .delete(session -> "/api/products/" + session.getString("productId"))
                .header("Authorization", session -> "Bearer " + session.getString("jwt_token"))
                .check(status().is(204))
                .check(responseTimeInMillis().lte(1000))
        ).exec(session -> {
            System.out.println("Deleted product with ID: " + session.getString("productId"));
            TESTED_ENDPOINTS.add("DELETE /api/products/{id}");
            return session;
        });
    }

    // ========================================
    // ADMINISTRATION ENDPOINTS
    // ========================================

    ChainBuilder getAuthorities() {
        return exec(
            http("Get Authorities")
                .get("/api/authorities")
                .header("Authorization", session -> "Bearer " + session.getString("jwt_token"))
                .check(status().is(200))
                .check(responseTimeInMillis().lte(1000))
                .check(jsonPath("$").exists())
        ).exec(session -> {
            TESTED_ENDPOINTS.add("GET /api/authorities");
            return session;
        });
    }

    ChainBuilder getAllUsers() {
        return exec(
            http("Get All Users (Admin)")
                .get("/api/admin/users")
                .header("Authorization", session -> "Bearer " + session.getString("jwt_token"))
                .check(status().is(200))
                .check(responseTimeInMillis().lte(1000))
                .check(jsonPath("$").exists())
        ).exec(session -> {
            TESTED_ENDPOINTS.add("GET /api/admin/users");
            return session;
        });
    }

    ChainBuilder getPublicUsers() {
        return exec(
            http("Get Public Users")
                .get("/api/users")
                .header("Authorization", session -> "Bearer " + session.getString("jwt_token"))
                .check(status().is(200))
                .check(responseTimeInMillis().lte(1000))
                .check(jsonPath("$").exists())
        ).exec(session -> {
            TESTED_ENDPOINTS.add("GET /api/users");
            return session;
        });
    }

    // ========================================
    // UTILITY METHODS
    // ========================================

    private static String getCurrentTimestamp() {
        return ZonedDateTime.now(ZoneOffset.UTC).format(DateTimeFormatter.ISO_INSTANT);
    }

    // ========================================
    // MAIN SCENARIO
    // ========================================

    ScenarioBuilder comprehensiveApiTestScenario = scenario("Comprehensive API Performance Test")
        .exec(authenticateUser())
        .pause(1, 2)
        .exec(verifyAuthentication())
        .pause(1, 2)
        // Category workflow
        .exec(getAllCategories())
        .pause(1, 2)
        .exec(createCategory())
        .pause(1, 2)
        .exec(getCategoryById())
        .pause(1, 2)
        .exec(updateCategory())
        .pause(1, 2)
        .exec(partialUpdateCategory())
        .pause(1, 2)
        // Product workflow (requires category)
        .exec(getAllProducts())
        .pause(1, 2)
        .exec(createProduct())
        .pause(1, 2)
        .exec(getProductById())
        .pause(1, 2)
        .exec(updateProduct())
        .pause(1, 2)
        .exec(partialUpdateProduct())
        .pause(1, 2)
        .exec(getFilteredProducts())
        .pause(1, 2)
        // Admin endpoints
        .exec(getAuthorities())
        .pause(1, 2)
        .exec(getAllUsers())
        .pause(1, 2)
        .exec(getPublicUsers())
        .pause(1, 2)
        // Cleanup
        .exec(deleteProduct())
        .pause(1, 2)
        .exec(deleteCategory())
        // Coverage reporting
        .exec(session -> {
            printCoverageReport();
            return session;
        });

    // ========================================
    // SIMULATION SETUP
    // ========================================

    {
        setUp(comprehensiveApiTestScenario.injectOpen(rampUsers(users).during(Duration.ofSeconds(rampDurationSeconds))))
            .protocols(httpProtocol)
            .assertions(
                // Global performance assertions
                global().successfulRequests().percent().gte(95.0),
                global().responseTime().mean().lte(1000),
                global().responseTime().percentile3().lte(2000),
                // Specific endpoint assertions
                details("Authenticate User").responseTime().mean().lte(2000),
                details("Create Category").responseTime().mean().lte(1000),
                details("Create Product").responseTime().mean().lte(1000)
            );
    }

    // ========================================
    // COVERAGE REPORTING
    // ========================================

    private static void printCoverageReport() {
        System.out.println("\n" + "=".repeat(80));
        System.out.println("API ENDPOINT COVERAGE REPORT");
        System.out.println("=".repeat(80));

        Set<String> missedEndpoints = new HashSet<>(DISCOVERED_ENDPOINTS);
        missedEndpoints.removeAll(TESTED_ENDPOINTS);

        double coverage = ((double) TESTED_ENDPOINTS.size() / DISCOVERED_ENDPOINTS.size()) * 100;

        System.out.printf("Endpoints Covered: %d/%d (%.1f%%)%n", TESTED_ENDPOINTS.size(), DISCOVERED_ENDPOINTS.size(), coverage);

        if (!missedEndpoints.isEmpty()) {
            System.out.println("\nMissing Endpoints:");
            missedEndpoints.stream().sorted().forEach(endpoint -> System.out.println("  - " + endpoint));
        }

        System.out.println("\nTested Endpoints:");
        TESTED_ENDPOINTS.stream().sorted().forEach(endpoint -> System.out.println("  ✓ " + endpoint));

        System.out.println("=".repeat(80));
        System.out.println("Test Configuration:");
        System.out.printf("  Base URL: %s%n", System.getProperty("baseUrl", "http://localhost:8080"));
        System.out.printf("  Users: %d%n", Integer.getInteger("users", 10));
        System.out.printf("  Ramp Duration: %d seconds%n", Integer.getInteger("rampDuration", 10));
        System.out.printf("  Test Completed: %s%n", getCurrentTimestamp());
        System.out.println("=".repeat(80) + "\n");
    }
}
