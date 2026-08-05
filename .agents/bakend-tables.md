Правила по работе с таблицами бад данных бэкенда

 - таблицы БД при создании добавляются в файл миграции
 - Все таблицы имеют служебное поле ctime: timestamptz, utime: timestamptz. Поля заполняются JPA Handler ом. При конвертации из DTO игнорируются. 
   В клссах модели и ДТО имеют тип OffsetDateTime. в Модели и DTO поля прнимают НУЛЛ значения. При конвертации в JPA эти поля игнорируются. 
 - JPA классы для таблиц имеют окончания "Entity". JPA Классы  кладуться в pakage <MODULE_ROOT_PACKAGE>.hql.model
 - для JPA класса создается класс JpaRepository и кладется в pakage <MODULE_ROOT_PACKAGE>.hql.repository
 - классы JpaRepository имеют окончание Repository
 - при выходе ответов из транзакции JPA классы конвертятся в DTO. 
 - DTO классы для JPA лежат в папке  <MODULE_ROOT_PACKAGE>.hql.dto
 - DTO классы для JPA имеют окончание Dto
 - классы для конвертации JPА модели в DTO и обратно имеют суффикс Mapper и располагаются pakage <MODULE_ROOT_PACKAGE>.hql.mapper