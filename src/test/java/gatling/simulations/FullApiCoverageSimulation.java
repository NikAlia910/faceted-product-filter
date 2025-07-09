package gatling.simulations;

import static io.gatling.javaapi.core.CoreDsl.*;
import static io.gatling.javaapi.http.HttpDsl.*;

import io.gatling.javaapi.core.*;
import io.gatling.javaapi.http.*;
import java.math.BigDecimal;
import java.time.Duration;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ThreadLocalRandom;

public class FullApiCoverageSimulation extends Simulation {

    private static final String BASE_URL = System.getProperty("baseURL", "http://localhost:8080");
    private static final Map<String, Boolean> apiCoverage = new ConcurrentHashMap<>();

    private static final HttpProtocolBuilder httpConf = http
        .baseUrl(BASE_URL)
        .acceptHeader("application/json")
        .contentTypeHeader("application/json");

    // Helper: Authenticate and extract JWT
    private ChainBuilder authRequest() {
        return exec(
            http("Authenticate")
                .post("/api/authenticate")
                .body(StringBody("{" + "\"username\": \"admin\"," + "\"password\": \"admin\"}"))
                .check(status().is(200))
                .check(jsonPath("$.id_token").saveAs("jwt_token"))
                .check(responseTimeInMillis().lt(1000))
        )
            .exec(session -> {
                String token = session.getString("jwt_token");
                System.out.println("Token: " + token);
                apiCoverage.put("/api/authenticate [POST]", true);
                return session;
            })
            .exitHereIfFailed();
    }

    // Helper: Create Category
    private ChainBuilder createCategory() {
        return exec(session -> {
            String uuidName = "Category-" + UUID.randomUUID();
            String body = String.format("{\"name\":\"%s\",\"description\":\"desc-%s\"}", uuidName, uuidName);
            return session.set("categoryBody", body);
        })
            .exec(
                http("Create Category")
                    .post("/api/categories")
                    .header("Authorization", "Bearer #{jwt_token}")
                    .body(StringBody("#{categoryBody}"))
                    .check(status().is(201))
                    .check(jsonPath("$.id").saveAs("categoryId"))
                    .check(responseTimeInMillis().lt(1000))
            )
            .exec(session -> {
                String id = session.getString("categoryId");
                System.out.println("Created Category ID: " + id);
                apiCoverage.put("/api/categories [POST]", true);
                return session;
            })
            .exitHereIfFailed();
    }

    // Helper: Get All Categories
    private ChainBuilder getAllCategories() {
        return exec(
            http("Get All Categories")
                .get("/api/categories?page=0&size=10&sort=id,desc")
                .header("Authorization", "Bearer #{jwt_token}")
                .check(status().is(200))
                .check(responseTimeInMillis().lt(1000))
        )
            .exec(session -> {
                apiCoverage.put("/api/categories [GET]", true);
                System.out.println("Retrieved all categories");
                return session;
            })
            .exitHereIfFailed();
    }

    // Helper: Get Category
    private ChainBuilder getCategory() {
        return exec(
            http("Get Category")
                .get("/api/categories/#{categoryId}")
                .header("Authorization", "Bearer #{jwt_token}")
                .check(status().is(200))
                .check(responseTimeInMillis().lt(1000))
        )
            .exec(session -> {
                String id = session.getString("categoryId");
                System.out.println("Fetched Category ID: " + id);
                apiCoverage.put("/api/categories/{id} [GET]", true);
                return session;
            })
            .exitHereIfFailed();
    }

    // Helper: Update Category
    private ChainBuilder updateCategory() {
        return exec(session -> {
            String uuidName = "Updated-Category-" + UUID.randomUUID();
            String body = String.format("{\"id\":#{categoryId},\"name\":\"%s\",\"description\":\"updated-desc-%s\"}", uuidName, uuidName);
            return session.set("updatedCategoryBody", body);
        })
            .exec(
                http("Update Category")
                    .put("/api/categories/#{categoryId}")
                    .header("Authorization", "Bearer #{jwt_token}")
                    .body(StringBody("#{updatedCategoryBody}"))
                    .check(status().is(200))
                    .check(responseTimeInMillis().lt(1000))
            )
            .exec(session -> {
                String id = session.getString("categoryId");
                System.out.println("Updated Category ID: " + id);
                apiCoverage.put("/api/categories/{id} [PUT]", true);
                return session;
            })
            .exitHereIfFailed();
    }

    // Helper: Patch Category
    private ChainBuilder patchCategory() {
        return exec(session -> {
            String body = String.format("{\"id\":#{categoryId},\"description\":\"patched-desc-%s\"}", UUID.randomUUID());
            return session.set("patchedCategoryBody", body);
        })
            .exec(
                http("Patch Category")
                    .patch("/api/categories/#{categoryId}")
                    .header("Authorization", "Bearer #{jwt_token}")
                    .body(StringBody("#{patchedCategoryBody}"))
                    .check(status().is(200))
                    .check(responseTimeInMillis().lt(1000))
            )
            .exec(session -> {
                String id = session.getString("categoryId");
                System.out.println("Patched Category ID: " + id);
                apiCoverage.put("/api/categories/{id} [PATCH]", true);
                return session;
            })
            .exitHereIfFailed();
    }

    // Helper: Delete Category
    private ChainBuilder deleteCategory() {
        return exec(
            http("Delete Category")
                .delete("/api/categories/#{categoryId}")
                .header("Authorization", "Bearer #{jwt_token}")
                .check(status().is(204))
                .check(responseTimeInMillis().lt(1000))
        )
            .exec(session -> {
                String id = session.getString("categoryId");
                System.out.println("Deleted Category ID: " + id);
                apiCoverage.put("/api/categories/{id} [DELETE]", true);
                return session;
            })
            .exitHereIfFailed();
    }

    // Helper: Create Product
    private ChainBuilder createProduct() {
        return exec(session -> {
            String uuidName = "Product-" + UUID.randomUUID();
            String now = ZonedDateTime.now(ZoneOffset.UTC).toString();
            String body = String.format(
                "{\"name\":\"%s\",\"description\":\"desc-%s\",\"price\":%s,\"imageUrl\":\"http://img.com/%s.png\",\"rating\":%s,\"category\":{\"id\":%s}}",
                uuidName,
                uuidName,
                new BigDecimal(ThreadLocalRandom.current().nextDouble(1, 100)).setScale(2, BigDecimal.ROUND_HALF_UP),
                uuidName,
                ThreadLocalRandom.current().nextDouble(0, 5),
                session.getString("categoryId")
            );
            return session.set("productBody", body);
        })
            .exec(
                http("Create Product")
                    .post("/api/products")
                    .header("Authorization", "Bearer #{jwt_token}")
                    .body(StringBody("#{productBody}"))
                    .check(status().is(201))
                    .check(jsonPath("$.id").saveAs("productId"))
                    .check(responseTimeInMillis().lt(1000))
            )
            .exec(session -> {
                String id = session.getString("productId");
                System.out.println("Created Product ID: " + id);
                apiCoverage.put("/api/products [POST]", true);
                return session;
            })
            .exitHereIfFailed();
    }

    // Helper: Get All Products
    private ChainBuilder getAllProducts() {
        return exec(
            http("Get All Products")
                .get("/api/products?page=0&size=10&sort=id,desc&eagerload=true")
                .header("Authorization", "Bearer #{jwt_token}")
                .check(status().is(200))
                .check(responseTimeInMillis().lt(1000))
        )
            .exec(session -> {
                apiCoverage.put("/api/products [GET]", true);
                System.out.println("Retrieved all products");
                return session;
            })
            .exitHereIfFailed();
    }

    // Helper: Get Filtered Products
    private ChainBuilder getFilteredProducts() {
        return exec(
            http("Get Filtered Products")
                .get("/api/products/filter?minPrice=10&maxPrice=50&categoryIds=#{categoryId}&minRating=3")
                .header("Authorization", "Bearer #{jwt_token}")
                .check(status().is(200))
                .check(responseTimeInMillis().lt(1000))
        )
            .exec(session -> {
                apiCoverage.put("/api/products/filter [GET]", true);
                System.out.println("Retrieved filtered products");
                return session;
            })
            .exitHereIfFailed();
    }

    // Helper: Get Product
    private ChainBuilder getProduct() {
        return exec(
            http("Get Product")
                .get("/api/products/#{productId}")
                .header("Authorization", "Bearer #{jwt_token}")
                .check(status().is(200))
                .check(responseTimeInMillis().lt(1000))
        )
            .exec(session -> {
                String id = session.getString("productId");
                System.out.println("Fetched Product ID: " + id);
                apiCoverage.put("/api/products/{id} [GET]", true);
                return session;
            })
            .exitHereIfFailed();
    }

    // Helper: Update Product
    private ChainBuilder updateProduct() {
        return exec(session -> {
            String uuidName = "Updated-Product-" + UUID.randomUUID();
            String body = String.format(
                "{\"id\":#{productId},\"name\":\"%s\",\"description\":\"updated-desc-%s\",\"price\":%s,\"imageUrl\":\"http://img.com/%s.png\",\"rating\":%s,\"category\":{\"id\":%s}}",
                uuidName,
                uuidName,
                new BigDecimal(ThreadLocalRandom.current().nextDouble(1, 100)).setScale(2, BigDecimal.ROUND_HALF_UP),
                uuidName,
                ThreadLocalRandom.current().nextDouble(0, 5),
                session.getString("categoryId")
            );
            return session.set("updatedProductBody", body);
        })
            .exec(
                http("Update Product")
                    .put("/api/products/#{productId}")
                    .header("Authorization", "Bearer #{jwt_token}")
                    .body(StringBody("#{updatedProductBody}"))
                    .check(status().is(200))
                    .check(responseTimeInMillis().lt(1000))
            )
            .exec(session -> {
                String id = session.getString("productId");
                System.out.println("Updated Product ID: " + id);
                apiCoverage.put("/api/products/{id} [PUT]", true);
                return session;
            })
            .exitHereIfFailed();
    }

    // Helper: Patch Product
    private ChainBuilder patchProduct() {
        return exec(session -> {
            String body = String.format(
                "{\"id\":#{productId},\"description\":\"patched-desc-%s\",\"rating\":%s}",
                UUID.randomUUID(),
                ThreadLocalRandom.current().nextDouble(0, 5)
            );
            return session.set("patchedProductBody", body);
        })
            .exec(
                http("Patch Product")
                    .patch("/api/products/#{productId}")
                    .header("Authorization", "Bearer #{jwt_token}")
                    .body(StringBody("#{patchedProductBody}"))
                    .check(status().is(200))
                    .check(responseTimeInMillis().lt(1000))
            )
            .exec(session -> {
                String id = session.getString("productId");
                System.out.println("Patched Product ID: " + id);
                apiCoverage.put("/api/products/{id} [PATCH]", true);
                return session;
            })
            .exitHereIfFailed();
    }

    // Helper: Delete Product
    private ChainBuilder deleteProduct() {
        return exec(
            http("Delete Product")
                .delete("/api/products/#{productId}")
                .header("Authorization", "Bearer #{jwt_token}")
                .check(status().is(204))
                .check(responseTimeInMillis().lt(1000))
        )
            .exec(session -> {
                String id = session.getString("productId");
                System.out.println("Deleted Product ID: " + id);
                apiCoverage.put("/api/products/{id} [DELETE]", true);
                return session;
            })
            .exitHereIfFailed();
    }

    private ScenarioBuilder scn = scenario("Full API Coverage Scenario")
        // Authentication
        .exec(authRequest())
        .pause(1)
        // Category CRUD
        .exec(createCategory())
        .pause(1)
        .exec(getAllCategories())
        .pause(1)
        .exec(getCategory())
        .pause(1)
        .exec(updateCategory())
        .pause(1)
        .exec(patchCategory())
        .pause(1)
        // Product CRUD (using the created category)
        .exec(createProduct())
        .pause(1)
        .exec(getAllProducts())
        .pause(1)
        .exec(getFilteredProducts())
        .pause(1)
        .exec(getProduct())
        .pause(1)
        .exec(updateProduct())
        .pause(1)
        .exec(patchProduct())
        .pause(1)
        // Cleanup in reverse order
        .exec(deleteProduct())
        .pause(1)
        .exec(deleteCategory());

    {
        setUp(scn.injectOpen(rampUsers(10).during(Duration.ofSeconds(10)))).protocols(httpConf);
    }

    @Override
    public void after() {
        long covered = apiCoverage.values().stream().filter(Boolean::booleanValue).count();
        long total = apiCoverage.size();
        System.out.printf("✅ API Coverage: %d / %d (%.2f%%)%n", covered, total, ((100.0 * covered) / total));

        // Print uncovered endpoints
        System.out.println("\nEndpoint Coverage Details:");
        apiCoverage.forEach((endpoint, isCovered) -> {
            System.out.printf("%s: %s%n", endpoint, isCovered ? "✅" : "❌");
        });
    }
}
