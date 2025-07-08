# 🚀 Performance Testing Guide

This guide covers the comprehensive API performance testing setup for the JHipster application using Gatling.

## 📋 Overview

The `ComprehensiveApiPerformanceTest` provides:

- ✅ **Complete API Coverage**: Tests all discovered endpoints (Categories, Products, Users, Authentication)
- ✅ **JWT Authentication**: Proper token handling with debugging capabilities
- ✅ **Coverage Reporting**: Shows which endpoints were tested vs discovered
- ✅ **Performance Assertions**: Global and endpoint-specific performance checks
- ✅ **Realistic Data**: Uses timestamps and counters for unique test data
- ✅ **Modular Design**: Reusable methods for different entity operations
- ✅ **Environment Configuration**: Configurable via system properties

## 🎯 Tested Endpoints

### Authentication

- `POST /api/authenticate` - JWT token acquisition
- `GET /api/account` - Authentication verification

### Categories (Full CRUD)

- `GET /api/categories` - List all categories
- `POST /api/categories` - Create category
- `GET /api/categories/{id}` - Get category by ID
- `PUT /api/categories/{id}` - Update category
- `PATCH /api/categories/{id}` - Partial update category
- `DELETE /api/categories/{id}` - Delete category

### Products (Full CRUD + Filtering)

- `GET /api/products` - List all products
- `POST /api/products` - Create product
- `GET /api/products/{id}` - Get product by ID
- `PUT /api/products/{id}` - Update product
- `PATCH /api/products/{id}` - Partial update product
- `DELETE /api/products/{id}` - Delete product
- `GET /api/products/filter` - Filter products by criteria

### Administration

- `GET /api/authorities` - List authorities
- `GET /api/admin/users` - List all users (admin only)
- `GET /api/users/public` - List public user information

## 🚀 Quick Start

### Prerequisites

1. **Application Running**: Ensure your JHipster application is running on the target URL
2. **Admin User**: Default admin credentials (`admin/admin`) must be available
3. **Maven**: Maven must be installed for running Gatling tests

### Simple Execution

```bash
# Windows
run-performance-test.cmd

# Linux/macOS
chmod +x run-performance-test.sh
./run-performance-test.sh
```

### Custom Configuration

```bash
# Windows
run-performance-test.cmd --users 25 --ramp 30 --url https://myapp.com

# Linux/macOS
./run-performance-test.sh --users 50 --ramp 60 --url http://localhost:8080
```

### Direct Maven Execution

```bash
mvn gatling:test \
    -Dgatling.simulationClass=gatling.simulations.ComprehensiveApiPerformanceTest \
    -Dusers=10 \
    -DrampDuration=10 \
    -DbaseUrl=http://localhost:8080
```

## ⚙️ Configuration Options

| Parameter      | Default               | Description                        |
| -------------- | --------------------- | ---------------------------------- |
| `users`        | 10                    | Number of concurrent virtual users |
| `rampDuration` | 10                    | Ramp-up duration in seconds        |
| `baseUrl`      | http://localhost:8080 | Application base URL               |

## 📊 Performance Assertions

### Global Assertions

- **Success Rate**: ≥ 95% of requests must succeed
- **Mean Response Time**: ≤ 1000ms average response time
- **95th Percentile**: ≤ 2000ms for 95% of requests

### Endpoint-Specific Assertions

- **Authentication**: ≤ 2000ms mean response time
- **Create Operations**: ≤ 1000ms mean response time
- **Read Operations**: ≤ 1000ms mean response time

## 📈 Coverage Reporting

After each test run, a detailed coverage report is printed:

```
================================================================================
API ENDPOINT COVERAGE REPORT
================================================================================
Endpoints Covered: 18/22 (81.8%)

Missing Endpoints:
  - POST /api/admin/users
  - GET /api/admin/users/{login}
  - PUT /api/admin/users
  - DELETE /api/admin/users/{login}

Tested Endpoints:
  ✓ DELETE /api/categories/{id}
  ✓ DELETE /api/products/{id}
  ✓ GET /api/account
  ✓ GET /api/authorities
  ...
================================================================================
```

## 🛠️ Test Data Generation

### Categories

- **Name**: `Category_{counter}_{timestamp}`
- **Description**: `Test category created at {timestamp}`

### Products

- **Name**: `Product_{counter}_{timestamp}`
- **Price**: Incremental values starting from 99.99
- **Rating**: Rotating values between 3.0-5.0
- **Image URL**: Generated example URLs
- **Category**: Links to previously created category

### Timestamps

- **Format**: ISO-8601 UTC format using `ZonedDateTime.now(ZoneOffset.UTC)`
- **Usage**: Ensures unique data across test runs

## 🔧 Debugging Features

### JWT Token Debugging

The test includes token extraction debugging:

```
=== Starting authentication for user session ===
JWT Token extracted: eyJhbGciOiJIUzUxMiJ9.eyJzdWIiOiJhZG1pbiIsImF1dGgiOiJST0xFX0...
```

### Entity Creation Tracking

```
Created category with ID: 1234
Created product with ID: 5678
Deleted product with ID: 5678
Deleted category with ID: 1234
```

## 🎯 Test Workflow

1. **Authentication** - Acquire JWT token using admin credentials
2. **Account Verification** - Verify token works correctly
3. **Category Operations** - Full CRUD cycle for categories
4. **Product Operations** - Full CRUD cycle for products (dependent on categories)
5. **Admin Operations** - Test administrative endpoints
6. **Cleanup** - Delete created test data
7. **Coverage Report** - Display endpoint coverage summary

## 📁 Output Files

Results are stored in `target/gatling/`:

- **HTML Reports**: Interactive Gatling reports with charts and metrics
- **Simulation Logs**: Raw performance data for analysis
- **Console Output**: Real-time test progress and coverage report

## 🔄 CI/CD Integration

### Jenkins Pipeline Example

```groovy
stage('Performance Test') {
    steps {
        sh './run-performance-test.sh --users 20 --ramp 30'
        publishHTML([
            allowMissing: false,
            alwaysLinkToLastBuild: true,
            keepAll: true,
            reportDir: 'target/gatling',
            reportFiles: 'index.html',
            reportName: 'Gatling Performance Report'
        ])
    }
}
```

### GitHub Actions Example

```yaml
- name: Run Performance Tests
  run: ./run-performance-test.sh --users 15 --ramp 20

- name: Upload Gatling Reports
  uses: actions/upload-artifact@v3
  with:
    name: gatling-reports
    path: target/gatling/
```

## 🐛 Troubleshooting

### Common Issues

1. **Authentication Failures**

   - Ensure admin user exists with credentials `admin/admin`
   - Check if JWT is enabled in application configuration

2. **Connection Errors**

   - Verify application is running on specified base URL
   - Check firewall and network connectivity

3. **Test Data Conflicts**

   - Category names are unique - conflicts may occur if previous test data exists
   - Consider database cleanup between test runs

4. **Performance Assertion Failures**
   - Adjust assertion thresholds based on your infrastructure
   - Consider system resources and load during testing

### Debug Mode

Enable verbose logging by adding to Maven command:

```bash
mvn gatling:test -Dlogback.configurationFile=logback-test.xml -Dgatling.simulationClass=...
```

## 📞 Support

For issues or questions:

1. Check the Gatling console output for specific error messages
2. Review the generated HTML reports in `target/gatling/`
3. Verify all prerequisites are met
4. Check application logs for server-side errors

---

**Happy Performance Testing! 🚀**
