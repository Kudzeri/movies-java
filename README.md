# Приложение Movies

Spring Boot приложение для управления фильмами с JWT аутентификацией, базой данных PostgreSQL и кэшированием Redis.

## Возможности

- RESTful API для управления фильмами
- Аутентификация на основе JWT
- База данных PostgreSQL для хранения данных
- Redis для кэширования
- Контейнеризация с Docker
- Документация Swagger/OpenAPI

## Требования

- Java 21
- Maven 3.9.6
- Docker и Docker Compose
- PostgreSQL 16
- Redis 7

## Настройка окружения

1. Создайте файл `.env` в корне проекта со следующими переменными:

```env
# Настройки базы данных
DB_URL=jdbc:postgresql://localhost:5432/movies
DB_USERNAME=postgres
DB_PASSWORD=your_password_here

# Настройки JWT
JWT_SECRET=your_jwt_secret_here
JWT_EXPIRATION=3600000

# Настройки Redis
REDIS_HOST=your_redis_host
REDIS_PORT=your_redis_port
REDIS_USERNAME=your_redis_username
REDIS_PASSWORD=your_redis_password
```

2. Создайте файл `.env.example` с такой же структурой, но с placeholder значениями.

## Локальная разработка

### Запуск с помощью Maven

1. Сборка проекта:
```bash
mvn clean package
```

2. Запуск приложения:
```bash
java -jar target/movies-0.0.1-SNAPSHOT.jar
```

### Запуск с помощью Docker

1. Сборка и запуск всех сервисов:
```bash
docker-compose up --build
```

2. Остановка всех сервисов:
```bash
docker-compose down
```

3. Остановка и удаление томов (это удалит все данные):
```bash
docker-compose down -v
```

## Документация API

После запуска приложения, вы можете получить доступ к Swagger UI по адресу:
```
http://localhost:8081/swagger-ui.html
```

## Структура проекта

```
src/main/java/com/example/movies/
├── config/         # Классы конфигурации
├── controller/     # REST контроллеры
├── filter/         # Фильтры запросов/ответов
├── model/          # Модели данных
├── repository/     # JPA репозитории
├── security/       # Конфигурация безопасности
└── service/        # Бизнес-логика
```

## Docker сервисы

Приложение состоит из трех Docker сервисов:

1. **app**: Spring Boot приложение
   - Порт: 8081
   - Переменные окружения из файла .env

2. **db**: База данных PostgreSQL
   - Порт: 5432
   - Постоянный том для хранения данных

3. **redis**: Кэш Redis
   - Порт: 6379
   - Постоянный том для хранения данных

## Вклад в проект

1. Форкните репозиторий
2. Создайте ветку для вашей функции (`git checkout -b feature/amazing-feature`)
3. Зафиксируйте изменения (`git commit -m 'Добавлена новая функция'`)
4. Отправьте изменения в ветку (`git push origin feature/amazing-feature`)
5. Откройте Pull Request

## Лицензия

Этот проект лицензирован под MIT License - подробности в файле LICENSE. 