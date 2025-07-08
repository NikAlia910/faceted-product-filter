# PERFORMANCE TESTING

This project includes a comprehensive Gatling performance test for the API, covering authentication, CRUD operations, and admin endpoints.

## How to Run

### Windows

```
./run-performance-test.cmd [USERS] [RAMP_DURATION_SECONDS]
```

### Linux/macOS

```
./run-performance-test.sh [USERS] [RAMP_DURATION_SECONDS]
```

### Directly with Maven

```
mvn gatling:test -Dusers=10 -DrampDuration=10
```

- **USERS**: Number of concurrent users (default: 1)
- **RAMP_DURATION_SECONDS**: Ramp-up duration in seconds (default: 1)

## What It Tests

- JWT authentication and token extraction
- Full CRUD for Categories and Products
- Admin and public user endpoints
- Performance assertions (success rate, response times)
- API coverage reporting

## Output

- HTML report: `target/gatling/<run-folder>/index.html`
- Console summary with coverage and performance

## Troubleshooting

- Ensure the backend is running at the configured base URL (default: http://localhost:8080)
- For custom base URL: `-DbaseUrl=http://your-server:port`
