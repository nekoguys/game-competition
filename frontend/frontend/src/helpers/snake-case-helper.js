function toSnakeCase(key) {
    return key.replace(/\.?([A-Z]+)/g, function(x, y) {return '_' + y.toLowerCase();}).replace(/^_/, '');
}

export default toSnakeCase;