# Currency Rate Application


## Spring Boot backend

### Structure:
- backend/  -> Spring Boot application (Maven)

### Instructions:
```
  cd backend
  mvn clean package
  java -jar target/tecajnica-1.0.0.jar
```


## JavaFx application frontend

### Structure:
- tecajnica/  -> JavaFX application (Maven)

### Instructions:
```
 mvn clean package
 java --module-path target/libs --add-modules javafx.controls,javafx.fxml -jar target/fx-exchange-rate-1.0-SNAPSHOT.jar
```
