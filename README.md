# ID2203 - Distributed Systems, Advanced - Course Project

To start a cluster of size three

    sbt server/"run -p 11111"
    sbt server/"run -p 22222 -c localhost:11111"
    sbt server/"run -p 33333 -c localhost:11111"

with one client connected to each node.

    client/"run -p 44444 -b localhost:11111"
    client/"run -p 55555 -b localhost:22222"
    client/"run -p 66666 -b localhost:33333"
