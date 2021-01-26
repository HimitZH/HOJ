FROM registry.docker-cn.com/library/ubuntu:16.04

COPY *.jar /app.jar

RUN sed -i s@/archive.ubuntu.com/@/mirror.tuna.tsinghua.edu.cn/@g /etc/apt/sources.list
RUN apt-get clean
RUN apt-get update

RUN buildDeps='software-properties-common git libtool cmake python-dev python3-pip python-pip libseccomp-dev' && \
    apt-get update && apt-get install -y python python3 python-pkg-resources python3-pkg-resources gcc g++ $buildDeps && \
    add-apt-repository ppa:openjdk-r/ppa && apt-get update && apt-get install -y openjdk-8-jdk && \
    cd /tmp && git clone -b newnew  --depth 1 https://github.com/QingdaoU/Judger && cd Judger && \
    mkdir build && cd build && cmake .. && make && make install  && \
    apt-get purge -y --auto-remove $buildDeps && \
    apt-get clean && rm -rf /var/lib/apt/lists/* && \
    useradd -u 12001 compiler && useradd -u 12002 code && useradd -u 12003 spj && usermod -a -G spj

RUN rm -rf /judge/* && \
    mkdir -p /judge/run /judge/spj /judge/test_case

CMD ["--server.port=8010"]

EXPOSE 8010

ENTRYPOINT ["java", "-jar", "/app.jar"]