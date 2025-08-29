docker run -d -p 10000:10000 -p 10002:10002 --env SERVICE_NAME=hiveserver2 --name hive4 apache/hive:4.1.0

echo "Open UI at http://localhost:10002/"
