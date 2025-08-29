echo "Starting Beeline shell connected to Hive..."
echo "Examples"
echo "  show databases;"
echo "  show tables;"
echo "  create table hive_example(a string, b int) partitioned by(c int);"

docker exec -it hive4 beeline -u 'jdbc:hive2://localhost:10000/'
