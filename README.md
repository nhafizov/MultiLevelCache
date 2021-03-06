# MultiLevelCache

двухуровневый кэш, устроенный следующим образом:

- Хранит строки по ключу (ключ - long, значение - String)
- Поддерживает две Eviction Policy (LFU и LRU)
- Первый кэш хранит данные в оперативной памяти, а при переполнении выделенной памяти в оперативной памяти вытесняет объекты на диск
- Принимает на вход выделенный размер оперативной памяти и размер памяти на диске
- Производит компактизацию диска
- Интерфейс содержит методы put и get
- Скорость:
  - get O(L), где L - длина строки
  - put O(L), где L - длина строки
- Хранит все ключи в оперативной памяти
- Подсчет памяти в байтах приближенный (учитывает только размер ключа (8 байт) и размер строки в байтах)
