# Build Stage
FROM maven:3.9.6-eclipse-temurin-21 AS build
WORKDIR /app
COPY pom.xml .
COPY src ./src

# Don't run tests
RUN mvn clean package -DskipTests

# Run Stage
FROM tomcat:10.1-jdk21
# Delete old root folder
RUN rm -rf /usr/local/tomcat/webapps/ROOT

#copy .war file and reanme to root.war
COPY --from=build /app/target/*.war /usr/local/tomcat/webapps/ROOT.war

EXPOSE 8080
CMD ["catalina.sh", "run"]