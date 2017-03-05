# ID2203 - Distributed Systems, Advanced - Course Project

To start a cluster of size three

    sbt server/"run -p 10000"
    sbt server/"run -p 10001 -c localhost:10000"
    sbt server/"run -p 10002 -c localhost:10000"

with one client connected to each node.

    client/"run -p 20000 -b localhost:10000"
    client/"run -p 20001 -b localhost:10001"
    client/"run -p 20002 -b localhost:10002"
