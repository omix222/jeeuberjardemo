# Jakarta EE 10 Application(OpenLiberty)

## Introduction

The generation of the executable jar file can be performed by issuing the following command


    mvn clean package

This will create an executable jar file **jeedemo.jar** within the _target_ maven folder. This can be started by executing the following command

    java -jar target/jeedemo.jar

To launch the test page, open your browser at the following URL

    http://localhost:9080/index.html 