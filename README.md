RESTful API to Oracle databases
=======

<a href="http://valdasraps.github.io/resthub" target="_blank">RestHub website</a><br>
<a href="https://raw.githubusercontent.com/valdasraps/resthub/master/doc/RestHUB_docs.pdf" target="_blank">RestHub documentation</a>
![Architecture](https://raw.githubusercontent.com/valdasraps/resthub/master/doc/resthub.jpg "Architecture")

#### Overview

RestHub was designed to provide a way to access data from the client application through RESTful API. In this manner developers will no longer have to implement difficult approaches for data retrieval from the database in representational layer and can separate application and representation codebase. They will be able to access data through the RestHub system by sending simple SQL queries.

#### Benefits

Separate data access from the representation:

- Data access via API
- Application and representation on separate codebase
- Standard, reusable API
- Many ways to use the service (Browser, Python, Java, JS, ...)

#### Features

Self descriptive:

- Includes metadata
- HATEOAS (including hypermedia links with the responses).

Secure:

- Specialized multilevel query parsers
- Multithreading
- Timeouts

Fast:

- Caching
- Asynchronous prefetching

Flexible:

- SQL for querying

#### Types of resources

- Tables: Views with optional parameters and metadata, back-end control.
- Queries: Views on Tables, front-end control.
- Cache: Query result in different representation for fast retrieval.

#### API

| URL | Method | Description |
|-----|--------|-------------|
| / | GET | List of tables in JSON |
| /tables | GET | List of tables in JSON |
| /tables | PUT | Refresh all tables |
| /tables/{namespace} | GET | List of tables in JSON by namespace |
| /tables/{namespace} | PUT | Refresh namespace tables |
| /tables/{namespace}/{name} | PUT | Refresh namespace table by name |
| /table/{namespace}/{name} | GET | Table description in JSON |
| /queries | GET | List of queries in JSON |
| /query | POST | Create query from provided entity SQL and return automatically generated {id} |
| /query/{id} | GET | Query description in JSON |
| /query/{id} | DELETE | Remove query |
| /query/{id}/{count} | GET | Rows count |
| /query/{id}/data | GET | Get query data. Media type is by Accept header. For example: “Accept” : “application/xml” |
| /query/{id}/page/{pp}/{p}/data | GET | Get query page data. Media type is by Accept header. Variables: pp - data rows per page, p - page number |
| /query/{id}/cache | GET | Get query cache information |
| /query/{id}/cache | DELETE | Clean all query cache |
| /query/{id}/{row}/{col}/lob | GET | Retrieve large object from row column|
| /query/{id}/page/{pp}/{p}/{row}/{col}/lob | GET | Retrieve large object from row column per page |
| /table/{namespace}/{name}/cache | GET | Get the list of query caches that use the table defined |
| /query/{id}/data?{p}={v} | GET | Get query data with parameters. Variables: p - parameter name, v - parameter value. |
| /blacklist | GET | List of blacklist tables in JSON |
| /blacklist | DELETE | Clear blacklist tables |
| /blacklist/{namespace} | GET | List of blacklist tables in JSON by namespace |
| /blacklist/{namespace} | DELETE | Clear blacklist by namespace |
| /blacklist/{namespace}/{name} | GET | Retrieve blacklist table from namespace by name |
| /blacklist/{namespace}/{name} | DELETE | Clear blacklist table from namespace  by name |

