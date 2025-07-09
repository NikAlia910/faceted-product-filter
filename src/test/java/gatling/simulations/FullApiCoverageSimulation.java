package gatling.simulations;

import static io.gatling.javaapi.core.CoreDsl.*;
import static io.gatling.javaapi.http.HttpDsl.*;

import io.gatling.javaapi.core.*;
import io.gatling.javaapi.http.*;
import java.time.Duration;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.*;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.Collectors;

public class FullApiCoverageSimulation extends Simulation {

    // Base URL
    String baseURL = Optional.ofNullable(System.getProperty("baseURL")).orElse("http://localhost:8080");

    // HTTP Protocol
    HttpProtocolBuilder httpConf = http
        .baseUrl(baseURL)
        .inferHtmlResources()
        .acceptHeader("application/json")
        .contentTypeHeader("application/json")
        .userAgentHeader("Gatling/FullApiCoverageSimulation")
        .silentResources();

    // API Coverage Map
    static Map<String, Boolean> apiCoverage = new ConcurrentHashMap<>();
    static List<Map<String, Object>> apiReport = Collections.synchronizedList(new ArrayList<>());

    // Helper: Generate unique name
    private static String uniqueName(String prefix) {
        return prefix + "-" + UUID.randomUUID();
    }

    // Helper: Current UTC timestamp
    private static String nowUtc() {
        return ZonedDateTime.now(ZoneOffset.UTC).toString();
    }

    // Helper: Print debug
    private static void debug(String label, Object value) {
        System.out.println("[DEBUG] " + label + ": " + value);
    }

    // Helper to add Authorization header to requests after authentication
    private static HttpRequestActionBuilder withAuth(HttpRequestActionBuilder req) {
        return req.header("Authorization", "Bearer ${jwt_token}");
    }

    // --- AUTH ---
    ChainBuilder authenticate = exec(
        http("POST /api/authenticate")
            .post("/api/authenticate")
            .body(StringBody("{" + "\"username\":\"admin\"," + "\"password\":\"admin\"," + "\"rememberMe\":false}"))
            .check(status().is(200))
            .check(jsonPath("$.id_token").saveAs("jwt_token"))
            .check(responseTimeInMillis().saveAs("responseTime"))
            .check(bodyString().saveAs("auth_response"))
    ).exec(session -> {
        debug("JWT Token", session.getString("jwt_token"));
        debug("Auth Response Time", session.getLong("responseTime"));
        apiCoverage.put("/api/authenticate [POST]", true);
        apiReport.add(
            Map.of(
                "Endpoint",
                "/api/authenticate",
                "Method",
                "POST",
                "Status",
                "200",
                "ResponseTime",
                String.valueOf(session.getLong("responseTime")),
                "Success",
                "true",
                "Covered",
                "true"
            )
        );
        return session;
    });

    // --- CATEGORY ---
    ChainBuilder createCategory = exec(session -> {
        String uuid = UUID.randomUUID().toString();
        return session.set("categoryName", uuid);
    })
        .exec(
            withAuth(
                http("POST /api/categories")
                    .post("/api/categories")
                    .body(
                        StringBody(session -> "{\"name\":\"" + session.getString("categoryName") + "\",\"description\":\"Gatling Test\"}")
                    )
                    .check(status().is(201))
                    .check(jsonPath("$.id").saveAs("categoryId"))
                    .check(responseTimeInMillis().saveAs("responseTime"))
            )
        )
        .exec(session -> {
            String id = session.contains("categoryId") ? session.getString("categoryId") : null;
            debug("Category Created", id);
            debug("Category Create Response Time", session.getLong("responseTime"));
            apiCoverage.put("/api/categories [POST]", id != null);
            apiReport.add(
                Map.of(
                    "Endpoint",
                    "/api/categories",
                    "Method",
                    "POST",
                    "Status",
                    id != null ? "201" : "401",
                    "ResponseTime",
                    String.valueOf(session.getLong("responseTime")),
                    "Success",
                    String.valueOf(id != null),
                    "Covered",
                    String.valueOf(id != null)
                )
            );
            return session;
        });

    ChainBuilder getCategory = exec(
        http("GET /api/categories/{id}")
            .get(session -> "/api/categories/" + session.getString("category_id"))
            .header("Authorization", "Bearer ${jwt_token}")
            .check(status().is(200))
            .check(responseTimeInMillis().saveAs("responseTime"))
            .check(bodyString().saveAs("category_get_response"))
    ).exec(session -> {
        apiCoverage.put("/api/categories/{id} [GET]", true);
        apiReport.add(
            Map.of(
                "endpoint",
                "/api/categories/{id}",
                "method",
                "GET",
                "status",
                200,
                "responseTime",
                session.getLong("responseTime"),
                "success",
                true,
                "covered",
                true
            )
        );
        return session;
    });

    ChainBuilder deleteCategory = exec(
        http("DELETE /api/categories/{id}")
            .delete(session -> "/api/categories/" + session.getString("category_id"))
            .header("Authorization", "Bearer ${jwt_token}")
            .check(status().is(204))
            .check(responseTimeInMillis().saveAs("responseTime"))
    ).exec(session -> {
        apiCoverage.put("/api/categories/{id} [DELETE]", true);
        apiReport.add(
            Map.of(
                "endpoint",
                "/api/categories/{id}",
                "method",
                "DELETE",
                "status",
                204,
                "responseTime",
                session.getLong("responseTime"),
                "success",
                true,
                "covered",
                true
            )
        );
        return session;
    });

    // --- PRODUCT ---
    ChainBuilder createProduct = exec(session -> {
        String name = uniqueName("prod");
        session.set("product_name", name);
        session.set("product_desc", "desc-" + name);
        session.set("product_price", "99.99");
        session.set("product_image", "http://img/" + name);
        session.set("product_rating", "4.5");
        return session;
    })
        .exec(
            http("POST /api/products")
                .post("/api/products")
                .header("Authorization", "Bearer ${jwt_token}")
                .body(
                    StringBody(session ->
                        String.format(
                            "{\"name\":\"%s\",\"description\":\"%s\",\"price\":%s,\"imageUrl\":\"%s\",\"rating\":%s,\"category\":{\"id\":%s}}",
                            session.getString("product_name"),
                            session.getString("product_desc"),
                            session.getString("product_price"),
                            session.getString("product_image"),
                            session.getString("product_rating"),
                            session.getString("category_id")
                        )
                    )
                )
                .check(status().is(201))
                .check(headerRegex("Location", "/api/products/(\\d+)").saveAs("product_id"))
                .check(responseTimeInMillis().saveAs("responseTime"))
                .check(bodyString().saveAs("product_response"))
        )
        .exec(session -> {
            debug("Product Created", session.getString("product_id"));
            apiCoverage.put("/api/products [POST]", true);
            apiReport.add(
                Map.of(
                    "endpoint",
                    "/api/products",
                    "method",
                    "POST",
                    "status",
                    201,
                    "responseTime",
                    session.getLong("responseTime"),
                    "success",
                    true,
                    "covered",
                    true
                )
            );
            return session;
        });

    ChainBuilder getProduct = exec(
        http("GET /api/products/{id}")
            .get(session -> "/api/products/" + session.getString("product_id"))
            .header("Authorization", "Bearer ${jwt_token}")
            .check(status().is(200))
            .check(responseTimeInMillis().saveAs("responseTime"))
            .check(bodyString().saveAs("product_get_response"))
    ).exec(session -> {
        apiCoverage.put("/api/products/{id} [GET]", true);
        apiReport.add(
            Map.of(
                "endpoint",
                "/api/products/{id}",
                "method",
                "GET",
                "status",
                200,
                "responseTime",
                session.getLong("responseTime"),
                "success",
                true,
                "covered",
                true
            )
        );
        return session;
    });

    ChainBuilder deleteProduct = exec(
        http("DELETE /api/products/{id}")
            .delete(session -> "/api/products/" + session.getString("product_id"))
            .header("Authorization", "Bearer ${jwt_token}")
            .check(status().is(204))
            .check(responseTimeInMillis().saveAs("responseTime"))
    ).exec(session -> {
        apiCoverage.put("/api/products/{id} [DELETE]", true);
        apiReport.add(
            Map.of(
                "endpoint",
                "/api/products/{id}",
                "method",
                "DELETE",
                "status",
                204,
                "responseTime",
                session.getLong("responseTime"),
                "success",
                true,
                "covered",
                true
            )
        );
        return session;
    });

    // --- USER (ADMIN) ---
    ChainBuilder getUsers = exec(
        http("GET /api/admin/users")
            .get("/api/admin/users")
            .header("Authorization", "Bearer ${jwt_token}")
            .check(status().is(200))
            .check(responseTimeInMillis().saveAs("responseTime"))
            .check(jsonPath("$[0].id").optional().saveAs("admin_user_id"))
    ).exec(session -> {
        apiCoverage.put("/api/admin/users [GET]", true);
        apiReport.add(
            Map.of(
                "endpoint",
                "/api/admin/users",
                "method",
                "GET",
                "status",
                200,
                "responseTime",
                session.getLong("responseTime"),
                "success",
                true,
                "covered",
                true
            )
        );
        return session;
    });

    // --- AUTHORITY (ADMIN) ---
    ChainBuilder getAuthorities = exec(
        http("GET /api/authorities")
            .get("/api/authorities")
            .header("Authorization", "Bearer ${jwt_token}")
            .check(status().is(200))
            .check(responseTimeInMillis().saveAs("responseTime"))
    ).exec(session -> {
        apiCoverage.put("/api/authorities [GET]", true);
        apiReport.add(
            Map.of(
                "endpoint",
                "/api/authorities",
                "method",
                "GET",
                "status",
                200,
                "responseTime",
                session.getLong("responseTime"),
                "success",
                true,
                "covered",
                true
            )
        );
        return session;
    });

    // --- ACCOUNT ---
    ChainBuilder getAccount = exec(
        http("GET /api/account")
            .get("/api/account")
            .header("Authorization", "Bearer ${jwt_token}")
            .check(status().is(200))
            .check(responseTimeInMillis().saveAs("responseTime"))
    ).exec(session -> {
        apiCoverage.put("/api/account [GET]", true);
        apiReport.add(
            Map.of(
                "endpoint",
                "/api/account",
                "method",
                "GET",
                "status",
                200,
                "responseTime",
                session.getLong("responseTime"),
                "success",
                true,
                "covered",
                true
            )
        );
        return session;
    });

    // --- PUBLIC USERS ---
    ChainBuilder getPublicUsers = exec(
        http("GET /api/users").get("/api/users").check(status().is(200)).check(responseTimeInMillis().saveAs("responseTime"))
    ).exec(session -> {
        apiCoverage.put("/api/users [GET]", true);
        apiReport.add(
            Map.of(
                "endpoint",
                "/api/users",
                "method",
                "GET",
                "status",
                200,
                "responseTime",
                session.getLong("responseTime"),
                "success",
                true,
                "covered",
                true
            )
        );
        return session;
    });

    // --- MAIN SCENARIO ---
    ScenarioBuilder scn = scenario("Full API Coverage Scenario")
        .exec(authenticate)
        .exec(createCategory)
        .exec(getCategory)
        .exec(createProduct)
        .exec(getProduct)
        .exec(getUsers)
        .exec(getAuthorities)
        .exec(getAccount)
        .exec(getPublicUsers)
        .exec(deleteProduct)
        .exec(deleteCategory);

    {
        setUp(scn.injectOpen(rampUsers(10).during(Duration.ofSeconds(10))))
            .protocols(httpConf)
            .assertions(global().responseTime().max().lt(1000), global().successfulRequests().percent().is(100.0));
    }

    // --- REPORTING ---
    @Override
    public void after() {
        long covered = apiCoverage.values().stream().filter(Boolean::booleanValue).count();
        long total = apiCoverage.size();
        System.out.printf("\n✅ API Coverage: %d / %d (%.2f%%)%n", covered, total, ((100.0 * covered) / total));
        System.out.println("\nEndpoint,Method,Status,ResponseTime,Success,Covered");
        for (Map<String, Object> row : apiReport) {
            System.out.printf(
                "%s,%s,%s,%s,%s,%s\n",
                row.get("endpoint"),
                row.get("method"),
                row.get("status"),
                row.get("responseTime"),
                row.get("success"),
                row.get("covered")
            );
        }
    }
}
