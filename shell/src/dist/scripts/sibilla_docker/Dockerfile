FROM python:3

# Installing jdk11
RUN apt-get update && apt-get upgrade
RUN apt-get install -y openjdk-11-jdk

# Adding new user
RUN adduser sibilla --disabled-password --gecos ""
USER sibilla
WORKDIR /home/sibilla

# Cloning and building sibilla repository
RUN git clone https://github.com/quasylab/sibilla.git repository
WORKDIR repository
RUN ./gradlew build && ./gradlew installDist
ENV SSHELL_PATH="/home/sibilla/repository/shell/build/install/sshell"

# Installing python libraries
RUN pip install --no-cache-dir --no-warn-script-location ./shell/build/install/sshell/scripts/sibilla_py jupyterlab matplotlib

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
