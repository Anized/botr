# Apache Camel Introduction
A small exposition of the uses, terminology and practice of the Apache Camel framework

## Routes
A Camel route is a blueprint for a process execution, consisting of a number of stages linked together, 
much like a pipeline in the `bash` shell, where the output of one operation becomes the input of the 
next operation in the pipeline.

A simple example can be depicted as below, where data sent to the 'from' box is transmitted to the 'process' box,
where some operation might be performed on it, and finally output to the 'to' box.
```text
  +------+    +---------+    +------+
  | from | => | process | => |  to  |
  +------+    +---------+    +------+
```

```java
  from("direct:start")
    .process(this::operation)
    .to("direct:next")
```

## Exchange
For each invocation of a route, an exchange is created, and passed from stage to stage.  This exchange holds the data
being processed, along with house-keeping items such as headers, properties and meta-data.

Typically, an operation within a stage will access the input data, use or modify it, and set as the output data.
 
```
  +--------------------------------------------+
  |                   Exchange                 |  Exchange[Id: ID-LP-CCS-54505441-1573671324375-0-2, 
  | +----------------------------------------+ |    ExchangePattern: InOut,
  | | Properties, MEP, Exception, ExchangeId | |    Properties: {CamelCharsetName=utf-8, 
  | +----------------------------------------+ |      CamelToEndpoint=http://worldclockapi.com/api/json/cet/now}
  |        Input                 Output        |      
  | +------------------+   +-----------------+ |    Headers: {CamelHttpMethod=GET, CamelHttpResponseCode=200, 
  | |     Headers      |   |     Headers     | |      CamelHttpResponseText=OK, Content-Length=261, 
  | +------------------+   +-----------------+ |      Content-Type=application/json; charset=utf-8,
  | +------------------+   +-----------------+ |      Date=Sun, 08 Mar 2020 12:42:03 GMT, Expires=-1}
  | |       Body       |   |       Body      | |    BodyType: org.anized.jafool.DateTimeReport,
  | +------------------+   +-----------------+ |    Body: org.anized.jafool.DateTimeReport@5b8dfcc1
  +--------------------------------------------+  ]
```
### MEP
Message Exchange Pattern

There are three MEPs available: `InOut`, `InOnly` and `InOptionalOut`

*  The *out* message from one stage becomes the *in* message for the following stage
*  If there is no *out* message, then the *in* message is used
*  For `InOut`, the *out* from the last stage in the route is returned to the producer
*  For `InOnly`, the last *out* is discarded

## Components
Apache Camel routes are built-up from a library of components: pre-built integrations with commonly used platforms
and frameworks.  Components are invoked by being named in the *scheme* potion of the `uri` in a `to` stage, for example.
This example utilises the `http` component - note, that although the endpoint used looks like a regular URL:

    .toD("http://worldclockapi.com/api/json/${body}/now")

The scheme `http` is being used to instruct Camel to pass this URI to the `http` component (where the scheme is then
also used in the web-service call).  This is not always the case, as often the scheme portion is only used to identify
the component, and discarded once that component is invoked to the process the request.

Camel has a catalog of over 200 components, including message queues, database access, spark, Azure storage queues, to
name but a few.  It is relatively easy to build custom components to support commonly needed integrations in an
enterprise, as well.