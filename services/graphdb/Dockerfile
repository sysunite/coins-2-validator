FROM java:8 

# Install maven
RUN apt-get update
RUN apt-get install -y zip

WORKDIR /opt

# Prepare by downloading dependencies
ADD graphdb-free-8.1.1-dist.zip /opt/graphdb-free-8.1.1-dist.zip
RUN ["unzip", "graphdb-free-8.1.1-dist.zip"]
RUN ["rm", "graphdb-free-8.1.1-dist.zip"]

CMD ["/opt/graphdb-free-8.1.1/bin/graphdb"]