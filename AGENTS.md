# Руководство по разработке Vacation Sheet

## Обзор проекта

Vacation Sheet — локальное веб-приложение для управления отпусками. Репозиторий представляет собой монорепозиторий с Angular SPA, бэкендом на Kotlin и Spring Boot и базой данных PostgreSQL.

## Структура репозитория

- `frontend/`: Angular SPA, обслуживаемое Nginx в Docker
- `backend/`: модуль Gradle с REST API на Kotlin и Spring Boot
- `settings.gradle.kts`, `build.gradle.kts`, `gradlew`: корневая мультимодульная Gradle-сборка
- `compose.yml`: полное локальное окружение
- `README.md`: инструкции по настройке для разработчиков

## Технологический стек

Использовать последние стабильные совместимые версии, если конкретная версия уже не зафиксирована в проекте.

### Фронтенд

- Angular 22
- TypeScript 6
- Angular Material 3
- Standalone-компоненты
- Angular Signals для управления состоянием
- Angular HttpClient
- SCSS
- Unit-тесты Angular на базе Vitest
- npm
- Только SPA; не добавлять SSR

### Бэкенд

- Java 21
- Kotlin 2.2
- Spring Boot 3.5
- Gradle Kotlin DSL
- Spring MVC REST API
- Spring Data JPA и Hibernate
- PostgreSQL 18
- Миграции Flyway
- Spring Security OAuth2 Client
- Yandex OAuth2
- Springdoc Swagger UI
- Spring Boot Actuator
- JUnit 5, MockK и Testcontainers

### Инфраструктура

- Docker Compose запускает PostgreSQL, бэкенд и фронтенд
- Nginx раздаёт сборку Angular и проксирует запросы к бэкенду
- Браузер использует единый origin для фронтенда и бэкенда
- Приложение доступно через порт `4200`
- Бэкенд и PostgreSQL доступны только внутри сети Compose

## Архитектурные правила

- REST endpoints должны использовать префикс `/api`.
- Контроллеры должны оставаться тонкими, бизнес-логику размещать в сервисах.
- На границе API использовать DTO запросов и ответов.
- Преобразовывать DTO вручную; не добавлять MapStruct без изменения требований.
- Возвращать ошибки API через Spring `ProblemDetail`.
- Проверять входные данные с помощью Jakarta Bean Validation.
- Делать изменения небольшими и не создавать абстракции без доказанной необходимости.
- Не добавлять отдельные файлы OpenAPI-контракта и сгенерированные TypeScript-клиенты.
- Springdoc может автоматически генерировать OpenAPI-документ для Swagger UI.

## Правила работы с данными

- Для доступа к данным использовать репозитории Spring Data JPA.
- Предпочитать производные методы репозиториев и запросы JPQL/HQL.
- Не использовать Criteria API.
- Не использовать генерацию схемы Hibernate для изменения базы данных.
- Значение `spring.jpa.hibernate.ddl-auto` должно оставаться `validate`.
- Каждое изменение схемы оформлять новой миграцией Flyway.
- Никогда не изменять уже применённую миграцию; добавлять следующую версионированную миграцию.
- Значение `spring.jpa.open-in-view` должно оставаться отключённым.

## Модель безопасности

- Для аутентификации используется Yandex OAuth2 Authorization Code flow.
- Spring Boot выступает OAuth2-клиентом и хранит аутентификацию в HTTP-сессии в памяти.
- Angular-приложение никогда не должно получать access token Яндекса.
- Session cookie должна оставаться `HttpOnly` с `SameSite=Lax`.
- CSRF-защита должна оставаться включённой.
- Angular использует cookie `XSRF-TOKEN` и заголовок `X-XSRF-TOKEN`.
- `/api/auth/csrf` инициализирует CSRF-токен.
- Неаутентифицированные запросы к `/api/**` должны возвращать HTTP `401`, а не перенаправлять на вход.
- OAuth-вход начинается через `/oauth2/authorization/yandex`.
- Для выхода используется `POST /api/auth/logout`.
- Endpoints Swagger намеренно доступны без авторизации:
  - `/swagger-ui.html`
  - `/swagger-ui/**`
  - `/v3/api-docs/**`
- `/actuator/health/**` доступен без авторизации для health checks контейнеров.

## Ограничение по домену email

Разрешённый домен Yandex email настраивается следующим образом:

```yaml
app:
  security:
    allowed-email-domain: ""
```

- Пустое значение разрешает любой домен email.
- Непустое значение требует точного совпадения домена без учёта регистра.
- Поддомены не принимаются, если они не указаны явно.

## Пользователи и роли

- При первом успешном OAuth2-входе сохранять пользователя в PostgreSQL.
- При последующих входах обновлять email, отображаемое имя и время обновления.
- Внешним ключом идентификации служит Yandex user ID.
- В приложении будет три роли, но их названия и модель авторизации ещё не определены.
- Не придумывать и не реализовывать семантику ролей до согласования требований.

## Проекты

- Сущность `Project` содержит название, необязательное описание и даты создания и обновления.
- `Project` связан с `UserAccount` отношением many-to-many через таблицу `project_members`.
- Проекты можно создавать, изменять и удалять.
- Привязка пользователя к проекту выполняется идемпотентным `PUT`, отвязка — `DELETE`.
- Не возвращать JPA-сущности через API; использовать DTO.

## Конфигурация

Основная конфигурация бэкенда находится в `backend/src/main/resources/application.yml`.

Перед проверкой реального OAuth-входа заменить заглушки Yandex `client-id` и `client-secret`. Не добавлять реальные production-секреты в Git. Yandex redirect URI для Docker:

```text
http://localhost:4200/login/oauth2/code/yandex
```

Стандартные параметры локальной базы данных:

```text
Database: vacation_sheet
Username: vacation_sheet
Password: vacation_sheet
```

Compose переопределяет JDBC URL, чтобы бэкенд подключался к сервису `database`.

## Существующее API

- `GET /api/auth/csrf`: инициализация и получение CSRF-токена
- `GET /api/auth/me`: получение текущего аутентифицированного пользователя
- `POST /api/auth/logout`: завершение текущей сессии
- `GET /api/users`: получение списка зарегистрированных пользователей
- `GET /api/projects`: получение проектов с участниками
- `POST /api/projects`: создание проекта
- `PUT /api/projects/{id}`: изменение проекта
- `DELETE /api/projects/{id}`: удаление проекта
- `PUT /api/projects/{projectId}/users/{userId}`: привязка пользователя к проекту
- `DELETE /api/projects/{projectId}/users/{userId}`: отвязка пользователя от проекта
- `GET /actuator/health`: health check контейнера
- `GET /v3/api-docs`: сгенерированное описание API
- `GET /swagger-ui.html`: страница Swagger UI

## Правила фронтенда

- Использовать standalone-компоненты Angular.
- Для состояния приложения использовать Signals и сервисы.
- Не добавлять NgRx, пока сложность состояния не покажет конкретную необходимость.
- Использовать компоненты Angular Material и сохранять существующий визуальный стиль.
- Использовать современный control flow шаблонов Angular: `@if`, `@for`.
- Использовать относительные URL `/api`; не прописывать адрес бэкенда в коде приложения.
- Поддерживать адаптивность интерфейса для desktop и mobile.
- Добавлять unit-тесты для новых stores, сервисов и нетривиальных компонентов.

## Структура пакетов бэкенда

Базовый пакет: `com.example.vacationsheet`.

Бэкенд использует Packaging by Layer. Каждый класс должен находиться в соответствующем пакете:

- `config`: конфигурация приложения и безопасности
- `controller`: REST-контроллеры
- `dto`: DTO запросов и ответов
- `entity`: JPA-сущности
- `exception`: исключения и обработка ошибок REST API
- `repository`: репозитории Spring Data JPA
- `service`: бизнес-логика, загрузка OAuth2-пользователей и политики доступа

Не вводить Packaging by Feature/Domain без явного изменения архитектуры проекта.

## Команды разработки

Запуск полного окружения из корня репозитория:

```shell
docker compose up --build
```

Остановка контейнеров без удаления данных PostgreSQL:

```shell
docker compose down
```

Проверка бэкенда в Windows:

```shell
gradlew.bat :backend:clean :backend:test :backend:bootJar
```

Проверка бэкенда в Unix-подобных системах:

```shell
./gradlew :backend:clean :backend:test :backend:bootJar
```

Проверка фронтенда:

```shell
cd frontend
npm ci
npm test -- --watch=false
npm run build
```

Проверка конфигурации Compose:

```shell
docker compose config --quiet
```

## Критерии готовности

Перед завершением изменения:

1. Добавить миграцию Flyway для каждого изменения схемы базы данных.
2. Добавить или обновить целевые тесты изменённого поведения.
3. При изменении бэкенда запустить тесты и задачу `bootJar`.
4. При изменении фронтенда запустить unit-тесты и production-сборку.
5. При изменении контейнеров проверить `docker compose config`.
6. Убедиться, что защищённые endpoints без сессии по-прежнему возвращают `401`.
7. После изменения безопасности проверить исключения Swagger из авторизации и работу CSRF.
8. Не добавлять в Git OAuth-секреты, сгенерированные результаты сборки и каталоги зависимостей.
