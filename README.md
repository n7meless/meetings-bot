# MeetingsBot
MeetingBot - Телеграм-бот для автоматизации организации встреч между участниками команды. 

## Используемые технологии

* Java
* Gradle
* Spring Boot, Spring Data, Spring Scheduler
* PostgreSQL, Redis
* Junit5, Testcontainers, Mockito

## Инструкция по запуску проекта на локальном компьютере

- Склонировать репозиторий meetings-bot

```bash
git clone https://github.com/n7meless/meetings-bot.git
```

* В docker.env файл вставить свои данные для настройки бота.

```bash
# Образец сконфигурированного .env файла
TELEGRAM_USERNAME=bot
TELEGRAM_TOKEN=1234567:alksdalsLKSADSALKLasdl
```
* Запустить приложение

```bash
docker-compose up
```
