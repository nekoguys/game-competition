import React from "react";

import "./results-table.css";


class CompetitionResultsTable extends React.Component{

    firstRow(teamsCount) {
        const oneColWidth = (100 / (teamsCount + 4 + 2));

        const toStr = (x) => (oneColWidth * x) + "%";
        return (
            <tr>
                <td colSpan={4} width={toStr(4)} style={{textAlign: "center"}}>
                    {"Раунд/Команда"}
                </td>
                {
                    this.range(1, teamsCount).map(el => {
                        return (<td>{el}</td>);
                    })
                }
                <td>
                    {"Q"}
                </td>
                <td>
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
                if (el % 2 === 1) {
                    return (
                        <tr>
                            <td rowSpan={2} colSpan={3} width={toStr(3)}>
                                {Math.ceil(el / 2)}
                            </td>
                            <td width={toStr(1)}>
                                {"q"}
                            </td>

                            {this.range(1, teamsCount).map(() => {
                                return (
                                    <td width={toStr(1)}/>
                                );
                            })}
                            <td width={toStr(1)}/>
                            <td width={toStr(1)}/>
                        </tr>
                    )
                } else if (el !== 0) {
                    return (
                        <tr>
                            <td width={toStr(1)}>{"П"}</td>
                            {this.range(1, teamsCount).map(() => {
                                return (
                                    <td width={toStr(1)}/>
                                )
                            })}
                            <td width={toStr(1)}/>
                            <td width={toStr(1)}/>
                        </tr>
                    )
                } else {
                    return (
                        <tr>
                            <td colSpan={4} width={toStr(4)}>
                                {"ΣП"}
                            </td>
                            {this.range(1, teamsCount).map(() => {
                                return (
                                    <td width={toStr(1)}/>
                                )
                            })}
                            <td colSpan={2}/>
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
                    {this.firstRow(teamsCount)}
                    {this.roundRows(teamsCount, roundsCount)}
                </table>
            </div>
        )
    }
}

export default CompetitionResultsTable;
