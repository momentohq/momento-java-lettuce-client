{{ ossHeader }}

# Momento Lettuce Compatibility Client

## What and why?

This project provides a Momento-backed implementation of [lettuce](https://github.com/redis/lettuce)
The goal is to provide a drop-in replacement for [lettuce](https://github.com/redis/lettuce) so that you can
use the same code with either a Redis server or with the Momento Cache service!

## Installation

The Momento Lettuce compatibility client is [available on Maven Central](https://search.maven.org/artifact/software.momento.java/momento-lettuce). You can add it to your project via:

```xml
<dependency>
  <groupId>software.momento.java</groupId>
  <artifactId>momento-lettuce</artifactId>
  <version>0.1.0</version>
</dependency>
```

## Usage

To switch your existing `lettuce` application to use Momento Cache, you only need to change the code where you construct your client object. Here is an example of constructing a Momento lettuce client:

```java
```

Additionally, to understand what APIs are supported, you can use the interface `MomentoRedisReactiveCommands` which contains only those APIs that are supported by this compatibility client:

```java
```

## Current Redis API support

This library supports the most popular Redis APIs, but does not yet support all Redis APIs. We currently support the most common APIs related to string values (`get`, `set`, `unlink`), as well as list (`lpush`, `lrange`, `ltrim`). We will be adding support for additional APIs in the future. If there is a particular API that you need support for, please drop by our [Discord](https://discord.com/invite/3HkAKjUZGq) or e-mail us at [support@momentohq.com](mailto:support@momentohq.com) and let us know!

{{ ossFooter }}
