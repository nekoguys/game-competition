# Подробная информация находится в wiki

## Игра "Конкуренция" по заказу эконома

Видео демонстрация: [Видео](https://www.youtube.com/watch?v=L1rckCTg1oc)

## Выбор технологий

почему база данных
постгрес: <img src="https://user-images.githubusercontent.com/43180408/137919589-d9ca7f3e-a07c-448e-b7fc-e2fc6b74b622.png" alt="database" width="200" height="100">

### Как запустить бекенд

Для сборки проекта нужен JDK17 и maven. Их можно установить через `homebrew`:

```
brew install openjdk@17 maven
```

Далее бекенд нужно собрать. Для этого из директории `backend` нужно выполнить команду:

```
mvn -Dmaven.test.skip=true --projects app/web --also-make package
```

И запустить собранный jar-файл.
По-умолчанию в качестве базы используется встроенный h2,
так что поднимать постгрес в докере не нужно

```
java -jar app/web/target/backend-app-web-0.0.1-SNAPSHOT.jar 
```
