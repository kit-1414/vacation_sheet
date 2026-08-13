# Серверная часть - бэкенд

## Структура модулей бэкенда

- Все пакеты бэкенда приложения имеют корневой пакет `MODULE_ROOT_PACKAGE`, внутри которого расположены пакеты по слоям.

## Конфигурация OAuth

Основная конфигурация бэкенда находится в `vacation-sheet-service/src/main/resources/application.yml`.

Перед проверкой реального OAuth-входа заменить заглушки Yandex `client-id` и `client-secret`. Не добавлять реальные production-секреты в Git. Yandex redirect URI для Docker:

```text
http://localhost:8055/login/oauth2/code/yandex
```
## Конфигурация Базы данных
Стандартные параметры локальной базы данных:

```text
Database: vacation_sheet_db
Username: postgres
Password: postgres
```

Compose переопределяет JDBC URL, чтобы бэкенд подключался к сервису `postgres`.

## Существующее API

- `GET /login`: инициализация OAuth2-входа (редирект на провайдера)
- `GET /api/auth/me`: получение текущего аутентифицированного пользователя
- `POST /api/auth/logout`: завершение текущей сессии
- `GET /api/users`: получение списка зарегистрированных пользователей
- `GET /api/users/{id}`: получение пользователя
- `POST /api/users`: создание пользователя
- `PUT /api/users/{id}`: изменение пользователя
- `DELETE /api/users/{id}`: удаление пользователя
- `GET /api/projects`: получение проектов с участниками
- `GET /api/projects/{id}`: получение проекта с участниками и руководителями
- `POST /api/projects`: создание проекта
- `PUT /api/projects/{id}`: изменение проекта
- `DELETE /api/projects/{id}`: удаление проекта
- `PUT /api/projects/{projectId}/users/{userId}`: привязка пользователя к проекту
- `DELETE /api/projects/{projectId}/users/{userId}`: отвязка пользователя от проекта
- `PUT /api/projects/{projectId}/managers/{userId}`: назначение руководителя проекта
- `DELETE /api/projects/{projectId}/managers/{userId}`: удаление руководителя проекта
- `GET /actuator/health`: health check контейнера
- `GET /v3/api-docs`: сгенерированное описание API
- `GET /swagger-ui.html`: страница Swagger UI


## Структура пакетов бэкенда

Базовый пакет: `com.example.vacationsheet.mainapp`.

Бэкенд использует Packaging by Layer. Каждый класс должен находиться в соответствующем пакете:

- `config`: конфигурация приложения и безопасности
- `controller`: REST-контроллеры
- `dto`: DTO запросов и ответов, не связанных напрямую с JPA-моделями
- `exception`: исключения и обработка ошибок REST API
- `service`: бизнес-логика, загрузка OAuth2-пользователей и политики доступа
- `hql.model`: JPA-сущности с суффиксом `Entity`
- `hql.repository`: репозитории Spring Data JPA с суффиксом `Repository`
- `hql.dto`: DTO для JPA-моделей с суффиксом `Dto`
- `hql.mapper`: ручные преобразования JPA-моделей и DTO в классах с суффиксом `Mapper`
- `hql.handler`: обработчики жизненного цикла JPA

Не вводить Packaging by Feature/Domain без явного изменения архитектуры проекта.
