# 1단계: 빌드 (Node.js)
FROM node:20-alpine AS build
WORKDIR /app

# 의존성 설치 캐싱
COPY package*.json ./
RUN npm ci

# 소스 복사 및 빌드
COPY . .
RUN npm run build

# 2단계: 실행 (Nginx)
FROM nginx:stable-alpine

# 빌드된 정적 파일 복사
COPY --from=build /app/dist /usr/share/nginx/html

# 커스텀 Nginx 설정 복사
COPY nginx.conf /etc/nginx/conf.d/default.conf

EXPOSE 80

CMD ["nginx", "-g", "daemon off;"]
