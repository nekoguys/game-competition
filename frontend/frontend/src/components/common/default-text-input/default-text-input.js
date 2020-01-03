import React from "react";
import * as PropTypes from "prop-types";

import "./default-text-input.css"

class DefaultTextInput extends React.Component {
    constructor(props) {
        super(props);

        this.state = {
            text: ""
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
        const {style, placeholder, type = "text"} = this.props;
        return (
            <input className={"form-control text-input"}
                   style={style}
                   placeholder={placeholder}
                   value={this.state.text}
                   type={type}
                   onChange={this.onTextChanged}
            />
        )
    }
}

DefaultTextInput.propTypes = {
    style: PropTypes.any,
    placeholder: PropTypes.string
};

export default DefaultTextInput;