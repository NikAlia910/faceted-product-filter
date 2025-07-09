# Comprehensive API Gatling Simulation

This document explains how to run the comprehensive Gatling simulation that tests **100% of your API endpoints** with proper authentication, coverage tracking, and detailed reporting.

## Features

✅ **100% API Coverage** - Tests all available endpoints  
✅ **JWT Authentication** - Properly handles Bearer token authentication  
✅ **CRUD Operations** - Complete Create, Read, Update, Delete testing  
✅ **Unique Data Generation** - Uses UUIDs to avoid conflicts  
✅ **Performance Assertions** - Response time < 1000ms  
✅ **Detailed Reporting** - Coverage breakdown by category  
✅ **Debug Output** - Prints tokens, responses, and progress  
✅ **Load Testing** - Realistic user simulation pattern

## Tested Endpoints

### Authentication & Account Management (9 endpoints)

- `POST /api/authenticate` - Login with JWT token extraction
- `GET /api/authenticate` - Check authentication status
- `GET /api/account` - Get current user account info
- `POST /api/account` - Update account information
- `POST /api/register` - Register new account
- `GET /api/activate` - Activate account
- `POST /api/account/change-password` - Change password
- `POST /api/account/reset-password/init` - Initialize password reset
- `POST /api/account/reset-password/finish` - Complete password reset

### Category Management (6 endpoints)

- `GET /api/categories` - Get all categories
- `POST /api/categories` - Create new category
- `GET /api/categories/{id}` - Get specific category
- `PUT /api/categories/{id}` - Update category
- `PATCH /api/categories/{id}` - Partial update category
- `DELETE /api/categories/{id}` - Delete category

### Product Management (7 endpoints)

- `GET /api/products` - Get all products
- `POST /api/products` - Create new product
- `GET /api/products/{id}` - Get specific product
- `PUT /api/products/{id}` - Update product
- `PATCH /api/products/{id}` - Partial update product
- `DELETE /api/products/{id}` - Delete product
- `GET /api/products/filter` - Get filtered products

### User Management (5 endpoints)

- `GET /api/admin/users` - Get all users (admin)
- `POST /api/admin/users` - Create new user (admin)
- `GET /api/admin/users/{login}` - Get specific user (admin)
- `PUT /api/admin/users/{login}` - Update user (admin)
- `DELETE /api/admin/users/{login}` - Delete user (admin)

### Authority Management (4 endpoints)

- `GET /api/authorities` - Get all authorities
- `POST /api/authorities` - Create new authority
- `GET /api/authorities/{id}` - Get specific authority
- `DELETE /api/authorities/{id}` - Delete authority

### Public APIs (1 endpoint)

- `GET /api/users` - Get all public users

## How to Run

### Prerequisites

1. Make sure your application is running on `localhost:8080`
2. Ensure the default admin user exists (`admin`/`admin`)
3. Maven and Java 11+ installed

### Run the Simulation

```bash
# Run from project root
mvn clean gatling:test -Dgatling.simulationClass=gatling.simulations.ComprehensiveApiGatlingTest

# Or with custom base URL
mvn clean gatling:test -Dgatling.simulationClass=gatling.simulations.ComprehensiveApiGatlingTest -DbaseURL=http://localhost:8080

# Run with custom user count and ramp duration
mvn clean gatling:test -Dgatling.simulationClass=gatling.simulations.ComprehensiveApiGatlingTest -Dusers=20 -Dramp=20
```

### Sample Output

The simulation will provide detailed output including:

```
🔐 JWT Token extracted: eyJhbGciOiJIUzI1NiIs...
✅ API Covered: /api/authenticate [POST]
✅ API Covered: /api/authenticate [GET]
👤 Account Info: {"id":1,"login":"admin",...}
✅ API Covered: /api/account [GET]
📂 Category created at: /api/categories/1
✅ API Covered: /api/categories [POST]
📦 Product created at: /api/products/1
✅ API Covered: /api/products [POST]
...

================================================================================
🎯 COMPREHENSIVE API COVERAGE REPORT
================================================================================
✅ API Coverage: 32 / 32 (100.00%)

📊 DETAILED COVERAGE BREAKDOWN:
--------------------------------------------------------------------------------
Endpoint                                          Method     Status
--------------------------------------------------------------------------------
/api/account                                      GET        ✅ PASS
/api/account                                      POST       ✅ PASS
/api/account/change-password                      POST       ✅ PASS
/api/account/reset-password/finish               POST       ✅ PASS
/api/account/reset-password/init                 POST       ✅ PASS
/api/activate                                     GET        ✅ PASS
/api/admin/users                                  GET        ✅ PASS
/api/admin/users                                  POST       ✅ PASS
/api/admin/users/{login}                          DELETE     ✅ PASS
/api/admin/users/{login}                          GET        ✅ PASS
/api/admin/users/{login}                          PUT        ✅ PASS
/api/authenticate                                 GET        ✅ PASS
/api/authenticate                                 POST       ✅ PASS
/api/authorities                                  GET        ✅ PASS
/api/authorities                                  POST       ✅ PASS
/api/authorities/{id}                             DELETE     ✅ PASS
/api/authorities/{id}                             GET        ✅ PASS
/api/categories                                   GET        ✅ PASS
/api/categories                                   POST       ✅ PASS
/api/categories/{id}                              DELETE     ✅ PASS
/api/categories/{id}                              GET        ✅ PASS
/api/categories/{id}                              PATCH      ✅ PASS
/api/categories/{id}                              PUT        ✅ PASS
/api/products                                     GET        ✅ PASS
/api/products                                     POST       ✅ PASS
/api/products/filter                              GET        ✅ PASS
/api/products/{id}                                DELETE     ✅ PASS
/api/products/{id}                                GET        ✅ PASS
/api/products/{id}                                PATCH      ✅ PASS
/api/products/{id}                                PUT        ✅ PASS
/api/register                                     POST       ✅ PASS
/api/users                                        GET        ✅ PASS
--------------------------------------------------------------------------------

📈 COVERAGE BY CATEGORY:
----------------------------------------
Authentication & Account: 9/9 (100.0%)
Categories: 6/6 (100.0%)
Products: 7/7 (100.0%)
Users: 6/6 (100.0%)
Authorities: 4/4 (100.0%)
================================================================================
🎉 CONGRATULATIONS! 100% API COVERAGE ACHIEVED!
================================================================================
```

## Configuration

### Load Testing Parameters

- **Users**: 10 concurrent users (configurable with `-Dusers=N`)
- **Ramp Duration**: 10 seconds (configurable with `-Dramp=N`)
- **Response Time SLA**: < 1000ms

### Authentication

- **Username**: `admin`
- **Password**: `admin`
- **JWT Token**: Automatically extracted and reused

### Data Generation

- **Unique IDs**: Uses UUID.randomUUID() for uniqueness
- **Timestamps**: Uses ZonedDateTime.now(ZoneOffset.UTC)
- **Cleanup**: Automatically cleans up created test data

## Troubleshooting

### Common Issues

1. **401 Unauthorized**: Check if admin user exists and password is correct
2. **404 Not Found**: Ensure application is running on correct port
3. **500 Internal Server Error**: Check application logs for database issues
4. **Timeout**: Increase response time threshold or check system performance

### Debug Mode

The simulation includes extensive debug output:

- JWT tokens (first 20 chars)
- API responses
- Created entity URLs
- Coverage progress

### Customization

To modify the simulation:

1. **Add new endpoints**: Update the `apiCoverage` map in the static block
2. **Change payloads**: Modify the JSON bodies in the ChainBuilder methods
3. **Adjust assertions**: Update the `.check()` statements
4. **Modify load pattern**: Change the `rampUsers()` and `during()` parameters

## Performance Metrics

The simulation tracks:

- **Response times** (< 1000ms assertion)
- **Success rates** (2xx status codes)
- **API coverage percentage**
- **Throughput** (requests per second)
- **Error rates** by endpoint

## Integration

This simulation can be integrated into:

- **CI/CD pipelines** for automated API testing
- **Performance monitoring** for regular health checks
- **Regression testing** after code changes
- **Load testing** for capacity planning

## File Location

The simulation is located at:

```
src/test/java/gatling/simulations/ComprehensiveApiGatlingTest.java
```

## Requirements Met

✅ JWT token extraction with `.check(jsonPath("$.id_token").saveAs("jwt_token"))`  
✅ Bearer token authentication with `.header("Authorization", "Bearer ${jwt_token}")`  
✅ UUID generation for unique names  
✅ 2xx status code assertions  
✅ Response time < 1000ms assertions  
✅ Debug output with `System.out.println()`  
✅ Realistic load pattern with `rampUsers(10).during(Duration.ofSeconds(10))`  
✅ UTC timestamps with `ZonedDateTime.now(ZoneOffset.UTC)`  
✅ 100% API endpoint coverage  
✅ Coverage tracking with `Map<String, Boolean> apiCoverage`  
✅ Detailed reporting with success/fail status  
✅ Proper payload validation  
✅ Clean code organization with reusable chain builders
