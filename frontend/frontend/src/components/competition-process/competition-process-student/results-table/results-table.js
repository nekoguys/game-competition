import React from "react";

import "./results-table.css";
import round from "../../../../helpers/round-helper";
import {withTranslation} from "react-i18next";

class StudentResultsTable extends React.Component {
    firstRow(roundsCount) {
        const oneColumnWidth = 100 / (roundsCount + 5);

        const toStr = (x) => (oneColumnWidth * x) + "%";

        return (
            <tr key={-1}>
                <td key={0} width={toStr(2)}>
                    {this.props.i18n.t("competition_process.student.results.round")}
                </td>

                {
                    this.range(1, roundsCount + 1).map(roundNumber => {
                        return <td key={roundNumber} width={toStr(1)}>{roundNumber}</td>
                    })
                }

                <td key={roundsCount + 2} width={toStr(3)} rowSpan={3}>{"ΣП"}</td>

            </tr>
        )
    }

    commonRow(roundsCount, key, heading, source={}, additional) {
        const oneColumnWidth = 100 / (roundsCount + 5);

        const toStr = (x) => (oneColumnWidth * x) + "%";

        return (
            <tr key={key}>
                <td key={0} width={toStr(2)}>{heading}</td>
                {
                    this.range(1, roundsCount + 1).map(roundNumber => {
                        let ans = "";
                        if (roundNumber in source) {
                            ans = round(source[roundNumber]);
                        }

                        return <td key={roundNumber} width={toStr(1)}>{ans}</td>
                    })
                }
                {additional}
            </tr>
        )
    }

    range = (start, end) => {
        const length = end - start;
        return Array.from({ length }, (_, i) => start + i);
    };


    render() {
        const {roundsCount} = this.props;

        const result = Object.keys(this.props.results).reduce((prev, key) => {
            return prev + this.props.results[key];
        }, 0);
        console.log({result});
        return (
            <table className={"competition-process-student-results-table"}>
                <tbody>
                {this.firstRow(roundsCount)}
                {this.commonRow(roundsCount, 1, 'q', this.props.answers)}
                {this.commonRow(roundsCount, 2, 'P', this.props.prices)}
                {this.commonRow(roundsCount, 3, 'П', this.props.results, <td key={roundsCount + 1}>{result}</td>)}
                </tbody>
            </table>
        )
    }
}

export default withTranslation('translation')(StudentResultsTable);
