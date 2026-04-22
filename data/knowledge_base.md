# Topic: Core Java
## Question: Jaka jest różnica między operatorem == a metodą equals()?
Answer: Operator == porównuje referencje obiektów (czy wskazują na to samo miejsce w pamięci), natomiast metoda equals() służy do porównywania wartości (stanu) obiektów. W przypadku typów prymitywnych operator == porównuje ich wartości.

## Question: Czym różni się String, StringBuilder i StringBuffer?
Answer: String jest niemutowalny (niemodyfikowalny) – każda operacja modyfikująca tworzy nowy obiekt. StringBuilder i StringBuffer są mutowalne. StringBuffer jest bezpieczny dla wątków (thread-safe) ze względu na wewnętrzną synchronizację, natomiast StringBuilder nie jest zsynchronizowany, przez co działa szybciej w środowiskach jednowątkowych.

## Question: Co to jest Garbage Collector i jak działa?
Answer: Garbage Collector (GC) to mechanizm automatycznego zarządzania pamięcią w Javie. Działa w tle i automatycznie wyszukuje oraz usuwa z pamięci sterty (Heap) obiekty, do których nie ma już żadnych żywych referencji w programie, zwalniając w ten sposób zasoby.

# Topic: Object-Oriented Programming
## Question: Czym jest hermetyzacja (enkapsulacja)?
Answer: Hermetyzacja to ukrywanie stanu wewnętrznego obiektu (pól klasy) poprzez ustawienie ich modyfikatora dostępu na private i udostępnianie ich światu zewnętrznemu wyłącznie poprzez publiczne metody dostępowe (gettery i settery). Chroni to dane przed niekontrolowaną modyfikacją.

# Topic: Collections
## Question: Jaka jest różnica między ArrayList a LinkedList?
Answer: ArrayList opiera się na dynamicznej tablicy, co zapewnia bardzo szybki dostęp do elementów po indeksie, ale wolne wstawianie i usuwanie elementów w środku listy (wymaga przesuwania innych elementów). LinkedList opiera się na strukturze listy dwukierunkowej, gdzie wstawianie i usuwanie jest szybkie (przy znanej pozycji), ale dostęp po indeksie jest powolny.

## Question: Czym różni się HashMap od HashSet?
Answer: HashMap przechowuje dane w postaci par klucz-wartość (key-value), gdzie każdy klucz musi być unikalny. HashSet przechowuje tylko zbiór unikalnych wartości. Pod maską HashSet używa HashMap do przechowywania swoich elementów (element jest kluczem, a wartością jest stały obiekt-wypełniacz).

# Topic: Exceptions
## Question: Jaka jest różnica między wyjątkami Checked i Unchecked w Javie?
Answer: Wyjątki Checked (np. IOException) dziedziczą po klasie Exception i muszą być jawnie obsłużone w kodzie (blokiem try-catch) lub zadeklarowane w sygnaturze metody (throws). Wyjątki Unchecked (np. NullPointerException) dziedziczą po RuntimeException i nie wymagają jawnej obsługi, zazwyczaj wskazując na błędy programistyczne.

# Topic: Spring Framework
## Question: Na czym polega Wstrzykiwanie Zależności (Dependency Injection - DI)?
Answer: Wstrzykiwanie Zależności to wzorzec, w którym obiekt nie tworzy samodzielnie swoich zależności (np. operatorem new), ale otrzymuje je z zewnątrz. W Springu odpowiada za to kontener IoC, który wstrzykuje beany (najlepiej przez konstruktor), co drastycznie ułatwia testowanie i zmniejsza sprzężenie klas.

## Question: Czym różnią się adnotacje @Component, @Service i @Repository?
Answer: Wszystkie trzy oznaczają beany zarządzane przez Springa. @Component to główna, ogólna adnotacja. @Service to jej specjalizacja dla klas z logiką biznesową. @Repository to specjalizacja dla klas dostępu do danych (DAO), która dodatkowo automatycznie tłumaczy wyjątki bazodanowe na hierarchię wyjątków Springa (DataAccessException).

## Question: Co robi adnotacja @SpringBootApplication?
Answer: @SpringBootApplication to skrót, który łączy trzy adnotacje: @Configuration (oznacza klasę konfiguracyjną), @EnableAutoConfiguration (włącza automatyczną konfigurację Spring Boota na podstawie bibliotek w classpath) oraz @ComponentScan (nakazuje Springowi skanowanie bieżącego pakietu i jego podpakietów w poszukiwaniu beanów).