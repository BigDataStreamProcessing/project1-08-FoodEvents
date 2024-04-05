# Charakterystyka danych
Sprzedaż oraz zakup dań przez sieć sklepów.

W strumieniu pojawiają się zdarzenia zgodne ze schematem `FoodEvent`.

```
create json schema FoodEvent(ean string, dish string, quantity int,
shop string, its string, ets string);
```

Każde zdarzenie związane z jest z faktem sprzedaży lub zakupu 
konkretnego dania przez określony sklep.

Dane uzupełnione są o dwie etykiety czasowe. 
* Pierwsza (`ets`) związana jest z momentem sprzedaży lub zakupu. 
  Etykieta ta może się losowo spóźniać w stosunku do czasu systemowego maksymalnie do 30 sekund.
* Druga (`its`) związana jest z momentem rejestracji zdarzenia systemie.

# Opis atrybutów

Atrybuty w każdym zdarzeniu zgodnym ze schematem `FoodEvent` mają następujące znaczenie:

- `ean` - kod kreskowy produktu
- `dish` - nazwa gotowego dania, które zostało zakupione
- `quantity` - liczba sprzedanych/zakupionych dań (wartość ujemna to zakup; wartość dodatnia to sprzedaż)
- `shop` - nazwa sklepu
- `ets` - czas sprzedaży/zakupu
- `its` - czas rejestracji faktu sprzedaży/zakupu w systemie

# Zadania
Opracuj rozwiązania poniższych zadań. 
* Opieraj się strumieniu zdarzeń zgodnych ze schematem `FoodEvent`
* W każdym rozwiązaniu możesz skorzystać z jednego lub kilku poleceń EPL.
* Ostatnie polecenie będące ostatecznym rozwiązaniem zadania musi 
  * być poleceniem `select` 
  * posiadającym etykietę `answer`, przykładowo:

```aidl
@name('answer') SELECT ean, dish, quantity, shop, ets, its
FROM FoodEvent#ext_timed(java.sql.Timestamp.valueOf(its).getTime(), 3 sec)
```

## Zadanie 1
Dla każdego sklepu utrzymuj sumę liczby sprzedanych dań w ciągu ostatniej minuty.

Wyniki powinny zawierać, następujące kolumny:
- `quantity_sum` - suma liczby sprzedanych dań
- `shop` - nazwę sklepu

## Zadanie 2
Wykrywaj przypadki pojedynczej sprzedaży większej liczby niż 3 sztuki.

Wyniki powinny zawierać, następujące kolumny:
- `quantity` - liczbę sprzedanych dań
- `shop` - nazwę sklepu
- `dish` - danie, którego sprzedaż została wykryta
- `its` - czas rejestracji sprzedaży

## Zadanie 3
Wykrywaj przypadki pojedynczej sprzedaży większej ilości niż średnia ilość sztuk w ramach sprzedaży tego samego dania w ciągu ostatniej minuty.

Wyniki powinny zawierać, następujące kolumny:
- `quantity` - liczbę sprzedanych dań
- `shop` - nazwę sklepu
- `dish` - danie, którego sprzedaż została wykryta
- `its` - czas rejestracji sprzedaży

## Zadanie 4
Utrzymywane są listy 5 sklepów o największej liczbie zdarzeń (sprzedaż/zakup) dotyczących: (1) dań o nazwie kebab, (2) dań o nazwie pizza, zarejestrowanych w ciągu ostatnich 10 sekund. 

Porównuj ze sobą sklepy, które znalazły się na obu listach

Wyniki powinny zawierać, następujące kolumny:
- `shop` - nazwę sklepu
- `number_pizza` - liczba zdarzeń dotyczących pizzy
- `number_kebab` - liczba zdarzeń dotyczących kebaba

## Zadanie 5

Wykrywaj serie co najmniej dwóch sprzedaży, do momentu wystąpienia zdarzenia (sprzedaży lub zakupu) w sklepie 'Lidl'. Ogranicz wykryte serie tylko do takich, w których różnica w liczbie dań pomiędzy zdarzeniem w Lidlu a sprzedażą w pierwszym sklepie jest większa od 1.

Wyniki powinny zawierać, następujące kolumny:
- `quantity_diff` - różnica w sprzedaży pomiędzy zdarzeniem 'w Lidlu' a pierwszą sprzedażą w serii
- `shop` - nazwa sklepu pierwszej sprzedaży w serii
- `ets` - czas rejestracji pierwszej sprzedaży w serii

## Zadanie 6

Wyszukuj trzy kolejne zdarzenia dotyczące sklepu lidl (nie muszą one następować bezpośrednio po sobie), w trakcie których nie miało miejsce zdarzenie w sklepie Carrefour.

Wyniki powinny zawierać, następujące kolumny:
- `quantity_1` - liczba towarów w pierwszym zdarzeniu w Lidlu
- `quantity_2` - liczba towarów w drugim zdarzeniu w Lidlu
- `quantity_3` - liczba towarów w trzecim zdarzeniu w Lidlu

## Zadanie 7

Dla każdego sklepu wykrywaj serie co najmniej trzech zdarzeń, w których liczba towarów każdorazowo będzie się zwiększała. Seria ma kończyć się przed zdarzeniem, którego liczba towarów będzie mniejsza niż liczba towarów w zdarzeniu go poprzedzającym.

Wyniki powinny zawierać, następujące kolumny:
- `shop` - nazwa sklepu
- `quantity_1` - liczba towarów w pierwszym zdarzeniu 
- `quantity_2` - liczba towarów w drugim zdarzeniu 
- `quantity_3` - liczba towarów w trzecim zdarzeniu 
