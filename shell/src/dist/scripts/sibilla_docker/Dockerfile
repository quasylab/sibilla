FROM python:3-alpine

# Installing jdk11
RUN apk add --no-cache openjdk11-jdk
RUN ln -s /usr/lib/jvm/java-11-openjdk/lib/server/libjvm.so /usr/lib/jvm/java-11-openjdk/lib/

# Installing Build dependencies
RUN apk add --no-cache --virtual build-dep git build-base libffi-dev

# Adding new user
RUN addgroup sibilla && adduser -S sibilla -G sibilla
USER sibilla
WORKDIR /home/sibilla

# Cloning and building sibilla repository
RUN git clone https://github.com/f-biondi/sibilla repository
WORKDIR repository
RUN ./gradlew build && ./gradlew installDist
ENV SSHELL_PATH="/home/sibilla/repository/shell/build/install/sshell"
RUN python3 -m pip install --no-cache-dir --no-warn-script-location ./shell/build/install/sshell/scripts/sibilla_py

# Installing jupyterlab
RUN python3 -m pip install --no-cache-dir --no-warn-script-location jupyterlab

# Removing build dependencies
USER root
RUN apk del --purge -r build-dep

# copying workspace directory and setting entrypoint command
USER sibilla
COPY --chown=sibilla:sibilla workspace /home/sibilla/workspace
WORKDIR /home/sibilla/workspace
EXPOSE 8888
ENTRYPOINT /home/sibilla/.local/bin/jupyter lab \ 
--ip=* \
--no-browser \ 
--notebook-dir=/home/sibilla/workspace \
--ServerApp.token='' \ 
--ServerApp.password=''
