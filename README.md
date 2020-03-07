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
    .process(this::operate)
    .to("direct:next")
```

## Exchange
For each invocation of a route, an exchange is created, and passed from stage to stage.  This exchange holds the data
being processed, along with house-keeping items such as headers, properties and meta-data.

Typically, an operation within a stage will access the input data, use or modify it, and set as the output data.
```
  +--------------------------------------------+
  |                   Exchange                 |
  | +----------------------------------------+ |
  | | Properties, MEP, Exception, ExchangeId | |
  | +----------------------------------------+ |
  |        Input                 Output        |
  | +------------------+   +-----------------+ |
  | |     Headers      |   |     Headers     | |  
  | +------------------+   +-----------------+ |  
  | +------------------+   +-----------------+ |
  | |       Body       |   |       Body      | |  
  | +------------------+   +-----------------+ |  
  +--------------------------------------------+
```
### MEP
Message Exchange Pattern

There are three MEPs available: `InOut`, `InOnly` and `InOptionalOut`

*  The *out* message from one stage becomes the *in* message for the following stage
*  If there is no *out* message, then the *in* message is used
*  For `InOut`, the *out* from the last stage in the route is returned to the producer
*  For `InOnly`, the last *out* is discarded

