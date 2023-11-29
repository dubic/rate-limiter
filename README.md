# Rate limiter

A rate limiter implementation to protect an endpoint from too many requests

This service was built using java, spring boot, and redis.

## Local setup using docker
* Clone repository: [git clone https://github.com/dubic/rate-limiter.git]()
* Go to rate-limiter root directory
* Build application docker image: ```./mvnw spring-boot:build-image -DskipTests```. On 
  completion, the docker image generated will be ```docker.io/library/rate-limiter:0.0.
  1-SNAPSHOT```
* Start the application by running: ```docker-compose up```. docker compose must be installed 
  locally [https://docs.docker.com/compose/install/]().
* The service is accessing on port 7000

## Local setup using maven
* Install and start redis
* Clone repository: [git clone https://github.com/dubic/rate-limiter.git]()
* Go to rate-limiter root directory
* Run with: ```./mvnw spring-boot:run```
* The service is accessing on port 7000

## Accessing the service
3 clients with their corresponding limits will be 
created when the service starts. To change the clients data, 
modify the clients in the main class 
```com.dubic.ratelimiter.RateLimiterApplication```.

The class definition for a client is thus:
```java
public record Client(String clientId, String name, Long monthlyMax, Long minuteMax){}
```

```java
@Override
    public void run(String... args) throws Exception {
        this.clientService.createClient(new Client("clientA", "Client A", 10L, 10L));
        this.clientService.createClient(new Client("clientB", "Client B", 30L, 5L));
        this.clientService.createClient(new Client("clientC", "Client C", 20L, 3L));
    }
```

Call the endpoint using curl ```curl -v -H "Content-Type: application/json" -H 
"clientId: clientA" -d "{\"subject\":\"testing rate limiter\"}" 
http://localhost:7000/ratelimited```.
The corresponding client's id must be passed in header ```clientId```

### successful response from service
```
*   Trying 127.0.0.1:7000...
* Connected to localhost (127.0.0.1) port 7000 (#0)
> POST /ratelimited HTTP/1.1
> Host: localhost:7000
> User-Agent: curl/8.0.1
> Accept: */*
> Content-Type: application/json
> clientId: clientA
> Content-Length: 34
>
< HTTP/1.1 200
< X-rate-monthly-requests: 1
< X-rate-monthly-requests-remaining: 9
< Content-Type: text/plain;charset=UTF-8
< Content-Length: 10
< Date: Tue, 31 Oct 2023 09:25:09 GMT
<
Successful*
```
```X-rate-monthly-requests``` and ```X-rate-monthly-requests-remaining```
response headers indicate the number of requests sent by the client
for the current month, and the number of requests remaining.

## Run the tests locally
* Install and start redis
* Clone repository: [git clone https://github.com/dubic/rate-limiter.git]()
* From ratelimiter root, run: ```./mvnw clean test```

There are 3 tests defined in ```com.dubic.ratelimiter.RateLimiterApplicationTests```.

1. ```void monthlyRequestsExceeded()``` tests the monthly cap/limit for a client. If exceeded, a 
   429 code is returned.
2. ```void perMinuteRequestsExceeded()``` tests that if a client exceeds the assigned request 
   limit for a minute (Time window chosen as minute for simplicity), a 202 response is sent.
3. ```void systemPerMinuteRequestsExceeded()``` tests that if the system requests limit is 
   exceeded, a 202 is returned. Here also, the endpoint is called simultaneously using 3 threads 
   for the 3 clients.