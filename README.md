RESTful API to Oracle databases
=======

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

