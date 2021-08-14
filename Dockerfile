FROM openjdk:16-jdk-slim

COPY target/mnemosyne-1.0.0.jar /usr/src
WORKDIR /usr/src
CMD java -Xms128M -Xmx128M -Xss1024k -XX:ReservedCodeCacheSize=32M -XX:CodeCacheExpansionSize=512k -XX:MaxDirectMemorySize=64M -XX:CompressedClassSpaceSize=32M -XX:MaxMetaspaceSize=64M -XX:+PerfDisableSharedMem -XX:+UnlockDiagnosticVMOptions -XX:NativeMemoryTracking=summary -XX:+PrintNMTStatistics -jar ./mnemosyne-1.0.0.jar

EXPOSE 8080
EXPOSE 6565