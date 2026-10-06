# Vacation Sheet

Монорепозиторий приложения для учёта отпусков:

- `vacation-sheet-ui`: Angular SPA, Angular Material и Signals
- `vacation-sheet-service`: Kotlin, Spring Boot 3, Spring Data JPA, Flyway и PostgreSQL
- авторизация: серверная OAuth2-сессия через Яндекс

## Локальный запуск

Создайте локальный файл переменных окружения из шаблона и укажите в нём параметры PostgreSQL и Yandex OAuth:

```shell
cp .env.example .env
```

Затем запустите:

```shell
docker compose up --build
```

Приложение будет доступно на <http://localhost:4200>, Swagger UI на <http://localhost:4200/swagger-ui.html>.

После входа доступны страницы проектов и пользователей. Проекты можно создавать, редактировать, удалять и связывать с зарегистрированными пользователями.

Для ограничения входа задайте `ALLOWED_EMAIL_DOMAIN` в `.env`. Пустая строка разрешает все домены.

## Публикация и production-запуск

Создание Git-тега с именем по шаблону `release_v_*` запускает проверки и публикует два образа в GitHub Container Registry. Тег должен указывать на коммит из ветки `main`:

- `ghcr.io/kit-1414/vacation_sheet-service:<Git-тег>`
- `ghcr.io/kit-1414/vacation_sheet-ui:<Git-тег>`

На сервере авторизуйтесь в GHCR, подготовьте production-переменные и запустите Compose:

```shell
docker login ghcr.io -u kit-1414
cp .env.prod.example .env.prod
docker compose --env-file .env.prod -f docker-compose-prod.yml pull
docker compose --env-file .env.prod -f docker-compose-prod.yml up -d
```

В `.env.prod` установите `IMAGE_TAG=<Git-тег>`, например `IMAGE_TAG=release_v_1.2.3`, публичный HTTPS URL приложения, OAuth-параметры и стойкий пароль PostgreSQL. Файл `.env.prod` исключён из Git.

## Проверки

```shell
./gradlew :vacation-sheet-service:test :vacation-sheet-service:bootJar

cd vacation-sheet-ui
npm test -- --watch=false
npm run build
```
