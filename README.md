# ECache
ECache is a distributed memory caching system designed for distribute file system.


Ecache allows applications to cache data in memory of DataNode. In order to provide a direct Access to DataNode when searching, Ecache uses a dynamic hash table where each bucket represents a DataNode.

### Ecache consists of three modules:
* **ECache-Common:** is a common module
* **ECache-SlaveNode:** Is the module that runs at the DataNode level
* **ECache-MasterNode:** Is the module that runs at the NameNode level
* **ECache-Client:** Is the client module needed to perform operations
