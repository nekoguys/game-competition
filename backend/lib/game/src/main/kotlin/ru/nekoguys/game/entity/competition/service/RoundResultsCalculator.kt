package ru.nekoguys.game.entity.competition.service

import ru.nekoguys.game.entity.competition.model.CompetitionRoundAnswer
import ru.nekoguys.game.entity.competition.model.CompetitionSettings
import ru.nekoguys.game.entity.competition.model.CompetitionTeam

object RoundResultsCalculator {
    fun calculateResults(
        settings: CompetitionSettings,
        roundAnswers: List<CompetitionRoundAnswer>,
    ): RoundResults {
        val price = calculatePrice(settings, roundAnswers)
        val teamLossLimit = settings.teamLossLimit
        val bannedTeamIds = mutableListOf<CompetitionTeam.Id>()

        val defaultIncome = -getTotalCosts(settings, 0)
        val incomes = mutableMapOf<CompetitionTeam.Id, Double>()
            .withDefault { defaultIncome }

        for (answer in roundAnswers) {
            val totalCost = getTotalCosts(settings, answer.production)
            val income = answer.production * price - totalCost
            incomes[answer.teamId] = income

            if (income < -teamLossLimit) {
                bannedTeamIds += answer.teamId
            }
        }

        return RoundResults(
            results = incomes,
            bannedTeamIds = bannedTeamIds,
            price = price,
        )
    }

    private fun calculatePrice(
        settings: CompetitionSettings,
        roundAnswers: List<CompetitionRoundAnswer>,
    ): Double {
        val totalProduction = roundAnswers.sumOf { it.production }

        // production = a - b * price
        // price = (a - production) / b
        val result = with(settings.demandFormula) {
            (freeCoefficient - totalProduction) / xCoefficient
        }
        return if (result.isFinite() && result > 0.0) {
            return result
        } else {
            0.0
        }
    }

    private fun getTotalCosts(
        settings: CompetitionSettings,
        production: Int,
    ): Double =
        with(settings.expensesFormula) {
            xSquareCoefficient * production * production +
                    xCoefficient * production +
                    freeCoefficient
        }

    /*
    @Override
    public RoundResultsHolder calculateResults(DbCompetitionRoundInfo roundInfo, DbCompetition competition) {
        double price = calculatePrice(roundInfo, competition.getParameters());
        List<DbRoundResultElement> results = new ArrayList<>();
        Set<Integer> visited = new HashSet<>();

        List<Integer> bannedTeams = new ArrayList<>();
        double teamLossUpperbound = competition.getParameters().getTeamLossUpperbound();
        for (var answer : roundInfo.getAnswerList()) {
            if (!answer.getSubmitter().isBanned()) {
                double totalCost = getTotalCosts(answer.getValue(), competition.getParameters());
                double income = answer.getValue() * price - totalCost;

                if (income < -teamLossUpperbound) {
                    bannedTeams.add(answer.getSubmitter().getIdInGame());
                }

                visited.add(answer.getSubmitter().getIdInGame());

                results.add(DbRoundResultElement.builder().income(income).team(answer.getSubmitter()).build());
            }
        }

        competition.getTeams().stream()
                .filter(team -> !visited.contains(team.getIdInGame()))
                .forEach(el -> {
                    results.add(DbRoundResultElement.builder()
                            .income(-getTotalCosts(0, competition.getParameters()))
                            .team(el)
                            .build()
                    );
                });

        return RoundResultsHolder.builder().bannedTeams(bannedTeams).price(price).results(results).build();
    }


    private double getTotalCosts(int produced, DbCompetition.Parameters competitionParameters) {
        var expensesFormula = competitionParameters.getExpensesFormula();
        double x2Coef = Double.parseDouble(expensesFormula.get(0));
        double xCoef = Double.parseDouble(expensesFormula.get(1));
        double freeCoef = Double.parseDouble(expensesFormula.get(2));

        return x2Coef * produced * produced + xCoef * produced + freeCoef;
    }
     */

    /*
    private double calculatePrice(DbCompetitionRoundInfo roundInfo, DbCompetition.Parameters competitionParameters) {
        int totalProduced = roundInfo.getAnswerList().stream()
                .filter(el -> !el.getSubmitter().isBanned())
                .map(DbAnswer::getValue)
                .reduce(Integer::sum).orElse(0);
        //Q = a - bp
        //p = (a-Q)/b
        double a = Double.parseDouble(competitionParameters.getDemandFormula().get(0));
        double b = Double.parseDouble(competitionParameters.getDemandFormula().get(1));

        return Math.max((a - totalProduced) / b, 0);
    }
     */
}

data class RoundResults(
    val results: Map<CompetitionTeam.Id, Double>,
    val bannedTeamIds: List<CompetitionTeam.Id>,
    val price: Double,
)
