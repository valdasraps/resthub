RESTful API to Oracle databases
=======

[![Maven Central](https://maven-badges.herokuapp.com/maven-central/lt.emasina/resthub/badge.svg)](https://maven-badges.herokuapp.com/maven-central/lt.emasina/resthub/)
[![LGPLv3 License](http://img.shields.io/badge/license-LGPLv3-blue.svg)](https://www.gnu.org/licenses/lgpl.html)
[![Java Development Kit 1.7](https://img.shields.io/badge/JDK-1.7-green.svg)](http://www.oracle.com/technetwork/java/javase/downloads/jdk7-downloads-1880260.html)
[![Coverage Status](https://coveralls.io/repos/valdasraps/resthub/badge.svg?branch=master&service=github)](https://coveralls.io/github/valdasraps/resthub?branch=master)
[![Build Status](https://travis-ci.org/valdasraps/resthub.svg?branch=master)](https://travis-ci.org/valdasraps/resthub)

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
| /tables | GET | List of tables in JSON |
| /tables/{namespace} | GET | List of namespace tables in JSON |
| /table/{namespace}/{name} | GET | Table description in JSON |
| /queries | GET | List of queries in JSON |
| /query | POST | Create query from provided entity SQL and return automatically generated {id}. Query can include parameters :name (strings) or :s__name for strings, :n__name for numbers and :d__name for dates |
| /query/{id} | GET | Query description in JSON |
| /query/{id} | DELETE | Remove query |
| /query/{id}/count | GET | Get query data size |
| /query/{id}/data | GET | Get query data. Media type is by Accept header. For example: “Accept” : “application/xml” |
| /query/{id}/page/{pp}/{p}/data | GET | Get query page data. Media type is by Accept header. Variables: pp - data rows per page, p - page number |
| /table/{namespace}/{table}/data | GET | Get table data. Media type is by Accept header. For example: “Accept” : “application/xml”. Creates query and redirects to it. |
| /table/{namespace}/{table}/page/{pp}/{p}/data | GET | Get query page data. Media type is by Accept header. Variables: pp - data rows per page, p - page number. Creates query and redirects to it. |
| /query/{id}/cache | GET | Get query cache information |
| /query/{id}/cache | DELETE | Clean all query cache |
| /table/{namespace}/{name}/cache | GET | Get the list of query caches that use the table defined |
| /query/{id}/data?{p}={v} | GET | Get query data with parameters. Variables: p - parameter name, v - parameter value. |
| /blacklist | GET | List of blacklisted tables in JSON |
| /blacklist/{namespace} | GET | List of blacklisted namespace tables in JSON |
| /blacklist/{namespace}/{name} | GET | Blacklisted table description in JSON |
| /blacklist | DELETE | Remove all tables from blacklist (refresh) |
| /blacklist/{namespace} | DELETE | Remove all namespace tables from blacklist (refresh) |
| /blacklist/{namespace}/{name} | DELETE | Remove table from blacklist (refresh) |
| /info | GET | General information about the service |
