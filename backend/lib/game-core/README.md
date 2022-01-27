# Библиотека `game-core`

Тут находятся базовые классы, интерфейсы для реализации своих игр

## Как создать свою игру

Можешь посмотреть файлы в этом порядке и почитать комментарии:

1. [GameCommand.kt](src/main/kotlin/ru/nekoguys/game/core/GameCommand.kt) - команды в сессии
2. [GameMessage.kt](src/main/kotlin/ru/nekoguys/game/core/GameMessage.kt) - сообщения игрокам в сессии
3. [GameRule.kt](src/main/kotlin/ru/nekoguys/game/core/GameRule.kt) - как описать логику своей игры

## Как играть в свою игру

Посмотри на интерфейс [GameSession](src/main/kotlin/ru/nekoguys/game/core/session/GameSession.kt)

## Тестовый проект с использованием `game-core`

Посмотри на [тесты](src/test/kotlin/ru/nekoguys/game/core/rps) - там реализована игра в "камень-ножницы-бумару" с
использованием `game-core`
