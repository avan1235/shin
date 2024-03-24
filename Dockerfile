FROM container-registry.oracle.com/graalvm/native-image:17 AS builder
RUN curl -sSL $(curl -s https://api.github.com/repos/upx/upx/releases/latest | \
    grep browser_download_url | \
    grep amd64 | \
    cut -d '"' -f 4) -o upx.tar.xz
RUN microdnf install findutils xz
RUN tar -xf upx.tar.xz && \
    cd upx-*-amd64_linux && \
    mv upx /bin/upx
COPY . .
RUN chmod +x ./gradlew
RUN ./gradlew server:nativeCompile
RUN /bin/upx --best --lzma ./server/build/native/nativeCompile/server

FROM debian:12-slim as runner

ARG POSTGRES_PORT
ARG POSTGRES_DB
ARG POSTGRES_USER
ARG POSTGRES_PASSWORD
ARG POSTGRES_HOST
ARG HOST
ARG PORT
ARG REDIRECT_BASE_URL
ARG CORS_PORT
ARG CORS_HOST
ARG CORS_SCHEME

ENV POSTGRES_PORT=${POSTGRES_PORT}
ENV POSTGRES_DB=${POSTGRES_DB}
ENV POSTGRES_USER=${POSTGRES_USER}
ENV POSTGRES_PASSWORD=${POSTGRES_PASSWORD}
ENV POSTGRES_HOST=${POSTGRES_HOST}
ENV HOST=${HOST}
ENV PORT=${PORT}
ENV REDIRECT_BASE_URL=${REDIRECT_BASE_URL}
ENV CORS_PORT=${CORS_PORT}
ENV CORS_HOST=${CORS_HOST}
ENV CORS_SCHEME=${CORS_SCHEME}

EXPOSE ${PORT}

WORKDIR /home

COPY --from=builder /app/server/build/native/nativeCompile/server ./server

ENTRYPOINT ["/home/server"]
