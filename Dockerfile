FROM gradle:4.7.0-jdk8-alpine AS build
COPY --chown=gradle:gradle . /newtech/code/admin-cn
WORKDIR /newtech/code/admin-cn
RUN gradle build

FROM openjdk:8-jre-slim
EXPOSE 8080
COPY --from=build /newtech/code/admin-cn/build/libs/niutaike_boot-1.0.0.jar /app/
RUN bash -c 'touch /app/niutaike_boot-1.0.0.jar'
EXPOSE ${EXPOSED_PORT}
ENTRYPOINT ["java", "-XX:+UnlockExperimentalVMOptions", "-XX:+UseCGroupMemoryLimitForHeap", "-Djava.security.egd=file:/dev/./urandom","-jar","/app/niutaike_boot-1.0.0.jar"]
