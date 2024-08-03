FROM eclipse-temurin:17 as app-build

ENV APP_BUILD_DIR=/opt/build

WORKDIR ${APP_BUILD_DIR}
COPY ./wasp-core/target/wasp-core-*.jar ./wasp-core.jar
RUN java -Djarmode=layertools -jar wasp-core.jar extract

FROM eclipse-temurin:17

ENV APP_BUILD_DIR=/opt/build

RUN groupadd wasp \
  && useradd --gid wasp --shell /bin/bash --create-home wasp-core
USER wasp-core:wasp
WORKDIR /opt/wasp-core

COPY --from=app-build ${APP_BUILD_DIR}/spring-boot-loader/ ./
COPY --from=app-build ${APP_BUILD_DIR}/dependencies/ ./
COPY --from=app-build ${APP_BUILD_DIR}/snapshot-dependencies/ ./
COPY --from=app-build ${APP_BUILD_DIR}/application/ ./

ENTRYPOINT ["java", "org.springframework.boot.loader.launch.JarLauncher"]