export class LocalStorageWrapper {
    constructor(key, defaultValue = undefined) {
        this.key = key;
        this.defaultValue = defaultValue;
    }

    get() {
        return window.localStorage.getItem(this.key) || this.defaultValue;
    }

    set(newValue) {
        window.localStorage.setItem(this.key, newValue);
    }
}

export class FieldStorageWrapper {
    constructor(defaultValue) {
        this.currentValue = defaultValue;
    }

    get() {
        return this.currentValue;
    }

    set(newValue) {
        this.currentValue = newValue;
    }
}

export class FunctionStorageWrapper {
    constructor(getter, setter) {
        this.getter = getter;
        this.setter = setter;
    }

    get() {
        this.getter();
    }

    set(newValue) {
        this.setter(newValue);
    }
}