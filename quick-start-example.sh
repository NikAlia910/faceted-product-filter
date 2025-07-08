#!/bin/bash

echo "🎯 Quick Start Example for Gatling Performance Testing"
echo "======================================================="
echo ""
echo "This script demonstrates how to run the comprehensive API performance test"
echo "with different configurations."
echo ""

echo "1️⃣  Basic test with default settings (10 users, 10 second ramp-up):"
echo "   ./run-performance-test.sh"
echo ""

echo "2️⃣  Test with custom user load:"
echo "   ./run-performance-test.sh --users 25 --ramp 30"
echo ""

echo "3️⃣  Test against different environment:"
echo "   ./run-performance-test.sh --url https://staging.myapp.com --users 15"
echo ""

echo "4️⃣  High-load stress test:"
echo "   ./run-performance-test.sh --users 100 --ramp 60"
echo ""

echo "5️⃣  Direct Maven execution:"
echo "   mvn gatling:test \\"
echo "     -Dgatling.simulationClass=gatling.simulations.ComprehensiveApiPerformanceTest \\"
echo "     -Dusers=20 \\"
echo "     -DrampDuration=15 \\"
echo "     -DbaseUrl=http://localhost:8080"
echo ""

echo "📊 After running any test, check the results in:"
echo "   - target/gatling/ (HTML reports)"
echo "   - Console output (coverage report)"
echo ""

echo "🔍 For help with any script:"
echo "   ./run-performance-test.sh --help"
echo ""

echo "Prerequisites:"
echo "✅ JHipster application running"
echo "✅ Admin user (admin/admin) available"
echo "✅ Maven installed"
echo ""

read -p "Would you like to run a basic performance test now? (y/N): " -n 1 -r
echo ""
if [[ $REPLY =~ ^[Yy]$ ]]; then
    echo "🚀 Starting basic performance test..."
    ./run-performance-test.sh
else
    echo "ℹ️  You can run tests manually using the commands above."
fi 