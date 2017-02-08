# ID2203 -- Distributed Systems, Advanced -- Course Project

## Infrastructure
For this task you have to design the basic infrastructural layers for the key-value store.
Your system should support a partitioned key-space of some kind (e.g. hash-partitioned
strings or range-partitioned integers). The partitions need to be distributed over the
available nodes such that all value are replicated with a specific replication degree δ.
You are free to keep δ as a configuration value or hardcode it, as long as it fulfils the
requirements of your chosen replication algorithm (next task), so plan ahead.
For this task you need to be able to set up the system, assign nodes to partitions and
replication groups, lookup (preloaded) values from an external system (client), and
detect (but not necessarily handle) failures of nodes. Additionally, you should be able
to broadcast information within replication groups and possibly across.
On the verification side you have to write simulator scenarios that test all the features
mentioned above. You should also be able to run in a simple deployment (it's fine to
run multiple JVMs on the same machine and consider them separate nodes).
For the report describe and motivate all your decisions and tests.
