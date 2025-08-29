# DataMesh Manager Connector for Hive

This connector allows DataMesh Manager to synchronize assets from Hive-compatible systems via JDBC. It supports Apache Hive, Apache Impala, and other Hive-compatible systems.

## Features

- Connects to Hive-compatible systems via JDBC
- Discovers databases, tables, and columns
- Converts database objects to DataMesh Manager assets
- Supports detailed table information parsing
- Creates hierarchical asset relationships (database → schema → table)
- Configurable asset ID prefixes and ownership

## Supported Systems

- Apache Hive
- Apache Impala
- Cloudera Hive/Impala (via compatible JDBC drivers)
- Any Hive-compatible system supporting standard SQL commands

## Requirements

- Java 17+
- Maven 3.6+
- Access to a Hive-compatible system
- DataMesh Manager API access

## Configuration

Configure the connector in `application.properties`:

```properties
# DataMesh Manager API configuration
datameshmanager.client.host=https://api.datamesh-manager.com
datameshmanager.client.apikey=your-api-key

# Hive connection settings
datameshmanager.client.hive.connection.host=localhost
datameshmanager.client.hive.connection.port=10000
datameshmanager.client.hive.connection.database=default
datameshmanager.client.hive.connection.username=your-username
datameshmanager.client.hive.connection.password=your-password
datameshmanager.client.hive.connection.driver-class-name=org.apache.hive.jdbc.HiveDriver
datameshmanager.client.hive.connection.jdbc-url=jdbc:hive2://localhost:10000/default

# Asset synchronization settings
datameshmanager.client.hive.assets.connectorid=hive-assets
datameshmanager.client.hive.assets.enabled=true
datameshmanager.client.hive.assets.pollinterval=PT10M
datameshmanager.client.hive.assets.detailed-table-info=json
datameshmanager.client.hive.assets.id-prefix=hive-
datameshmanager.client.hive.assets.owner=
```

### Configuration Properties Reference

#### Connection Properties
| Property | Description | Default | Required |
|----------|-------------|---------|----------|
| `datameshmanager.client.hive.connection.host` | Hive server hostname | `localhost` | Yes |
| `datameshmanager.client.hive.connection.port` | Hive server port | `10000` | Yes |
| `datameshmanager.client.hive.connection.database` | Default database | `default` | Yes |
| `datameshmanager.client.hive.connection.username` | Username for authentication | - | Yes |
| `datameshmanager.client.hive.connection.password` | Password for authentication | - | Yes |
| `datameshmanager.client.hive.connection.driver-class-name` | JDBC driver class name | `org.apache.hive.jdbc.HiveDriver` | Yes |
| `datameshmanager.client.hive.connection.jdbc-url` | Full JDBC connection URL | `jdbc:hive2://localhost:10000/default` | Yes |

#### Asset Properties
| Property | Description | Options | Default | Required |
|----------|-------------|---------|---------|----------|
| `datameshmanager.client.hive.assets.connectorid` | Unique connector identifier | Any string | `hive-assets` | Yes |
| `datameshmanager.client.hive.assets.enabled` | Enable asset synchronization | `true`, `false` | `true` | No |
| `datameshmanager.client.hive.assets.pollinterval` | Synchronization interval (ISO-8601) | Any valid duration | `PT10M` | No |
| `datameshmanager.client.hive.assets.detailed-table-info` | How to handle detailed table information | `json`, `raw`, `ignore` | `json` | No |
| `datameshmanager.client.hive.assets.id-prefix` | Prefix for all asset IDs | Any string | `hive-` | No |
| `datameshmanager.client.hive.assets.owner` | Default owner team ID for all assets | Valid team ID or empty | Empty | No |

## JDBC Driver Examples

The connector supports various Hive-compatible JDBC drivers:

### Apache Hive
```properties
datameshmanager.client.hive.connection.driver-class-name=org.apache.hive.jdbc.HiveDriver
datameshmanager.client.hive.connection.jdbc-url=jdbc:hive2://localhost:10000/default
```

### Apache Impala
**Note**: Requires the Impala JDBC driver JAR file to be present in the `/drivers` folder. Maven will automatically include all JARs from this folder in the build.
```properties
datameshmanager.client.hive.connection.driver-class-name=org.apache.impala.jdbc.jdbc41.Driver
datameshmanager.client.hive.connection.jdbc-url=jdbc:impala://localhost:21000/default
```

### Cloudera Hive
**Note**: Requires the Cloudera Hive JDBC driver JAR file to be present in the `/drivers` folder. Maven will automatically include all JARs from this folder in the build.
```properties
datameshmanager.client.hive.connection.driver-class-name=com.cloudera.hive.jdbc.HS2Driver
datameshmanager.client.hive.connection.jdbc-url=jdbc:hive2://localhost:10000/default
```

### Cloudera Impala
**Note**: Requires the Cloudera Impala JDBC driver JAR file to be present in the `/drivers` folder. Maven will automatically include all JARs from this folder in the build.
```properties
datameshmanager.client.hive.connection.driver-class-name=com.cloudera.impala.jdbc.Driver
datameshmanager.client.hive.connection.jdbc-url=jdbc:impala://localhost:21050/default
```

## JDBC Driver Setup

The connector is configured to automatically include **all** JDBC driver JAR files placed in the `/drivers` folder during the Maven build process. This allows you to add any JDBC drivers without modifying the build configuration.

Simply place your JDBC driver JAR files in the `/drivers` folder:

```
/drivers/
├── HiveJDBC42.jar       # Example: Cloudera Hive driver
├── ImpalaJDBC42.jar     # Example: Cloudera Impala driver  
├── custom-driver.jar    # Any other JDBC driver
└── ...                  # Maven will include all *.jar files
```

During the build process:
1. Maven automatically copies all `.jar` files from `/drivers` to the classpath
2. The Spring Boot application can dynamically load these drivers at runtime
3. No code changes are required to add new drivers

## Detailed Table Information

The connector can extract detailed table information using `DESCRIBE EXTENDED` command:

- **`json`**: Parse the detailed information using HiveObjectParser and store as JSON object
- **`raw`**: Store the detailed information as raw string data  
- **`ignore`**: Skip processing detailed table information completely

## Asset Hierarchy

The connector creates a two-level hierarchy with parent relationships:

- **Database**: ID format: `{id-prefix}.{database}`
- **Table**: ID format: `{id-prefix}.{database}.{table}` (parent: database)

In Hive, databases and schemas are equivalent concepts, so the connector skips the schema level and creates tables directly under databases.

## Development and Testing

For development and testing purposes, the project includes convenient scripts to set up a local Hive environment using Docker:

### Setting up Local Hive Environment

#### start-hive.sh
Starts a local Apache Hive 4.1.0 container with HiveServer2 for development and testing:

```bash
./start-hive.sh
```

This script:
- Runs Apache Hive 4.1.0 in a Docker container named `hive4`
- Exposes HiveServer2 on port 10000 (JDBC connections)
- Exposes Hive Web UI on port 10002
- Provides access to the Web UI at http://localhost:10002/

#### start-beeline.sh
Connects to the local Hive instance using Beeline shell for interactive testing:

```bash
./start-beeline.sh
```

This script:
- Connects to the HiveServer2 instance running in the `hive4` container
- Provides example commands for testing:
  - `show databases;` - List available databases
  - `show tables;` - List tables in current database
  - `create table hive_example(a string, b int) partitioned by(c int);` - Create a test table

### Development Workflow

1. Start the local Hive environment:
   ```bash
   ./start-hive.sh
   ```

2. Configure the connector to use the local Hive instance in `application-local.properties`:
   ```properties
   datameshmanager.client.hive.connection.host=localhost
   datameshmanager.client.hive.connection.port=10000
   datameshmanager.client.hive.connection.jdbc-url=jdbc:hive2://localhost:10000/default
   ```

3. Test the connection using Beeline:
   ```bash
   ./start-beeline.sh
   ```

4. Run the connector with the local profile:
   ```bash
   mvn spring-boot:run -Dspring-boot.run.profiles=local
   ```

## Building and Running

### Build the application
```bash
mvn clean package
```

### Run with Maven
```bash
mvn spring-boot:run
```

### Run the JAR directly
```bash
java -jar target/datamesh-manager-connector-hive-0.0.1-SNAPSHOT.jar
```

### Docker
```bash
# Build the image
docker build -t datamesh-manager-connector-hive .

# Run the container
docker run -p 8080:8080 -v $(pwd)/application.properties:/app/application.properties datamesh-manager-connector-hive
```

## Authentication and Security

The connector supports various authentication mechanisms depending on your Hive setup:

- **No Authentication**: Connect without credentials
- **Username/Password**: Basic authentication with username and password
- **Kerberos**: For secure environments (driver dependent)

Configure SSL connections through JDBC URL parameters as supported by your specific JDBC driver.

## Monitoring

The application exposes actuator endpoints for monitoring:

- Health check: `http://localhost:8080/actuator/health`
- Metrics: `http://localhost:8080/actuator/metrics`

## Troubleshooting

### Common Issues

1. **JDBC Driver Not Found**: Ensure the correct JDBC driver is included in the classpath. The default setup includes Apache Hive JDBC driver.

2. **Connection Timeout**: Check network connectivity and firewall settings between the connector and Hive server.

3. **Authentication Failures**: Verify username, password, and authentication mechanism configuration.

4. **No Tables Found**: Ensure the user has permissions to access databases and tables in the Hive system.

### Logging

Enable debug logging for more detailed information:

```properties
logging.level.datameshmanager.hive=DEBUG
```

## License

MIT
