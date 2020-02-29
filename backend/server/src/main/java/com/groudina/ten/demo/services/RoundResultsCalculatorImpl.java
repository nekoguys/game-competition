package com.groudina.ten.demo.services;

import com.groudina.ten.demo.models.DbAnswer;
import com.groudina.ten.demo.models.DbCompetition;
import com.groudina.ten.demo.models.DbCompetitionRoundInfo;
import com.groudina.ten.demo.models.DbRoundResultElement;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
public class RoundResultsCalculatorImpl implements IRoundResultsCalculator {

    @Override
    public List<DbRoundResultElement> calculateResults(DbCompetitionRoundInfo roundInfo, DbCompetition.Parameters competitionParameters) {
        double price = calculatePrice(roundInfo, competitionParameters);
        List<DbRoundResultElement> results = new ArrayList<>();

        for (var answer : roundInfo.getAnswerList()) {
            double totalCost = getTotalCosts(answer.getValue(), competitionParameters);
            double income = answer.getValue() * price - totalCost;

            results.add(DbRoundResultElement.builder().income(income).team(answer.getSubmitter()).build());
        }

        return results;
    }

    private double getTotalCosts(int produced, DbCompetition.Parameters competitionParameters) {
        var expensesFormula = competitionParameters.getExpensesFormula();
        double x2Coef = Double.parseDouble(expensesFormula.get(0));
        double xCoef = Double.parseDouble(expensesFormula.get(1));
        double freeCoef = Double.parseDouble(expensesFormula.get(2));

        return x2Coef * produced * produced + xCoef * produced + freeCoef;
    }

    private double calculatePrice(DbCompetitionRoundInfo roundInfo, DbCompetition.Parameters competitionParameters) {
        int totalProduced = roundInfo.getAnswerList().stream().map(DbAnswer::getValue).reduce(Integer::sum).orElse(0);
        //Q = a - bp
        //p = (a-Q)/b
        double a = Double.parseDouble(competitionParameters.getDemandFormula().get(0));
        double b = Double.parseDouble(competitionParameters.getDemandFormula().get(1));

        return (a - totalProduced) / b;
    }
}
