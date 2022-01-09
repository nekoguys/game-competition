# Библиотека `game-core`

Тут находятся базовые классы, интерфейсы для реализации своих игр

## Что тут происходит

Можешь посмотреть файлы в этом порядке и почитать комментарии:

1. [GameSession.kt](src/main/kotlin/ru/selemilka/game/core/base/GameSession.kt) - что такое игровая сессия, как с ней
   взаимодействовать
2. [GameCommand.kt](src/main/kotlin/ru/selemilka/game/core/base/GameCommand.kt) - команды в сессии
3. [GameMessage.kt](src/main/kotlin/ru/selemilka/game/core/base/GameMessage.kt) - сообщения игрокам в сессии
4. [GameRule.kt](src/main/kotlin/ru/selemilka/game/core/base/GameRule.kt) - как описать логику своей игры

## Тестовый проект с использованием `game-core`

Посмотри на [тесты](src/test/kotlin/ru/selemilka/game/rps) - там реализована игра в "камень-ножницы-бумару" с
использованием `game-core`
