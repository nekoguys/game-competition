import React from "react";
import "./default-checkbox-button-group.css";

class DefaultCheckboxButtonGroup extends React.Component {
    constructor(props) {
        super(props);

        this.choices = props.choices;
        const initialChoiceIndex = props.initialChoiceIndex ?? 0;

        this.state = {
            choiceIndex: initialChoiceIndex
        };
    }

    onChoiceChanged = (choiceIndex) => {
        this.setState(() => {
            const {onChoiceChanged = [(_val) => {}]} = this.props;
            onChoiceChanged[choiceIndex]();
            return {choiceIndex: choiceIndex};
        });

    };

    render() {
        const notSelectedClassName = "btn btn-group-elem";
        const selectedClassName = notSelectedClassName + " active";
        const {buttonStyle = {}, style = {}, buttonClasses=""} = this.props;

        const innerElements = this.choices.map((value, index) => {
            const isSelected = index === this.state.choiceIndex;
            return (
                <label key={index} className={(isSelected ? selectedClassName : notSelectedClassName) + buttonClasses} style={buttonStyle}>
                    <input type={"radio"} autoComplete={"off"} defaultChecked={isSelected} onClick={() => this.onChoiceChanged(index)}/>
                    {value}
                </label>
            )
        });
        innerElements.splice(1, 0, <div key={123} className={"btn-divider"}/>)
        return (
            <div className={"btn-group btn-group-toggle"} style={style}>
                {innerElements}
            </div>
        );
    };
}

export default DefaultCheckboxButtonGroup;
