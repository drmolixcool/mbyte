FROM nginx:alpine

COPY maven/target/gui-dist/ /usr/share/nginx/html/
COPY maven/provisioning/nginx.conf /etc/nginx/conf.d/default.conf
