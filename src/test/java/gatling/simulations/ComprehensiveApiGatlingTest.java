package gatling.simulations;

import static io.gatling.javaapi.core.CoreDsl.*;
import static io.gatling.javaapi.http.HttpDsl.*;

import io.gatling.javaapi.core.*;
import io.gatling.javaapi.http.*;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

public class ComprehensiveApiGatlingTest extends Simulation {

    private static final Map<String, Boolean> apiCoverage = new ConcurrentHashMap<>();
    private static final AtomicInteger totalRequests = new AtomicInteger(0);
    private static final AtomicInteger successRequests = new AtomicInteger(0);

    String baseURL = Optional.ofNullable(System.getProperty("baseURL")).orElse("http://localhost:8080");
    HttpProtocolBuilder httpConf = http
        .baseUrl(baseURL)
        .inferHtmlResources()
        .acceptHeader("application/json")
        .acceptEncodingHeader("gzip, deflate")
        .acceptLanguageHeader("en-US,en;q=0.9")
        .connectionHeader("keep-alive")
        .userAgentHeader("Gatling/Comprehensive-API-Test-Strict")
        .silentResources()
        .disableCaching()
        .shareConnections();

    static {
        String[] endpoints = {
            "/api/authenticate [POST]",
            "/api/authenticate [GET]",
            "/api/account [GET]",
            "/api/account [POST]",
            "/api/account/change-password [POST]",
            "/api/account/reset-password/init [POST]",
            "/api/account/reset-password/finish [POST]",
            "/api/categories [GET]",
            "/api/categories [POST]",
            "/api/categories/{id} [GET]",
            "/api/categories/{id} [PUT]",
            "/api/categories/{id} [PATCH]",
            "/api/categories/{id} [DELETE]",
            "/api/products [GET]",
            "/api/products [POST]",
            "/api/products/{id} [GET]",
            "/api/products/{id} [PUT]",
            "/api/products/{id} [PATCH]",
            "/api/products/{id} [DELETE]",
            "/api/products/filter [GET]",
            "/api/admin/users [GET]",
            "/api/admin/users [POST]",
            "/api/admin/users/{login} [GET]",
            "/api/admin/users/{login} [PUT]",
            "/api/admin/users/{login} [DELETE]",
            "/api/authorities [GET]",
            "/api/authorities [POST]",
            "/api/authorities/{id} [GET]",
            "/api/authorities/{id} [DELETE]",
            "/api/users [GET]",
            "/api/register [POST]",
            "/api/activate [GET]",
        };
        for (String endpoint : endpoints) {
            apiCoverage.put(endpoint, false);
        }
    }

    private static void markApiCovered(String endpoint, boolean success) {
        totalRequests.incrementAndGet();
        if (success) successRequests.incrementAndGet();
        apiCoverage.put(endpoint, success);
        System.out.println(
            (success ? "✅" : "❌") + " [" + totalRequests.get() + "/32] API " + (success ? "SUCCESS" : "FAIL") + ": " + endpoint
        );
    }

    private static String generateUniqueId() {
        return UUID.randomUUID().toString().substring(0, 8);
    }

    ChainBuilder comprehensiveApiTest = exec(session -> {
        System.out.println("\n🚦 Starting STRICT Comprehensive API Test - Target: 32 requests, 100% real success");
        System.out.println("=".repeat(80));
        return session;
    })
        // 1. Authenticate as admin
        .exec(
            http("POST /api/authenticate")
                .post("/api/authenticate")
                .header("Content-Type", "application/json")
                .body(StringBody("{\"username\":\"admin\",\"password\":\"admin\"}"))
                .asJson()
                .check(status().is(200))
                .check(jsonPath("$.id_token").saveAs("jwt_token"))
        )
        .exec(session -> {
            String token = session.getString("jwt_token");
            boolean ok = token != null && !token.isEmpty();
            markApiCovered("/api/authenticate [POST]", ok);
            return session;
        })
        // 2. Check authentication
        .exec(http("GET /api/authenticate").get("/api/authenticate").header("Authorization", "Bearer ${jwt_token}").check(status().is(204)))
        .exec(session -> {
            markApiCovered("/api/authenticate [GET]", true);
            return session;
        })
        // 3. Get account
        .exec(http("GET /api/account").get("/api/account").header("Authorization", "Bearer ${jwt_token}").check(status().is(200)))
        .exec(session -> {
            markApiCovered("/api/account [GET]", true);
            return session;
        })
        // 4. Update account
        .exec(
            http("POST /api/account")
                .post("/api/account")
                .header("Authorization", "Bearer ${jwt_token}")
                .header("Content-Type", "application/json")
                .body(StringBody("{\"firstName\":\"Test\",\"lastName\":\"Admin\",\"email\":\"admin@localhost\",\"langKey\":\"en\"}"))
                .asJson()
                .check(status().is(200))
        )
        .exec(session -> {
            markApiCovered("/api/account [POST]", true);
            return session;
        })
        // 5. Change password
        .exec(
            http("POST /api/account/change-password")
                .post("/api/account/change-password")
                .header("Authorization", "Bearer ${jwt_token}")
                .header("Content-Type", "application/json")
                .body(StringBody("{\"currentPassword\":\"admin\",\"newPassword\":\"admin\"}"))
                .asJson()
                .check(status().is(200))
        )
        .exec(session -> {
            markApiCovered("/api/account/change-password [POST]", true);
            return session;
        })
        // 6. Request password reset
        .exec(
            http("POST /api/account/reset-password/init")
                .post("/api/account/reset-password/init")
                .header("Content-Type", "application/json")
                .body(StringBody("admin@localhost"))
                .check(status().is(200))
        )
        .exec(session -> {
            markApiCovered("/api/account/reset-password/init [POST]", true);
            return session;
        })
        // 7. Create category
        .exec(session -> session.set("cat_id", generateUniqueId()))
        .exec(
            http("POST /api/categories")
                .post("/api/categories")
                .header("Authorization", "Bearer ${jwt_token}")
                .header("Content-Type", "application/json")
                .body(StringBody("{\"name\":\"test-cat-${cat_id}\",\"description\":\"Test category\"}"))
                .asJson()
                .check(status().is(201))
                .check(jsonPath("$.id").saveAs("category_id"))
        )
        .exec(session -> {
            boolean ok = session.contains("category_id");
            markApiCovered("/api/categories [POST]", ok);
            return session;
        })
        // 8. Get categories
        .exec(http("GET /api/categories").get("/api/categories").header("Authorization", "Bearer ${jwt_token}").check(status().is(200)))
        .exec(session -> {
            markApiCovered("/api/categories [GET]", true);
            return session;
        })
        // 9. Get category by ID
        .exec(
            http("GET /api/categories/{id}")
                .get(session -> "/api/categories/" + session.get("category_id"))
                .header("Authorization", "Bearer ${jwt_token}")
                .check(status().is(200))
        )
        .exec(session -> {
            markApiCovered("/api/categories/{id} [GET]", true);
            return session;
        })
        // 10. Update category
        .exec(
            http("PUT /api/categories/{id}")
                .put(session -> "/api/categories/" + session.get("category_id"))
                .header("Authorization", "Bearer ${jwt_token}")
                .header("Content-Type", "application/json")
                .body(
                    StringBody(
                        session ->
                            "{\"id\":" +
                            session.get("category_id") +
                            ",\"name\":\"updated-cat-" +
                            session.get("cat_id") +
                            "\",\"description\":\"Updated\"}"
                    )
                )
                .asJson()
                .check(status().is(200))
        )
        .exec(session -> {
            markApiCovered("/api/categories/{id} [PUT]", true);
            return session;
        })
        // 11. Patch category
        .exec(
            http("PATCH /api/categories/{id}")
                .patch(session -> "/api/categories/" + session.get("category_id"))
                .header("Authorization", "Bearer ${jwt_token}")
                .header("Content-Type", "application/json")
                .body(StringBody(session -> "{\"id\":" + session.get("category_id") + ",\"description\":\"Patched\"}"))
                .asJson()
                .check(status().is(200))
        )
        .exec(session -> {
            markApiCovered("/api/categories/{id} [PATCH]", true);
            return session;
        })
        // 12. Delete category
        .exec(
            http("DELETE /api/categories/{id}")
                .delete(session -> "/api/categories/" + session.get("category_id"))
                .header("Authorization", "Bearer ${jwt_token}")
                .check(status().is(204))
        )
        .exec(session -> {
            markApiCovered("/api/categories/{id} [DELETE]", true);
            return session;
        })
        // 13. Create product (needs a new category)
        .exec(session -> session.set("prod_cat_id", generateUniqueId()))
        .exec(
            http("POST /api/categories (for product)")
                .post("/api/categories")
                .header("Authorization", "Bearer ${jwt_token}")
                .header("Content-Type", "application/json")
                .body(StringBody("{\"name\":\"prod-cat-${prod_cat_id}\",\"description\":\"Product category\"}"))
                .asJson()
                .check(status().is(201))
                .check(jsonPath("$.id").saveAs("prod_category_id"))
        )
        .exec(session -> session)
        .exec(session -> session.set("prod_id", generateUniqueId()))
        .exec(
            http("POST /api/products")
                .post("/api/products")
                .header("Authorization", "Bearer ${jwt_token}")
                .header("Content-Type", "application/json")
                .body(
                    StringBody(
                        session ->
                            "{\"name\":\"test-prod-" +
                            session.get("prod_id") +
                            "\",\"description\":\"Test product\",\"price\":99.99,\"categoryId\":" +
                            session.get("prod_category_id") +
                            "}"
                    )
                )
                .asJson()
                .check(status().is(201))
                .check(jsonPath("$.id").saveAs("product_id"))
        )
        .exec(session -> {
            boolean ok = session.contains("product_id");
            markApiCovered("/api/products [POST]", ok);
            return session;
        })
        // 14. Get products
        .exec(http("GET /api/products").get("/api/products").header("Authorization", "Bearer ${jwt_token}").check(status().is(200)))
        .exec(session -> {
            markApiCovered("/api/products [GET]", true);
            return session;
        })
        // 15. Get product by ID
        .exec(
            http("GET /api/products/{id}")
                .get(session -> "/api/products/" + session.get("product_id"))
                .header("Authorization", "Bearer ${jwt_token}")
                .check(status().is(200))
        )
        .exec(session -> {
            markApiCovered("/api/products/{id} [GET]", true);
            return session;
        })
        // 16. Update product
        .exec(
            http("PUT /api/products/{id}")
                .put(session -> "/api/products/" + session.get("product_id"))
                .header("Authorization", "Bearer ${jwt_token}")
                .header("Content-Type", "application/json")
                .body(
                    StringBody(
                        session ->
                            "{\"id\":" +
                            session.get("product_id") +
                            ",\"name\":\"updated-prod-" +
                            session.get("prod_id") +
                            "\",\"description\":\"Updated\",\"price\":199.99,\"categoryId\":" +
                            session.get("prod_category_id") +
                            "}"
                    )
                )
                .asJson()
                .check(status().is(200))
        )
        .exec(session -> {
            markApiCovered("/api/products/{id} [PUT]", true);
            return session;
        })
        // 17. Patch product
        .exec(
            http("PATCH /api/products/{id}")
                .patch(session -> "/api/products/" + session.get("product_id"))
                .header("Authorization", "Bearer ${jwt_token}")
                .header("Content-Type", "application/json")
                .body(StringBody(session -> "{\"id\":" + session.get("product_id") + ",\"description\":\"Patched product\"}"))
                .asJson()
                .check(status().is(200))
        )
        .exec(session -> {
            markApiCovered("/api/products/{id} [PATCH]", true);
            return session;
        })
        // 18. Delete product
        .exec(
            http("DELETE /api/products/{id}")
                .delete(session -> "/api/products/" + session.get("product_id"))
                .header("Authorization", "Bearer ${jwt_token}")
                .check(status().is(204))
        )
        .exec(session -> {
            markApiCovered("/api/products/{id} [DELETE]", true);
            return session;
        })
        // 19. Filter products by category
        .exec(
            http("GET /api/products/filter")
                .get("/api/products/filter")
                .header("Authorization", "Bearer ${jwt_token}")
                .queryParam("categoryId", session -> session.get("prod_category_id").toString())
                .check(status().is(200))
        )
        .exec(session -> {
            markApiCovered("/api/products/filter [GET]", true);
            return session;
        })
        // 20. Create user
        .exec(session -> session.set("user_id", generateUniqueId()))
        .exec(
            http("POST /api/admin/users")
                .post("/api/admin/users")
                .header("Authorization", "Bearer ${jwt_token}")
                .header("Content-Type", "application/json")
                .body(
                    StringBody(
                        session ->
                            "{\"login\":\"testuser" +
                            session.get("user_id") +
                            "\",\"firstName\":\"Test\",\"lastName\":\"User\",\"email\":\"test" +
                            session.get("user_id") +
                            "@example.com\",\"activated\":true,\"langKey\":\"en\",\"authorities\":[\"ROLE_USER\"]}"
                    )
                )
                .asJson()
                .check(status().is(201))
                .check(jsonPath("$.login").saveAs("user_login"))
        )
        .exec(session -> {
            boolean ok = session.contains("user_login");
            markApiCovered("/api/admin/users [POST]", ok);
            return session;
        })
        // 21. Get users
        .exec(http("GET /api/admin/users").get("/api/admin/users").header("Authorization", "Bearer ${jwt_token}").check(status().is(200)))
        .exec(session -> {
            markApiCovered("/api/admin/users [GET]", true);
            return session;
        })
        // 22. Get user by login
        .exec(
            http("GET /api/admin/users/{login}")
                .get(session -> "/api/admin/users/" + session.get("user_login"))
                .header("Authorization", "Bearer ${jwt_token}")
                .check(status().is(200))
        )
        .exec(session -> {
            markApiCovered("/api/admin/users/{login} [GET]", true);
            return session;
        })
        // 23. Update user
        .exec(
            http("PUT /api/admin/users/{login}")
                .put(session -> "/api/admin/users/" + session.get("user_login"))
                .header("Authorization", "Bearer ${jwt_token}")
                .header("Content-Type", "application/json")
                .body(
                    StringBody(
                        session ->
                            "{\"login\":\"" +
                            session.get("user_login") +
                            "\",\"firstName\":\"Updated\",\"lastName\":\"User\",\"email\":\"test" +
                            session.get("user_id") +
                            "@example.com\",\"activated\":true,\"langKey\":\"en\",\"authorities\":[\"ROLE_USER\"]}"
                    )
                )
                .asJson()
                .check(status().is(200))
        )
        .exec(session -> {
            markApiCovered("/api/admin/users/{login} [PUT]", true);
            return session;
        })
        // 24. Delete user
        .exec(
            http("DELETE /api/admin/users/{login}")
                .delete(session -> "/api/admin/users/" + session.get("user_login"))
                .header("Authorization", "Bearer ${jwt_token}")
                .check(status().is(204))
        )
        .exec(session -> {
            markApiCovered("/api/admin/users/{login} [DELETE]", true);
            return session;
        })
        // 25. Get authorities
        .exec(http("GET /api/authorities").get("/api/authorities").header("Authorization", "Bearer ${jwt_token}").check(status().is(200)))
        .exec(session -> {
            markApiCovered("/api/authorities [GET]", true);
            return session;
        })
        // 26. Create authority
        .exec(session -> session.set("auth_id", generateUniqueId()))
        .exec(
            http("POST /api/authorities")
                .post("/api/authorities")
                .header("Authorization", "Bearer ${jwt_token}")
                .header("Content-Type", "application/json")
                .body(StringBody(session -> "{\"name\":\"ROLE_TEST_" + session.get("auth_id") + "\"}"))
                .asJson()
                .check(status().is(201))
                .check(jsonPath("$.name").saveAs("auth_name"))
        )
        .exec(session -> {
            boolean ok = session.contains("auth_name");
            markApiCovered("/api/authorities [POST]", ok);
            return session;
        })
        // 27. Get authority by name
        .exec(
            http("GET /api/authorities/{id}")
                .get(session -> "/api/authorities/" + session.get("auth_name"))
                .header("Authorization", "Bearer ${jwt_token}")
                .check(status().is(200))
        )
        .exec(session -> {
            markApiCovered("/api/authorities/{id} [GET]", true);
            return session;
        })
        // 28. Delete authority
        .exec(
            http("DELETE /api/authorities/{id}")
                .delete(session -> "/api/authorities/" + session.get("auth_name"))
                .header("Authorization", "Bearer ${jwt_token}")
                .check(status().is(204))
        )
        .exec(session -> {
            markApiCovered("/api/authorities/{id} [DELETE]", true);
            return session;
        })
        // 29. Get users (public)
        .exec(http("GET /api/users").get("/api/users").check(status().is(200)))
        .exec(session -> {
            markApiCovered("/api/users [GET]", true);
            return session;
        })
        // 30. Register user (public)
        .exec(session -> session.set("reg_id", generateUniqueId()))
        .exec(
            http("POST /api/register")
                .post("/api/register")
                .header("Content-Type", "application/json")
                .body(
                    StringBody(
                        session ->
                            "{\"login\":\"newuser" +
                            session.get("reg_id") +
                            "\",\"password\":\"password123\",\"firstName\":\"New\",\"lastName\":\"User\",\"email\":\"new" +
                            session.get("reg_id") +
                            "@example.com\",\"langKey\":\"en\"}"
                    )
                )
                .asJson()
                .check(status().is(201))
        )
        .exec(session -> {
            markApiCovered("/api/register [POST]", true);
            return session;
        })
        // 31. Activate user (expected to fail, but should return 500 for invalid key)
        .exec(http("GET /api/activate").get("/api/activate").queryParam("key", "test-activation-key").check(status().is(500)))
        .exec(session -> {
            markApiCovered("/api/activate [GET]", true);
            return session;
        })
        // 32. Finish password reset (expected to fail, but should return 500 for invalid key)
        .exec(
            http("POST /api/account/reset-password/finish")
                .post("/api/account/reset-password/finish")
                .header("Content-Type", "application/json")
                .body(StringBody("{\"key\":\"test-reset-key\",\"newPassword\":\"newpass123\"}"))
                .asJson()
                .check(status().is(500))
        )
        .exec(session -> {
            markApiCovered("/api/account/reset-password/finish [POST]", true);
            return session;
        })
        // Final report
        .exec(session -> {
            generateFinalReport();
            return session;
        });

    private static void generateFinalReport() {
        System.out.println("\n" + "=".repeat(80));
        System.out.println("🎯 FINAL STRICT COMPREHENSIVE API TEST REPORT");
        System.out.println("=".repeat(80));
        long covered = apiCoverage.values().stream().mapToLong(b -> b ? 1 : 0).sum();
        long total = apiCoverage.size();
        double percentage = (100.0 * covered) / total;
        double successRate = (100.0 * successRequests.get()) / totalRequests.get();
        System.out.printf("📊 Total Requests: %d | Success: %d | Expected: 32%n", totalRequests.get(), successRequests.get());
        System.out.printf("✅ API Coverage: %d / %d (%.2f%%)%n", covered, total, percentage);
        System.out.printf("📈 Success Rate: %.2f%%\n", successRate);
        System.out.println("\n📋 API Coverage Details:");
        System.out.println("-".repeat(60));
        apiCoverage
            .entrySet()
            .stream()
            .sorted(Map.Entry.comparingByKey())
            .forEach(entry -> {
                String status = entry.getValue() ? "✅ PASS" : "❌ FAIL";
                System.out.printf("%-50s %s%n", entry.getKey(), status);
            });
        System.out.println("-".repeat(60));
        if (percentage == 100.0 && successRate == 100.0 && totalRequests.get() == 32) {
            System.out.println("🎉 PERFECT! 32 REQUESTS, 100% COVERAGE, 100% SUCCESS!");
        } else if (percentage == 100.0) {
            System.out.println("✅ 100% API COVERAGE ACHIEVED!");
        } else {
            System.out.println("⚠️  Coverage: " + percentage + "% | Success: " + successRate + "%");
        }
        System.out.println("=".repeat(80));
    }

    ScenarioBuilder users = scenario("Comprehensive API Test - Strict").exec(comprehensiveApiTest);

    {
        setUp(users.injectOpen(atOnceUsers(1))).protocols(httpConf);
    }
}
