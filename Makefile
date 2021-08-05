.phony: migrate
migrate:
	./mvnw flyway:migrate

.phony: run
run:
	./mvnw clean install && java -jar ./target/mnemosyne-1.0.0.jar
