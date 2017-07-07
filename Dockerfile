FROM node:latest
RUN mkdir -p /usr/src/app
WORKDIR /usr/src/app
COPY package.json /usr/src/app
COPY target/demo /usr/src/app/target/demo
COPY resources/public /usr/src/app/resources/public
RUN npm install
EXPOSE 3000
CMD ["npm", "start"]
