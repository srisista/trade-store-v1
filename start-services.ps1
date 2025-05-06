# Check if Docker is running
$dockerStatus = docker info 2>&1
if ($LASTEXITCODE -ne 0) {
    Write-Host "Docker is not running. Please start Docker Desktop first."
    exit 1
}

# Check if required containers are running
$containers = @(
    @{Name="postgres"; Image="postgres:13-alpine"; Port="5432"},
    @{Name="mongodb"; Image="mongo:4.0.21"; Port="27017"},
    @{Name="kafka"; Image="confluentinc/cp-kafka:7.3.0"; Port="9092"}
)

foreach ($container in $containers) {
    $running = docker ps --filter "name=$($container.Name)" --format "{{.Names}}"
    if (-not $running) {
        Write-Host "Starting $($container.Name)..."
        switch ($container.Name) {
            "postgres" {
                docker run -d --name postgres -e POSTGRES_PASSWORD=postgres -e POSTGRES_USER=postgres -e POSTGRES_DB=tradestore -p 5432:5432 postgres:13-alpine
            }
            "mongodb" {
                docker run -d --name mongodb -p 27017:27017 mongo:4.0.21
            }
            "kafka" {
                docker run -d --name kafka -p 9092:9092 -e KAFKA_ADVERTISED_LISTENERS=PLAINTEXT://localhost:9092 -e KAFKA_LISTENERS=PLAINTEXT://0.0.0.0:9092 -e KAFKA_ZOOKEEPER_CONNECT=localhost:2181 confluentinc/cp-kafka:7.3.0
            }
        }
    } else {
        Write-Host "$($container.Name) is already running."
    }
}

Write-Host "All required services are running." 