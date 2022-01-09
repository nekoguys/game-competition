import React, {Suspense} from 'react';
import ReactDOM from 'react-dom';
import App from "./components/app";
import {I18nextProvider} from "react-i18next";
import i18next from "i18next";

import translationEN from './locales/en/translation.json';
import translationRU from './locales/ru/translation.json';

// the translations
const resources = {
    en: {
        translation: translationEN
    },
    ru: {
        translation: translationRU
    }
};

i18next.init({
    interpolation: { escapeValue: false },  // React already does escaping
    lng: window.localStorage.getItem("language") ?? "en", // language to use
    resources: resources
});

ReactDOM.render(<Suspense fallback={"loading"}><I18nextProvider i18n={i18next}><App /></I18nextProvider></Suspense>, document.getElementById('root'));

// If you want your app to work offline and load faster, you can change
// unregister() to register() below. Note this comes with some pitfalls.
// Learn more about service workers: https://bit.ly/CRA-PWA
