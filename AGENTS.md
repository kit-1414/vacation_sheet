# Руководство по разработке Vacation Sheet

## Обзор проекта

Vacation Sheet — локальное веб-приложение для управления отпусками. 
Репозиторий представляет собой монорепозиторий с Angular SPA, бэкендом на Kotlin и Spring Boot и базой данных PostgreSQL.

## Структура репозитория

- `vacation-sheet-ui/`
   - Angular SPA, обслуживаемое Nginx в Docker.
   - Внешний порт 4200
- `vacation-sheet-service/`
    - Модуль Gradle с REST API на Kotlin и Spring Boot.
    - `MODULE_ROOT_PACKAGE = com.example.vacationsheet.mainapp`
    - Внешний порт 8055
- `settings.gradle.kts`, `build.gradle.kts`, `gradlew`: корневая мультимодульная Gradle-сборка
- `docker-compose.yml`: полное локальное окружение
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

- Проект управляется Git.
- Docker Compose запускает PostgreSQL, бэкенд и фронтенд
- Nginx раздаёт сборку Angular и проксирует запросы к бэкенду
- Точкой входа в приложение является фронтенд, который вызывает бэкенд.
- Бэкенд доступен через Swagger в браузере.
- В бэкенде можно авторизоваться по URL входа. Callback тот же, что и при штатной работе с фронтендом.


## Архитектурные правила

- REST endpoints должны использовать префикс `/api`.
- Контроллеры должны оставаться тонкими, бизнес-логику размещать в сервисах.
- На границе сервисов использовать DTO запросов и ответов.
- Преобразовывать DTO вручную; не добавлять MapStruct без изменения требований.
- Возвращать ошибки API через Spring `ProblemDetail`.
- Проверять входные данные с помощью Jakarta Bean Validation.
- Делать изменения небольшими и не создавать абстракции без доказанной необходимости.
- Не добавлять отдельные файлы OpenAPI-контракта и сгенерированные TypeScript-клиенты.
- Springdoc может автоматически генерировать OpenAPI-документ для Swagger UI.


## Модель безопасности

- Для аутентификации используется Yandex OAuth2 Authorization Code flow.
- Spring Boot выступает OAuth2-клиентом и хранит аутентификацию в HTTP-сессии в памяти.
- Angular-приложение никогда не должно получать access token Яндекса.
- Session cookie должна оставаться `HttpOnly` с `SameSite=Lax`.
- CSRF-защита отключена.
- При старте Angular вызывает `/api/auth/me`: ответ `200` продолжает работу, ответ `403` перенаправляет на `/login`.
- Неаутентифицированные запросы к `/api/**` должны возвращать HTTP `403`, а не перенаправлять на вход.
- Вход выполняется только через URL `/login`; он перенаправляет на OAuth2-провайдера. Фронтенд не знает деталей авторизации и адресов провайдера.
- Для выхода используется `POST /api/auth/logout`.
- В конфигурации бэкенда свойство `app.frontend.url` задаёт корневой URL фронтенда. На этот URL выполняется редирект после успешной авторизации.
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
gradlew.bat :vacation-sheet-service:clean :vacation-sheet-service:test :vacation-sheet-service:bootJar
```

Проверка бэкенда в Unix-подобных системах:

```shell
./gradlew :vacation-sheet-service:clean :vacation-sheet-service:test :vacation-sheet-service:bootJar
```

Проверка фронтенда:

```shell
cd vacation-sheet-ui
npm ci
npm test -- --watch=false
npm run build
```

Проверка конфигурации Compose:

```shell
docker compose config --quiet
```

# Правила фронтенда

- Использовать standalone-компоненты Angular.
- Для состояния приложения использовать Signals и сервисы.
- Не добавлять NgRx, пока сложность состояния не покажет конкретную необходимость.
- Использовать компоненты Angular Material и сохранять существующий визуальный стиль.
- Использовать современный control flow шаблонов Angular: `@if`, `@for`.
- Использовать относительные URL `/api`; не прописывать адрес бэкенда в коде приложения.
- Поддерживать адаптивность интерфейса для desktop и mobile.
- Добавлять unit-тесты для новых stores, сервисов и нетривиальных компонентов.

## Критерии готовности

Перед завершением изменения:

1. Добавить миграцию Flyway для каждого изменения схемы базы данных.
2. Добавить или обновить целевые тесты изменённого поведения.
3. При изменении бэкенда запустить тесты и задачу `bootJar`.
4. При изменении фронтенда запустить unit-тесты и production-сборку.
5. При изменении контейнеров проверить `docker compose config`.
6. Убедиться, что защищённые endpoints без сессии возвращают `403`.
7. После изменения безопасности проверить исключения Swagger из авторизации и redirect на `/login` при ответе `403` от `/api/auth/me`.
8. Не добавлять в Git OAuth-секреты, сгенерированные результаты сборки и каталоги зависимостей.
