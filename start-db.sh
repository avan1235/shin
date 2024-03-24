#!/usr/bin/env bash

docker compose --file docker-compose.yml --env-file .env build shin-dev-postgres
docker compose --file docker-compose.yml --env-file .env up shin-dev-postgres
