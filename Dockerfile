FROM node:latest
RUN mkdir -p /usr/src/app
WORKDIR /usr/src/app
COPY package.json /usr/src/app
COPY target/demo /usr/src/app/target/demo
COPY config.example.edn /usr/src/app
COPY TODOs.example.org /usr/src/app
COPY resources/public /usr/src/app/resources/public
RUN npm install
EXPOSE 80
CMD ["npm", "start"]
