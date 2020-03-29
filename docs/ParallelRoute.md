# Parallel Routes
## Example
This example implements a simple process, whereby a request - a book title - is transformed into a
detailed `BookRecord` domain object.  To do this, four endpoints are queried: initially, the book title
is used to look-up the ISBN code for the book, it's natural key, that will be used in later processing 
stages.

Having obtained this key, the process fans-out into three parallel stages, to query various pieces of data 
needed to assemble the domain object.  These queries happen concurrently, and to make the example realistic,
includes a random delay, making it unlikely that the stages will complete in any deterministic order.

Each of these processes has it's own copy of the _exchange_ output from stage (2), into which it will store
the data it is responsible for querying, as the output `body`, a Java `Properties` instance, with a key/value
pair representing the data retrieved.

As the stages complete, the aggregation strategy object, `MergeHub`, sets this map to be the output object.

Finally, the route specifies that the resulting value should be converted to a `BookRecord`, which is done
by a registered converter with the signature `Properties => BookRecord` - this function gets the data
returned by the previous stages from the properties map, using them to construct the domain object.

```
      (1)               (2)         (3)      (i) +----------------------+          (4)           (5)
       |                 |           |  ,------> | seda:published-query | -------,  |             | 
       |                 |           | /         +----------------------+         \ |             |
  +----*----+     +------*------+    |/     (ii) +----------------------+          \|      +------*-----+
  | "title" | --> | setIsbnCode | ---*---------> |   seda:price-query   | ----------*----> | BookRecord |
  +---------+     +-------------+     \          +----------------------+          /       +------------+
                                       \   (iii) +----------------------+         /           
                                        `------> |   seda:author-query  | -------'
                                                 +----------------------+
```
1.  Request received
1.  ISBN Code lookup call
1.  Parallel fan-out:
    1.  Query publication date
    1.  Query price
    1.  Query author
1.  Merge hub: aggregate data
1.  Build result object

## Implementation
```java
from("direct:book-search")
        .process(library::setIsbnCode)
        .multicast()
        .parallelProcessing(true)
        .executorService(ec)
        .aggregationStrategy(new MergeHub())
            .to(seda("published-query"))
            .to(seda("price-query"))
            .to(seda("author-query"))
        .end()
        .convertBodyTo(BookRecord.class);
```
Rather than make external calls, this example uses methods in a `BookServices` class to lookup the data needed. 
The route stages use the `seda:` component
([staged event-driven architecture](https://en.wikipedia.org/wiki/Staged_event-driven_architecture):
an async in-memory queue) to connect these services.  

In a real-world scenario, each parallel stage might need to construct a different request and/or endpoint,
call the appropriate component, e.g., `http:`, and transform the result back into the required form. 

Additionally, a realistic implementation should take care of connection issues with external systems, perhaps
handling IO errors with an incremental back-off algorithm, as well as dealing with other errors, such as 500s
returned by the remote service.  This can be handled declaratively using Camel's error and exception handling
mechanisms.

On completion, each of three parallel stages sets a `Properties` (map) object as it's output, with a key/value pair
representing the data retrieved. The outputs of the each of the stages are aggregated in `MergeHub`, where, as
each stage completes, it's output map is merged with the output of previous stages (if any).

On completion of all stages, the values from resulting properties map are used to construct the `BookRecord` that
is returned to the caller.