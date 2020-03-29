# Parallel Routes

As an example of parallel processing, consider the enterprise 'scatter-gather' pattern, as illustrated below. 
Here, a message is routed to a number recipients, and the responses re-aggregated back into a single message.
These recipients could be called sequentially, or, as in the example developed here, in parallel, potentially
saving time if the stages are remote and slow, but also providing the flexibility to return a result even when
not all the stages return in a given time-frame, or if some fail.
![Scatter-Gather](http://www.enterpriseintegrationpatterns.com/img/BroadcastAggregate.gif "Scatter-Gather")

## Example
This example implements a process similar to the one depicted above, but, where a request - a book title - is 
transformed into a resulting `BookRecord` domain object.
  
To do this, four endpoints are queried: initially, the book title is used to look-up the ISBN code for the book, 
a natural key, that will be used in later processing stages.

Having obtained this key, it is broadcast to three parallel stages, querying various pieces of data 
needed to assemble the domain object.  These queries occur concurrently, and to make the example realistic,
includes a random delay, making it unlikely that the stages will complete in any deterministic order.

Each of these processes has it's own copy of the _exchange_ output from stage (2), with the ISBN code, and into
which it will store the results of the query as the output `body`, a Java `Properties` instance, with a key/value
pair representing the result of the query. As each stage completes, the aggregation strategy object, `MergeHub`, 
combines this with the results of the other stages, finally outputting a single, aggregated result.

Ultimately, the route specifies that the resulting value should be converted to a `BookRecord`, performed
by a registered converter with the signature `Properties => BookRecord` - this function takes the aggregated 
properties returned by the previous stages, constructing the domain object.

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
Rather than make external calls, this example uses methods in a `LibraryServices` class to query the data needed. 
The route stages use the `seda:` component
([staged event-driven architecture](https://en.wikipedia.org/wiki/Staged_event-driven_architecture):
an async in-memory queue) to connect to these services.  

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