# Vacation Sheet

Монорепозиторий приложения для учёта отпусков:

- `vacation-sheet-ui`: Angular SPA, Angular Material и Signals
- `vacation-sheet-service`: Kotlin, Spring Boot 3, Spring Data JPA, Flyway и PostgreSQL
- авторизация: серверная OAuth2-сессия через Яндекс

## Локальный запуск

Укажите Yandex OAuth `client-id` и `client-secret` в `vacation-sheet-service/src/main/resources/application.yml`, затем запустите:

```shell
docker compose up --build
```

Приложение будет доступно на <http://localhost:4200>, Swagger UI на <http://localhost:4200/swagger-ui.html>.

После входа доступны страницы проектов и пользователей. Проекты можно создавать, редактировать, удалять и связывать с зарегистрированными пользователями.

Для ограничения входа задайте `app.security.allowed-email-domain`. Пустая строка разрешает все домены.

## Проверки

```shell
./gradlew :vacation-sheet-service:test :vacation-sheet-service:bootJar

cd vacation-sheet-ui
npm test -- --watch=false
npm run build
```
