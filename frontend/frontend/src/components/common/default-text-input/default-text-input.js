import React from "react";
import * as PropTypes from "prop-types";

import "./default-text-input.css"

class DefaultTextInput extends React.Component {
    constructor(props) {
        super(props);

        const {defaultText = ""} = props;

        this.state = {
            text: defaultText
        }
    }

    text() {
        return this.state.text;
    }

    componentDidUpdate(prevProps, prevState, snapshot) {
        if (this.props.defaultText !== prevProps.defaultText) {
            this.setState({text: this.props.defaultText}, () => {
                const {onChange = () => {}} = this.props;
                onChange(this.text());
            });
        }
    }

    onTextChanged = (event) => {
        const {onChange = () => {}} = this.props;
        this.setState({
            text: event.target.value
        });

        onChange(event.target.value)
    };

    render() {
        const {style, placeholder, type = "text",
            onKeyDown=(_v) => {}, onFocus=()=>{}, onClick=()=>{},
            readOnly=false, additionalClassNames=""} = this.props;
        return (
            <input className={"form-control text-input " + additionalClassNames}
                   style={style}
                   placeholder={placeholder}
                   value={this.state.text}
                   type={type}
                   onChange={this.onTextChanged}
                   onKeyDown={(ev) => onKeyDown(ev)}
                   onFocus={onFocus}
                   onClick={onClick}
                   readOnly={readOnly}
            />
        )
    }
}

DefaultTextInput.propTypes = {
    style: PropTypes.any,
    placeholder: PropTypes.string
};

export default DefaultTextInput;