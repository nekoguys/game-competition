import React from "react";

import "./results-table.css";


class CompetitionResultsTable extends React.Component{

    firstRow(teamsCount) {
        const oneColWidth = (100 / (teamsCount + 4 + 2));

        const toStr = (x) => (oneColWidth * x) + "%";
        return (
            <tr key={-1}>
                <td colSpan={4} width={toStr(4)} style={{textAlign: "center"}} key={0}>
                    {"Раунд/Команда"}
                </td>
                {
                    this.range(1, teamsCount + 1).map(el => {
                        return (<td key={el}>{el}</td>);
                    })
                }
                <td key={teamsCount+1}>
                    {"Q"}
                </td>
                <td key={teamsCount+2}>
                    {"P"}
                </td>
            </tr>
        );
    }

    roundRows(teamsCount, roundsCount) {
        const oneColWidth = (100 / (teamsCount + 4 + 2));

        const toStr = (x) => (oneColWidth * x) + "%";

        return (
            this.range(1, roundsCount*2 + 1).concat([0]).map(el => {
                const roundNumber = Math.ceil((el + 1) / 2);

                if (el % 2 === 1) {
                    return (
                        <tr key={el}>
                            <td rowSpan={2} colSpan={3} width={toStr(3)} key={-1}>
                                {roundNumber}
                            </td>
                            <td width={toStr(1)} key={0}>
                                {"q"}
                            </td>

                            {this.range(1, teamsCount + 1).map(teamInd => {
                                let ans = "";
                                if (roundNumber in this.props.answers) {
                                    if (teamInd in this.props.answers[roundNumber]) {
                                        ans = this.props.answers[roundNumber][teamInd];
                                    }
                                }
                                return (
                                    <td width={toStr(1)} key={teamInd}>{ans}</td>
                                );
                            })}

                            {
                                <td width={toStr(1)} key={teamsCount + 1}>
                                    {
                                        this.range(1, teamsCount + 1).map(teamInd => {
                                            let ans = 0;
                                            if (roundNumber in this.props.answers) {
                                                if (teamInd in this.props.answers[roundNumber]) {
                                                    ans = this.props.answers[roundNumber][teamInd];
                                                }
                                            }
                                            return ans;
                                        }).reduce((prev, curr) => prev + curr)
                                    }
                                </td>
                            }

                            <td width={toStr(1)} key={teamsCount + 2}/>
                        </tr>
                    )
                } else if (el !== 0) {
                    return (
                        <tr key={el}>
                            <td width={toStr(1)} key={0}>{"П"}</td>
                            {this.range(1, teamsCount + 1).map(teamInd => {
                                let ans = "";

                                if (roundNumber in this.props.results) {
                                    if (teamInd in this.props.results[roundNumber]) {
                                        ans = this.props.results[roundNumber][teamInd];
                                    }
                                }
                                return (
                                    <td width={toStr(1)} key={teamInd}>{ans}</td>
                                )
                            })}
                            <td width={toStr(1)} key={teamsCount + 1}/>
                            <td width={toStr(1)} key={teamsCount + 2}/>
                        </tr>
                    )
                } else {
                    return (
                        <tr key={roundsCount* 2 + 2}>
                            <td colSpan={4} width={toStr(4)} key={0}>
                                {"ΣП"}
                            </td>
                            {this.range(1, teamsCount + 1).map(el => {
                                return (
                                    <td width={toStr(1)} key={el}/>
                                )
                            })}
                            <td colSpan={2} key={teamsCount + 1}/>
                        </tr>
                    )
                }
            })
        )
    }

    range = (start, end) => {
        const length = end - start;
        return Array.from({ length }, (_, i) => start + i);
    };

    render() {
        const {teamsCount, roundsCount} = this.props;

        return (
            <div style={{width: "100%"}}>
                <div style={{textAlign: "center", fontSize: "23px", paddingBottom: "10px"}}>
                    {"Статистика"}
                </div>
                <table style={{width: "100%"}}>
                    <tbody>
                    {this.firstRow(teamsCount)}
                    {this.roundRows(teamsCount, roundsCount)}
                    </tbody>
                </table>
            </div>
        )
    }
}

export default CompetitionResultsTable;
