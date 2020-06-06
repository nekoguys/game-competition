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

export default getValueForJsonObject;
