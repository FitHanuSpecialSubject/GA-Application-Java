FROM node:alpine AS build
RUN apk add --no-cache git
WORKDIR /app
RUN git clone --depth 1 https://github.com/FitHanuSpecialSubject/GA-Webapp.git .
RUN npm i -f
RUN rm -rf /app/.env
COPY .env /app
RUN npm run build


FROM nginx:alpine
RUN rm /etc/nginx/conf.d/default.conf
COPY --from=build /app/dist /usr/share/nginx/html
COPY ./default.conf /etc/nginx/conf.d/default.conf
# For container resources and configs inspect
VOLUME /usr/share/nginx/html
VOLUME /etc/nginx