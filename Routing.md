# Routing in Web Applications - Senior Software Engineer & Architect Interview Guide

**Focus: PHP, Python, Java**

---

# 1. What is Routing?

## Interview Question

**What is routing in web applications?**

## Answer

Routing is the mechanism that maps an incoming HTTP request to the appropriate handler in an application.

When a client sends a request such as:

```
GET /users/123
```

the routing layer determines:

* Which controller/function should execute.
* Which parameters should be extracted.
* Which middleware should run.
* Which HTTP methods are allowed.

Without routing, every request would have to be manually inspected.

---

# Request Lifecycle

```
Client
   ↓
Web Server (Nginx/Apache)
   ↓
Application Entry Point
   ↓
Router
   ↓
Middleware
   ↓
Controller/Handler
   ↓
Business Logic
   ↓
Response
```

---

# 2. How Does Routing Work Internally?

## Interview Question

**Explain how routing works internally.**

## Answer

Most frameworks follow these steps:

### Step 1: Receive Request

Example:

```
GET /products/10
```

Framework extracts:

```
Method = GET
Path = /products/10
```

---

### Step 2: Build Route Table

Routes are registered:

```
GET /products
GET /products/{id}
POST /products
DELETE /products/{id}
```

Internally stored as:

```
[
    ("GET", "/products"),
    ("GET", "/products/{id}"),
    ("POST", "/products"),
    ("DELETE", "/products/{id}")
]
```

---

### Step 3: Match Route

Router checks:

```
Method == GET ?
Path matches ?
```

Example:

```
GET /products/10
```

matches

```
GET /products/{id}
```

Extracted:

```
id = 10
```

---

### Step 4: Execute Middleware

Examples:

```
Authentication
Authorization
Logging
Rate Limiting
CORS
```

---

### Step 5: Invoke Controller

Example:

```
ProductController.getProduct(10)
```

---

### Step 6: Return Response

Example:

```json
{
    "id": 10,
    "name": "iPhone"
}
```

---

# 3. How Are Routes Stored?

## Interview Question

**How are routes stored internally?**

## Answer

There are three major approaches.

---

## Linear Search

```
for route in routes:
    if match(route):
        return route
```

Complexity:

```
O(N)
```

Used by:

* Small frameworks
* Simple routers

---

## Hash Map

Stored by HTTP method.

Example:

```
GET:
    /users
    /health

POST:
    /login
```

Complexity:

```
O(1)
```

for static routes.

---

## Trie (Prefix Tree)

Used by high-performance frameworks.

Example:

```
/
├── users
│   └── :id
└── products
    └── :id
```

Request:

```
GET /users/10
```

Traversal:

```
/
→ users
→ :id
```

Complexity:

```
O(path segments)
```

Used by:

* FastAPI
* Gin
* Echo
* Many API gateways

---

# 4. Static vs Dynamic Routes

## Interview Question

**What are static and dynamic routes?**

## Answer

### Static Routes

Fixed paths.

Example:

```
GET /health
GET /login
```

Fast lookup.

---

### Dynamic Routes

Contain variables.

Example:

```
GET /users/{id}
GET /orders/{orderId}
```

Parameters extracted during matching.

Example:

```
/users/42

id = 42
```

---

# PHP Routing

# 5. How Routing Works in PHP

## Interview Question

**Explain routing in PHP.**

## Answer

Traditional PHP:

```
Apache
↓
index.php
↓
if-else
↓
include files
```

Example:

```php
$uri = $_SERVER['REQUEST_URI'];

if ($uri == '/users') {
    getUsers();
} elseif (preg_match('/\/users\/(\d+)/', $uri, $matches)) {
    getUser($matches[1]);
}
```

Problems:

* Hard to maintain
* Not scalable
* Slow with many routes

---

# Laravel Routing

Example:

```php
Route::get('/users', [UserController::class, 'index']);

Route::get('/users/{id}', [UserController::class, 'show']);

Route::post('/users', [UserController::class, 'store']);
```

Internally:

```
Bootstrap
↓
RouteServiceProvider
↓
Compiled Route Collection
↓
Middleware
↓
Controller
```

---

## Laravel Route Caching

Interview Favorite.

Command:

```bash
php artisan route:cache
```

What happens?

Laravel converts routes into a serialized optimized array.

Instead of:

```
Parsing route files every request
```

it uses:

```
bootstrap/cache/routes.php
```

Benefits:

* Faster startup
* Lower CPU

Used heavily in production.

---

# Python Routing

# 6. How Routing Works in Python

Frameworks:

* Django
* Flask
* FastAPI

---

# Flask Routing

Example:

```python
@app.route('/users/<int:id>')
def get_user(id):
    return {"id": id}
```

Internally:

```
Werkzeug Router
↓
Rule Objects
↓
Regex Matching
↓
View Function
```

Example regex:

```
/users/<int:id>

↓

^/users/([0-9]+)$
```

---

# Django Routing

Example:

```python
urlpatterns = [
    path('users/<int:id>/', views.user)
]
```

Internally:

```
Request
↓
URL Resolver
↓
Regex Pattern Matching
↓
View
```

---

# FastAPI Routing

Example:

```python
@app.get("/users/{id}")
async def get_user(id: int):
    return {"id": id}
```

Internally:

```
Starlette Router
↓
Trie-like matching
↓
Dependency Injection
↓
Handler
```

Why FastAPI is fast:

* ASGI
* Async support
* Efficient routing

---

# Java Routing

# 7. How Routing Works in Java

Frameworks:

* Spring MVC
* Spring Boot
* JAX-RS

---

# Spring MVC Routing

Example:

```java
@GetMapping("/users/{id}")
public User getUser(@PathVariable Long id) {
    return userService.find(id);
}
```

Internally:

```
DispatcherServlet
↓
HandlerMapping
↓
HandlerAdapter
↓
Controller
↓
Response
```

---

# Spring MVC Flow

```
Client
↓
DispatcherServlet
↓
RequestMappingHandlerMapping
↓
RequestMappingHandlerAdapter
↓
Controller
↓
HttpMessageConverter
↓
Response
```

---

# Handler Mapping

Spring scans:

```java
@RestController
@RequestMapping("/users")
```

and builds mappings:

```
GET /users/{id}
POST /users
PUT /users/{id}
```

Stored during startup.

No reflection during every request.

---

# Spring Path Matching

Older versions:

```
AntPathMatcher
```

Example:

```
/users/*
/users/**
```

---

Newer versions:

```
PathPatternParser
```

Benefits:

* Better memory usage
* Faster matching
* Lower GC pressure

Architect-level discussion point.

---

# 8. Routing in Microservices

## Interview Question

**How does routing work in microservices?**

## Answer

Routing happens at multiple levels.

```
Client
↓
API Gateway
↓
Service Discovery
↓
Microservice Router
↓
Controller
```

Example:

```
GET /payments/10
```

Gateway:

```
payments.company.com
↓
Payment Service
↓
PaymentController
```

---

API Gateway Responsibilities:

* Authentication
* Rate limiting
* SSL termination
* Load balancing
* Request routing

Examples:

* Kong
* NGINX
* Spring Cloud Gateway
* Envoy

---

# 9. Routing vs Load Balancing

## Interview Question

**Difference between routing and load balancing?**

## Answer

| Routing                                | Load Balancing               |
| -------------------------------------- | ---------------------------- |
| Chooses destination service            | Chooses instance             |
| Based on URL/path                      | Based on server availability |
| Application concern                    | Infrastructure concern       |
| Example: `/payments` → Payment Service | Payment Service → Server-2   |
| Layer 7 mostly                         | Layer 4 or Layer 7           |

---

# 10. Route Parameters vs Query Parameters

## Interview Question

**Difference between route parameters and query parameters?**

## Answer

Route Parameter:

```
GET /users/42
```

Represents:

```
Specific resource.
```

Example:

```
id = 42
```

---

Query Parameter:

```
GET /users?page=2&size=10
```

Represents:

```
Filtering or pagination.
```

Example:

```
page = 2
size = 10
```

---

# 11. Middleware and Routing

## Interview Question

**How do middleware and routing work together?**

## Answer

Execution order:

```
Request
↓
Global Middleware
↓
Route Middleware
↓
Controller
↓
Response Middleware
↓
Response
```

Examples:

Global:

```
Logging
CORS
```

Route-specific:

```
Authentication
Admin authorization
```

---

# 12. Architect-Level Questions

## Q: Why not use regex for all routes?

### Answer

Regex matching every request becomes expensive.

```
O(number of routes)
```

Trie structures provide:

```
O(path depth)
```

and better scalability.

---

## Q: Why compile routes at startup?

### Answer

Avoids:

```
Reflection
Annotation scanning
Regex generation
```

during requests.

Improves:

* Throughput
* Startup predictability
* Lower latency

---

## Q: How would you design routing for 100,000 APIs?

### Answer

Use:

* Trie-based lookup
* Route compilation
* Sharding by HTTP method
* API Gateway
* Service discovery
* Route caching
* Canary routing support

---

## Q: What is canary routing?

### Answer

A percentage of traffic is routed to a newer version.

Example:

```
90% → v1
10% → v2
```

Used for safe deployments.

---

# Senior Engineer Quick Revision

Remember:

```
PHP (Laravel)
→ Route Cache
→ Middleware
→ Route Collection

Python (Flask)
→ Werkzeug
→ Regex Matching

Python (FastAPI)
→ Starlette Router
→ ASGI
→ Async

Java (Spring)
→ DispatcherServlet
→ HandlerMapping
→ HandlerAdapter
→ Controller

Routing Algorithms
→ Linear Search
→ Hash Map
→ Trie

Architecture
→ API Gateway
→ Service Discovery
→ Canary Routing
→ Load Balancing
```

---

# One-Line Interview Summary

"Routing is the process of efficiently mapping an incoming HTTP request to the correct handler using optimized lookup structures, middleware pipelines, and framework-specific dispatch mechanisms while ensuring scalability, maintainability, and low latency."


# AWS Route 53, ALB, Apache & PHP Routing - Senior Software Engineer / Architect Interview Guide

## Real Production Request Flow

Most senior engineers stop at framework routing:

```text
Laravel Router
↓
Controller
```

However, architect interviews focus on the entire request lifecycle.

### Complete Request Journey

```text
User Browser
    ↓
DNS Resolution (Route 53)
    ↓
Application Load Balancer (ALB)
    ↓
Target Group
    ↓
EC2 Instance
    ↓
Apache
    ↓
PHP-FPM
    ↓
Laravel public/index.php
    ↓
Laravel Router
    ↓
Middleware
    ↓
Controller
    ↓
Business Logic
    ↓
Response
    ↓
Browser
```

---

# Q1. Explain the complete lifecycle of a request in your production environment.

## Answer

Suppose a user accesses:

```text
https://amstaging.example.com/tests/123
```

The flow is:

```text
Browser
↓
Route 53 resolves the domain
↓
ALB receives HTTPS request
↓
ALB evaluates listener rules
↓
Request forwarded to Target Group
↓
Healthy EC2 instance selected
↓
Apache receives request
↓
Apache rewrites request to index.php
↓
PHP-FPM executes PHP
↓
Laravel Router matches route
↓
Middleware executes
↓
Controller invoked
↓
Response generated
↓
Returned to Browser
```

---

# Q2. Is Route 53 part of routing or hosting?

## Answer

Route 53 is part of routing.

Specifically, it performs **DNS-level routing**.

It is not a hosting service.

### Multiple Layers of Routing

| Layer               | Component | Responsibility               |
| ------------------- | --------- | ---------------------------- |
| DNS Routing         | Route 53  | Decide destination endpoint  |
| Application Routing | ALB       | Route requests to services   |
| Web Server Routing  | Apache    | Rewrite and forward requests |
| Framework Routing   | Laravel   | Invoke controllers           |

---

# Q3. What is Route 53?

## Answer

Route 53 is AWS's managed DNS service.

Responsibilities include:

* Domain registration
* DNS resolution
* Health checks
* Traffic routing
* Failover
* Global traffic distribution

Example:

```text
amstaging.example.com
        ↓
internal-alb.ap-south-1.elb.amazonaws.com
```

Route 53 never handles HTTP requests.

It only tells clients where to send them.

---

# Q4. How does Route 53 work internally?

## Answer

When a browser requests:

```text
https://amstaging.example.com
```

the following occurs:

```text
Browser
↓
Local DNS Cache
↓
ISP Recursive Resolver
↓
Route 53 Name Servers
↓
ALB DNS Name Returned
↓
ALB IP Address Resolved
↓
Browser connects to ALB
```

---

# Q5. What routing policies does Route 53 support?

## Answer

## Simple Routing

Single endpoint.

```text
example.com
↓
ALB
```

---

## Weighted Routing

Traffic split.

Example:

```text
90% → ALB V1
10% → ALB V2
```

Used for:

* Canary releases
* A/B testing

---

## Latency Routing

Route users to the closest AWS region.

Example:

```text
India Users
↓
Mumbai Region

US Users
↓
Virginia Region
```

---

## Failover Routing

Primary:

```text
Mumbai
```

Secondary:

```text
Singapore
```

If health checks fail:

```text
Automatically switch traffic.
```

---

## Geolocation Routing

Example:

```text
India
↓
Indian Site

Europe
↓
European Site
```

---

# Q6. What is an Application Load Balancer (ALB)?

## Answer

ALB is AWS's Layer 7 load balancer.

It understands HTTP and HTTPS traffic.

Responsibilities:

* Load balancing
* Host-based routing
* Path-based routing
* SSL termination
* WebSocket support
* Health checks
* Sticky sessions

---

# Q7. How does ALB routing work?

## Answer

ALB evaluates listener rules sequentially.

Example:

```text
IF Host = api.example.com
THEN API Target Group

ELSE IF Path = /admin/*
THEN Admin Target Group

ELSE
Default Target Group
```

Request:

```text
GET /admin/users
```

Flow:

```text
ALB
↓
Listener Rules Evaluated
↓
Admin Target Group Selected
↓
Healthy EC2 Chosen
```

---

# Q8. Difference between Route 53 and ALB.

## Answer

| Route 53              | ALB                    |
| --------------------- | ---------------------- |
| DNS Service           | Load Balancer          |
| Global                | Regional               |
| Returns endpoint      | Forwards requests      |
| No HTTP understanding | Understands HTTP/HTTPS |
| Layer 3 DNS           | Layer 7                |
| Weighted DNS routing  | Path and host routing  |

---

# Q9. What is a Target Group?

## Answer

A Target Group is a logical collection of backend servers.

Example:

```text
PHP-App-TG
    ↓
EC2-1
EC2-2
EC2-3
```

ALB forwards traffic only to healthy targets.

Supported targets:

* EC2 Instances
* IP Addresses
* Lambda Functions

---

# Q10. How does ALB choose an EC2 instance?

## Answer

Process:

```text
Request arrives
↓
Listener Rule Match
↓
Target Group Selected
↓
Health Status Checked
↓
Load Balancing Algorithm Applied
↓
Instance Selected
```

ALB uses:

```text
Least Outstanding Requests
```

instead of simple round robin.

This improves utilization under uneven workloads.

---

# Q11. How do ALB health checks work?

## Answer

ALB periodically calls:

```text
GET /health
```

Expected response:

```text
HTTP 200
```

Example:

```text
Healthy Threshold: 5
Unhealthy Threshold: 2
Interval: 30 seconds
```

If failures exceed threshold:

```text
Instance marked unhealthy
↓
Traffic stopped
```

---

# Q12. What is Apache's role?

## Answer

Apache acts as the web server.

Responsibilities:

* Receive requests from ALB
* Serve static assets
* Rewrite URLs
* Forward PHP requests to PHP-FPM
* Manage virtual hosts
* Apply security rules

Flow:

```text
ALB
↓
Apache
↓
PHP-FPM
↓
Laravel
```

---

# Q13. Why is RewriteRule required in Laravel?

## Answer

Laravel defines routes in PHP.

Apache does not understand:

```text
/assessments/123
/users/45
/tests/start
```

Without RewriteRule:

```text
Apache searches for physical files.
```

Result:

```text
404 Not Found
```

With RewriteRule:

```apache
RewriteEngine On

RewriteCond %{REQUEST_FILENAME} !-f
RewriteCond %{REQUEST_FILENAME} !-d
RewriteRule ^ index.php [L]
```

Flow becomes:

```text
Request
↓
index.php
↓
Laravel Router
↓
Controller
```

---

# Q14. Why use Apache + PHP-FPM instead of mod_php?

## Answer

mod_php:

```text
Apache Process
↓
Embedded PHP Interpreter
```

Problems:

* Higher memory usage
* Poor process isolation
* Difficult scaling

PHP-FPM:

```text
Apache
↓
FastCGI
↓
PHP Worker Pool
```

Benefits:

* Better resource utilization
* Independent scaling
* Improved performance
* Process management

---

# Q15. Where is SSL terminated in your architecture?

## Answer

Typically:

```text
Browser
↓ HTTPS
ALB
↓ HTTP
Apache
```

ALB decrypts SSL traffic.

Benefits:

* Centralized certificate management
* Lower EC2 CPU utilization
* Easier certificate rotation

Sometimes SSL is re-encrypted:

```text
Browser
↓ HTTPS
ALB
↓ HTTPS
Apache
```

Used for stricter compliance requirements.

---

# Q16. What happens if one EC2 instance goes down?

## Answer

Flow:

```text
ALB Health Check Fails
↓
Instance Marked Unhealthy
↓
Removed From Rotation
↓
Traffic Sent To Healthy Instances
```

Users generally experience no downtime.

---

# Q17. How would you perform a canary deployment?

## Answer

Option 1: Route 53 Weighted Routing

```text
90% → Old ALB
10% → New ALB
```

Option 2: ALB Weighted Target Groups

```text
95% → Target Group V1
5% → Target Group V2
```

Monitor:

* Error rates
* Latency
* CPU utilization

Gradually increase traffic.

---

# Q18. What if Route 53 fails?

## Answer

Route 53 is globally distributed.

Additionally:

```text
DNS responses are cached.
```

Therefore:

* Existing users continue to function.
* Only fresh DNS resolutions may be impacted.

AWS Route 53 provides very high availability.

---

# Q19. Explain the difference between DNS failover and ALB failover.

## Answer

## DNS Failover

Handled by Route 53.

Example:

```text
Mumbai Region
↓
Unavailable
↓
Singapore Region
```

Traffic shifts across regions.

---

## ALB Failover

Handled within a region.

Example:

```text
EC2-1 fails
↓
Traffic shifts to EC2-2
```

No DNS changes required.

---

# Q20. Architect-Level Summary

The complete routing stack in production is:

```text
Route 53
    ↓
Application Load Balancer
    ↓
Target Group
    ↓
EC2
    ↓
Apache
    ↓
PHP-FPM
    ↓
Laravel Router
    ↓
Middleware
    ↓
Controller
```

# Java Application Routing, Deployment & Nginx - Senior Engineer / Architect Interview Guide

## Typical Java Production Architecture

Unlike PHP, Java applications are usually deployed as a build artifact.

Examples:

* JAR (Spring Boot)
* WAR (Tomcat/JBoss/WebLogic)

Most modern systems use Spring Boot executable JARs.

A typical request flow looks like this:

```text
User Browser
    ↓
Route 53
    ↓
Application Load Balancer (ALB)
    ↓
Target Group
    ↓
EC2 Instance
    ↓
Nginx
    ↓
Spring Boot Application
    ↓
DispatcherServlet
    ↓
HandlerMapping
    ↓
Controller
    ↓
Service
    ↓
Database
    ↓
Response
```

---

# Q1. Explain the complete request lifecycle in your Java application.

## Answer

Suppose a user accesses:

```text
https://api.example.com/users/123
```

The flow is:

```text
Browser
↓
Route 53 resolves DNS
↓
ALB receives HTTPS request
↓
ALB evaluates listener rules
↓
Target Group selected
↓
Healthy EC2 chosen
↓
Nginx receives request
↓
Nginx proxies request to Spring Boot
↓
DispatcherServlet receives request
↓
HandlerMapping finds controller
↓
Controller executes
↓
Business logic invoked
↓
Response returned
↓
Nginx
↓
ALB
↓
Browser
```

---

# Q2. How is a Java application deployed?

## Answer

During CI/CD:

### Step 1: Source Code

```text
Git Repository
```

Example:

```text
Spring Boot Project
```

---

### Step 2: Build

Using Maven:

```bash
mvn clean package
```

or Gradle:

```bash
gradle build
```

Produces:

```text
target/app.jar
```

or

```text
build/libs/app.jar
```

---

### Step 3: Deployment

Artifact copied to EC2:

```text
scp app.jar EC2
```

or through:

```text
Jenkins
Bamboo
GitHub Actions
CodeDeploy
```

---

### Step 4: Start Application

Example:

```bash
java -jar app.jar
```

or:

```bash
nohup java -jar app.jar &
```

Usually managed using:

```text
systemd
supervisord
Docker
Kubernetes
```

---

# Q3. Why is Nginx used in front of Spring Boot?

## Answer

Spring Boot can directly serve requests.

Example:

```text
Browser
↓
Spring Boot
```

However, production systems commonly use:

```text
Browser
↓
Nginx
↓
Spring Boot
```

Benefits:

* Reverse proxy
* SSL termination
* Static file serving
* Compression
* Rate limiting
* Request buffering
* Connection management
* Load balancing

---

# Q4. What is a reverse proxy?

## Answer

A reverse proxy receives requests on behalf of backend servers.

Flow:

```text
Client
↓
Nginx
↓
Spring Boot
```

The client never directly accesses the Java application.

Benefits:

* Hides backend topology
* Improves security
* Enables scaling
* Simplifies SSL

---

# Q5. Explain Nginx routing.

## Answer

Example:

```nginx
server {
    listen 80;

    location /api/ {
        proxy_pass http://localhost:8080;
    }

    location /admin/ {
        proxy_pass http://localhost:8081;
    }
}
```

Request:

```text
GET /api/users
```

Flow:

```text
Nginx
↓
Matches /api/
↓
localhost:8080
```

---

# Q6. Explain Spring Boot routing internally.

## Answer

Example Controller:

```java
@RestController
@RequestMapping("/users")
public class UserController {

    @GetMapping("/{id}")
    public User getUser(@PathVariable Long id) {
        return service.get(id);
    }
}
```

Internally:

```text
Spring Startup
↓
Scan Controllers
↓
Build Handler Mapping Table
↓
DispatcherServlet
↓
HandlerMapping
↓
Controller
```

---

# Q7. What is DispatcherServlet?

## Answer

DispatcherServlet is Spring MVC's Front Controller.

Every request passes through it.

Flow:

```text
Request
↓
DispatcherServlet
↓
HandlerMapping
↓
HandlerAdapter
↓
Controller
↓
View/JSON Response
```

Responsibilities:

* Route requests
* Invoke interceptors
* Handle exceptions
* Convert responses

---

# Q8. How does Spring find the correct controller?

## Answer

During startup, Spring scans:

```java
@RestController
@GetMapping
@PostMapping
```

and builds mappings.

Example:

```text
GET /users/{id}
POST /users
DELETE /users/{id}
```

Stored in memory.

During requests:

```text
DispatcherServlet
↓
Lookup Mapping
↓
Invoke Controller
```

No controller scanning happens per request.

---

# Q9. What happens when you deploy a new build?

## Answer

Typical flow:

```text
Developer
↓
Git Push
↓
CI Pipeline Triggered
↓
Run Tests
↓
Build JAR
↓
Upload Artifact
↓
Deploy to EC2
↓
Restart Application
↓
Health Checks
↓
Traffic Enabled
```

---

# Q10. How can deployments occur without downtime?

## Answer

Using ALB and multiple EC2 instances.

Example:

```text
Instance-1
Instance-2
Instance-3
```

Deployment:

```text
Remove Instance-1
↓
Deploy New Build
↓
Health Check Passes
↓
Add Back To ALB
↓
Repeat
```

Called:

```text
Rolling Deployment
```

---

# Q11. Explain Blue-Green Deployment.

## Answer

Environment:

```text
Blue → Production
Green → New Version
```

Flow:

```text
Deploy Green
↓
Run Validation
↓
Switch ALB Traffic
↓
Blue Becomes Backup
```

Benefits:

* Near-zero downtime
* Easy rollback

---

# Q12. Explain Canary Deployment.

## Answer

Traffic split:

```text
95%
↓
Old Version

5%
↓
New Version
```

Monitor:

* Errors
* CPU
* Memory
* Latency

Gradually increase traffic.

Possible through:

```text
ALB Weighted Target Groups
Route 53 Weighted Routing
Kubernetes
```

---

# Q13. Why use ALB if Nginx can load balance?

## Answer

Nginx can load balance.

However:

## ALB

Provides:

* Managed service
* Multi-AZ availability
* AWS integration
* Health checks
* SSL certificates via ACM
* Target Groups

---

## Nginx

Provides:

* Reverse proxy
* Local routing
* Fine-grained control
* Request manipulation

Often both are used.

Architecture:

```text
Route 53
↓
ALB
↓
Nginx
↓
Spring Boot
```

---

# Q14. Why not expose Spring Boot directly?

## Answer

Possible:

```text
ALB
↓
Spring Boot
```

But Nginx provides:

* Better connection handling
* Static asset serving
* Request buffering
* Compression
* Security headers
* Rate limiting

Therefore many organizations keep Nginx.

---

# Q15. Where is SSL terminated?

## Answer

Most common:

```text
Browser
↓ HTTPS
ALB
↓ HTTP
Nginx
↓ HTTP
Spring Boot
```

Alternative:

```text
Browser
↓ HTTPS
ALB
↓ HTTPS
Nginx
↓ HTTPS
Spring Boot
```

Used for compliance.

---

# Q16. What happens if a Spring Boot instance crashes?

## Answer

Flow:

```text
ALB Health Check Fails
↓
Target Marked Unhealthy
↓
Traffic Redirected
↓
Auto Scaling Launches Replacement
```

Users continue to receive service.

---

# Q17. What are the health checks in this architecture?

## Answer

Example endpoint:

```java
GET /actuator/health
```

Response:

```json
{
    "status": "UP"
}
```

Flow:

```text
ALB
↓
Nginx
↓
Spring Boot
↓
Actuator
```

Healthy instances continue receiving traffic.

---

# Q18. Architect-Level Comparison: PHP vs Java Routing

| PHP (Laravel)                   | Java (Spring Boot)                 |
| ------------------------------- | ---------------------------------- |
| Apache/Nginx → PHP-FPM          | Nginx → JVM                        |
| index.php Front Controller      | DispatcherServlet Front Controller |
| Route Cache                     | Handler Mapping Cache              |
| PHP Worker Processes            | JVM Threads                        |
| Routes loaded from PHP          | Routes scanned at startup          |
| Controller executed per request | Controller executed per request    |

---

# Q19. Architect-Level Production Flow

## PHP

```text
Route 53
↓
ALB
↓
Apache
↓
PHP-FPM
↓
Laravel Router
↓
Controller
```

---

## Java

```text
Route 53
↓
ALB
↓
Nginx
↓
Spring Boot
↓
DispatcherServlet
↓
HandlerMapping
↓
Controller
```

---

# FastAPI Production Routing, Deployment & Nginx - Senior Engineer / Architect Interview Guide

## Typical FastAPI Production Architecture

Unlike PHP, FastAPI applications run as long-lived processes using an ASGI server.

Most production deployments use:

* FastAPI
* Uvicorn
* Gunicorn + Uvicorn Workers
* Nginx
* AWS ALB
* Route 53

Typical architecture:

```text
User Browser
    ↓
Route 53
    ↓
Application Load Balancer (ALB)
    ↓
Target Group
    ↓
EC2 Instance
    ↓
Nginx
    ↓
Gunicorn
    ↓
Uvicorn Workers
    ↓
FastAPI
    ↓
Starlette Router
    ↓
Dependency Injection
    ↓
Endpoint Function
    ↓
Database/Services
    ↓
Response
```

---

# Q1. Explain the complete request lifecycle in a FastAPI application.

## Answer

Suppose a client calls:

```text
GET https://api.example.com/users/123
```

The flow is:

```text
Browser
↓
Route 53 resolves DNS
↓
ALB receives HTTPS request
↓
ALB evaluates listener rules
↓
Target Group selected
↓
Healthy EC2 selected
↓
Nginx receives request
↓
Nginx proxies request
↓
Gunicorn receives request
↓
Uvicorn Worker handles request
↓
FastAPI application invoked
↓
Starlette Router matches route
↓
Dependencies resolved
↓
Endpoint executed
↓
Response returned
↓
Nginx
↓
ALB
↓
Browser
```

---

# Q2. How is FastAPI deployed?

## Answer

FastAPI is not deployed like PHP.

It runs as a continuously running process.

Typical deployment flow:

```text
Git Repository
↓
CI/CD Pipeline
↓
Run Unit Tests
↓
Build Artifact
↓
Copy Code to EC2
↓
Install Dependencies
↓
Start Gunicorn/Uvicorn
↓
Nginx Health Checks
↓
ALB Health Checks
↓
Traffic Enabled
```

---

# Q3. What artifact is deployed in FastAPI?

## Answer

Unlike Java:

```text
Java
↓
JAR/WAR
```

FastAPI usually deploys:

```text
Application Source Code
+
requirements.txt
```

or

```text
Docker Image
```

Examples:

```bash
pip install -r requirements.txt
```

or

```bash
docker pull company/fastapi-app:v1
```

---

# Q4. Why is Nginx used in front of FastAPI?

## Answer

FastAPI can directly serve traffic:

```text
Client
↓
Uvicorn
```

However, production systems usually use:

```text
Client
↓
Nginx
↓
Gunicorn/Uvicorn
```

Benefits:

* SSL termination
* Reverse proxy
* Compression
* Rate limiting
* Static file serving
* Connection buffering
* Request size limits
* Security headers

---

# Q5. Why not expose Uvicorn directly?

## Answer

Uvicorn is an ASGI server.

It is excellent at handling requests but lacks production features.

Nginx provides:

```text
Connection Management
SSL
Security
Buffering
Request Filtering
```

Hence:

```text
Nginx
↓
Uvicorn
```

is preferred.

---

# Q6. What is ASGI?

## Answer

ASGI stands for:

```text
Asynchronous Server Gateway Interface
```

It is the successor to WSGI.

WSGI:

```text
One request
↓
One worker
↓
Synchronous
```

ASGI:

```text
Multiple concurrent requests
↓
Async support
↓
WebSockets
↓
Background tasks
```

FastAPI is built on ASGI.

---

# Q7. What is Uvicorn?

## Answer

Uvicorn is an ASGI server.

Responsibilities:

* Accept requests
* Run event loop
* Execute FastAPI application
* Return responses

Example:

```bash
uvicorn main:app --host 0.0.0.0 --port 8000
```

Flow:

```text
Client
↓
Uvicorn
↓
FastAPI
```

---

# Q8. Why use Gunicorn with Uvicorn?

## Answer

Uvicorn alone:

```text
Single Process
```

Gunicorn provides:

```text
Process Management
Multiple Workers
Graceful Restart
Worker Monitoring
```

Architecture:

```text
Nginx
↓
Gunicorn Master
↓
Uvicorn Worker 1
Uvicorn Worker 2
Uvicorn Worker 3
```

Example:

```bash
gunicorn main:app \
-k uvicorn.workers.UvicornWorker \
-w 4
```

---

# Q9. How does FastAPI routing work internally?

## Answer

Example:

```python
@app.get("/users/{id}")
async def get_user(id: int):
    return {"id": id}
```

Internally:

```text
Application Startup
↓
Register Routes
↓
Build Router Tree
↓
Receive Request
↓
Match Path
↓
Extract Parameters
↓
Resolve Dependencies
↓
Execute Endpoint
```

---

# Q10. What router does FastAPI use?

## Answer

FastAPI uses:

```text
Starlette Router
```

Starlette provides:

* Route matching
* Middleware execution
* Parameter extraction
* Request dispatching

FastAPI adds:

* Validation
* Dependency Injection
* OpenAPI generation

---

# Q11. How are path parameters extracted?

## Answer

Example:

```python
@app.get("/users/{id}")
async def get_user(id: int):
    pass
```

Request:

```text
GET /users/123
```

Extraction:

```text
id = 123
```

Validation:

```text
String → Integer Conversion
```

Invalid input:

```text
GET /users/abc
```

Response:

```text
422 Unprocessable Entity
```

---

# Q12. What is Dependency Injection in FastAPI?

## Answer

Example:

```python
def get_db():
    return Session()

@app.get("/users")
def users(db=Depends(get_db)):
    pass
```

Flow:

```text
Request
↓
Dependency Resolver
↓
Database Session Created
↓
Endpoint Executed
↓
Cleanup
```

Benefits:

* Reusability
* Testability
* Separation of concerns

---

# Q13. How does Nginx route FastAPI traffic?

## Answer

Example:

```nginx
server {
    listen 80;

    location / {
        proxy_pass http://127.0.0.1:8000;
    }
}
```

Flow:

```text
Request
↓
Nginx
↓
localhost:8000
↓
Gunicorn
↓
FastAPI
```

---

# Q14. How does deployment happen without downtime?

## Answer

Suppose:

```text
EC2-1
EC2-2
EC2-3
```

Deployment:

```text
Remove EC2-1 from ALB
↓
Deploy New Version
↓
Restart Gunicorn
↓
Health Check Passes
↓
Add Back to ALB
↓
Repeat
```

This is called:

```text
Rolling Deployment
```

---

# Q15. What happens when Gunicorn restarts?

## Answer

Gunicorn supports:

```text
Graceful Restart
```

Process:

```text
Start New Workers
↓
Allow Existing Requests to Finish
↓
Stop Old Workers
```

Result:

```text
Minimal downtime
```

---

# Q16. What health checks are used?

## Answer

Typical endpoint:

```python
@app.get("/health")
async def health():
    return {"status": "UP"}
```

Flow:

```text
ALB
↓
Nginx
↓
Gunicorn
↓
FastAPI
↓
Health Endpoint
```

Expected:

```text
HTTP 200
```

---

# Q17. What happens if a worker crashes?

## Answer

Example:

```text
Gunicorn
↓
Worker 2 crashes
```

Gunicorn:

```text
Detects Failure
↓
Starts New Worker
```

Traffic continues.

If all workers fail:

```text
Health Checks Fail
↓
ALB Removes Instance
```

---

# Q18. Explain FastAPI async execution.

## Answer

Example:

```python
@app.get("/users")
async def users():
    data = await service()
    return data
```

Flow:

```text
Request
↓
Event Loop
↓
Coroutine Scheduled
↓
I/O Wait
↓
Another Request Executes
↓
Coroutine Resumes
```

Benefits:

* Better concurrency
* Efficient I/O handling
* Lower resource usage

---

# Q19. Architect-Level Comparison

| PHP                    | Java              | FastAPI                   |
| ---------------------- | ----------------- | ------------------------- |
| Apache → PHP-FPM       | Nginx → JVM       | Nginx → Gunicorn/Uvicorn  |
| index.php              | DispatcherServlet | Starlette Router          |
| Process Workers        | JVM Threads       | Async Event Loop          |
| Route Cache            | Handler Mapping   | Router Tree               |
| Request per PHP Worker | Thread Pool       | Async Coroutines          |
| Stateless Workers      | Long-running JVM  | Long-running ASGI Workers |

---

# Q20. Architect-Level Production Flow

```text
Route 53
↓
ALB
↓
Nginx
↓
Gunicorn
↓
Uvicorn Workers
↓
FastAPI
↓
Starlette Router
↓
Dependencies
↓
Endpoint
↓
Business Logic
↓
Database
```

---

