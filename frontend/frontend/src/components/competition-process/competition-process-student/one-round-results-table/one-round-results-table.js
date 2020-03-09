import React from "react";

class OneRoundResultsTable extends React.Component {
    firstRow(roundsCount) {
        const oneColumnWidth = 100 / (roundsCount + 5);

        const toStr = (x) => (oneColumnWidth * x) + "%";

        const {currentRoundNumber} = this.props;

        let res;

        if (currentRoundNumber > 1) {
            res = <td key={1} width={toStr(1)}>{currentRoundNumber - 1}</td>
        }

        return (
            <tr key={-1}>
                <td key={0} width={toStr(1)}>{"Раунд"}</td>

                {res}

                <td key={2} width={toStr(1)}>{currentRoundNumber}</td>

                <td key={3} width={toStr(1)} rowSpan={3}>{"ΣП"}</td>
            </tr>
        )
    }

    commonRow(roundsCount, key, heading, source={}, additional) {
        const oneColumnWidth = 100 / (roundsCount + 5);

        const toStr = (x) => (oneColumnWidth * x) + "%";

        const {currentRoundNumber} = this.props;

        let res;

        if (currentRoundNumber > 1) {
            res = <td key={1} width={toStr(1)}>{source[currentRoundNumber - 1]}</td>
        }

        return (
            <tr key={key}>
                <td key={0} width={toStr(1)}>{heading}</td>
                {res}
                <td key={2} width={toStr(1)}>{source[currentRoundNumber]}</td>
                {additional}
            </tr>
        )
    }

    render() {
        const {roundsCount} = this.props;

        const result = Object.keys(this.props.results).reduce((prev, key) => {
            return prev + this.props.results[key];
        }, 0);

        return (
            <div style={{width: "50%", margin: "0 auto", minWidth: "300px"}}>
            <table width={"100%"}>
                <tbody>
                {this.firstRow(roundsCount)}
                {this.commonRow(roundsCount, 1, 'q', this.props.answers)}
                {this.commonRow(roundsCount, 2, 'P', this.props.prices)}
                {this.commonRow(roundsCount, 3, 'П', this.props.results, <td key={3}>{result}</td>)}
                </tbody>
            </table>
            </div>
        );
    }
}

export default OneRoundResultsTable;
