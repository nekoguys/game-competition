function getValueForJsonObject(fieldName, value) {
    const parseFormula = (formula) => {return formula.split(";").filter(x => x)};
    const identity = (val) => val;
    const rules = {
        expensesFormula: parseFormula,
        demandFormula: parseFormula,
        roundsCount: parseInt,
        roundLength: parseInt,
        maxTeamSize: parseInt,
        maxTeamsAmount: parseInt,
        teamLossUpperbound: parseFloat
    };

    return (rules[fieldName] || identity)(value);
}


export const toCompetitionFormJsonObject = (obj) => {
    let jsonObj = {};

    Object.keys(obj).forEach(key => {
        jsonObj[key] = getValueForJsonObject(key, obj[key]);
    });

    return jsonObj;
}

export const makeStartingCompetitionForm = () => {
    return {
        name: "",
        expensesFormula: "",
        demandFormula: "",
        maxTeamsAmount: "",
        maxTeamSize: "",
        roundsCount: "",
        roundLength: "",
        instruction: "",
        teamLossUpperbound: "",
        shouldShowStudentPreviousRoundResults: false,
        shouldShowResultTableInEnd: false,
        isAutoRoundEnding: false
    }
}

export default getValueForJsonObject;
