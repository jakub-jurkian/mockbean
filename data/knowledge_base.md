# Topic: Core Java
## Question: Jaka jest różnica między operatorem == a metodą equals()?
Answer: Operator == porównuje referencje obiektów (czy wskazują na to samo miejsce w pamięci), natomiast metoda equals() służy do porównywania wartości (stanu) obiektów. W przypadku typów prymitywnych operator == porównuje ich wartości.

## Question: Czym różni się String, StringBuilder i StringBuffer?
Answer: String jest niemutowalny (niemodyfikowalny) – każda operacja modyfikująca tworzy nowy obiekt. StringBuilder i StringBuffer są mutowalne. StringBuffer jest bezpieczny dla wątków (thread-safe) ze względu na wewnętrzną synchronizację, natomiast StringBuilder nie jest zsynchronizowany, przez co działa szybciej w środowiskach jednowątkowych.

## Question: Co to jest Garbage Collector i jak działa?
Answer: Garbage Collector (GC) to mechanizm automatycznego zarządzania pamięcią w Javie. Działa w tle i automatycznie wyszukuje oraz usuwa z pamięci sterty (Heap) obiekty, do których nie ma już żadnych żywych referencji w programie, zwalniając w ten sposób zasoby.

## Question: Co to jest String Pool w Javie?
Answer: String Pool to specjalny obszar w pamięci sterty (Heap), w którym przechowywane są literały łańcuchowe (String). Jeśli tworzymy nowy String za pomocą literału (np. String s = "test"), JVM najpierw sprawdza String Pool. Jeśli taki ciąg już tam istnieje, zwracana jest do niego referencja, co pozwala oszczędzać pamięć.

## Question: Czym są wyrażenia lambda wprowadzone w Javie 8?
Answer: Wyrażenia lambda to krótki sposób na zapisanie anonimowej metody. Pozwalają one na traktowanie kodu jako danych i przekazywanie zachowań (funkcji) jako argumentów do metod. Ich składnia to zazwyczaj `(parametry) -> { ciało metody }`.

## Question: Czym jest interfejs funkcyjny (Functional Interface)?
Answer: Interfejs funkcyjny to interfejs, który posiada dokładnie jedną abstrakcyjną metodę (może mieć wiele metod default lub static). Służą one jako typy docelowe dla wyrażeń lambda. Często oznacza się je adnotacją @FunctionalInterface.

## Question: Co to jest Stream API?
Answer: Stream API (wprowadzone w Javie 8) to narzędzie do deklaratywnego przetwarzania kolekcji danych. Pozwala na tworzenie potoków operacji (takich jak map, filter, reduce), które mogą być wykonywane sekwencyjnie lub zrównoleglone, bez modyfikowania oryginalnego źródła danych.

## Question: Jaka jest różnica między operacjami map() a flatMap() w Stream API?
Answer: Operacja map() służy do transformacji każdego elementu w strumieniu w dokładnie jeden inny element (relacja 1:1). Operacja flatMap() pozwala na transformację każdego elementu w zero, jeden lub wiele elementów (np. spłaszczając strumień list do jednego płaskiego strumienia elementów z tych list).

## Question: Czym jest klasa Optional i jaki problem rozwiązuje?
Answer: Optional to kontener, który może, ale nie musi, zawierać wartość nie-nullową. Został wprowadzony w Javie 8, aby zredukować problem wszechobecnych wyjątków NullPointerException, zmuszając programistę do jawnego obsłużenia sytuacji, w której wartości brakuje.

## Question: Jakie są modyfikatory dostępu w Javie i czym się różnią?
Answer: Java ma 4 modyfikatory: 1. public (widoczny wszędzie), 2. protected (widoczny w tym samym pakiecie oraz w podklasach), 3. default/package-private (brak słowa kluczowego - widoczny tylko w obrębie tego samego pakietu), 4. private (widoczny tylko w obrębie danej klasy).

## Question: Co to jest varargs w Javie?
Answer: Varargs (zmienna liczba argumentów) to mechanizm wprowadzony w Javie 5, który pozwala metodzie przyjąć zero lub więcej argumentów określonego typu. Oznacza się go trzema kropkami (np. `void metoda(String... args)`). Wewnątrz metody argument ten jest traktowany jak tablica.

## Question: Co oznacza słowo kluczowe synchronized?
Answer: Słowo kluczowe synchronized służy do kontroli dostępu do bloku kodu lub metody w środowisku wielowątkowym. Zapewnia, że w danym momencie tylko jeden wątek może wykonywać dany zsynchronizowany fragment kodu, zapobiegając problemom ze spójnością danych (race conditions).

## Question: Jaka jest różnica między błędem (Error) a wyjątkiem (Exception)?
Answer: Obie klasy dziedziczą po Throwable. Error reprezentuje poważne problemy na poziomie maszyny wirtualnej JVM (np. OutOfMemoryError, StackOverflowError), których aplikacja zazwyczaj nie powinna łapać ani obsługiwać. Exception reprezentuje błędy, z których aplikacja może się zregenerować i powinna je obsłużyć.

# Topic: Object-Oriented Programming
## Question: Czym jest hermetyzacja (enkapsulacja)?
Answer: Hermetyzacja to ukrywanie stanu wewnętrznego obiektu (pól klasy) poprzez ustawienie ich modyfikatora dostępu na private i udostępnianie ich światu zewnętrznemu wyłącznie poprzez publiczne metody dostępowe (gettery i settery). Chroni to dane przed niekontrolowaną modyfikacją.

## Question: Czym jest abstrakcja w programowaniu obiektowym?
Answer: Abstrakcja to ukrywanie skomplikowanych szczegółów implementacyjnych i pokazywanie użytkownikowi tylko niezbędnych funkcjonalności. W Javie realizuje się ją za pomocą klas abstrakcyjnych i interfejsów, skupiając się na tym "co" robi dany obiekt, a nie "jak" to robi.

## Question: Na czym polega zasada Single Responsibility Principle (SRP) z SOLID?
Answer: Zasada SRP mówi, że każda klasa powinna mieć tylko jedną odpowiedzialność i tylko jeden powód do zmiany. Jeśli klasa zajmuje się jednocześnie np. logiką biznesową, zapisem do bazy danych i formatowaniem tekstu, łamie tę zasadę.

## Question: Na czym polega zasada Open/Closed Principle (OCP) z SOLID?
Answer: Zasada OCP mówi, że klasy, moduły i funkcje powinny być otwarte na rozbudowę (extension), ale zamknięte na modyfikacje (modification). Oznacza to, że powinniśmy móc dodawać nowe zachowania bez zmieniania istniejącego, działającego kodu, np. poprzez użycie interfejsów i polimorfizmu.

## Question: Jaka jest różnica między agregacją a kompozycją?
Answer: Obie opisują relację "has-a". W agregacji (słaba relacja) obiekt podrzędny może istnieć niezależnie od obiektu nadrzędnego (np. Wydział ma Profesorów). W kompozycji (silna relacja) obiekt podrzędny nie ma racji bytu bez nadrzędnego – ginie wraz z nim (np. Budynek ma Pokoje).

## Question: Czy można utworzyć instancję (obiekt) klasy abstrakcyjnej?
Answer: Nie, bezpośrednie utworzenie instancji klasy abstrakcyjnej za pomocą słowa kluczowego new (np. `new MojaKlasaAbstrakcyjna()`) jest zabronione. Można natomiast utworzyć obiekt klasy anonimowej, która od razu implementuje brakujące metody abstrakcyjne w locie.


# Topic: Collections
## Question: Jaka jest różnica między ArrayList a LinkedList?
Answer: ArrayList opiera się na dynamicznej tablicy, co zapewnia bardzo szybki dostęp do elementów po indeksie, ale wolne wstawianie i usuwanie elementów w środku listy (wymaga przesuwania innych elementów). LinkedList opiera się na strukturze listy dwukierunkowej, gdzie wstawianie i usuwanie jest szybkie (przy znanej pozycji), ale dostęp po indeksie jest powolny.

## Question: Czym różni się HashMap od HashSet?
Answer: HashMap przechowuje dane w postaci par klucz-wartość (key-value), gdzie każdy klucz musi być unikalny. HashSet przechowuje tylko zbiór unikalnych wartości. Pod maską HashSet używa HashMap do przechowywania swoich elementów (element jest kluczem, a wartością jest stały obiekt-wypełniacz).

## Question: Czym różni się interfejs Iterable od Iterator?
Answer: Iterable to interfejs (zawierający m.in. zbiory i listy), który gwarantuje, że po danej kolekcji można iterować (np. w pętli foreach). Posiada on metodę iterator(), która zwraca obiekt typu Iterator. Iterator z kolei to obiekt "wskaźnika", który faktycznie przemieszcza się po elementach (metody hasNext(), next(), remove()).

## Question: Jaka jest różnica między interfejsem Collection a klasą Collections?
Answer: Collection to główny interfejs (dziedziczący po Iterable) w hierarchii kolekcji Javy, po którym dziedziczą Set, List i Queue. Z kolei java.util.Collections (z literą 's') to klasa narzędziowa (utility class) zawierająca statyczne metody do operowania na kolekcjach, np. sort(), reverse(), shuffle().

## Question: Jak działa TreeMap i czym różni się od HashMap?
Answer: Obie implementują interfejs Map. HashMap nie gwarantuje żadnego ułożenia kluczy i opiera się na hashowaniu. TreeMap opiera się na strukturze drzewa czerwono-czarnego (Red-Black Tree), dzięki czemu klucze są w nim zawsze posortowane (naturalnie lub przy użyciu przekazanego Comparatora), ale operacje dodawania/wyszukiwania są wolniejsze niż w HashMap (O(log n) vs O(1)).

## Question: Kiedy użyłbyś CopyOnWriteArrayList zamiast ArrayList?
Answer: CopyOnWriteArrayList jest używane w środowiskach wielowątkowych, gdzie operacje odczytu (iteracji) są znacznie częstsze niż operacje zapisu. Przy każdej modyfikacji (dodanie/usunięcie elementu) tworzona jest nowa, odseparowana kopia wewnętrznej tablicy. Zapewnia to bezpieczeństwo bez użycia blokad (locks), co czyni iterację niezwykle szybką, ale zapis kosztownym.

## Question: Czym charakteryzuje się struktura Stack i jaka klasa w Javie ją implementuje?
Answer: Stack (Stos) działa na zasadzie LIFO (Last-In-First-Out) – ostatni dodany element jest zdejmowany jako pierwszy (np. stos talerzy). W Javie istnieje stara klasa Stack (dziedzicząca po Vector, jest zsynchronizowana), jednak obecnie zaleca się używanie interfejsu Deque i jego implementacji (np. ArrayDeque) do symulowania zachowania stosu.

# Topic: Exceptions
## Question: Jaka jest różnica między wyjątkami Checked i Unchecked w Javie?
Answer: Wyjątki Checked (np. IOException) dziedziczą po klasie Exception i muszą być jawnie obsłużone w kodzie (blokiem try-catch) lub zadeklarowane w sygnaturze metody (throws). Wyjątki Unchecked (np. NullPointerException) dziedziczą po RuntimeException i nie wymagają jawnej obsługi, zazwyczaj wskazując na błędy programistyczne.

## Question: Jak można stworzyć własny wyjątek (Custom Exception) w Javie?
Answer: Własny wyjątek tworzy się poprzez utworzenie nowej klasy, która dziedziczy po klasie Exception (jeśli ma to być wyjątek typu Checked) lub po klasie RuntimeException (jeśli ma to być wyjątek typu Unchecked). Warto również zaimplementować konstruktor przyjmujący wiadomość o błędzie (String).

## Question: Jaka jest różnica między ClassNotFoundException a NoClassDefFoundError?
Answer: ClassNotFoundException to wyjątek (Exception), który występuje podczas dynamicznego ładowania klas (np. przy użyciu Class.forName()), gdy dana klasa nie znajduje się w Classpath. NoClassDefFoundError to błąd (Error), który występuje, gdy klasa była obecna podczas kompilacji kodu, ale z jakiegoś powodu nie można jej odnaleźć podczas działania programu (Runtime).

## Question: W jakiej kolejności należy łapać wyjątki w wielokrotnych blokach catch?
Answer: Wyjątki należy łapać w kolejności od najbardziej specyficznych (najniżej w hierarchii dziedziczenia) do najbardziej ogólnych (najwyżej w hierarchii). Jeśli jako pierwszy umieścimy catch(Exception e), przechwyci on wszystko, a kolejne, bardziej precyzyjne bloki catch staną się nieosiągalne, co wywoła błąd kompilacji.

## Question: Co to jest "wyjątek stłumiony" (Suppressed Exception)?
Answer: Mechanizm ten wprowadzono w instrukcji try-with-resources. Jeśli wystąpi wyjątek w głównym bloku try, a następnie metoda zamykająca zasób (np. close()) wyrzuci kolejny wyjątek, ten drugi wyjątek jest "tłumiony" i dołączany do pierwotnego. Można je odzyskać używając metody getSuppressed() na pierwotnym wyjątku.

## Question: Czy można wyrzucić wyjątek typu Checked bez deklarowania go w klauzuli throws?
Answer: W normalnym, czystym kodzie Javy – nie, kompilator to zablokuje. Istnieją jednak "hacki" (tzw. Sneaky Throws), które pozwalają ominąć sprawdzanie kompilatora, np. korzystając z mechanizmu generyków, klasy Unsafe, lub (najczęściej w praktyce) używając adnotacji @SneakyThrows z biblioteki Lombok.

# Topic: Spring Framework
## Question: Na czym polega Wstrzykiwanie Zależności (Dependency Injection - DI)?
Answer: Wstrzykiwanie Zależności to wzorzec, w którym obiekt nie tworzy samodzielnie swoich zależności (np. operatorem new), ale otrzymuje je z zewnątrz. W Springu odpowiada za to kontener IoC, który wstrzykuje beany (najlepiej przez konstruktor), co drastycznie ułatwia testowanie i zmniejsza sprzężenie klas.

## Question: Czym różnią się adnotacje @Component, @Service i @Repository?
Answer: Wszystkie trzy oznaczają beany zarządzane przez Springa. @Component to główna, ogólna adnotacja. @Service to jej specjalizacja dla klas z logiką biznesową. @Repository to specjalizacja dla klas dostępu do danych (DAO), która dodatkowo automatycznie tłumaczy wyjątki bazodanowe na hierarchię wyjątków Springa (DataAccessException).

## Question: Co robi adnotacja @SpringBootApplication?
Answer: @SpringBootApplication to skrót, który łączy trzy adnotacje: @Configuration (oznacza klasę konfiguracyjną), @EnableAutoConfiguration (włącza automatyczną konfigurację Spring Boota na podstawie bibliotek w classpath) oraz @ComponentScan (nakazuje Springowi skanowanie bieżącego pakietu i jego podpakietów w poszukiwaniu beanów).

## Question: Czym jest cykl życia Beana w Springu?
Answer: Cykl życia Beana obejmuje jego utworzenie przez kontener IoC, wstrzyknięcie zależności (DI), wywołanie metod inicjalizujących (np. adnotowanych jako @PostConstruct), udostępnienie go aplikacji, a na koniec wywołanie metod niszczących (np. adnotowanych jako @PreDestroy) przed ubiciem kontenera.

## Question: Co to jest Spring AOP (Aspect-Oriented Programming)?
Answer: AOP to programowanie zorientowane aspektowo. Pozwala na oddzielenie logiki biznesowej od logiki pobocznej (cross-cutting concerns), takiej jak logowanie, transakcje bazodanowe czy autoryzacja. AOP pozwala "wpleść" ten dodatkowy kod przed, po, lub w trakcie wykonywania głównych metod, bez ingerowania w ich ciało.

## Question: Jaka jest różnica między @Controller a @RestController?
Answer: Adnotacja @Controller służy głównie w aplikacjach generujących widoki (np. Thymeleaf) – zwraca nazwę widoku (HTML), który ma zostać wyrenderowany. @RestController to połączenie @Controller z @ResponseBody – powoduje, że dane zwracane przez metody są automatycznie serializowane (najczęściej do formatu JSON/XML) i wysyłane bezpośrednio w ciele odpowiedzi HTTP.

## Question: Co robi adnotacja @Autowired w Springu?
Answer: @Autowired to adnotacja używana do automatycznego wstrzykiwania zależności. Spring skanuje kontekst aplikacji w poszukiwaniu beana, którego typ pasuje do zadeklarowanego pola, konstruktora lub metody typu setter, i automatycznie przypisuje tam odpowiedni obiekt. W nowym kodzie zaleca się wstrzykiwanie przez konstruktor (często wspierane przez Lombok).

## Question: Jakie są główne zasięgi (scopes) beanów w Springu?
Answer: Dwa najważniejsze to "singleton" (domyślny; w kontenerze Springa istnieje tylko jedna, współdzielona instancja danego beana) oraz "prototype" (przy każdym zażądaniu beana tworzona jest nowa instancja). W środowisku webowym dochodzą do tego scopes: "request" (jeden bean na jedno żądanie HTTP) i "session" (jeden na sesję HTTP).

# Topic: Multithreading
## Question: Jaka jest różnica między rozszerzeniem klasy Thread a implementacją interfejsu Runnable?
Answer: Oba podejścia pozwalają na stworzenie wątku. Implementacja interfejsu Runnable jest zazwyczaj preferowana, ponieważ Java nie obsługuje wielokrotnego dziedziczenia klas – implementując Runnable, nasza klasa może wciąż dziedziczyć po innej klasie. Dodatkowo Runnable oddziela zadanie (logikę) od samego mechanizmu wykonawczego (Thread).

## Question: Czym jest zakleszczenie (Deadlock)?
Answer: Deadlock to sytuacja w programowaniu współbieżnym, w której dwa lub więcej wątków czeka w nieskończoność na zwolnienie zasobów (blokad), które trzymają nawzajem. Żaden z wątków nie może ruszyć dalej, co prowadzi do trwałego zawieszenia tej części aplikacji.

## Question: Do czego służy metoda join() z klasy Thread?
Answer: Metoda join() wywołana na obiekcie wątku (np. watekA.join()) powoduje, że aktualnie działający wątek zatrzymuje swoje działanie i czeka cierpliwie, aż dany watek (watekA) całkowicie zakończy swoją pracę.

## Question: Czym jest ThreadLocal w Javie?
Answer: ThreadLocal to klasa, która pozwala na przechowywanie wartości niezależnie dla każdego wątku. Nawet jeśli wiele wątków ma dostęp do tej samej referencji ThreadLocal, każdy z nich widzi tylko i wyłącznie własną kopię zapisanej tam wartości, co eliminuje konieczność synchronizacji.

## Question: Jaka jest różnica między metodami wait() a sleep()?
Answer: Metoda sleep(czas) należy do klasy Thread – po prostu wstrzymuje wykonanie wątku na zadany czas, ale NIE zwalnia trzymanych przez wątek blokad (monitorów). Metoda wait() należy do klasy Object – musi być wywołana w bloku zsynchronizowanym i ZWALNIA trzymaną blokadę, czekając aż inny wątek wywoła notify() lub notifyAll().

# Topic: Hibernate / Spring Data JPA
## Question: Co to jest problem N+1 zapytań (N+1 Select problem) w Hibernate i jak go rozwiązać?
Answer: Problem N+1 pojawia się przy relacjach (np. 1-do-wielu), gdy aplikacja najpierw wykonuje 1 zapytanie po listę n encji nadrzędnych, a następnie (z powodu leniwego ładowania - Lazy Fetching) wykonuje N osobnych zapytań, by pobrać powiązane z nimi encje podrzędne. Rozwiązuje się to m.in. używając zapytania "JOIN FETCH" w JPQL lub EntityGraphs.

## Question: Jaka jest różnica między adnotacją @OneToMany a @ManyToOne w JPA?
Answer: Obie adnotacje opisują asocjację między encjami w bazie. @OneToMany ("jeden do wielu") umieszczamy zazwyczaj nad kolekcją (np. jeden Autor ma wiele Książek). @ManyToOne ("wiele do jednego") umieszczamy w encji podrzędnej, gdzie wskazuje ona na pojedynczy obiekt rodzica (wiele Książek ma jednego Autora). Często używa się ich razem jako relacji dwukierunkowej.

## Question: Co robi właściwość spring.jpa.hibernate.ddl-auto w pliku properties/yml?
Answer: Właściwość ta kontroluje zachowanie Hibernate'a w kwestii generowania i zarządzania schematem bazy danych. Wartość "create" lub "create-drop" niszczy i tworzy tabele na nowo przy każdym starcie. Wartość "update" próbuje dopasować tabelę do zmian w kodzie. W środowisku produkcyjnym (lub przy użyciu Flyway/Liquibase) powinna mieć wartość "validate" lub "none".

## Question: Czym różni się metoda save() od saveAndFlush() w Spring Data JPA?
Answer: Metoda save() zapisuje obiekt w pamięci podręcznej (Session/Persistence Context) i opóźnia rzeczywisty zrzut danych do bazy danych (INSERT/UPDATE) do momentu zakończenia transakcji (commit). saveAndFlush() natychmiast wymusza zrzut tych danych z pamięci podręcznej na dysk bazy danych.

## Question: Jak zdefiniować własne zapytanie w interfejsie rozszerzającym JpaRepository?
Answer: Można to zrobić na trzy sposoby: 1. Używając Derived Query Methods (tworząc nazwę metody jak np. findByEmailAndStatus), 2. Używając adnotacji @Query nad metodą, w której wpisujemy własne zapytanie w języku JPQL (lub natywnym SQL z flagą nativeQuery = true), 3. Tworząc interfejs i klasę implementującą własną logikę np. z użyciem Criteria API lub JdbcTemplate.