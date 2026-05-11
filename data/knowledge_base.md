# Topic: Core Java
## Question: What is the difference between the == operator and the equals() method?
Answer: The == operator compares object references (whether they point to the same memory location), whereas the equals() method is used to compare the values (state) of objects. For primitive types, the == operator compares their values directly.

## Question: What is the difference between String, StringBuilder, and StringBuffer?
Answer: String is immutable — every modifying operation creates a new object. StringBuilder and StringBuffer are mutable. StringBuffer is thread-safe due to internal synchronisation, whereas StringBuilder is not synchronised and therefore faster in single-threaded environments.

## Question: What is the Garbage Collector and how does it work?
Answer: The Garbage Collector (GC) is Java's automatic memory management mechanism. It runs in the background and automatically finds and removes from heap memory any objects that no longer have live references in the program, thereby freeing resources.

## Question: What is the String Pool in Java?
Answer: The String Pool is a special area in heap memory where string literals are stored. When a new String is created using a literal (e.g. String s = "test"), the JVM first checks the String Pool. If that string already exists there, a reference to it is returned, saving memory.

## Question: What are lambda expressions introduced in Java 8?
Answer: Lambda expressions are a concise way to write an anonymous method. They allow code to be treated as data and behaviour (functions) to be passed as arguments to methods. Their syntax is typically `(parameters) -> { method body }`.

## Question: What is a functional interface?
Answer: A functional interface is an interface that has exactly one abstract method (it may have multiple default or static methods). Functional interfaces serve as target types for lambda expressions and are often annotated with @FunctionalInterface.

## Question: What is the Stream API?
Answer: The Stream API (introduced in Java 8) is a tool for declarative processing of data collections. It allows building pipelines of operations (such as map, filter, reduce) that can be executed sequentially or in parallel, without modifying the original data source.

## Question: What is the difference between map() and flatMap() in the Stream API?
Answer: The map() operation transforms each element in the stream into exactly one other element (1:1 relationship). The flatMap() operation transforms each element into zero, one, or many elements (e.g. flattening a stream of lists into a single flat stream of elements from those lists).

## Question: What is the Optional class and what problem does it solve?
Answer: Optional is a container that may or may not hold a non-null value. It was introduced in Java 8 to reduce the prevalence of NullPointerExceptions by forcing the programmer to explicitly handle the case where a value is absent.

## Question: What are the access modifiers in Java and how do they differ?
Answer: Java has 4 access modifiers: 1. public (visible everywhere), 2. protected (visible within the same package and in subclasses), 3. default/package-private (no keyword — visible only within the same package), 4. private (visible only within the declaring class).

## Question: What is varargs in Java?
Answer: Varargs (variable-length arguments) is a mechanism introduced in Java 5 that allows a method to accept zero or more arguments of a specified type. It is denoted by three dots (e.g. `void method(String... args)`). Inside the method, the argument is treated as an array.

## Question: What does the synchronized keyword mean?
Answer: The synchronized keyword is used to control access to a block of code or a method in a multithreaded environment. It ensures that only one thread can execute the synchronised section at any given time, preventing data consistency issues (race conditions).

## Question: What is the difference between an Error and an Exception?
Answer: Both inherit from Throwable. An Error represents serious problems at the JVM level (e.g. OutOfMemoryError, StackOverflowError) that an application should generally not catch or handle. An Exception represents errors from which an application can recover and which it should handle.

# Topic: Object-Oriented Programming
## Question: What is encapsulation?
Answer: Encapsulation is the hiding of an object's internal state (class fields) by setting their access modifier to private and exposing them to the outside world only through public accessor methods (getters and setters). This protects data from uncontrolled modification.

## Question: What is abstraction in object-oriented programming?
Answer: Abstraction is the hiding of complex implementation details and exposing to the user only the necessary functionality. In Java it is achieved using abstract classes and interfaces, focusing on "what" an object does rather than "how" it does it.

## Question: What is the Single Responsibility Principle (SRP) from SOLID?
Answer: The SRP states that every class should have only one responsibility and only one reason to change. If a class handles business logic, database persistence, and text formatting all at once, it violates this principle.

## Question: What is the Open/Closed Principle (OCP) from SOLID?
Answer: The OCP states that classes, modules, and functions should be open for extension but closed for modification. This means we should be able to add new behaviour without changing existing, working code — for example, by using interfaces and polymorphism.

## Question: What is the difference between aggregation and composition?
Answer: Both describe a "has-a" relationship. In aggregation (weak relationship) the child object can exist independently of the parent (e.g. a Department has Professors). In composition (strong relationship) the child object has no meaning without the parent — it is destroyed along with it (e.g. a Building has Rooms).

## Question: Can you instantiate an abstract class?
Answer: No, directly instantiating an abstract class using the new keyword (e.g. `new MyAbstractClass()`) is forbidden. However, it is possible to create an anonymous class object that immediately implements the missing abstract methods inline.


# Topic: Collections
## Question: What is the difference between ArrayList and LinkedList?
Answer: ArrayList is backed by a dynamic array, providing very fast index-based access but slow insertion and removal in the middle (requires shifting elements). LinkedList is backed by a doubly-linked list structure, making insertion and removal fast (when the position is known), but index-based access slow.

## Question: What is the difference between HashMap and HashSet?
Answer: HashMap stores data as key-value pairs, where each key must be unique. HashSet stores only a collection of unique values. Under the hood, HashSet uses a HashMap to store its elements (the element is the key, and the value is a constant placeholder object).

## Question: What is the difference between the Iterable interface and Iterator?
Answer: Iterable is an interface (implemented by collections and lists) that guarantees a collection can be iterated (e.g. in a for-each loop). It has an iterator() method that returns an Iterator object. An Iterator is the "cursor" object that actually traverses the elements (via hasNext(), next(), remove()).

## Question: What is the difference between the Collection interface and the Collections class?
Answer: Collection is the primary interface (extending Iterable) in Java's collection hierarchy, from which Set, List, and Queue inherit. java.util.Collections (with a capital 's') is a utility class containing static methods for operating on collections, such as sort(), reverse(), and shuffle().

## Question: How does TreeMap work and how does it differ from HashMap?
Answer: Both implement the Map interface. HashMap guarantees no ordering of keys and is based on hashing. TreeMap is backed by a Red-Black Tree, which keeps keys always sorted (naturally or using a provided Comparator), but add/lookup operations are slower than in HashMap (O(log n) vs O(1)).

## Question: When would you use CopyOnWriteArrayList instead of ArrayList?
Answer: CopyOnWriteArrayList is used in multithreaded environments where read (iteration) operations are far more frequent than write operations. Every modification (adding/removing an element) creates a new, separate copy of the internal array. This provides thread-safety without locks, making iteration very fast but writes expensive.

## Question: What characterises a Stack and which Java class implements it?
Answer: A Stack operates on the LIFO principle (Last-In-First-Out) — the last element added is the first to be removed (like a stack of plates). Java has the legacy Stack class (which extends Vector and is synchronised), but the recommended approach today is to use the Deque interface and its implementations (e.g. ArrayDeque) to simulate stack behaviour.

# Topic: Exceptions
## Question: What is the difference between Checked and Unchecked exceptions in Java?
Answer: Checked exceptions (e.g. IOException) extend the Exception class and must be explicitly handled in code (with a try-catch block) or declared in the method signature (throws). Unchecked exceptions (e.g. NullPointerException) extend RuntimeException and do not require explicit handling — they typically indicate programming errors.

## Question: How do you create a custom exception in Java?
Answer: A custom exception is created by defining a new class that extends Exception (for a Checked exception) or RuntimeException (for an Unchecked exception). It is good practice to also implement a constructor that accepts an error message (String).

## Question: What is the difference between ClassNotFoundException and NoClassDefFoundError?
Answer: ClassNotFoundException is an exception that occurs during dynamic class loading (e.g. with Class.forName()) when the class is not found on the classpath. NoClassDefFoundError is an Error that occurs when a class was present at compile time but cannot be found at runtime for some reason.

## Question: In what order should exceptions be caught in multiple catch blocks?
Answer: Exceptions should be caught from the most specific (lowest in the inheritance hierarchy) to the most general (highest). If catch(Exception e) is placed first, it will catch everything and subsequent, more specific catch blocks become unreachable, causing a compilation error.

## Question: What is a suppressed exception?
Answer: This mechanism was introduced with try-with-resources. If an exception occurs in the main try block and then the resource-closing method (e.g. close()) throws another exception, the second exception is "suppressed" and attached to the original one. It can be retrieved using the getSuppressed() method on the original exception.

## Question: Can you throw a Checked exception without declaring it in a throws clause?
Answer: In normal, clean Java code — no, the compiler will block this. However, there are "hacks" (known as Sneaky Throws) that bypass compiler checking, for example using generics tricks, the Unsafe class, or (most commonly in practice) the @SneakyThrows annotation from Lombok.

# Topic: Spring Framework
## Question: What is Dependency Injection (DI)?
Answer: Dependency Injection is a pattern in which an object does not create its own dependencies (e.g. with the new operator) but receives them from the outside. In Spring, the IoC container handles this by injecting beans (preferably via constructor injection), which greatly simplifies testing and reduces class coupling.

## Question: What is the difference between @Component, @Service, and @Repository?
Answer: All three mark beans managed by Spring. @Component is the primary, general-purpose annotation. @Service is its specialisation for classes containing business logic. @Repository is its specialisation for data access classes (DAO) and additionally translates database exceptions into Spring's exception hierarchy (DataAccessException).

## Question: What does the @SpringBootApplication annotation do?
Answer: @SpringBootApplication is a shortcut that combines three annotations: @Configuration (marks a configuration class), @EnableAutoConfiguration (enables Spring Boot's auto-configuration based on classpath libraries), and @ComponentScan (instructs Spring to scan the current package and its sub-packages for beans).

## Question: What is the Bean lifecycle in Spring?
Answer: The Bean lifecycle includes its creation by the IoC container, dependency injection (DI), invocation of initialisation methods (e.g. annotated with @PostConstruct), availability to the application, and finally invocation of destruction methods (e.g. annotated with @PreDestroy) before the container shuts down.

## Question: What is Spring AOP (Aspect-Oriented Programming)?
Answer: AOP is aspect-oriented programming. It allows separating business logic from cross-cutting concerns such as logging, database transactions, or authorisation. AOP lets you "weave" this additional code before, after, or around the execution of main methods without modifying their body.

## Question: What is the difference between @Controller and @RestController?
Answer: @Controller is used mainly in applications that generate views (e.g. Thymeleaf) — it returns the name of the view (HTML) to be rendered. @RestController combines @Controller with @ResponseBody — it causes the data returned by methods to be automatically serialised (usually to JSON/XML) and sent directly in the HTTP response body.

## Question: What does the @Autowired annotation do in Spring?
Answer: @Autowired is used for automatic dependency injection. Spring scans the application context for a bean whose type matches the declared field, constructor, or setter method and automatically assigns the appropriate object there. In modern code, constructor injection is preferred (often supported by Lombok).

## Question: What are the main bean scopes in Spring?
Answer: The two most important are "singleton" (default — only one shared instance of a given bean exists in the Spring container) and "prototype" (a new instance is created every time the bean is requested). In a web environment, additional scopes include "request" (one bean per HTTP request) and "session" (one per HTTP session).

# Topic: Multithreading
## Question: What is the difference between extending the Thread class and implementing the Runnable interface?
Answer: Both approaches allow creating a thread. Implementing Runnable is generally preferred because Java does not support multiple class inheritance — by implementing Runnable, a class can still extend another class. Additionally, Runnable separates the task (logic) from the execution mechanism (Thread).

## Question: What is a deadlock?
Answer: A deadlock is a situation in concurrent programming where two or more threads wait indefinitely for the release of resources (locks) that they are holding from each other. Neither thread can proceed, leading to a permanent suspension of that part of the application.

## Question: What does the join() method from the Thread class do?
Answer: The join() method, called on a thread object (e.g. threadA.join()), causes the currently running thread to pause and wait until the specified thread (threadA) has fully completed its work.

## Question: What is ThreadLocal in Java?
Answer: ThreadLocal is a class that allows storing values independently for each thread. Even if multiple threads have access to the same ThreadLocal reference, each of them sees only its own copy of the stored value, eliminating the need for synchronisation.

## Question: What is the difference between the wait() and sleep() methods?
Answer: The sleep(time) method belongs to the Thread class — it simply pauses thread execution for a given time but does NOT release any locks (monitors) the thread holds. The wait() method belongs to the Object class — it must be called in a synchronised block and RELEASES the held lock, waiting until another thread calls notify() or notifyAll().

# Topic: Hibernate / Spring Data JPA
## Question: What is the N+1 queries problem in Hibernate and how do you solve it?
Answer: The N+1 problem occurs with relationships (e.g. one-to-many) when the application first executes 1 query to fetch a list of n parent entities, then (due to lazy loading) executes N separate queries to fetch the associated child entities. It can be resolved using a "JOIN FETCH" query in JPQL or EntityGraphs.

## Question: What is the difference between @OneToMany and @ManyToOne in JPA?
Answer: Both annotations describe an association between entities. @OneToMany ("one to many") is typically placed above a collection (e.g. one Author has many Books). @ManyToOne ("many to one") is placed in the child entity and points to a single parent object (many Books have one Author). They are often used together as a bidirectional relationship.

## Question: What does the spring.jpa.hibernate.ddl-auto property do?
Answer: This property controls Hibernate's behaviour regarding database schema generation and management. A value of "create" or "create-drop" destroys and recreates tables on every startup. "update" tries to align the table with code changes. In production (or when using Flyway/Liquibase) it should be set to "validate" or "none".

## Question: What is the difference between save() and saveAndFlush() in Spring Data JPA?
Answer: The save() method stores the object in the persistence cache (Session/Persistence Context) and defers the actual flush to the database (INSERT/UPDATE) until the transaction commits. saveAndFlush() immediately forces the flush of that data from the cache to the database.

## Question: How do you define a custom query in an interface extending JpaRepository?
Answer: There are three ways: 1. Using Derived Query Methods (naming the method like findByEmailAndStatus), 2. Using the @Query annotation above the method with a custom JPQL query (or native SQL with nativeQuery = true), 3. Creating an interface and implementation class with custom logic using the Criteria API or JdbcTemplate.
