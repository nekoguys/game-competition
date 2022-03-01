export class Language {
    static Russian = new Language("ru");
    static English = new Language("en");

    static forName(name) {
        if (name === this.Russian.name) {
            return this.Russian;
        }
        else if (name === this.English.name) {
            return this.English;
        }
        return null;
    }

    constructor(name) {
        this.name = name;
    }
}

export class LocalizationHelper {
    get language() {
        const storedValue = window.localStorage.getItem("language");
        const defaultValue = Language.Russian;
        if (storedValue) {
            return Language.forName(storedValue) || defaultValue;
        }
        return defaultValue;
    }

    setLanguage(language) {
        window.localStorage.setItem("language", language.name);
    }
}
