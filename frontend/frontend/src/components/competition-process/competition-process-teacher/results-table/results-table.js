import React from "react";
import ReactDOM from 'react-dom'

import "./results-table.css";
import round from "../../../../helpers/round-helper";


class CompetitionResultsTable extends React.Component {

    constructor(props) {
        super(props);

        this.refsMap = new Map();
        this.refsCount = new Map();

        this.setPopupRef = (element, column) => {
            this.refsMap.set(column, element);
            this.refsCount.set(column, 0);
        }
    }

    shouldShowStrategy = () => {
        return this.props.showStrategy ?? false;
    }

    togglePopup = (column) => {
        let node = ReactDOM.findDOMNode(this.refsMap.get(column));
        node.classList.toggle("show")

        if (this.refsCount.get(column)) {
            node.classList.toggle("hide");
        }
        this.refsCount.set(column, this.refsCount.get(column) + 1)
    }

    teamsPermutation = (teamsCount) => {
        let range;

        if (this.props.teamsPermutation !== undefined) {
            range = this.props.teamsPermutation;
        } else {
            range = this.range(1, teamsCount + 1)
        }

        return range;
    };

    firstRow(teamsCount, bannedTeams) {
        const oneColWidth = (100 / (teamsCount + 4 + 2));

        const toStr = (x) => (oneColWidth * x) + "%";
        console.log({bannedTeams})
        console.log({strat: this.props.strategy})
        return (
            <tr key={-1}>
                <td colSpan={4} width={toStr(4)} style={{textAlign: "center"}} key={0}>
                    {"Раунд/Команда"}
                </td>
                {
                    this.teamsPermutation(teamsCount).map(el => {
                        let style = {};
                        let className;
                        let popup;
                        if (bannedTeams.includes(el)) {
                            style['backgroundColor'] = '#ffffed';
                        }
                        let onClick_ = () => {};
                        if (this.shouldShowStrategy()) {
                            className = "popup";
                            const strat = el in this.props.strategy ? this.props.strategy[el]['strategy'] : "Стратегии нет";
                            popup = (
                                <div>
                                <span className={"popuptext"} ref={(element) => {
                                    this.setPopupRef(element, el);
                                }}>{strat}
                                </span>
                                    <span>{el}</span>
                                </div>
                            );
                            onClick_ = () => {
                                this.togglePopup(el);
                            }
                        } else {
                            popup = el;
                        }
                        return (
                            <td key={el} style={style} onClick={onClick_}>
                                <div className={className}>
                                    {popup}
                                </div>
                            </td>
                        );
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

    roundRows(teamsCount, roundsCount, bannedTeams) {
        const oneColWidth = (100 / (teamsCount + 4 + 2));

        const toStr = (x) => (oneColWidth * x) + "%";

        return (
            this.range(1, roundsCount*2 + 1).concat([0]).map(el => {
                const roundNumber = Math.ceil(el / 2);
                let price = "";

                if (roundNumber in this.props.prices) {
                    price = round(this.props.prices[roundNumber]);
                }

                if (el % 2 === 1) {
                    return (
                        <tr key={el}>
                            <td rowSpan={2} colSpan={3} width={toStr(3)} key={-1}>
                                {roundNumber}
                            </td>
                            <td width={toStr(1)} key={0}>
                                {"q"}
                            </td>

                            {this.teamsPermutation(teamsCount).map(teamInd => {
                                let ans = "";
                                if (roundNumber in this.props.answers) {
                                    if (teamInd in this.props.answers[roundNumber]) {
                                        ans = round(this.props.answers[roundNumber][teamInd]);
                                    }
                                }
                                let style = {};
                                if (bannedTeams.includes(teamInd)) {
                                    style.backgroundColor = '#ffffed'
                                }
                                return (
                                    <td width={toStr(1)} key={teamInd} style={style}>{ans}</td>
                                );
                            })}

                            {
                                <td width={toStr(1)} key={teamsCount + 1} rowSpan={2}>
                                    {
                                        this.teamsPermutation(teamsCount).map(teamInd => {
                                            let ans = 0;
                                            if (roundNumber in this.props.answers) {
                                                if (teamInd in this.props.answers[roundNumber]) {
                                                    ans = this.props.answers[roundNumber][teamInd];
                                                }
                                            }
                                            return ans;
                                        }).reduce((prev, curr) => prev + curr, 0)
                                    }
                                </td>
                            }
                            <td width={toStr(1)} key={teamsCount + 2} rowSpan={2}>{price}</td>
                        </tr>
                    )
                } else if (el !== 0) {
                    return (
                        <tr key={el}>
                            <td width={toStr(1)} key={0}>{"П"}</td>
                            {this.teamsPermutation(teamsCount).map(teamInd => {
                                let ans = "";

                                if (roundNumber in this.props.results) {
                                    if (teamInd in this.props.results[roundNumber]) {
                                        ans = round(this.props.results[roundNumber][teamInd]);
                                    }
                                }
                                let style = {};
                                if (bannedTeams.includes(teamInd)) {
                                    style.backgroundColor = '#ffffed'
                                }
                                return (
                                    <td width={toStr(1)} key={teamInd} style={style}>{ans}</td>
                                )
                            })}
                        </tr>
                    )
                } else {
                    return (
                        <tr key={roundsCount* 2 + 2}>
                            <td colSpan={4} width={toStr(4)} key={0}>
                                {"ΣП"}
                            </td>
                            {this.teamsPermutation(teamsCount).map(teamInd => {
                                let style = {};
                                if (bannedTeams.includes(teamInd)) {
                                    style.backgroundColor = '#ffffed'
                                }
                                return (
                                    <td width={toStr(1)} style={style} key={teamInd}>
                                        {round(this.range(1, roundsCount + 1).map(round => {
                                            let ans = 0;
                                            if (round in this.props.results) {
                                                if (teamInd in this.props.results[round]) {
                                                    ans = this.props.results[round][teamInd];
                                                }
                                            }
                                            return ans;
                                        }).reduce((prev, curr) => {
                                            return prev + curr;
                                        }, 0))}
                                    </td>
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
        const {teamsCount, roundsCount, bannedTeams=[]} = this.props;
        return (
            <div style={{width: "100%"}}>
                <div style={{textAlign: "center", fontSize: "23px", paddingBottom: "10px"}}>
                    {"Статистика"}
                </div>
                <table style={{width: "100%"}}>
                    <tbody>
                    {this.firstRow(teamsCount, bannedTeams)}
                    {this.roundRows(teamsCount, roundsCount, bannedTeams)}
                    </tbody>
                </table>
            </div>
        )
    }
}

export default CompetitionResultsTable;
