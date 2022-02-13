import React from "react";

import "./text-input-with-submit-button.css";

export class TextInputWithSubmitButton extends React.Component {
    constructor(props) {
        super(props);

        this.input = {};

        this.state = {
            text: ""
        }
    }

    onTextChanged = (text) => {
        this.setState(prevState => {
            const {
                onChange = (_val) => {
                }
            } = this.props;
            onChange(text);
            return {
                text: text
            }
        })
    };

    render() {
        const {
            type = "text",
            placeholder = "",
            containerStyle = {},
            containerClasses = "",
            buttonStyle = {},
            buttonClasses = "",
            inputStyle = {},
            inputClasses = "",
            imagePath = "",
            imgStyle = {},
            imgClasses = {},
            onSubmit = (_value) => {
            },
            alt = "submit competition id",
            clearOnSubmit = false
        } = this.props;

        let addProps = {};

        if (this.props.submitOnKey !== undefined) {
            addProps.onKeyDown = event => {
                if (event.key.toLowerCase() === this.props.submitOnKey) {
                    event.preventDefault();
                    event.stopPropagation();
                    onSubmit(this.state.text);
                    if (clearOnSubmit) {
                        this.input.value = "";
                        this.setState({text: ""});
                        this.input.focus();
                    }
                }
            }
        }

        return (
            <div className={"input-with-submit-button__container " + containerClasses} style={{...containerStyle}}>
                <input placeholder={placeholder} className={"input-with-submit-button__input " + inputClasses}
                       type={type} style={{...inputStyle}}
                       onChange={event => this.onTextChanged(event.target.value)} ref={el => this.input = el}
                       {...addProps}
                />
                <button style={buttonStyle} className={buttonClasses} onClick={() => {
                    onSubmit(this.state.text);
                    if (clearOnSubmit) {
                        this.input.value = "";
                        this.setState({text: ""});
                        this.input.focus();
                    }
                }} type={"submit"}><img src={imagePath} alt={alt} style={imgStyle} className={imgClasses}/></button>
            </div>
        )
    }
}