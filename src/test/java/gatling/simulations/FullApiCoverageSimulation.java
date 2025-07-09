package gatling.simulations;

import static io.gatling.javaapi.core.CoreDsl.*;
import static io.gatling.javaapi.http.HttpDsl.*;

import io.gatling.javaapi.core.*;
import io.gatling.javaapi.http.*;
import java.time.Duration;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class FullApiCoverageSimulation extends Simulation {

    // API Coverage tracking
    private static final Map<String, Boolean> apiCoverage = new ConcurrentHashMap<>();
    private static final Map<String, List<Double>> responseTimesByEndpoint = new ConcurrentHashMap<>();

    // Base configuration
    private final String baseURL = Optional.ofNullable(System.getProperty("baseURL")).orElse("http://localhost:8080");
    private final HttpProtocolBuilder httpProtocol = http
        .baseUrl(baseURL)
        .inferHtmlResources()
        .acceptHeader("application/json")
        .contentTypeHeader("application/json")
        .userAgentHeader("Gatling Performance Test");

    // Test data
    private final String adminUsername = "admin";
    private final String adminPassword = "admin";
    private final String testUserEmail = "test-user-" + UUID.randomUUID() + "@example.com";
    private final String testUserPassword = "test-password-123";

    // Helper methods for request chains
    private ChainBuilder authenticate() {
        return exec(
            http("Authentication")
                .post("/api/authenticate")
                .header("Accept", "application/json")
                .header("Content-Type", "application/json")
                .body(StringBody("{\"username\":\"" + adminUsername + "\", \"password\":\"" + adminPassword + "\"}"))
                .check(status().is(200))
                .check(jsonPath("$.id_token").saveAs("jwt_token"))
                .check(bodyString().saveAs("auth_response"))
        )
            .exec(session -> {
                System.out.println("🔐 Authentication successful. JWT Token: " + session.getString("jwt_token").substring(0, 20) + "...");
                apiCoverage.put("/api/authenticate [POST]", true);
                return session;
            })
            .exitHereIfFailed();
    }

    private ChainBuilder registerUser() {
        return exec(
            http("Register User")
                .post("/api/register")
                .header("Accept", "application/json")
                .header("Content-Type", "application/json")
                .body(
                    StringBody(
                        String.format(
                            """
                            {
                                "login": "test-user-%s",
                                "email": "%s",
                                "password": "%s",
                                "langKey": "en"
                            }
                            """,
                            UUID.randomUUID(),
                            testUserEmail,
                            testUserPassword
                        )
                    )
                )
                .check(status().is(201))
        ).exec(session -> {
            apiCoverage.put("/api/register [POST]", true);
            return session;
        });
    }

    private ChainBuilder getAccount() {
        return exec(
            http("Get Account")
                .get("/api/account")
                .header("Accept", "application/json")
                .header("Authorization", session -> "Bearer " + session.getString("jwt_token"))
                .check(status().is(200))
        ).exec(session -> {
            apiCoverage.put("/api/account [GET]", true);
            return session;
        });
    }

    private ChainBuilder createCategory() {
        String uuid = UUID.randomUUID().toString();
        return exec(
            http("Create Category")
                .post("/api/categories")
                .header("Accept", "application/json")
                .header("Content-Type", "application/json")
                .header("Authorization", session -> "Bearer " + session.getString("jwt_token"))
                .body(
                    StringBody(
                        String.format(
                            """
                            {
                                "name": "Category-%s",
                                "description": "Test category created at %s"
                            }
                            """,
                            uuid,
                            ZonedDateTime.now(ZoneOffset.UTC).toString()
                        )
                    )
                )
                .check(status().is(201))
                .check(jsonPath("$.id").saveAs("category_id"))
        ).exec(session -> {
            System.out.println("📁 Created Category - ID: " + session.getString("category_id"));
            apiCoverage.put("/api/categories [POST]", true);
            return session;
        });
    }

    private ChainBuilder getCategories() {
        return exec(
            http("Get Categories")
                .get("/api/categories")
                .header("Accept", "application/json")
                .header("Authorization", session -> "Bearer " + session.getString("jwt_token"))
                .check(status().is(200))
        ).exec(session -> {
            apiCoverage.put("/api/categories [GET]", true);
            return session;
        });
    }

    private ChainBuilder updateCategory() {
        return exec(
            http("Update Category")
                .put(session -> "/api/categories/" + session.getString("category_id"))
                .header("Accept", "application/json")
                .header("Content-Type", "application/json")
                .header("Authorization", session -> "Bearer " + session.getString("jwt_token"))
                .body(
                    StringBody(session ->
                        String.format(
                            """
                            {
                                "id": %s,
                                "name": "Updated Category-%s",
                                "description": "Updated test category at %s"
                            }
                            """,
                            session.getString("category_id"),
                            session.getString("category_id"),
                            ZonedDateTime.now(ZoneOffset.UTC).toString()
                        )
                    )
                )
                .check(status().is(200))
        ).exec(session -> {
            apiCoverage.put("/api/categories [PUT]", true);
            return session;
        });
    }

    private ChainBuilder deleteCategory() {
        return exec(
            http("Delete Category")
                .delete(session -> "/api/categories/" + session.getString("category_id"))
                .header("Accept", "application/json")
                .header("Authorization", session -> "Bearer " + session.getString("jwt_token"))
                .check(status().is(204))
        ).exec(session -> {
            System.out.println("🗑️ Deleted Category - ID: " + session.getString("category_id"));
            apiCoverage.put("/api/categories [DELETE]", true);
            return session;
        });
    }

    private ChainBuilder createProduct() {
        String uuid = UUID.randomUUID().toString();
        return exec(
            http("Create Product")
                .post("/api/products")
                .header("Accept", "application/json")
                .header("Content-Type", "application/json")
                .header("Authorization", session -> "Bearer " + session.getString("jwt_token"))
                .body(
                    StringBody(session ->
                        String.format(
                            """
                            {
                                "name": "Product-%s",
                                "description": "Test product created at %s",
                                "price": 99.99,
                                "rating": 4.5,
                                "imageUrl": "http://example.com/image.jpg",
                                "category": {
                                    "id": %s
                                }
                            }
                            """,
                            uuid,
                            ZonedDateTime.now(ZoneOffset.UTC).toString(),
                            session.getString("category_id")
                        )
                    )
                )
                .check(status().is(201))
                .check(jsonPath("$.id").saveAs("product_id"))
                .check(headerRegex("Location", "(.*)").saveAs("product_url"))
        ).exec(session -> {
            System.out.println(
                "📦 Created Product - ID: " + session.getString("product_id") + ", URL: " + session.getString("product_url")
            );
            apiCoverage.put("/api/products [POST]", true);
            return session;
        });
    }

    private ChainBuilder getProduct() {
        return exec(
            http("Get Product")
                .get(session -> "/api/products/" + session.getString("product_id"))
                .header("Accept", "application/json")
                .header("Authorization", session -> "Bearer " + session.getString("jwt_token"))
                .check(status().is(200))
                .check(jsonPath("$.name").exists())
        ).exec(session -> {
            apiCoverage.put("/api/products [GET]", true);
            return session;
        });
    }

    private ChainBuilder deleteProduct() {
        return exec(
            http("Delete Product")
                .delete(session -> "/api/products/" + session.getString("product_id"))
                .header("Accept", "application/json")
                .header("Authorization", session -> "Bearer " + session.getString("jwt_token"))
                .check(status().is(204))
        ).exec(session -> {
            System.out.println("🗑️ Deleted Product - ID: " + session.getString("product_id"));
            apiCoverage.put("/api/products [DELETE]", true);
            return session;
        });
    }

    private ChainBuilder getUsers() {
        return exec(
            http("Get Users")
                .get("/api/users")
                .header("Accept", "application/json")
                .header("Authorization", session -> "Bearer " + session.getString("jwt_token"))
                .check(status().is(200))
        ).exec(session -> {
            apiCoverage.put("/api/users [GET]", true);
            return session;
        });
    }

    private ChainBuilder getAuthorities() {
        return exec(
            http("Get Authorities")
                .get("/api/authorities")
                .header("Accept", "application/json")
                .header("Authorization", session -> "Bearer " + session.getString("jwt_token"))
                .check(status().is(200))
        ).exec(session -> {
            apiCoverage.put("/api/authorities [GET]", true);
            return session;
        });
    }

    private ChainBuilder changePassword() {
        return exec(
            http("Change Password")
                .post("/api/account/change-password")
                .header("Accept", "application/json")
                .header("Content-Type", "application/json")
                .header("Authorization", session -> "Bearer " + session.getString("jwt_token"))
                .body(
                    StringBody(
                        String.format(
                            """
                            {
                                "currentPassword": "%s",
                                "newPassword": "%s-updated"
                            }
                            """,
                            adminPassword,
                            adminPassword
                        )
                    )
                )
                .check(status().is(200))
        ).exec(session -> {
            apiCoverage.put("/api/account/change-password [POST]", true);
            return session;
        });
    }

    // Main scenario
    private final ScenarioBuilder scn = scenario("Full API Coverage Test")
        .exec(authenticate())
        .exec(getAccount())
        .exec(createCategory())
        .exec(getCategories())
        .exec(updateCategory())
        .exec(createProduct())
        .exec(getProduct())
        .exec(deleteProduct())
        .exec(deleteCategory());

    // Simulation setup
    {
        setUp(
            scn.injectOpen(
                nothingFor(2), // Initial pause to let the system stabilize
                atOnceUsers(1) // Run one user at a time to maintain data consistency
            )
        )
            .protocols(httpProtocol)
            .assertions(
                global().responseTime().percentile3().lt(3000),
                global().responseTime().mean().lt(1000),
                global().successfulRequests().percent().gt(95)
            )
            .andThen(() -> {
                // Print final coverage report
                long covered = apiCoverage.values().stream().filter(Boolean::booleanValue).count();
                long total = apiCoverage.size();
                double coveragePercentage = ((100.0 * covered) / total);

                System.out.println("\n📊 API Coverage Report");
                System.out.println("====================");
                System.out.printf("✅ Coverage: %d / %d (%.2f%%)%n", covered, total, coveragePercentage);
                System.out.println("\nEndpoint Coverage Details:");
                System.out.println("-------------------------");
                apiCoverage.forEach((endpoint, isCovered) ->
                    System.out.printf("%s: %s%n", endpoint, isCovered ? "✅ Covered" : "❌ Not Covered")
                );
            });
    }
}
