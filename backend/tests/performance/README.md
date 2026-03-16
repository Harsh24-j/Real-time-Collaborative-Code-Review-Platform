# JMeter Performance Testing - Complete Guide

This directory contains complete JMeter test plans for performance testing the Real-time Collaborative Code Review Platform.

## Section 1: JMeter Installation & Setup

### Installation
- **macOS (Homebrew)**: `brew install jmeter`
- **Linux**: `sudo apt-get install jmeter`
- **Windows**: Download from [JMeter](https://jmeter.apache.org/download_jmeter.cgi).

### CLI Execution
```bash
# Basic run
jmeter -n -t load_test.jmx -l results.jtl

# With HTML report
jmeter -n -t load_test.jmx -l results.jtl -e -o htmlreport/
```

## Section 2: Load Testing Plan
- **File**: `load_test.jmx`
- **Goal**: Simulate 100 concurrent users.

## Section 3: Stress Testing Plan
- **File**: `stress_test.jmx`
- **Goal**: Gradually increase to 500 users to find system limits.

## Section 4: WebSocket Testing
- **File**: `websocket_test.jmx`
- **Goal**: Test real-time collaborative broadcasting (needs WebSocket Samplers Plugin).

## Section 5: Running Tests
1. **GUI**: Run `bin/jmeter.bat` and open any `.jmx` file.
2. **CLI**: Use the commands in Section 1.

## Section 6: Performance Targets
- **Response Time (95th %ile)**: < 500ms
- **Throughput**: > 50 requests/second
- **Error Rate**: < 1%

## Section 7: Analyzing Results
Check the `results/` folder for `.jtl` files and the `htmlreport/` for visual dashboards.

## Section 8: Optimization Recommendations
- Add indices on foreign keys.
- Use Redis for session caching.
- Tune JVM heap size.
