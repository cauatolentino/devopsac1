# ---------------------------------------------------------------------------
# Etapa 1 - build do bundle com Vite
# ---------------------------------------------------------------------------
FROM node:22-alpine AS build
WORKDIR /build

COPY package*.json ./
RUN npm ci

COPY . .
RUN npm run build

# ---------------------------------------------------------------------------
# Etapa 2 - servir os arquivos estaticos com nginx
# ---------------------------------------------------------------------------
FROM nginx:alpine

COPY --from=build /build/dist /usr/share/nginx/html
COPY nginx.conf /etc/nginx/conf.d/default.conf

EXPOSE 80
