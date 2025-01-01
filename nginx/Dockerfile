FROM node:alpine AS build
RUN apk add --no-cache git
WORKDIR /app
RUN git clone --depth 1 https://github.com/suyttthideptrai/SS2-game-theory-solver.git .
RUN npm i -f
RUN rm -rf /app/.env
COPY .env /app
RUN npm run build


FROM nginx:alpine
RUN rm /etc/nginx/conf.d/default.conf
COPY --from=build /app/build /usr/share/nginx/html
COPY ./default.conf /etc/nginx/conf.d/default.conf
# For container resources and configs interaction
VOLUME /usr/share/nginx/html
VOLUME /etc/nginx