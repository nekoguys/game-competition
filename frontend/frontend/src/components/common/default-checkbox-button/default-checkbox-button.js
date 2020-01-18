import React from "react";

class DefaultCheckboxButton extends React.Component {
    constructor(props) {
        super(props);

        const {checked = false} = props;
        this.state = {
            checked: checked
        };

        this.onButtonClick = this.onButtonClick.bind(this);
    }

    onButtonClick() {
        const {isDisabled = false} = this.props;
        const {onChange = (_value) => {}} = this.props;

        if (!isDisabled) {
            this.setState(state => {
                onChange(!state.checked);
                return {checked: !state.checked};
            })
        }
    }

    value() {
        return this.state.checked;
    }

    render() {
        const {notActiveColor, activeColor, disabledColor, isDisabled, text = "", style = {}, labelStyle = {}} = this.props;

        let className = "btn text-center ";
        if (this.state.checked) {
            className += "active ";
        }

        let additionalStyle = {
            backgroundColor: isDisabled ? disabledColor: (this.state.checked ? activeColor : notActiveColor),
            ...style
        };

        return (
            <div className={"btn-group-toggle"} data-toggle={"buttons"} style={additionalStyle}>
                <label className={className} style={{...labelStyle, width: "100%",
                    display: "inline-block", textOverflow: "ellipsis", overflow: "hidden",
                }}>
                    <input type={"checkbox"} autoComplete={"off"} onClick={this.onButtonClick} /> {text}
                </label>
            </div>
        )
    }
}

export default DefaultCheckboxButton;

