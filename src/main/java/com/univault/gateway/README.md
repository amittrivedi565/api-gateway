Api Gateway:
-
- on startup all the required services will register themselves with gateway
- gateway has a registry of registered services
- registry contains info such as  service name, port, host and meta
- it acts as a router, routes the incoming request to the required service
- two types of routes are defined public and protected
- public routes are open, don't require authentication
- protected routes require auth token, validation done by auth service
