#Deprecated
FROM node:alpine

WORKDIR /app

COPY package.json .

RUN npm i -f 

COPY . .

#Temporary remove ip & port hard set

#ARG DEFAULT_BACKEND_URL=localhost
#ARG DEFAULT_BACKEND_PORT=8080
#
#ENV REACT_APP_BACKEND_URL=${DEFAULT_BACKEND_URL} \
#    REACT_APP_BACKEND_PORT=${DEFAULT_BACKEND_PORT}

EXPOSE 3000

CMD ["npm", "start"]




