![Build](https://github.com/central-university-dev/backend-academy-2025-spring-template/actions/workflows/build.yaml/badge.svg)

# Link Tracker

Проект сделан в рамках курса Академия Бэкенда.

Приложение для отслеживания обновлений контента по ссылкам.
При появлении новых событий отправляется уведомление в Telegram.

Проект написан на `Java 23` с использованием `Spring Boot 3`.

Проект состоит из 2-х приложений:
* Bot
* Scrapper

# Запуск

Для корректной работы приложений необходимо в модулях scrapper и bot в директории resources
создать файлы .env.properties. Образцы заполнения есть в файлах .properties.example

Запуск приложений производится из файлов ScrapperApplication и BotApplication
